package MindustryOptiFine.core.graphics.particles.Metaballs;

import arc.*;
import arc.graphics.Color;
import arc.graphics.gl.FrameBuffer;
import arc.struct.*;
import arc.util.*;
import MindustryOptiFine.core.graphics.automators.ManagedRenderTarget;

public class MetaballManager {
    public static final Seq<MetaballType> metaballTypes = new Seq<>();

    public static void load() {
        for (MetaballType type : metaballTypes) {
            type.register();
        }
    }

    public static void unload() {
        for (MetaballType type : metaballTypes) {
            type.dispose();
        }
        metaballTypes.clear();
    }

    public static void reset() {
        for (MetaballType type : metaballTypes) {
            type.clearInstances();
        }
    }

    public static void prepareMetaballTargets() {
        if (Core.scene != null && Core.scene.hasDialog()) {
            return;
        }

        Seq<MetaballType> activeTypes = new Seq<>();
        for (MetaballType type : metaballTypes) {
            if (type.shouldRender()) {
                activeTypes.add(type);
            }
        }

        if (activeTypes.isEmpty()) {
            return;
        }

        Log.info("MetaballManager: preparing " + activeTypes.size + " metaball types");

        for (MetaballType type : activeTypes) {
            type.update();

            for (ManagedRenderTarget target : type.layerTargets) {
                FrameBuffer fb = target.getTarget();
                if (fb == null || fb.getWidth() <= 0 || fb.getHeight() <= 0) {
                    continue;
                }
                
                Log.info("MetaballManager: beginning framebuffer (" + fb.getWidth() + "x" + fb.getHeight() + ")");
                fb.begin();
                Core.graphics.clear(Color.clear);

                type.drawInstances();

                fb.end();
                Log.info("MetaballManager: framebuffer ended");
            }
        }
    }

    public static void drawMetaballTargets() {
        for (MetaballType type : metaballTypes) {
            if (type.shouldRender()) {
                if (!type.drawnManually()) {
                    type.renderLayerWithShader();
                }
            }
        }
    }
}