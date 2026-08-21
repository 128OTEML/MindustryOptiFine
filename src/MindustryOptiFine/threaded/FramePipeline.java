package MindustryOptiFine.threaded;

import arc.ApplicationListener;
import arc.Core;
import arc.func.*;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Threads;

/**
 * L1 frame pipeline, driven from the mod on ANY backend (vanilla fat jar included).
 *
 * Instead of patching/replacing the backend jar, the mod slides its own {@link ApplicationListener} into the
 * frame loop via {@code Core.app.addListener(...)}. On the SDL backend the loop runs every listener's
 * {@code update()} (the game draws inside its own listener's update), then queues, then swaps — so our
 * listener runs once per rendered frame, right after the game's update+render and before the present.
 * That is exactly the slot the patched backend used to dedicate to {@code RenderScheduler.frameTick()}.
 *
 * GL/main thread = single producer & consumer of frame data. During frame F a job captures an independent
 * snapshot (deep copy, no GL), the worker precomputes it while the game keeps rendering, and N frames later
 * the result is applied back on the GL thread with a non-blocking-in-steady-state fence.
 */
public final class FramePipeline{
    /** Hooked once the mod registers with the app loop; null until then. */
    public static volatile FramePipeline instance;

    // stats, read by the debug dialog
    public volatile long jobsProcessed;
    public volatile long workNanos;
    public volatile long blockNanos;
    public volatile long lastFramesBehind;
    /** Number of frame-boundary ticks driven on the GL thread by the hooked listener. */
    public volatile long tickCount;
    /** nanoTime() of the most recent tick; 0 if the listener never ran. */
    public volatile long tickNanos;

    private final Object lock = new Object();
    private final Thread workerThread;
    private volatile boolean running = true;
    private volatile boolean firstTickLogged;

    // slot store: ONLY the GL thread mutates the seqs, always while holding `lock`.
    private final Seq<Slot> free = new Seq<>(8);
    private final Seq<Slot> inflight = new Seq<>(8);

    private static class Slot{
        Prov<Object> capture;
        Func<Object, Object> compute;
        Cons<Object> apply;
        int remaining;
        Object data;
        boolean computing;
    }

    private FramePipeline(){
        workerThread = Threads.daemon("MPOF-frame-pipeline-worker", this::workerLoop);
    }

    /** Installs the frame-loop hook. Safe to call once from the mod constructor. */
    public static void hook(){
        if(instance != null) return;
        FramePipeline p = new FramePipeline();
        instance = p;
        Core.app.addListener(new ApplicationListener(){
            @Override
            public void update(){
                p.tick();
            }
        });
        Log.info("MPOF: frame pipeline hooked via ApplicationListener (no backend patch required).");
    }

    /**
     * Registers a frame-delayed CPU job.
     *
     * @param capture called immediately (GL thread); MUST return a fresh, independent copy of the state.
     * @param compute called on the worker thread; may return null to apply the captured object in place,
     *                or a replacement object.
     * @param apply called on the GL thread {@code delayFrames} frames later.
     * @param delayFrames frames delayed before apply (at least 1).
     */
    public static boolean register(Prov<Object> capture, Func<Object, Object> compute, Cons<Object> apply, int delayFrames){
        FramePipeline p = instance;
        if(p == null) return false;
        p.submit(capture, compute, apply, delayFrames);
        return true;
    }

    private void submit(Prov<Object> capture, Func<Object, Object> compute, Cons<Object> apply, int delayFrames){
        if(!running){
            //shutting down; run inline so the caller still gets a result
            Object data = safeCapture(capture);
            Object out = safeCompute(compute, data);
            safeApply(apply, out == null ? data : out);
            jobsProcessed++;
            return;
        }

        Slot s;
        synchronized(lock){
            s = free.isEmpty() ? new Slot() : free.pop();
        }

        s.capture = capture;
        s.compute = compute;
        s.apply = apply;
        s.remaining = Math.max(1, delayFrames);
        s.data = s.capture.get();
        s.computing = true;

        synchronized(lock){
            inflight.add(s);
            lock.notifyAll();
        }
    }

    /** One frame boundary (GL thread), driven by the hooked ApplicationListener. */
    public void tick(){
        if(!running) return;
        tickNanos = System.nanoTime();
        tickCount++;
        if(!firstTickLogged){
            firstTickLogged = true;
            Log.info("MPOF: frame pipeline loaded OK - driving the frame loop in this game, no arc-sdl merge required.");
        }

        Seq<Slot> ready = new Seq<>(4);

        synchronized(lock){
            long waitStart = System.nanoTime();
            boolean blocked = false;

            for(int i = inflight.size - 1; i >= 0; i--){
                Slot s = inflight.get(i);
                if(s.remaining > 0){
                    s.remaining--;
                    continue;
                }
                //due: fence until the worker finished (worker never mutates the seq, so back-scan stays valid)
                if(s.computing){
                    blocked = true;
                    while(s.computing && running){
                        try{
                            lock.wait();
                        }catch(InterruptedException ignored){
                        }
                    }
                    if(!running) break;
                }
                inflight.remove(i);
                ready.add(s);
            }
            if(blocked) blockNanos += System.nanoTime() - waitStart;
            lastFramesBehind = ready.size;
        }

        for(Slot s : ready){
            safeApply(s.apply, s.data);
            jobsProcessed++;
            synchronized(lock){
                s.data = null;
                s.capture = null;
                s.compute = null;
                s.apply = null;
                free.add(s);
            }
        }
    }

    public int pending(){
        synchronized(lock){
            return inflight.size;
        }
    }

    public void stop(){
        running = false;
        synchronized(lock){
            lock.notifyAll();
        }
        if(workerThread != null){
            workerThread.interrupt();
        }
    }

    private void workerLoop(){
        while(running){
            Slot s;
            synchronized(lock){
                for(;;){
                    s = null;
                    for(int i = 0; i < inflight.size; i++){
                        Slot c = inflight.get(i);
                        if(c.computing){
                            s = c;
                            break;
                        }
                    }
                    if(s != null || !running) break;
                    try{
                        lock.wait();
                    }catch(InterruptedException e){
                        continue;
                    }
                }
                if(!running) return;
            }

            long t = System.nanoTime();
            Object out = safeCompute(s.compute, s.data);
            if(out != null) s.data = out;
            long elapsed = System.nanoTime() - t;

            synchronized(lock){
                s.computing = false;
                workNanos += elapsed;
                lock.notifyAll();
            }
        }
    }

    private static Object safeCapture(Prov<Object> capture){
        try{
            return capture.get();
        }catch(Throwable t){
            Log.err("MPOF pipeline capture failed", t);
            return null;
        }
    }

    private static Object safeCompute(Func<Object, Object> compute, Object data){
        try{
            return compute.get(data);
        }catch(Throwable t){
            Log.err("MPOF pipeline compute failed", t);
            return null;
        }
    }

    private static void safeApply(Cons<Object> apply, Object data){
        try{
            if(data != null) apply.get(data);
        }catch(Throwable t){
            Log.err("MPOF pipeline apply failed", t);
        }
    }
}