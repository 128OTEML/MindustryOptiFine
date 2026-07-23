package MindustryOptiFine.graphics.metaballs;

import arc.math.geom.*;
import arc.graphics.*;

public class MetaballInstance{
    public Vec2 position = new Vec2();
    public float radius = 50f;
    public float radiusSpeed = 0f;
    public float intensity = 1f;
    public Color color = Color.white.cpy();
    public int lifetime = 60;
    public int life = 0;

    public void update(){
        life++;
        radius += radiusSpeed;
        
        if(radius < 0) radius = 0;
    }

    public boolean isDead(){
        return life >= lifetime || radius <= 0;
    }

    public float lifeRatio(){
        return (float)life / lifetime;
    }
}