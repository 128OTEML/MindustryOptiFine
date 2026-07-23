package MindustryOptiFine.core.graphics.particles;

import arc.struct.*;

public abstract class ManualParticleRenderer<T extends Particle> implements IManualParticleRenderer {
    protected Seq<T> particles = new Seq<>();

    @Override
    public void registerRenderer() {
        ParticleManager.registerRenderer(getParticleType(), this);
    }

    protected abstract Class<T> getParticleType();

    @Override
    @SuppressWarnings("unchecked")
    public void addParticle(Particle particle) {
        if (getParticleType().isInstance(particle)) {
            particles.add((T) particle);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeParticle(Particle particle) {
        if (getParticleType().isInstance(particle)) {
            particles.remove((T) particle);
        }
    }

    @Override
    public void renderParticles() {
        renderParticlesImpl();
    }

    public abstract void renderParticlesImpl();
}