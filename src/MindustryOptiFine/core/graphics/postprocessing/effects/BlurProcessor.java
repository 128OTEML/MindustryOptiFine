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

public class BlurProcessor implements PostProcessor {
    private ManagedShader managedShader;
    private boolean enabled = true;
    private Vec2 blurStrength = new Vec2(1f, 1f);
    private int blurPasses = 2;

    // Reusable framebuffers for blur passes (created once, reused)
    private FrameBuffer temp1, temp2;

    /** Custom Shader subclass that applies blurDirection from BlurProcessor */
    private class BlurShader extends Shader {
        public float dirX = 1f, dirY = 0f;

        BlurShader(Fi vert, Fi frag) {
            super(vert, frag);
        }

        @Override
        public void apply() {
            setUniformf("u_blurDirection", dirX, dirY);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    private BlurShader blurShader;

    public BlurProcessor() {
        managedShader = ShaderManager.getShader("blur");

        // Try to create BlurShader from mod shader files (Vars.tree)
        try {
            Fi frag = Vars.tree.get("shaders/blur.frag");
            Fi vert = Vars.tree.get("shaders/screenspace.vert");
            if (frag.exists() && vert.exists()) {
                blurShader = new BlurShader(vert, frag);
                Log.info("BlurProcessor: created BlurShader from " + frag.path());
            } else {
                Log.err("BlurProcessor: shader files not found");
            }
        } catch (Exception e) {
            Log.err("BlurProcessor: failed to create BlurShader", e);
        }
    }

    private void ensureBuffers() {
        int w = Core.graphics.getWidth();
        int h = Core.graphics.getHeight();
        if (temp1 == null || temp1.getWidth() != w || temp1.getHeight() != h) {
            if (temp1 != null) temp1.dispose();
            if (temp2 != null) temp2.dispose();
            temp1 = new FrameBuffer(w, h);
            temp2 = new FrameBuffer(w, h);
        }
    }

    @Override
    public void apply(FrameBuffer source) {
        if (blurShader == null) {
            Draw.rect(new TextureRegion(source.getTexture()),
                Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f,
                Core.graphics.getWidth(), Core.graphics.getHeight());
            return;
        }

        ensureBuffers();
        FrameBuffer current = source;

        for (int i = 0; i < blurPasses; i++) {
            // Horizontal pass
            blurShader.dirX = blurStrength.x;
            blurShader.dirY = 0f;

            temp1.begin(Color.clear);
            renderWithShader(current, blurShader);
            temp1.end();

            // Vertical pass
            blurShader.dirX = 0f;
            blurShader.dirY = blurStrength.y;

            temp2.begin(Color.clear);
            renderWithShader(temp1, blurShader);
            temp2.end();

            current = temp2;
        }

        Draw.rect(new TextureRegion(current.getTexture()),
            Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f,
            Core.graphics.getWidth(), Core.graphics.getHeight());
    }

    private void renderWithShader(FrameBuffer source, Shader shader) {
        Draw.shader(shader);
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

    public void setBlurStrength(float x, float y) {
        blurStrength.set(x, y);
    }

    public void setBlurStrength(Vec2 strength) {
        blurStrength.set(strength);
    }

    public Vec2 getBlurStrength() {
        return blurStrength;
    }

    public void setBlurPasses(int passes) {
        blurPasses = passes;
    }

    public int getBlurPasses() {
        return blurPasses;
    }
}
