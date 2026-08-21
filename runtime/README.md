# MindustryOptiFine runtime(L2 基础)

本目录承载 L2(多线程渲染):一份从 Arc fork 的 `backend-sdl`,带共享 GL context 的渲染 worker 池。
**L1(CPU 帧流水线)已整体搬进 mod 内、不依赖本目录**;L2 在 1.1 的
slot/delay/fence 机制上加了 `gpu` 槽,把离屏渲染通道放到 worker(共享 context)上执行。

## 目录结构

- `backend-sdl/` — fork 的 Arc `backends/backend-sdl` 源码 +
  `build.gradle`(可独立编译、打包 `arc-sdl.jar`、并用 `-Pjnigen` 重编 native)。
  - `backend-sdl/src/arc/backend/sdl/threaded/RenderScheduler.java` — L1 帧流水线 + L2 渲染 worker 池;
    worker 用 `SDL_GL_SHARE_WITH_CURRENT_CONTEXT`(attribute 22)建自己的 context,`glFinish()` 隐式栅栏。
  - `backend-sdl/src/arc/backend/sdl/jni/SDL.java` — 新增 `SDL_GL_MakeCurrent` /
    `SDL_GL_GetCurrentContext` / `SDL_GL_DeleteContext` / `SDL_GL_Finish` (jnigen 语法)。
  - `backend-sdl/src/arc/backend/sdl/SdlApplication.java` — 主循环挂载了
    `RenderScheduler` 的创建/`frameTick()`/`stop()` 钩子,并把 window/主 context 交给调度器。
  - `backend-sdl/libs/` — 平台 native。**注意:仓库里这份是原生 arc 的预编译版,不带
    `SDL_GL_MakeCurrent` 符号;要跑 L2 必须在本机 `-Pjnigen` 重编**(见下)。
- `install-local.ps1` / `restore.ps1` — jar 级注入(classes + 可选 natives)+ 日后 L2 native 铺设。
- `.mpof-state.json` — install 时生成的状态记录,restore 依据它回滚。

## 构建

```powershell
# 只构建后端 jar(默认不跑 native 重编译)
.\gradlew.bat :runtime:backend-sdl:jar

# 需要重编 SDL/GLEW native(JNI 层有改动/L2 共享 context 必需)——本机需 C 工具链 + SDL 头文件
.\gradlew.bat :runtime:backend-sdl:jnigen -Pjnigen
# 产物会写回 backend-sdl/libs/<platform>/,随后再 jar 就会把新 native 打进 arc-sdl.jar
.\gradlew.bat :runtime:backend-sdl:jar
```

产物:`backend-sdl/build/libs/arc-sdl.jar`(直接替换游戏目录里的同名 jar)。

## 安装到本机游戏

### 方式一(推荐):直接当普通 mod 用,无需替换任何游戏文件

把编译好的 mod jar 放进游戏的 mods 目录即可,打包产物:
`build/libs/MindustryOptiFine.jar`(由 `packageMod` 任务产出,`deploy` 同理)。

```powershell
.\gradlew.bat packageMod
# 然后拷贝 build\libs\MindustryOptiFine.jar 到游戏 mods 目录(便携版一般是 <游戏目录>\mods)
```

L1 帧流水线**不再依赖替换 arc-sdl.jar**:mod 启动时通过公开接口
`Core.app.addListener(...)` 往帧循环里插入自己的 `ApplicationListener`。SDL 后端每帧会依序调用
每个 listener 的 `update()`(游戏自身的绘制也在其中),我们的 listener 落在游戏 update+render 之后、
帧交换之前——正好是原先 patched 后端里 `RenderScheduler.frameTick()` 的位置,因此**原版/整包
fat-jar/任何后端都能用**,不需要补丁、不需要管理员、不锁文件。

游戏内设置页可查看 “Pipeline Stats”,并用 “Pipeline demo load” 注入模拟负载实测线程化收益。

### 方式二:独立补丁脚本(可选,给未来的 L2 native 路线)

`runtime/` 下的 fork 后端(`backend-sdl`)现在只是 L2(native 共享 GL context)的地基,
一般不参与 L1。若日后需要往 libs 布局的 `arc-sdl.jar` 或整包 fat-jar 注入代码可复用:

```powershell
# libs 布局:自动检测 Steam 安装目录;需要管理员权限时按指引右键以管理员身份运行
.\runtime\install-local.ps1

# fat-jar 布局:直接指定桌面上的游戏整包,脚本把 arc/backend/sdl 类替换为 patched 编译产物
.\runtime\install-local.ps1 -GameJar "C:\...\Mindustry 159.7.jar" -InstallMod

# L2 共享 GL context:务必在本机重编 native 后再装(需 C 工具链;详见 build.gradle)
.\runtime\install-local.ps1 -GameDir "D:\Games\Mindustry" -RebuildNatives -InstallMod
```

脚本行为:
1. 按需用 Gradle 构建后端和打包 mod(`packageMod`)。
2. 按内容(`arc/backend/sdl/SdlApplication.class`)定位后端载体(libs 的 jar 或整包 fat-jar)。
3. 备份原版到 `.mpof-backup\arc-sdl.original.jar`;libs 布局直接替换 jar,整包布局注入
   patched 类到 jar 内部。
4. 可选把打包好的 mod jar 复制到 `<游戏目录>\mods` 或 `%APPDATA%\Mindustry\mods`。
5. 启动游戏(可 `-NoLaunch` 跳过)。

## 还原

```powershell
.\runtime\restore.ps1 -RemoveMod
```

## 运行时行为

- mod 的 `FramePipeline` 通过 `Core.app.addListener(...)` 自挂进帧循环(无需任何补丁):
  - listener 的 `update()` 落在游戏自身 update+render 之后、帧交换之前,`tick()` 驱动流水线。
  - 卸载到 worker 线程(延迟一帧+栅栏 `CountDownLatch`)再回到 GL 线程 apply,首个消费者:
    `SchedQuality` 把动态质量插值放到 worker 线程。
  - 任何后端/整包 fat-jar 都能跑;监听器顺序影响的是“应用值比决定值晚几帧”,不影响正确性。
- **L2(需 patch 后端 + 重编 native)**:mod 的 `BackendGpuProbe` 反射 `RenderScheduler`,
  `L2SharedPass`(游戏内 “L2 离屏 GPU 通道”)把离屏渲染通道交给共享 context 的 worker 线程;
  主线程仍占窗口帧缓冲,worker 在私有 FBO 上并行绘制,`glFinish` 栅栏后延迟一帧合成。
  - 没 patch/没重编 native → 探针报不可用,mod 照常单线程渲染,只少 L2 一项。
- 游戏内设置页可查看 “Pipeline Stats”,并用 “Pipeline demo load” 注入模拟负载实测线程化收益。