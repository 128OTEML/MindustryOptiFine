package MindustryOptiFine.core.graphics.particles.Metaballs;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.Vec2;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import MindustryOptiFine.core.graphics.automators.ManagedRenderTarget;
import MindustryOptiFine.core.graphics.shaders.ManagedShader;
import MindustryOptiFine.core.graphics.shaders.ShaderManager;

public abstract class MetaballType {
    protected Seq<MetaballInstance> particles = new Seq<>();

    public int activeParticleCount() {
        return particles.size;
    }

    public final Seq<ManagedRenderTarget> layerTargets = new Seq<>();

    public abstract boolean shouldRender();

    public abstract TextureRegion[] layerTextures();

    public abstract Color edgeColor();

    public abstract String metaballAtlasTextureToUse();

    public void createParticle(Vec2 spawnPosition, Vec2 velocity, float size) {
        createParticle(spawnPosition, velocity, size, 0f, 0f, 0f, 0f);
    }

    public void createParticle(Vec2 spawnPosition, Vec2 velocity, float size, float extraInfo0, float extraInfo1, float extraInfo2, float extraInfo3) {
        particles.add(new MetaballInstance(spawnPosition, velocity, size, extraInfo0, extraInfo1, extraInfo2, extraInfo3));
    }

    public void createParticle(MetaballInstance particle) {
        particles.add(particle);
    }

    public void createParticle(Seq<MetaballInstance> particles) {
        this.particles.addAll(particles);
    }

    public void clearInstances() {
        particles.clear();
    }

    public void update() {
        for (int i = 0; i < particles.size; i++) {
            MetaballInstance p = particles.get(i);
            updateParticle(p);
            p.center.add(p.velocity);
        }

        particles.removeAll(this::shouldKillParticle);
    }

    public void register() {
        MetaballManager.metaballTypes.add(this);

        int layerCount = layerTextures().length;
        for (int i = 0; i < layerCount; i++) {
            layerTargets.add(new ManagedRenderTarget(true, ManagedRenderTarget::createScreenSizedTarget, true));
        }
    }

    public void renderLayerWithShader() {
        Log.info("MetaballType: renderLayerWithShader called, layers: " + layerTargets.size);

        ManagedShader metaballShader = ShaderManager.getShader("metaballedge");
        if (metaballShader != null && metaballShader.shader != null) {
            Draw.shader(metaballShader.shader);
        }

        for (int i = 0; i < layerTargets.size; i++) {
            prepareShaderForTarget(i);
            FrameBuffer fb = layerTargets.get(i).getTarget();
            if (fb != null) {
                TextureRegion tex = new TextureRegion(fb.getTexture());
                Draw.rect(tex, Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, Core.graphics.getWidth(), Core.graphics.getHeight());
                Log.info("MetaballType: rendered layer " + i);
            }
        }

        if (metaballShader != null) {
            Draw.flush();
            Draw.shader();
        }
    }

    public void dispose() {
        for (ManagedRenderTarget target : layerTargets) {
            if (target != null) {
                target.dispose();
            }
        }
    }

    public boolean drawnManually() {
        return false;
    }

    public boolean layerIsFixedToScreen(int layerIndex) {
        return false;
    }

    public Vec2 calculateManualOffsetForLayer(int layerIndex) {
        return new Vec2();
    }

    public boolean performCustomSpritebatchBegin() {
        return false;
    }

    public void drawInstances() {
        TextureRegion texture = Core.atlas.find(metaballAtlasTextureToUse());
        if (texture == null) {
            return;
        }

        for (MetaballInstance particle : particles) {
            Draw.rect(texture, particle.center.x, particle.center.y, particle.size, particle.size);
        }

        extraDrawing();
    }

    public abstract boolean shouldKillParticle(MetaballInstance particle);

    public void prepareShaderForTarget(int layerIndex) {
        ManagedShader managed = ShaderManager.getShader("metaballedge");
        if (managed == null || managed.shader == null) {
            return;
        }

        TextureRegion layerTexture = layerTextures()[layerIndex];
        if (layerTexture == null || !layerTexture.found()) {
            return;
        }

        Shader shader = managed.shader;
        shader.bind();

        Vec2 screenSize = new Vec2(Core.graphics.getWidth(), Core.graphics.getHeight());
        Vec2 layerScrollOffset = Core.camera.position.cpy().div(screenSize).add(calculateManualOffsetForLayer(layerIndex));
        if (layerIsFixedToScreen(layerIndex)) {
            layerScrollOffset.set(0, 0);
        }

        shader.setUniformf("layerSize", layerTexture.width, layerTexture.height);
        shader.setUniformf("screenSize", screenSize.x, screenSize.y);
        shader.setUniformf("layerOffset", layerScrollOffset.x, layerScrollOffset.y);
        shader.setUniformf("edgeColor", edgeColor());
        shader.setUniformf("singleFrameScreenOffset", 0f, 0f);

        layerTexture.texture.bind(1);
    }

    public void extraDrawing() {
    }

    public abstract void updateParticle(MetaballInstance particle);
}