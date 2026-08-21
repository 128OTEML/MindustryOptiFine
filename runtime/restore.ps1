<#
.SYNOPSIS
Restores the original arc-sdl.jar that install-local.ps1 backed up, and optionally removes the installed mod jar.

.PARAMETER GameDir
Path to the Mindustry install directory. Only needed when the state file can't be read.

.PARAMETER RemoveMod
Also delete the mod jar that install-local.ps1 copied into the game's mods folder.

.PARAMETER AutoElevate
Relaunch as administrator automatically when needed (default: instruct instead).

.PARAMETER NoWait
Do not pause at the end (non-interactive use).

.EXAMPLE
.\restore.ps1
.\restore.ps1 -RemoveMod
#>
param(
    [string]$GameDir = "",
    [switch]$RemoveMod,
    [switch]$AutoElevate,
    [switch]$NoWait
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"
$runtime = $PSScriptRoot
$stateFile = Join-Path $runtime ".mpof-state.json"

# ---- elevation gate ----
$principal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if(!$isAdmin){
    if(-not $AutoElevate){
        Write-Host "需要管理员权限才能写回游戏目录。" -ForegroundColor Yellow
        Write-Host "请先关闭此窗口,然后右键本脚本 -> 以管理员身份运行,再操作一次。" -ForegroundColor Yellow
        if(-not $NoWait){ Read-Host "按 Enter 退出" }
        exit 1
    }

    $relaunch = @("-File", (Join-Path $PSScriptRoot "restore.ps1"))
    if($GameDir){ $relaunch += "-GameDir", $GameDir }
    if($RemoveMod){ $relaunch += "-RemoveMod" }
    if($NoWait){ $relaunch += "-NoWait" }
    $relaunch += "-AutoElevate"

    Write-Host "需要管理员权限,已请求提权。请在弹出的 UAC 窗口点「是」..." -ForegroundColor Yellow
    try{
        $p = Start-Process powershell -Verb RunAs -Wait -PassThru -WorkingDirectory $PSScriptRoot -ArgumentList $relaunch
    }catch{
        Write-Host "未能提权: $($_.Exception.Message)" -ForegroundColor Red
    }
    if($p -and $p.ExitCode -eq 0){
        Write-Host "管理员进程已完成,详见其窗口。" -ForegroundColor Green
    }else{
        Write-Host "管理员进程未正常完成(退出码 $($p.ExitCode))。请改为右键本脚本 -> 以管理员身份运行。" -ForegroundColor Red
    }
    if(-not $NoWait){ Read-Host "按 Enter 退出" }
    exit 0
}

$ok = $false
try{
    $state = $null
    if(Test-Path $stateFile){
        try{ $state = Get-Content $stateFile -Raw | ConvertFrom-Json }catch{}
    }

    $game = $GameDir
    if(-not $game -and $state -and $state.gameDir){ $game = $state.gameDir }
    if(-not $game -or -not (Test-Path $game)){
        $game = Read-Host "Game install directory not known. Enter its full path"
    }
    if(-not $game -or -not (Test-Path $game)){ throw "No valid game directory: $game" }

    $backupFile = Join-Path $game ".mpof-backup\arc-sdl.original.jar"
    if($state -and $state.backupFile){ $backupFile = $state.backupFile }

    if(-not (Test-Path $backupFile)){
        Write-Host "No backup found at $backupFile - nothing to restore." -ForegroundColor Yellow
    } else {
        $dest = Join-Path $game "arc-sdl.jar"
        if($state -and $state.originalJar){ $dest = $state.originalJar }

        Copy-Item -LiteralPath $backupFile -Destination $dest -Force
        Write-Host "Restored original backend -> $dest" -ForegroundColor Green

        Remove-Item -LiteralPath $backupFile -Force -ErrorAction SilentlyContinue
        $backupDir = Split-Path $backupFile
        $leftover = Get-ChildItem -Path $backupDir -Filter *.jar -ErrorAction SilentlyContinue
        if(-not $leftover){ Remove-Item -LiteralPath $backupDir -Force -ErrorAction SilentlyContinue }
    }

    if($RemoveMod -and $state -and $state.modJar -and (Test-Path $state.modJar)){
        Remove-Item -LiteralPath $state.modJar -Force
        Write-Host "Removed mod jar: $($state.modJar)" -ForegroundColor Green
    }

    Remove-Item -LiteralPath $stateFile -Force -ErrorAction SilentlyContinue
    Write-Host "Done. Restart the game for changes to take effect." -ForegroundColor Green
    $ok = $true
}catch{
    Write-Host ""
    Write-Host "还原失败:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host $_.ScriptStackTrace -ForegroundColor DarkGray
}finally{
    if(-not $NoWait){
        Read-Host "按 Enter 关闭此窗口..."
    }
}
exit $(if($ok){ 0 } else { 1 })