package MindustryOptiFine.threaded;

import arc.func.*;
import arc.util.*;

import java.lang.reflect.*;

/**
 * L2 probe: bridges to the render pool the (optional) patched backend exposes as
 * {@code arc.backend.sdl.threaded.RenderScheduler}. The scheduler's slot/delay/fence machinery IS the L1
 * pipeline; L2 reuses it with a {@code gpu} flag so {@code compute} runs on a worker holding a GL context that
 * SHARES with the main context.
 *
 * On any vanilla/unpatched backend (or a patched backend whose native lib was not rebuilt with
 * {@code SDL_GL_MakeCurrent}) this probe reports unavailable and the caller falls back to main-thread GL.
 * Safe to call before the backend is up: reflection simply finds nothing.
 */
public final class BackendGpuProbe{
    private static Class<?> schedulerClass;
    private static Method regGpu;
    private static Method gpuReadyM, workerCountM, gpuWorkersReadyM;
    private static Field instanceField;

    /** True when a patched RenderScheduler singleton exists. */
    public static volatile boolean available;
    /** User switch (settings toggle). */
    public static volatile boolean disabled;
    /** True when the scheduler's shared-context native API is present. */
    public static volatile boolean gpuSupport;
    public static volatile int workerCount;
    public static volatile int gpuWorkersReady;

    private static boolean init(){
        if(schedulerClass != null) return true;
        try{
            Class<?> c = Class.forName("arc.backend.sdl.threaded.RenderScheduler");
            instanceField = c.getField("instance");
            gpuReadyM = c.getMethod("gpuReady");
            workerCountM = c.getMethod("workerCount");
            gpuWorkersReadyM = c.getMethod("gpuWorkersReady");
            regGpu = c.getMethod("registerGpu", Prov.class, Func.class, Cons.class, int.class);
            schedulerClass = c;
            return true;
        }catch(Throwable t){
            return false;
        }
    }

    public static void refresh(){
        available = false;
        gpuSupport = false;
        gpuWorkersReady = 0;
        workerCount = 0;
        if(!init()) return;
        try{
            Object rs = instanceField.get(null);
            if(rs == null) return;
            available = true;
            gpuSupport = (boolean)gpuReadyM.invoke(rs);
            workerCount = (int)workerCountM.invoke(rs);
            gpuWorkersReady = (int)gpuWorkersReadyM.invoke(rs);
        }catch(Throwable t){
            Log.err("MPOF: L2 probe failed", t);
        }
    }

    /** L2 usable: patched scheduler present, natives rebuilt, user hasn't disabled it. */
    public static boolean gpuReady(){
        refresh();
        return available && gpuSupport && !disabled;
    }

    /**
     * Submits a shared-context GPU job. Returns true if handed to a GPU worker; false otherwise, in which case
     * the caller MUST run the pass inline on the GL thread.
     */
    public static boolean registerGpu(Prov<?> capture, Func<?, ?> compute, Cons<?> apply, int delayFrames){
        if(!gpuReady()) return false;
        try{
            regGpu.invoke(null,
                (Prov<Object>)capture, (Func<Object, Object>)compute, (Cons<Object>)apply, delayFrames);
            return true;
        }catch(Throwable t){
            Log.err("MPOF: gpu register failed: @", t);
            return false;
        }
    }
}