package MindustryOptiFine.core.graphics.particles.Metaballs;

import arc.math.geom.Vec2;

public class MetaballInstance {
    public Vec2 center;
    public Vec2 velocity;
    public float size;
    public float[] extraInfo;

    public MetaballInstance(Vec2 center, Vec2 velocity, float size) {
        this(center, velocity, size, 0f, 0f, 0f, 0f);
    }

    public MetaballInstance(Vec2 center, Vec2 velocity, float size, float extraInfo0, float extraInfo1, float extraInfo2, float extraInfo3) {
        this.center = center;
        this.velocity = velocity;
        this.size = size;
        this.extraInfo = new float[]{extraInfo0, extraInfo1, extraInfo2, extraInfo3};
    }
}