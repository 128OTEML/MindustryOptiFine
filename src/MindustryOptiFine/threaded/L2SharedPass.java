package MindustryOptiFine.threaded;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.util.*;
import MindustryOptiFine.shaders.ModShaders;

/**
 * L2 consumer: an offscreen render pass executed on a SHARED-GL-CONTEXT worker instead of the main thread.
 *
 * The pass runs on whatever render worker the patched backend owns: its GL context shares textures/FBOs/
 * programs/meshes with the main context, so the FBOs, the quad mesh and the shader created here on the GL
 * thread are visible inside {@link #compute}, which issues the actual {@code Gl.*} draw calls. The scheduler
 * calls {@code glFinish()} on the worker right before hand-back, and the delayed {@link #apply} then consumes
 * the rendered FBO safely on the main thread where {@link #drawOverlay()} shows it.
 *
 * When there is no patched backend / rebuilt native (L2 unavailable), {@link #tick()} is a no-op and the mod
 * keeps rendering fully on the main thread.
 */
public final class L2SharedPass{
    /** Master switch (settings toggle "al-l2-pass"). */
    public static volatile boolean enabled;
    /** Number of fullscreen passes per job (each pass is off-main-thread GPU work). */
    public static volatile int passCount = 4;
    /** Offscreen size (width). */
    public static volatile int bufferSize = 256;

    // runtime stats surfaced by the Pipeline Stats dialog
    public static volatile boolean rendering;     // a job is currently handed to a GPU worker
    public static volatile long lastGpuNanos;     // worker-side GPU time of the most recent pass
    public static volatile long totalPasses;      // cumulative passes completed

    private static Shader pass;
    private static Mesh quad;
    private static FrameBuffer a, b;
    private static boolean inited;
    private static boolean initFailed;

    private L2SharedPass(){
    }

    /** GL thread, lazily, once: create the shared objects (they live in the main context's share group). */
    public static void init(){
        if(inited || initFailed) return;
        try{
            pass = new Shader(ModShaders.getShaderFi("l2pass.vert"), ModShaders.getShaderFi("l2pass.frag"));
            quad = new Mesh(true, 4, 6,
                VertexAttribute.position,
                VertexAttribute.texCoords);
            quad.setVertices(new float[]{
                -1f, -1f, 0f, 0f,
                 1f, -1f, 1f, 0f,
                 1f,  1f, 1f, 1f,
                -1f,  1f, 0f, 1f
            });
            quad.setIndices(new short[]{0, 1, 2, 2, 3, 0});
            int w = Math.max(1, bufferSize);
            int h = Math.max(1, bufferSize / 2);
            a = new FrameBuffer(w, h);
            b = new FrameBuffer(w, h);
            inited = true;
            Log.info("MPOF: L2 shared pass resources ready (@x@).", w, h);
        }catch(Throwable t){
            initFailed = true;
            Log.err("MPOF: L2 pass init failed - falling back to main-thread rendering", t);
        }
    }

    /** GL thread, each frame: hand one offscreen pass to a GPU worker, or do nothing when L2 is unavailable. */
    public static void tick(){
        if(!enabled || initFailed) return;
        if(!inited) init();
        if(!inited) return;
        if(!BackendGpuProbe.gpuReady()){
            rendering = false;
            return;
        }
        rendering = BackendGpuProbe.registerGpu(L2SharedPass::capture, L2SharedPass::compute, L2SharedPass::apply, 1);
    }

    static Object capture(){
        PassData d = new PassData();
        d.w = Math.max(1, bufferSize);
        d.h = Math.max(1, bufferSize / 2);
        d.passes = Math.max(1, passCount);
        d.seed = (float)((System.nanoTime() >>> 16) % 1000) / 1000f;
        d.time = Time.time;
        d.frame = Time.globalTime;
        return d;
    }

    /** Worker thread (shared GL context current): issue the actual render passes. */
    static Object compute(Object data){
        PassData d = (PassData)data;
        long t0 = System.nanoTime();
        int passes = Math.max(1, d.passes);
        FrameBuffer last = a;
        for(int k = 0; k < passes; k++){
            FrameBuffer cur = (k % 2 == 0) ? a : b;
            FrameBuffer prev = (k % 2 == 0) ? b : a;
            cur.begin(Color.clear);
            pass.bind();
            pass.setUniformf("u_time", d.time);
            pass.setUniformf("u_seed", d.seed);
            pass.setUniformf("u_resolution", d.w, d.h);
            pass.setUniformi("u_k", k);
            if(k > 0){
                Gl.activeTexture(Gl.texture0);
                prev.getTexture().bind();
                pass.setUniformi("u_texture", 0);
            }
            pass.apply();
            quad.render(pass, Gl.triangles);
            cur.end();
            last = cur;
        }
        d.result = last;
        d.gpuNanos = System.nanoTime() - t0;
        return d;
    }

    /** GL thread, {@code delayFrames} later: record the finished result for compositing. */
    static void apply(Object data){
        PassData d = (PassData)data;
        if(d == null || d.result == null) return;
        lastResult = d.result;
        totalPasses += d.passes;
        lastGpuNanos = d.gpuNanos;
    }

    /** The FBO whose attachment contains the latest worker-rendered result. */
    public static volatile FrameBuffer lastResult;

    /** GL thread, draw phase: show the worker-rendered result as a small overlay in the corner. */
    public static void drawOverlay(){
        if(!enabled) return;
        FrameBuffer r = lastResult;
        if(r == null) return;
        int s = r.getWidth();
        TextureRegion region = new TextureRegion(r.getTexture());
        region.flip(false, true);
        float vs = s * 1f;
        float hs = s * 0.5f;
        Draw.rect(region, Core.graphics.getWidth() - vs / 2f - 8f, 8f + hs / 2f, vs, hs);
    }

    static final class PassData{
        int w, h, passes;
        float seed, time, frame;
        long gpuNanos;
        FrameBuffer result;
    }
}