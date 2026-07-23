package MindustryOptiFine.graphics.particles;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.graphics.g2d.*;
import arc.struct.*;

public abstract class ManualParticleRenderer<T extends Particle>{
    protected Seq<T> particles = new Seq<>();

    public abstract void renderParticles();

    public void add(T particle){
        particles.add(particle);
    }

    public void remove(T particle){
        particles.remove(particle);
    }

    public void clear(){
        particles.clear();
    }

    public Seq<T> getParticles(){
        return particles;
    }

    public int getCount(){
        return particles.size;
    }

    public void update(){
        for(int i = particles.size - 1; i >= 0; i--){
            T p = particles.get(i);
            p.update();
            
            if(p.isDead()){
                particles.remove(i);
            }
        }
    }
}