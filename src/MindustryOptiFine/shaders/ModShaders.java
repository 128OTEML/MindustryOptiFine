package MindustryOptiFine.shaders;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class ModShaders{
    public static Shader screenspace;
    public static BlurShader blur;
    public static ColorTweakShader colortweak;
    public static DistortionShader distortion;
    public static GlowShader glow;
    public static VignetteShader vignette;
    public static ChromaticAberrationShader chromatic;
    public static ScanlineShader scanline;
    public static PixelationShader pixelation;
    public static ModPanelShader panel;
    public static PowerfulSunShader sun;
    public static PrimitiveShader primitive;
    public static MetaballShader metaball;

    public static void init(){
        screenspace = safe("screenspace", () -> new LoadShader("screenspace", "screenspace"));
        blur = safe("blur", () -> new BlurShader());
        colortweak = safe("colortweak", () -> new ColorTweakShader());
        distortion = safe("distortion", () -> new DistortionShader());
        glow = safe("glow", () -> new GlowShader());
        vignette = safe("vignette", () -> new VignetteShader());
        chromatic = safe("chromatic", () -> new ChromaticAberrationShader());
        scanline = safe("scanline", () -> new ScanlineShader());
        pixelation = safe("pixelation", () -> new PixelationShader());
        panel = safe("panel", () -> new ModPanelShader());
        sun = safe("sun", () -> new PowerfulSunShader());
        primitive = safe("primitive", () -> new PrimitiveShader());
        metaball = safe("metaball", () -> new MetaballShader());
    }

    private static <T> T safe(String name, Prov<T> constructor){
        try{
            T shader = constructor.get();
            Log.info("ModShaders: loaded " + name + " successfully");
            return shader;
        }catch(Exception e){
            Log.err("ModShaders: failed to load " + name + " (shader resource disabled?): " + e.getMessage());
            return null;
        }
    }

    public static class LoadShader extends Shader{
        public LoadShader(String frag, String vert){
            super(getShaderFi(vert + ".vert"), getShaderFi(frag + ".frag"));
        }
    }

    public static Fi getShaderFi(String file){
        return tree.get("shaders/" + file);
    }

    public static class BlurShader extends Shader{
        public float radius = 4f;
        public boolean horizontal = true;

        public BlurShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("blur.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_radius", radius);
            setUniformf("u_horizontal", horizontal ? 1f : 0f);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static class ColorTweakShader extends Shader{
        public float brightness = 1f;
        public float contrast = 1f;
        public float saturation = 1f;
        public float gamma = 1f;

        public ColorTweakShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("colortweak.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_brightness", brightness);
            setUniformf("u_contrast", contrast);
            setUniformf("u_saturation", saturation);
            setUniformf("u_gamma", gamma);
        }
    }

    public static class DistortionShader extends Shader{
        public float strength = 0.05f;
        public float speed = 1f;

        public DistortionShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("distortion.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_strength", strength);
            setUniformf("u_speed", speed);
            setUniformf("u_time", Core.graphics.getDeltaTime());
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static class GlowShader extends Shader{
        public float threshold = 0.8f;
        public float intensity = 1.5f;

        public GlowShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("glow.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_threshold", threshold);
            setUniformf("u_intensity", intensity);
        }
    }

    public static class VignetteShader extends Shader{
        public float intensity = 0.5f;
        public float smoothness = 0.5f;
        public Color color = new Color(0, 0, 0, 1);

        public VignetteShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("vignette.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_intensity", intensity);
            setUniformf("u_smoothness", smoothness);
            setUniformf("u_color", color);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static class ChromaticAberrationShader extends Shader{
        public float intensity = 0.02f;
        public float angle = 0f;

        public ChromaticAberrationShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("chromatic.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_intensity", intensity);
            setUniformf("u_angle", angle);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static class ScanlineShader extends Shader{
        public float lineHeight = 4f;
        public float opacity = 0.1f;

        public ScanlineShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("scanline.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_lineHeight", lineHeight);
            setUniformf("u_opacity", opacity);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static class PixelationShader extends Shader{
        public float pixelSize = 8f;

        public PixelationShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("pixelation.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_pixelSize", pixelSize);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static class PrimitiveShader extends Shader{
        public Color color = Color.white;

        public PrimitiveShader(){
            super(getShaderFi("primitive.vert"), getShaderFi("primitive.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_color", color);
        }
    }

    public static class MetaballShader extends Shader{
        public float threshold = 0.6f;

        public MetaballShader(){
            super(getShaderFi("screenspace.vert"), getShaderFi("metaball.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_threshold", threshold);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }
}