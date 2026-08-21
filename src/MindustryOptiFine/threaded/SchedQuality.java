package MindustryOptiFine.threaded;

import arc.*;
import mindustry.Vars;
import MindustryOptiFine.MindustryOptiFine;

/**
 * First real L1 consumer: the dynamic-quality controller. The per-frame lerp math (which currently runs on the
 * GL thread as {@code MindustryOptiFine.updateDynamicQuality}) is moved onto the scheduler worker, one frame
 * ahead. GPU work (bloom FBO resize + parameter upload) stays on the GL thread in {@link #apply}.
 *
 * If the patched backend is absent, {@link #tick()} transparently falls back to the synchronous path.
 * An optional artificial worker load ({@code al-sched-load} setting, 0 = off) makes the pipelining measurable
 * without changing visuals.
 */
public final class SchedQuality{
    /** Optional injected worker load in nanoseconds (0 = off). */
    public static volatile long demoLoadNanos;

    // consumer counters, surfaced by the Pipeline Stats dialog so a dead gate is visible instead of silent 0s
    public static long asyncJobs;
    public static long syncFalls;
    public static long demoJobs;

    private SchedQuality(){
    }

    /** GL thread: capture an independent snapshot of the state needed to plan next frame's quality. */
    static Object capture(){
        QualityFrame f = new QualityFrame();
        f.zoom = Vars.renderer.getDisplayScale();
        f.currentScale = MindustryOptiFine.currentQualityScale;
        f.targetScale = MindustryOptiFine.targetQualityScale;
        f.transitionSpeed = MindustryOptiFine.qualityTransitionSpeed;
        f.bloomQuality = MindustryOptiFine.bloomQuality;
        f.blurAmount = Core.settings.getInt("al-bloom-blur-amount", 2);
        f.flareAmount = Core.settings.getInt("al-bloom-flare-amount", 3);
        return f;
    }

    /** Worker thread: pure math, no GL. Mirrors updateDynamicQuality() one frame ahead. */
    static Object compute(Object data){
        QualityFrame f = (QualityFrame)data;

        long load = demoLoadNanos;
        if(load > 0){
            long end = System.nanoTime() + load;
            while(System.nanoTime() < end){
                //busy wait: simulates a CPU-heavy precompute so the pipeline is measurable
            }
        }

        if(f.zoom <= 0.5f){
            f.targetScale = 0.5f;
        }else if(f.zoom <= 1f){
            f.targetScale = 0.75f;
        }else if(f.zoom <= 2f){
            f.targetScale = 1f;
        }else{
            f.targetScale = Math.min(f.zoom * 0.5f + 0.5f, 1.5f);
        }

        f.newScale = f.currentScale + (f.targetScale - f.currentScale) * f.transitionSpeed;
        f.targetBloomQuality = Math.max(1, (int)(f.bloomQuality * f.newScale));
        f.targetBlur = Math.max(1, (int)(f.blurAmount * f.newScale));
        f.targetFlare = Math.max(0, (int)(f.flareAmount * f.newScale));
        return f;
    }

    /** GL thread, one frame later: commit worker results; GPU-touching calls only. */
    static void apply(Object data){
        QualityFrame f = (QualityFrame)data;

        MindustryOptiFine.currentZoom = f.zoom;
        MindustryOptiFine.targetQualityScale = f.targetScale;
        MindustryOptiFine.currentQualityScale = f.newScale;

        if(MindustryOptiFine.bloom != null){
            MindustryOptiFine.bloom.resize(Core.graphics.getWidth(), Core.graphics.getHeight(), f.targetBloomQuality);
            MindustryOptiFine.bloom.blurPasses = f.targetBlur;
            MindustryOptiFine.bloom.flarePasses = f.targetFlare;
        }
    }

    /** One update tick; async when the frame pipeline is active, sync otherwise. */
    public static void tick(){
        if(SchedulerProbe.active()){
            if(SchedulerProbe.register(SchedQuality::capture, SchedQuality::compute, SchedQuality::apply, 1)){
                asyncJobs++;
                return;
            }
        }
        syncFalls++;
        MindustryOptiFine.updateDynamicQuality();
    }

    /**
     * Always-called probe (independent of auto-quality/camera gating): makes the pipeline measurable anywhere,
     * e.g. even from the main menu. No-op when the demo load is 0.
     */
    public static void demo(){
        long load = demoLoadNanos;
        if(load <= 0) return;
        if(SchedulerProbe.active()){
            if(SchedulerProbe.register(
                () -> new Object(),
                o -> {
                    long end = System.nanoTime() + load;
                    while(System.nanoTime() < end){
                        //busy wait: simulates CPU-heavy precompute for a measurable async result
                    }
                    return o;
                },
                o -> {
                },
                1
            )){
                demoJobs++;
            }
        }
    }

    static final class QualityFrame{
        float zoom, currentScale, targetScale, transitionSpeed;
        int bloomQuality, blurAmount, flareAmount;
        float newScale;
        int targetBloomQuality, targetBlur, targetFlare;
    }
}