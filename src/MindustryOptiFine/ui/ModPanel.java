package MindustryOptiFine.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.graphics.*;
import MindustryOptiFine.shaders.ModShaders;
import MindustryOptiFine.graphics.ShaderCache;

public class ModPanel extends Table {
    private boolean hovered = false;
    private float hoverProgress = 0f;
    private float targetHoverProgress = 0f;
    
    private float padding = 8f;
    private boolean useShaderBackground = true;
    
    public ModPanel() {
        super();
        initialize();
    }
    
    private void initialize() {
        update(() -> {
            targetHoverProgress = hovered ? 1f : 0f;
            hoverProgress += (targetHoverProgress - hoverProgress) * 0.1f;
            ModShaders.panel.hoverIntensity = hoverProgress;
        });
        
        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Element fromActor) {
                hovered = true;
            }
            
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element toActor) {
                hovered = false;
            }
        });
    }
    
    @Override
    public void draw() {
        float width = getWidth();
        float height = getHeight();
        float x = getX(0);
        float y = getY(0);
        
        if(useShaderBackground && ShaderCache.getNoiseTexture() != null){
            Draw.shader(ModShaders.panel);
            Draw.color(Color.white);
            Draw.rect(ShaderCache.getNoiseTexture(), x + width / 2, y + height / 2, width, height);
            Draw.shader();
        }else{
            Draw.color(Pal.accent);
            float alpha = 0.1f + hoverProgress * 0.1f;
            Fill.rect(x, y, width, height);
            Draw.color(Pal.accent.r, Pal.accent.g, Pal.accent.b, alpha);
            Fill.rect(x + 2f, y + 2f, width - 4f, height - 4f);
            Draw.color();
        }
        
        super.draw();
    }
    
    public ModPanel padding(float padding) {
        this.padding = padding;
        defaults().pad(padding);
        return this;
    }
    
    public ModPanel useShaderBackground(boolean use) {
        this.useShaderBackground = use;
        return this;
    }
    
    public float getHoverProgress() {
        return hoverProgress;
    }
}