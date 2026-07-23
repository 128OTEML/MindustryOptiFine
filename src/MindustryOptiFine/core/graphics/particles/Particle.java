package MindustryOptiFine.core.graphics.particles;

import arc.*;
import arc.graphics.*;
import arc.graphics.Blending;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import MindustryOptiFine.core.graphics.atlases.AtlasManager;

public abstract class Particle {
    public TextureRegion region;

    public abstract String atlasTextureName();

    protected boolean manuallyDrawn = false;

    public Vec2 position = new Vec2();
    public Vec2 velocity = new Vec2();
    public Vec2 scale = new Vec2(1f, 1f);
    public Color drawColor = Color.white;
    public Rect frame;
    public float rotation = 0f;
    public float rotationSpeed = 0f;
    public float opacity = 1f;
    public int time = 0;
    public int lifetime = 60;
    public int direction = 1;

    public float lifetimeRatio() {
        return time / (float)lifetime;
    }

    public Blending blend() {
        return Blending.normal;
    }

    public int frameCount() {
        return 1;
    }

    public Particle spawn() {
        time = 0;

        if (ParticleManager.activeParticles.size > ParticleManager.maxParticles) {
            ParticleManager.activeParticles.get(0).kill();
        }

        if (ParticleManager.manualRenderers.containsKey(getClass())) {
            manuallyDrawn = true;
        }

        ParticleManager.activeParticles.add(this);
        ParticleManager.addToDrawList(this);

        region = Core.atlas.find(atlasTextureName());
        if (region == null) {
            Texture tex = AtlasManager.getTexture(atlasTextureName());
            if (tex != null) {
                region = new TextureRegion(tex);
            }
        }
        return this;
    }

    public void kill() {
        time = lifetime;
    }

    public void update() {
    }

    public void draw() {
        Draw.color(drawColor.r, drawColor.g, drawColor.b, drawColor.a * opacity);
        if (frame != null) {
            Draw.rect(region, position.x, position.y, frame.width, frame.height);
        } else {
            Draw.rect(region, position.x, position.y, scale.x * (direction == -1 ? -1 : 1), scale.y);
        }
        Draw.color();
    }
}