package MindustryOptiFine.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.ui.*;

public class LockableToggle extends Table {
    private final Label label;
    private final Image lockIcon;
    private final CheckBox checkbox;
    
    private boolean enabled = true;
    private boolean locked = false;
    private boolean value = false;
    
    private Runnable onChanged;
    private Runnable onEnabledChanged;
    
    public LockableToggle(String text) {
        label = new Label(text);
        lockIcon = new Image();
        checkbox = new CheckBox("", Styles.defaultCheck);
        
        checkbox.changed(() -> {
            if(enabled && !locked) {
                value = checkbox.isChecked();
                if(onChanged != null) onChanged.run();
            } else {
                checkbox.setChecked(value);
            }
        });
        
        add(lockIcon).size(24f).padRight(8f);
        add(checkbox).padRight(8f);
        add(label).growX();
        
        updateVisuals();
    }
    
    private void updateVisuals() {
        Color color = enabled ? Color.white : Color.gray;
        
        label.setColor(color);
        checkbox.setColor(color);
        checkbox.setDisabled(!enabled || locked);
        
        if(locked) {
            lockIcon.setDrawable(Core.atlas.find("whiteui"));
            lockIcon.setColor(Color.yellow);
        } else {
            lockIcon.setColor(Color.clear);
        }
        
        if(onEnabledChanged != null) {
            onEnabledChanged.run();
        }
    }
    
    public LockableToggle enabled(boolean enabled) {
        this.enabled = enabled;
        updateVisuals();
        return this;
    }
    
    public LockableToggle locked(boolean locked) {
        this.locked = locked;
        updateVisuals();
        return this;
    }
    
    public LockableToggle value(boolean value) {
        this.value = value;
        checkbox.setChecked(value);
        return this;
    }
    
    public LockableToggle onChange(Runnable runnable) {
        this.onChanged = runnable;
        return this;
    }
    
    public LockableToggle onEnabledChange(Runnable runnable) {
        this.onEnabledChanged = runnable;
        return this;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public boolean getValue() {
        return value;
    }
    
    public CheckBox getCheckbox() {
        return checkbox;
    }
    
    public Label getLabel() {
        return label;
    }
}