package MindustryOptiFine.core.graphics;

import arc.util.*;
import mindustry.*;
import MindustryOptiFine.core.graphics.particles.ParticleManager;
import MindustryOptiFine.core.graphics.particles.Metaballs.MetaballManager;
import MindustryOptiFine.core.graphics.postprocessing.PostProcessingPipeline;
import MindustryOptiFine.core.graphics.automators.RenderTargetManager;
import MindustryOptiFine.core.graphics.primitives.PrimitiveRenderer;
import MindustryOptiFine.core.graphics.shaders.ShaderManager;

public class CoreGraphicsInit {
    public static void loadAll() {
        System.out.println("[CORE] CoreGraphicsInit: starting loadAll");
        
        System.out.println("[CORE] before ParticleManager.load()");
        ParticleManager.load();
        System.out.println("[CORE] after ParticleManager.load()");
        
        System.out.println("[CORE] before MetaballManager.load()");
        MetaballManager.load();
        System.out.println("[CORE] after MetaballManager.load()");
        
        System.out.println("[CORE] before RenderTargetManager.load()");
        RenderTargetManager.load();
        System.out.println("[CORE] after RenderTargetManager.load()");
        
        System.out.println("[CORE] before PostProcessingPipeline.load()");
        PostProcessingPipeline.load();
        System.out.println("[CORE] after PostProcessingPipeline.load()");
        
        System.out.println("[CORE] before PrimitiveRenderer.load()");
        PrimitiveRenderer.load();
        System.out.println("[CORE] after PrimitiveRenderer.load()");
        
        System.out.println("[CORE] before ShaderManager.load()");
        ShaderManager.load();
        System.out.println("[CORE] after ShaderManager.load()");
        
        System.out.println("[CORE] before ShaderManager.loadShaders()");
        ShaderManager.loadShaders();
        System.out.println("[CORE] after ShaderManager.loadShaders()");
        
        System.out.println("[CORE] CoreGraphicsInit: loadAll completed");
    }

    public static void updateAll() {
        ParticleManager.update();
        RenderTargetManager.handleTargetUpdateLoop();
        ShaderManager.update();
    }

    public static void drawAll() {
        if (Vars.state == null || !Vars.state.isGame()) {
            return;
        }
        
        try {
            ParticleManager.draw();
            MetaballManager.prepareMetaballTargets();
            MetaballManager.drawMetaballTargets();
        } catch (Exception e) {
            Log.err("CoreGraphics draw error", e);
        }
    }
}