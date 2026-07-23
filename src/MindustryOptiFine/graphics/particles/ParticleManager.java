package MindustryOptiFine.graphics.particles;

import arc.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.Vars;

public class ParticleManager{
    private static Seq<Particle> particles = new Seq<>();
    private static int maxParticles = 1000;

    public static void init(){
        Events.run(Trigger.update, ParticleManager::update);
        Events.run(Trigger.draw, ParticleManager::draw);
    }

    public static void add(Particle particle){
        if(particles.size >= maxParticles){
            particles.remove(0);
        }
        particles.add(particle);
    }

    public static void remove(Particle particle){
        particles.remove(particle);
    }

    public static void clear(){
        particles.clear();
    }

    private static void update(){
        if(Vars.state.isPaused()) return;
        
        for(int i = particles.size - 1; i >= 0; i--){
            Particle p = particles.get(i);
            p.update();
            
            if(p.isDead()){
                particles.remove(i);
            }
        }
    }

    private static void draw(){
        for(Particle p : particles){
            p.draw();
        }
    }

    public static void setMaxParticles(int max){
        maxParticles = max;
    }

    public static int getParticleCount(){
        return particles.size;
    }

    public static Seq<Particle> getParticles(){
        return particles;
    }
}