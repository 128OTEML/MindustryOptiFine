package MindustryOptiFine.graphics.particles;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;

public class Particle{
    public Vec2 position = new Vec2();
    public Vec2 velocity = new Vec2();
    public Color color = Color.white.cpy();
    public float rotation = 0f;
    public float rotationSpeed = 0f;
    public float scale = 1f;
    public float scaleSpeed = 0f;
    public int lifetime = 60;
    public int life = 0;
    public float alpha = 1f;
    public float alphaSpeed = 0f;
    public TextureRegion region;
    public boolean additive = false;

    public void update(){
        life++;
        
        position.add(velocity);
        rotation += rotationSpeed;
        scale += scaleSpeed;
        alpha += alphaSpeed;
        
        if(scale <= 0) scale = 0.01f;
        if(alpha <= 0) alpha = 0;
    }

    public void draw(){
        if(alpha <= 0) return;
        
        boolean wasAdditive = Draw.getBlend() == Blending.additive;
        
        if(additive){
            Draw.blend(Blending.additive);
        }
        
        Draw.color(color.r, color.g, color.b, alpha);
        
        if(region != null){
            Draw.rect(region, position.x, position.y, region.width * scale, region.height * scale, rotation);
        }else{
            Fill.circle(position.x, position.y, 4f * scale);
        }
        
        Draw.color();
        
        if(additive && !wasAdditive){
            Draw.blend();
        }
    }

    public boolean isDead(){
        return life >= lifetime || alpha <= 0;
    }

    public float lifeRatio(){
        return (float)life / lifetime;
    }

    public void spawn(){
        ParticleManager.add(this);
    }
}