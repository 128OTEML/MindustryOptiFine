package MindustryOptiFine.shaders;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.*;
import arc.util.*;
import MindustryOptiFine.shadow.ShadowRenderer;
import MindustryOptiFine.utils.RefUtils;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;

import java.lang.reflect.Field;

/**
 * Port of the Factoriodustry external fluid shader replacement (scripts/shaders.js).
 *
 * Replaces the vanilla water/tar/mud/slag surface shaders with the older-style
 * animated ones that fake 3D reflections. The per-frame "flying object" capture
 * between {@link Layer#flyingUnitLow} and {@link Layer#flyingUnit} is provided
 * by Shadow2 (see {@link ShadowRenderer#getFlyingBuffer()}) and sampled as
 * {@code u_flying} (unit 2) - there is no separate capture channel here.
 *
 * All resources are created lazily on the game loop / {@code ClientLoadEvent} only,
 * never during mod class loading: GL calls while the context is unavailable would
 * hard-crash the game without any Java log output.
 */
public class FluidShaders{
    private static boolean shadersInit;

    public static Shaders.SurfaceShader water, tar, mud, slag;

    /** Single entry point, call once from the mod constructor. Only registers event listeners here. */
    public static void init(){
        if(Vars.headless) return;

        //deferred shader build & replacement, mirroring the JS mod's ClientLoadEvent hook.
        //the u_flying capture itself now lives in ShadowRenderer.queue() (Shadow2 pipeline).
        Events.on(ClientLoadEvent.class, e -> Core.app.post(FluidShaders::initShaders));
    }

    /** Builds the shaders and swaps both {@link Shaders} and {@link CacheLayer} references. Requires Vars.tree to be usable. */
    public static void initShaders(){
        if(shadersInit || Vars.headless) return;

        try{
            water = new WaterShader();
            tar = new TarShader();
            mud = new MudShader();
            slag = new SlagShader();

            Shaders.water = water;
            Shaders.tar = tar;
            Shaders.mud = mud;
            Shaders.slag = slag;

            shadersInit = true;

            replaceCacheLayer("water", water);
            replaceCacheLayer("tar", tar);
            replaceCacheLayer("mud", mud);
            replaceCacheLayer("slag", slag);

            Log.info("FluidShaders: replaced vanilla water/tar/mud/slag shaders with external animated variants");
        }catch(Throwable t){
            //never let a shader compile failure hard-crash the client
            Log.err("FluidShaders: failed to load external fluid shaders", t);
        }
    }

    /** Mirrors the JS addShader(): swaps CacheLayer.<name> and the matching entry in CacheLayer.all. */
    private static void replaceCacheLayer(String name, Shader shader){
        Field field = RefUtils.getField(CacheLayer.class, name);
        if(field == null) return;

        CacheLayer original;
        try{
            original = (CacheLayer)field.get(null);
        }catch(Exception e){
            return;
        }
        if(original == null) return;

        CacheLayer layer = new CacheLayer.ShaderLayer(shader, original.liquid);
        try{
            field.set(null, layer);
        }catch(Exception ignored){
        }

        CacheLayer[] all = CacheLayer.all;
        for(int i = 0; i < all.length; i++){
            if(all[i] == original){
                all[i] = layer;
                all[i].id = i;
            }
        }
    }

    public abstract static class FluidSurfaceShader extends Shaders.SurfaceShader{
        public FluidSurfaceShader(String fragName){
            super(Vars.tree.get("shaders/screenspace.vert").readString(), Vars.tree.get("shaders/" + fragName + ".frag").readString());
        }

        protected void applyBase(){
            super.apply();
            FrameBuffer flying = ShadowRenderer.getFlyingBuffer();
            if(flying == null) return;
            flying.getTexture().bind(2);
            setUniformi("u_flying", 2);
        }
    }

    public static class WaterShader extends FluidSurfaceShader{
        public WaterShader(){
            super("water");
        }

        @Override
        public void apply(){
            applyBase();
            setUniformf("mscl", 300.0f, 60.0f);
            setUniformf("tscal", 1.0f);
        }
    }

    public static class TarShader extends FluidSurfaceShader{
        public TarShader(){
            super("tar");
        }

        @Override
        public void apply(){
            applyBase();
            setUniformf("mscl", 300.0f, 200.0f);
            setUniformf("tscal", 0.2f);
        }
    }

    public static class MudShader extends FluidSurfaceShader{
        public MudShader(){
            super("mud");
        }

        @Override
        public void apply(){
            applyBase();
            setUniformf("mscl", 100.0f, 100.0f);
            setUniformf("tscal", 0.02f);
        }
    }

    public static class SlagShader extends FluidSurfaceShader{
        public SlagShader(){
            super("slag");
        }

        @Override
        public void apply(){
            super.apply();
        }
    }
}