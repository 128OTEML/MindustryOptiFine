package MindustryOptiFine.core.graphics.particles;

import arc.*;
import arc.graphics.*;
import arc.graphics.Blending;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType;

public class ParticleManager {
    public static final Seq<Particle> activeParticles = new Seq<>();

    private static final ObjectMap<Blending, Seq<Particle>> drawCollectionsByBlendState = new ObjectMap<>();

    public static final ObjectMap<Class<?>, Seq<Particle>> manualDrawCollections = new ObjectMap<>();

    public static final ObjectMap<Class<?>, IManualParticleRenderer> manualRenderers = new ObjectMap<>();

    public static int maxParticles = 1000;

    public static void load() {
        Events.on(EventType.WorldLoadEvent.class, e -> reset());
    }

    public static void unload() {
        activeParticles.clear();
        for (Seq<Particle> collection : drawCollectionsByBlendState.values()) {
            collection.clear();
        }
        drawCollectionsByBlendState.clear();
    }

    public static void reset() {
        activeParticles.clear();
        for (Seq<Particle> collection : drawCollectionsByBlendState.values()) {
            collection.clear();
        }
    }

    public static void update() {
        for (int i = 0; i < activeParticles.size; i++) {
            Particle p = activeParticles.get(i);
            p.update();
            p.position.add(p.velocity);
            p.time++;
        }

        activeParticles.removeAll(p -> {
            if (p.time >= p.lifetime) {
                removeFromDrawList(p);
                return true;
            }
            return false;
        });
    }

    public static void draw() {
        for (ObjectMap.Entry<Blending, Seq<Particle>> entry : drawCollectionsByBlendState) {
            Draw.blend(entry.key);
            for (Particle particle : entry.value) {
                particle.draw();
            }
        }
        Draw.blend(Blending.normal);

        for (IManualParticleRenderer renderer : manualRenderers.values()) {
            renderer.renderParticles();
        }
    }

    public static void registerRenderer(Class<?> particleType, IManualParticleRenderer particleRenderer) {
        manualRenderers.put(particleType, particleRenderer);
    }

    public static void addToDrawList(Particle particle) {
        if (particle.manuallyDrawn) {
            IManualParticleRenderer renderer = manualRenderers.get(particle.getClass());
            if (renderer != null) {
                renderer.addParticle(particle);
            }
        } else {
            getCorrectDrawCollection(particle).add(particle);
        }
    }

    private static void removeFromDrawList(Particle particle) {
        if (particle.manuallyDrawn) {
            IManualParticleRenderer renderer = manualRenderers.get(particle.getClass());
            if (renderer != null) {
                renderer.removeParticle(particle);
            }
        } else {
            getCorrectDrawCollection(particle).remove(particle);
        }
    }

    private static Seq<Particle> getCorrectDrawCollection(Particle particle) {
        if (!particle.manuallyDrawn) {
            Blending blend = particle.blend();
            if (!drawCollectionsByBlendState.containsKey(blend)) {
                drawCollectionsByBlendState.put(blend, new Seq<>());
            }
            return drawCollectionsByBlendState.get(blend);
        } else {
            Class<?> type = particle.getClass();
            if (!manualDrawCollections.containsKey(type)) {
                manualDrawCollections.put(type, new Seq<>());
            }
            return manualDrawCollections.get(type);
        }
    }
}