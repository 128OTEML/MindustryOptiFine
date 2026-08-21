<#
.SYNOPSIS
Installs the MindustryOptiFine patched Arc SDL backend and optionally the mod jar into a local Mindustry install.
This REPLACES the game's arc-sdl.jar, so run restore.ps1 to undo. For personal/local use only.

.DESCRIPTION
1. Resolves the Mindustry install directory (param, auto-detect, or prompt).
2. Builds the patched backend jar (and the mod jar) via Gradle when needed.
3. Locates the exact jar in the game install that actually contains arc/backend/sdl/SdlApplication.class
   and replaces it (after backing it up).
4. Optionally copies the mod jar into the game's mods folder.
5. Launches the game unless -NoLaunch is given.

Windows gains for the manual flow: if the game lives under a protected directory (e.g. Program Files) the script
tells you to re-run as administrator. With -AutoElevate it relaunches itself elevated and relays the result.

.PARAMETER GameDir
Path to the Mindustry install directory. Auto-detected (Steam) or prompted when omitted.

.PARAMETER GameJar
Full path to the game's fat jar (e.g. "D:/Games/Mindustry 159.7.jar") for single-jar layouts where the
rendering backend is bundled inside the game jar. The script then injects the patched backend classes
into that jar instead of replacing a separate arc-sdl.jar.

.PARAMETER Build
Force a Gradle build of the backend and mod jars before installing.

.PARAMETER RebuildNatives
Rebuild the native (sdl-arc / libsdl-arc) libraries via jnigen before installing. REQUIRED for L2 (shared GL
contexts) because the stock game natives lack SDL_GL_MakeCurrent. Needs a C toolchain (see build.gradle).

.PARAMETER NoLaunch
Do not launch the game after installing.

.PARAMETER InstallMod
Also copy the built mod jar into the game's mods directory.

.PARAMETER AutoElevate
Relaunch as administrator automatically when needed (default: instruct instead).

.PARAMETER NoWait
Do not pause at the end (non-interactive use).

.EXAMPLE
.\install-local.ps1 -Build -InstallMod
.\install-local.ps1 -AutoElevate -GameDir "D:\Games\Mindustry"
#>
param(
    [string]$GameDir = "",
    [string]$GameJar = "",
    [switch]$Build,
    [switch]$RebuildNatives,
    [switch]$NoLaunch,
    [switch]$InstallMod,
    [switch]$AutoElevate,
    [switch]$NoWait
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"
$root = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot ".."))
$runtime = $PSScriptRoot
$backendJar = Join-Path $runtime "backend-sdl\build\libs\arc-sdl.jar"
$backendClasses = Join-Path $runtime "backend-sdl\build\classes\java\main"
$modJar = Join-Path $root "build\libs\MindustryOptiFine.jar"
$stateFile = Join-Path $runtime ".mpof-state.json"

# ---- elevation gate ----
$principal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if(!$isAdmin){
    if(-not $AutoElevate){
        Write-Host "需要管理员权限才能写入游戏目录(它可能位于 Program Files 之类受保护的位置)。" -ForegroundColor Yellow
        Write-Host "请先关闭此窗口,然后右键本脚本 -> 以管理员身份运行,再操作一次。" -ForegroundColor Yellow
        if(-not $NoWait){ Read-Host "按 Enter 退出" }
        exit 1
    }

    $relaunch = @("-File", (Join-Path $PSScriptRoot "install-local.ps1"))
    if($GameDir){ $relaunch += "-GameDir", $GameDir }
    if($GameJar){ $relaunch += "-GameJar", $GameJar }
    if($Build){ $relaunch += "-Build" }
    if($RebuildNatives){ $relaunch += "-RebuildNatives" }
    if($NoLaunch){ $relaunch += "-NoLaunch" }
    if($InstallMod){ $relaunch += "-InstallMod" }
    if($NoWait){ $relaunch += "-NoWait" }
    $relaunch += "-AutoElevate"

    Write-Host "需要管理员权限,已请求提权。请在弹出的 UAC 窗口点「是」..." -ForegroundColor Yellow
    try{
        $p = Start-Process powershell -Verb RunAs -Wait -PassThru -WorkingDirectory $PSScriptRoot -ArgumentList $relaunch
    }catch{
        Write-Host "未能提权: $($_.Exception.Message)" -ForegroundColor Red
    }
    if($p -and $p.ExitCode -eq 0){
        Write-Host "管理员进程已完成,详见其窗口。按任意键前请确认其内容。" -ForegroundColor Green
    }else{
        Write-Host "管理员进程未正常完成(退出码 $($p.ExitCode))。请改为右键本脚本 -> 以管理员身份运行。" -ForegroundColor Red
    }
    if(-not $NoWait){ Read-Host "按 Enter 退出" }
    exit 0
}

function Find-SdlJar([string]$dir){
    Add-Type -AssemblyName System.IO.Compression.FileSystem | Out-Null
    $jars = @(Get-ChildItem -Path $dir -Recurse -Filter *.jar -ErrorAction SilentlyContinue |
                Where-Object { $_.Length -gt 10000 })
    foreach($j in $jars){
        try{
            $zip = [System.IO.Compression.ZipFile]::OpenRead($j.FullName)
            try{
                $hit = $zip.Entries | Where-Object { $_.FullName -eq "arc/backend/sdl/SdlApplication.class" }
                if($hit){ return $j }
            }finally{ $zip.Dispose() }
        }catch{}
    }
    return $null
}

function Is-GameJar([string]$jar){
    Add-Type -AssemblyName System.IO.Compression.FileSystem | Out-Null
    try{
        $zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
        try{
            return @($zip.Entries | Where-Object{ $_.FullName -eq 'mindustry/desktop/DesktopLauncher.class' }).Count -gt 0
        } finally { $zip.Dispose() }
    }catch{ return $false }
}

# Rewrites a fat game jar: drops every arc/backend/sdl java class and injects our compiled (patched) set.
# All other entries (assets, natives, the rest of arc/mindustry) are copied through unchanged.
# When -NativeDir points at a rebuilt jnigen output dir, the matching sdl-arc native entries inside the jar are
# replaced as well (required for L2 shared GL contexts).
function Patch-FatJar([string]$JarPath, [string]$ClassesDir, [string]$NativeDir = ""){
    Add-Type -AssemblyName System.IO.Compression | Out-Null
    Add-Type -AssemblyName System.IO.Compression.FileSystem | Out-Null
    if(-not (Test-Path $ClassesDir)){ throw "Backend classes dir not found: $ClassesDir" }

    $dir = Split-Path $JarPath -Parent
    $tmp = Join-Path $dir "$([System.IO.Path]::GetFileNameWithoutExtension($JarPath)).patched.tmp"
    if(Test-Path $tmp){ Remove-Item $tmp -Force }

    $out = [System.IO.Compression.ZipFile]::Open($tmp, [System.IO.Compression.ZipArchiveMode]::Create)
    try{
        # copy the untouched jar, dropping vanilla arc/backend/sdl classes
        $in = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
        try{
            foreach($entry in $in.Entries){
                if($entry.FullName.StartsWith('arc/backend/sdl/') -and $entry.FullName.EndsWith('.class')){ continue }
                $e = $out.CreateEntry($entry.FullName)
                $rs = $entry.Open(); $ws = $e.Open()
                try{ $rs.CopyTo($ws) } finally { $rs.Dispose(); $ws.Dispose() }
            }
        } finally { $in.Dispose() }

        # inject our compiled backend classes (arc/backend/sdl/**, incl. threaded/RenderScheduler)
        $srcRoot = (Resolve-Path $ClassesDir).Path.TrimEnd('\')
        Get-ChildItem -Path $srcRoot -Recurse -File -Filter *.class | ForEach-Object {
            $rel = $_.FullName.Substring($srcRoot.Length + 1).Replace('\','/')
            $e = $out.CreateEntry($rel)
            $fs = $_.OpenRead(); $ws = $e.Open()
            try{ $fs.CopyTo($ws) } finally { $fs.Dispose(); $ws.Dispose() }
        }

        # replace the native libraries (L2 rebuild) when available
        if($NativeDir -and (Test-Path $NativeDir)){
            $out.Dispose()
            $z = [System.IO.Compression.ZipFile]::Open($tmp, [System.IO.Compression.ZipArchiveMode]::Update)
            try{
                foreach($c in @("sdl-arc64.dll","sdl-arc32.dll","libsdl-arc64.so","libsdl-arc32.so","libsdl-arc64.dylib","libsdl-arc64arm64.dylib","libsdl-arc32.dylib")){
                    $src = Get-ChildItem -Path $NativeDir -Recurse -Filter $c -ErrorAction SilentlyContinue | Select-Object -First 1
                    if(-not $src){ continue }
                    foreach($e in @($z.Entries)){
                        if($e.FullName.EndsWith("/" + $c) -or $e.FullName -eq $c){
                            $name = $e.FullName
                            $e.Delete()
                            $ne = $z.CreateEntry($name)
                            $fs = [System.IO.File]::OpenRead($src.FullName); $ws = $ne.Open()
                            try{ $fs.CopyTo($ws) } finally { $fs.Dispose(); $ws.Dispose() }
                            Write-Host "Replaced native $c in fat jar" -ForegroundColor Cyan
                        }
                    }
                }
            } finally { $z.Dispose() }
            $out = $null
        }
    } finally { if($out){ $out.Dispose() } }

    Remove-Item -LiteralPath $JarPath -Force
    Move-Item -LiteralPath $tmp -Destination $JarPath
}

function Resolve-GameDir {
    if($GameDir -ne ""){
        $g = [System.IO.Path]::GetFullPath($GameDir)
        if(Test-Path $g){ return $g }
        Write-Host "GameDir doesn't exist: $g" -ForegroundColor Yellow
    }
    $candidates = @(
        (Join-Path ${env:ProgramFiles(x86)} "Steam\steamapps\common\Mindustry"),
        (Join-Path ${env:ProgramFiles} "Steam\steamapps\common\Mindustry"),
        "C:\Program Files (x86)\Steam\steamapps\common\Mindustry",
        "$env:LOCALAPPDATA\Programs\Mindustry"
    )
    foreach($c in $candidates){
        if(Test-Path (Join-Path $c "Mindustry.exe")){ return $c }
    }
    $g = Read-Host "Mindustry install directory not auto-detected. Enter its full path"
    if($g -and (Test-Path $g)){ return [System.IO.Path]::GetFullPath($g) }
    throw "No valid Mindustry install directory."
}

function Invoke-GradleBuild {
    if($RebuildNatives){
        Write-Host "Rebuilding native libs via jnigen (needs a C toolchain; see backend-sdl/build.gradle)..." -ForegroundColor Cyan
        & (Join-Path $root "gradlew.bat") ":runtime:backend-sdl:jnigen" "-Pjnigen" --console=plain
        if($LASTEXITCODE -ne 0){ throw "jnigen native build failed (exit $LASTEXITCODE)." }
    }
    Write-Host "Building patched backend + packaged mod jar (packageMod)..." -ForegroundColor Cyan
    & (Join-Path $root "gradlew.bat") ":runtime:backend-sdl:jar" "packageMod" --console=plain
    if($LASTEXITCODE -ne 0){ throw "Gradle build failed (exit $LASTEXITCODE)." }
}

$ok = $false
try{
    # 1. resolve the target backend jar (fat game jar or separate arc-sdl.jar)
    $existing = ""
    $game = ""
    if($GameJar){
        if(-not (Test-Path $GameJar)){ throw "GameJar does not exist: $GameJar" }
        $existing = (Resolve-Path $GameJar).Path
        $game = Split-Path $existing
        Write-Host "Game jar: $existing" -ForegroundColor Cyan
    }else{
        $game = Resolve-GameDir
        Write-Host "Game install: $game" -ForegroundColor Cyan
        $found = Find-SdlJar $game
        if(-not $found){
            throw "Cannot locate the backend jar to replace under $game (no *.jar contains arc/backend/sdl/SdlApplication.class)."
        }
        $existing = $found.FullName
        Write-Host "Existing backend jar: $existing" -ForegroundColor Cyan
    }
    $fat = Is-GameJar $existing
    if($fat){
        Write-Host "Layout: fat jar (backend classes live inside the game jar) - will inject patched classes into the jar." -ForegroundColor Cyan
    }else{
        Write-Host "Layout: separate backend jar - will replace the jar with the patched arc-sdl.jar." -ForegroundColor Cyan
    }

    # 2. build when asked or when artifacts are missing
    $backendMissing = if($fat){ -not (Test-Path (Join-Path $backendClasses "arc\backend\sdl\SdlApplication.class")) } else { -not (Test-Path $backendJar) }
    $modMissing = -not (Test-Path $modJar)
    if($Build -or $RebuildNatives -or $backendMissing -or ($InstallMod -and $modMissing)){
        Invoke-GradleBuild
    }

    # 3. sanity-check the artifact we are about to install
    if($fat){
        if(-not (Test-Path (Join-Path $backendClasses "arc\backend\sdl\SdlApplication.class"))){ throw "Compiled backend classes not found: $backendClasses" }
        if(-not (Test-Path (Join-Path $backendClasses "arc\backend\sdl\threaded\RenderScheduler.class"))){ throw "RenderScheduler.class missing from $backendClasses - backend not patched?" }
    }else{
        Write-Host "Verifying patched backend: $backendJar" -ForegroundColor Cyan
        if(-not (Test-Path $backendJar)){ throw "Patched backend jar not found: $backendJar" }
        if(-not (Find-SdlJar (Split-Path $backendJar))){
            throw "Built backend jar does not contain arc/backend/sdl/SdlApplication! Refusing to install."
        }
    }

    # 4. backup + replace
    $backupDir = Join-Path $game ".mpof-backup"
    New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
    $backupFile = Join-Path $backupDir "arc-sdl.original.jar"
    if(-not (Test-Path $backupFile)){
        Write-Host "Backing up original to $backupFile" -ForegroundColor Cyan
        Copy-Item -LiteralPath $existing -Destination $backupFile
    } else {
        Write-Host "Backup already present: $backupFile (skipping; your original remains untouched)" -ForegroundColor Gray
    }

    if($fat){
        Patch-FatJar -JarPath $existing -ClassesDir $backendClasses -NativeDir (Join-Path $runtime "backend-sdl\libs")
        Write-Host "Injected patched backend classes -> $existing" -ForegroundColor Cyan
    }else{
        Write-Host "Installing patched backend -> $existing" -ForegroundColor Cyan
        Copy-Item -LiteralPath $backendJar -Destination $existing -Force
    }

    if(-not $RebuildNatives){
        Write-Host "NOTE: natives were NOT rebuilt. L1 (CPU pipeline) works, but L2 (shared GL workers) needs" -ForegroundColor DarkYellow
        Write-Host "      '.\install-local.ps1 -RebuildNatives' on a machine with a C toolchain, then reinstall." -ForegroundColor DarkYellow
    }

    $state = @{
        gameDir = $game
        originalJar = $existing
        backupFile = $backupFile
        installedJar = $existing
        layout = if($fat){ "fat" } else { "libs" }
        installTime = (Get-Date).ToString("s")
    }
    $state | ConvertTo-Json | Set-Content -Path $stateFile -Encoding UTF8

    # 5. optional mod install
    if($InstallMod){
        if(-not (Test-Path $modJar)){
            Write-Host "Mod jar not found: $modJar - skipping mod install" -ForegroundColor Yellow
        } else {
            $modDir = Join-Path $game "mods"
            if(-not (Test-Path $modDir)){
                $modDir = Join-Path $env:APPDATA "Mindustry\mods"
            }
            if(-not (Test-Path $modDir)){
                $modDir = Read-Host "Mindustry mods folder not found. Enter its path"
            }
            if($modDir -and (Test-Path $modDir)){
                Copy-Item -LiteralPath $modJar -Destination (Join-Path $modDir "MindustryOptiFine.jar") -Force
                Write-Host "Mod installed -> $modDir\MindustryOptiFine.jar" -ForegroundColor Cyan
                $state.modJar = (Join-Path $modDir "MindustryOptiFine.jar")
                $state | ConvertTo-Json | Set-Content -Path $stateFile -Encoding UTF8
            }
        }
    }

    # 6. launch
    Write-Host ""
    Write-Host "Installed. Backend: $($existing.Name) replaced." -ForegroundColor Green
    if(-not $NoLaunch){
        $exe = Join-Path $game "Mindustry.exe"
        if(Test-Path $exe){
            Write-Host "Launching $exe" -ForegroundColor Cyan
            try{
                Start-Process $exe -WorkingDirectory $game
            }catch{
                Write-Host "启动游戏失败: $($_.Exception.Message) (安装已完成,可手动启动)" -ForegroundColor Yellow
            }
        } else {
            Write-Host "Game exe not found, skipping launch. Start it manually." -ForegroundColor Yellow
        }
    }
    Write-Host ""
    Write-Host "To undo, run:  powershell -ExecutionPolicy Bypass -File $runtime\restore.ps1" -ForegroundColor Gray
    $ok = $true
}catch{
    Write-Host ""
    Write-Host "安装失败:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host $_.ScriptStackTrace -ForegroundColor DarkGray
}finally{
    if(-not $NoWait){
        Read-Host "按 Enter 关闭此窗口..."
    }
}
exit $(if($ok){ 0 } else { 1 })