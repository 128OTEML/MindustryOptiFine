package MindustryOptiFine.shadow;

import arc.Core;
import arc.Events;
import mindustry.game.EventType;

public class ShadowMain {

    public static void initEvents() {
        Events.on(EventType.WorldLoadEvent.class, e -> {
            ShadowRenderer.ChunkCache.init();
        });
        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if (e.tile != null) {
                ShadowRenderer.ChunkCache.invalidateTile(e.tile.x, e.tile.y);
            } else {
                ShadowRenderer.shadDirty = true;
            }
        });
        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if (e.tile != null) {
                ShadowRenderer.ChunkCache.invalidateTile(e.tile.x, e.tile.y);
            } else {
                ShadowRenderer.shadDirty = true;
            }
        });
        Events.on(EventType.TileChangeEvent.class, e -> {
            if (e.tile != null) {
                ShadowRenderer.ChunkCache.invalidateTile(e.tile.x, e.tile.y);
            } else {
                ShadowRenderer.shadDirty = true;
            }
        });
        Events.on(EventType.PickupEvent.class, e -> ShadowRenderer.shadDirty = true);
        Events.on(EventType.PayloadDropEvent.class, e -> ShadowRenderer.shadDirty = true);

        Events.run(EventType.Trigger.draw, () -> ShadowRenderer.weatherMult = 1f);
        Events.run(EventType.Trigger.draw, ShadowRenderer::queue);
    }

    public static void loadSettings() {
        ShadowRenderer.graphicsQuality    = Core.settings.getInt ("graphics_quality",            2);
        ShadowRenderer.enabled            = Core.settings.getBool("shadows_enabled",     true);
        ShadowRenderer.dayNightCycle      = Core.settings.getBool("day_night_cycle",             true);
        ShadowRenderer.rotateShadows      = !Core.settings.getBool("static_shadows",            false);
        ShadowRenderer.unitShadowsEnabled = Core.settings.getBool("unit_shadows",        true);
        int pScaleVal = Core.settings.getInt("prop_shadow_scale", 100);
        ShadowRenderer.propShadowScale    = pScaleVal / 100f;
        ShadowRenderer.oldShadowsEnabled  = (pScaleVal == 0);
        ShadowRenderer.SHADOW_LENGTH      = Core.settings.getInt ("shadow_length",               10);
        ShadowRenderer.SHADOW_ALPHA       = Core.settings.getInt ("shadow_opacity_percent",       45) / 100f;
        ShadowRenderer.blurRadius         = Core.settings.getInt ("blur_radius",                  35) / 10f;
        ShadowRenderer.shadowTint         = Core.settings.getInt ("shadow_tint_percent",          60) / 100f;
        ShadowRenderer.contactShadow      = Core.settings.getInt ("contact_shadow_percent",       45) / 100f;
        ShadowRenderer.darkFadeStrength   = Core.settings.getInt ("dark_fade_percent",            80) / 100f;

        ShadowRenderer.updateUnitShadows();
    }
}
