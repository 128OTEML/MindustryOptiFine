package MindustryOptiFine.core.graphics.postprocessing.effects;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import MindustryOptiFine.core.graphics.postprocessing.*;
import MindustryOptiFine.core.graphics.shaders.*;

public class PixelationProcessor implements PostProcessor {
    private ManagedShader managedShader;
    private boolean enabled = true;
    private Vec2 pixelationFactor = new Vec2(8, 8);

    private class PixelationShader extends Shader {
        public float px, py;

        PixelationShader(Fi vert, Fi frag) {
            super(vert, frag);
        }

        @Override
        public void apply() {
            setUniformf("pixelationFactor", px, py);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    private PixelationShader pixelationShader;

    public PixelationProcessor() {
        managedShader = ShaderManager.getShader("pixelation");

        try {
            Fi frag = Vars.tree.get("shaders/pixelation.frag");
            Fi vert = Vars.tree.get("shaders/screenspace.vert");
            if (frag.exists() && vert.exists()) {
                pixelationShader = new PixelationShader(vert, frag);
                Log.info("PixelationProcessor: created PixelationShader");
            }
        } catch (Exception e) {
            Log.err("PixelationProcessor: failed to create PixelationShader", e);
        }
    }

    @Override
    public void apply(FrameBuffer source) {
        if (pixelationShader == null) {
            Draw.rect(new TextureRegion(source.getTexture()),
                Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f,
                Core.graphics.getWidth(), Core.graphics.getHeight());
            return;
        }

        pixelationShader.px = pixelationFactor.x;
        pixelationShader.py = pixelationFactor.y;

        Draw.shader(pixelationShader);
        Draw.rect(new TextureRegion(source.getTexture()),
            Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f,
            Core.graphics.getWidth(), Core.graphics.getHeight());
        Draw.flush();
        Draw.shader();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPixelationFactor(float x, float y) {
        pixelationFactor.set(x, y);
    }

    public void setPixelationFactor(Vec2 factor) {
        pixelationFactor.set(factor);
    }

    public Vec2 getPixelationFactor() {
        return pixelationFactor;
    }
}
