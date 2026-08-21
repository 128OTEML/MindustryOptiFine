package MindustryOptiFine.threaded;

import arc.func.*;
import arc.util.*;

/**
 * Facade for the L1 frame pipeline. The pipeline itself ({@link FramePipeline}) runs inside the mod and is
 * hooked into the game's loop via {@code Core.app.addListener(...)}, so it works on any backend — vanilla fat
 * jar or patched — without replacing any game files.
 */
public final class SchedulerProbe{
    public static boolean available;
    public static boolean disabled;

    /** Should be true once the pipeline is hooked; kept lazy in case the probe is touched very early. */
    public static void refresh(){
        if(FramePipeline.instance != null) available = true;
    }

    /** True when the pipeline exists and the user hasn't disabled it. */
    public static boolean active(){
        refresh();
        return available && !disabled;
    }

    /** L2 (GPU off-main-thread) toggle used by the settings page. */
    public static void setGpuDisabled(boolean disable){
        BackendGpuProbe.disabled = disable;
    }

    /**
     * Submits a frame-delayed job. Returns true if it was handed to the worker; false if the pipeline is
     * absent (or disabled), in which case the caller MUST run capture+compute+apply inline.
     */
    public static boolean register(Prov<?> capture, Func<?, ?> compute, Cons<?> apply, int delayFrames){
        if(!active()) return false;
        try{
            return FramePipeline.register(
                (Prov<Object>)capture,
                (Func<Object, Object>)compute,
                (Cons<Object>)apply,
                delayFrames
            );
        }catch(Throwable t){
            Log.err("MPOF: pipeline register failed, disabling pipelining: @", t);
            disabled = true;
            return false;
        }
    }

    public static String stats(){
        FramePipeline p = FramePipeline.instance;
        if(p == null) return "RendererPipeline: not hooked yet (listener never installed).";
        long jobs = p.jobsProcessed;
        long work = p.workNanos;
        long blocked = p.blockNanos;
        int pending = p.pending();
        double avgUs = jobs == 0 ? 0 : work / (double)jobs / 1000d;
        double avgBlockedUs = jobs == 0 ? 0 : blocked / (double)jobs / 1000d;
        long lastMs = p.tickNanos == 0 ? -1 : (System.nanoTime() - p.tickNanos) / 1_000_000L;
        String state = (p.tickCount > 0 ? "active" : "hooked but no frame tick yet") + (disabled ? " (user-disabled)" : "");
        return "RendererPipeline: " + state + "\n" +
            "no arc-sdl merge required\n\n" +
            "frame ticks driven: " + p.tickCount + (lastMs >= 0 ? " (last " + lastMs + "ms ago)" : "") + "\n" +
            "consumer: quality-async=" + SchedQuality.asyncJobs +
            " quality-sync=" + SchedQuality.syncFalls +
            " demo=" + SchedQuality.demoJobs + "\n" +
            "jobs applied: " + jobs + "\n" +
            "avg worker time: " + Strings.fixed((float)avgUs, 2) + " us\n" +
            "avg GL-thread fence: " + Strings.fixed((float)avgBlockedUs, 2) + " us\n" +
            "slots in flight: " + pending + "\n\n" +
            "L2 (shared-GL workers): " + l2Status();
    }

    private static String l2Status(){
        BackendGpuProbe.refresh();
        if(!BackendGpuProbe.available){
            return "not on this backend (unpatched jar)";
        }
        if(!BackendGpuProbe.gpuSupport){
            return "backend patched but native lib lacks SDL_GL_MakeCurrent - run :runtime:backend-sdl:jnigen -Pjnigen to rebuild natives";
        }
        String state = BackendGpuProbe.disabled ? " (user-disabled)" : "";
        return "gpu workers " + BackendGpuProbe.gpuWorkersReady + "/" + BackendGpuProbe.workerCount +
            (L2SharedPass.rendering ? " rendering off-main" : " idle") + state +
            "\n  last gpu pass: " + Strings.fixed((float)L2SharedPass.lastGpuNanos / 1000f, 2) + " us" +
            ", cumulative passes: " + L2SharedPass.totalPasses;
    }
}