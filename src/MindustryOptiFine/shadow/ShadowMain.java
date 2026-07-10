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
            }
        });

        Events.run(EventType.Trigger.draw, () -> ShadowRenderer.weatherMult = 1f);
        Events.run(EventType.Trigger.draw, ShadowRenderer::queue);
    }

    public static void loadSettings() {
        ShadowRenderer.graphicsQuality    = Core.settings.getInt ("graphics_quality",            2);
        ShadowRenderer.enabled            = Core.settings.getBool("shadows_enabled",     true);
        ShadowRenderer.dayNightCycle      = Core.settings.getBool("day_night_cycle",             true);
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

    public static void addSettings(mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable st) {
        st.checkPref("shadows_enabled", false, val -> {
            ShadowRenderer.enabled = val;
            ShadowRenderer.updateUnitShadows();
        });
        st.checkPref("day_night_cycle", true, val -> ShadowRenderer.dayNightCycle = val);
        st.checkPref("unit_shadows", true, val -> {
            ShadowRenderer.unitShadowsEnabled = val;
            ShadowRenderer.updateUnitShadows();
        });

        st.sliderPref("graphics_quality", 2, 0, 2, 1, s -> {
            ShadowRenderer.graphicsQuality = s;
            return s == 0 ? "Low" : s == 1 ? "Medium" : "High";
        });
        st.sliderPref("shadow_length", 10, 0, 30, 1, s -> {
            ShadowRenderer.SHADOW_LENGTH = s;
            return s + " tiles";
        });
        st.sliderPref("prop_shadow_scale", 100, 0, 200, 10, s -> {
            ShadowRenderer.propShadowScale = s / 100f;
            boolean oldVal = ShadowRenderer.oldShadowsEnabled;
            ShadowRenderer.oldShadowsEnabled = (s == 0);
            if (oldVal != ShadowRenderer.oldShadowsEnabled) {
                ShadowRenderer.ChunkCache.invalidateAll();
            }
            return s == 0 ? "Mindustry (Old)" : s + "%";
        });
        st.sliderPref("shadow_opacity_percent", 45, 0, 100, 1, s -> {
            ShadowRenderer.SHADOW_ALPHA = s / 100f;
            return s + "%";
        });
        st.sliderPref("blur_radius", 35, 10, 80, 5, s -> {
            ShadowRenderer.blurRadius = s / 10f;
            return (s / 10f) + " px";
        });
        st.sliderPref("shadow_tint_percent", 60, 0, 100, 5, s -> {
            ShadowRenderer.shadowTint = s / 100f;
            return s + "%";
        });
        st.sliderPref("contact_shadow_percent", 45, 0, 100, 5, s -> {
            ShadowRenderer.contactShadow = s / 100f;
            return s + "%";
        });
        st.sliderPref("dark_fade_percent", 80, 0, 100, 5, s -> {
            ShadowRenderer.darkFadeStrength = s / 100f;
            return s + "%";
        });
    }
}