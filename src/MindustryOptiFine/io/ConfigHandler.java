package MindustryOptiFine.io;

import MindustryOptiFine.ui.UIUtils;
import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.mod.*;

import static arc.Core.bundle;
import static arc.Core.settings;
import static mindustry.Vars.ui;

public class ConfigHandler extends SettingHandler{
    private Mod mod;

    public ConfigHandler(Mod mod){
        super(mod.getClass().getSimpleName());
        this.mod = mod;
    }

    public static ConfigHandler request(Mod mod){
        return new ConfigHandler(mod);
    }

    public void newSettingsCategory(String name, String description, Cons<Table> builder){
        ui.settings.addCategory(bundle.get("settings." + name + ".name", name), bundle.getOrNull("settings." + name + ".description"), st -> {
            builder.get(st);
        });
    }

    public void buildTip(Table st){
        st.label(() -> "[lightgray]Hover over settings for tooltips[]").padTop(8f).row();
    }

    public void checkb(String name, boolean def, String title, String tip, Table st){
        Cell<CheckBox> cell = st.check(bundle.get("settings." + prefix(name), title), def, b -> settings.put(prefix(name), b));
        UIUtils.tooltip(cell.get(), bundle.get("settings." + prefix(name) + ".tip", tip));
    }

    public void slideri(String name, int def, String title, String tip, Table st, int min, int max, int step){
        Cell<Slider> sliderCell = st.slider(min, max, step, i -> settings.put(prefix(name), (int)i));
        UIUtils.tooltip(sliderCell.get(), bundle.get("settings." + prefix(name) + ".tip", tip));
        st.label(() -> bundle.get("settings." + prefix(name), title)).padLeft(5f);
    }

    // Alias methods for convenience
    public boolean getb(String name, boolean def){
        return getBool(name, def);
    }

    public int geti(String name, int def){
        return getInt(name, def);
    }
}