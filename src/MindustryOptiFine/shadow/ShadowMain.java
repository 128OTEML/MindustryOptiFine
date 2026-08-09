package MindustryOptiFine.shadow;

import arc.Core;
import arc.Events;
import mindustry.game.EventType;

public class ShadowMain {
    /** 保险机制计数器：建造/拆除事件可能丢失，每 SAFETY_INTERVAL 帧强制刷新一次 chunk 缓存 */
    private static final int SAFETY_INTERVAL = 20;
    private static int safetyFrames = 0;

    public static void initEvents() {
        Events.on(EventType.WorldLoadEvent.class, e -> {
            ShadowRenderer.ChunkCache.init();
            safetyFrames = 0;
        });
        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if (e.tile != null) {
                ShadowRenderer.ChunkCache.invalidateTile(e.tile.x, e.tile.y);
            }
        });

        Events.run(EventType.Trigger.draw, () -> ShadowRenderer.weatherMult = 1f);
        Events.run(EventType.Trigger.draw, () -> {
            if (ShadowRenderer.enabled && ++safetyFrames >= SAFETY_INTERVAL) {
                safetyFrames = 0;
                ShadowRenderer.ChunkCache.invalidateAll();
            }
        });
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
