package MindustryOptiFine.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;

public class AnimatedIcon extends Image {
    private boolean hovered = false;
    private float hoverProgress = 0f;
    private float targetHoverProgress = 0f;
    private float speed = 1f;
    private Color tintColor = Color.white;
    
    public AnimatedIcon(TextureRegion region) {
        super(region);
        initialize();
    }
    
    public AnimatedIcon(Texture texture) {
        super(texture);
        initialize();
    }
    
    private void initialize() {
        update(() -> {
            targetHoverProgress = hovered ? 1f : 0f;
            hoverProgress += (targetHoverProgress - hoverProgress) * 0.15f;
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
        float centerX = getX(0) + width / 2f;
        float centerY = getY(0) + height / 2f;
        
        if(hoverProgress > 0.01f) {
            float time = Time.time * speed * 0.05f;
            float glowSize = 1.2f + hoverProgress * 0.3f + Mathf.sin(time) * 0.1f;
            
            for(int i = 0; i < 8; i++) {
                float angle = (time + i * Mathf.PI / 4f) % (Mathf.PI * 2);
                float len = 0.3f + Mathf.sin(time * 2f + i) * 0.1f;
                
                float x = Mathf.cos(angle) * width * len;
                float y = Mathf.sin(angle) * height * len;
                
                Draw.color(Pal.accent, hoverProgress * 0.5f);
                Fill.circle(centerX + x, centerY + y, 3f + hoverProgress * 2f);
            }
            
            Draw.color(Pal.accent, hoverProgress * 0.3f);
            Fill.circle(centerX, centerY, width * glowSize * 0.5f);
        }
        
        Draw.color(tintColor, hoverProgress > 0 ? 1f : 0.8f);
        super.draw();
        Draw.color();
    }
    
    public AnimatedIcon speed(float speed) {
        this.speed = speed;
        return this;
    }
    
    public AnimatedIcon color(Color color) {
        this.tintColor = color;
        return this;
    }
    
    public float getHoverProgress() {
        return hoverProgress;
    }
}