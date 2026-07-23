package MindustryOptiFine.core.graphics.particles;

public interface IManualParticleRenderer {
    void registerRenderer();

    void addParticle(Particle particle);

    void removeParticle(Particle particle);

    void renderParticles();
}