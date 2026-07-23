package MindustryOptiFine.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.*;

public class InstancedSpriteBatch {
    public static boolean enabled = true;
    
    private static final int MAX_INSTANCES = 8191;
    
    private Texture currentTexture;
    private int spriteCount;
    
    public InstancedSpriteBatch() {
        if(!enabled) return;
    }
    
    public void begin() {
        if(!enabled) return;
        currentTexture = null;
        spriteCount = 0;
        Draw.flush();
    }
    
    public void end() {
        if(!enabled) return;
        flush();
        Draw.flush();
    }
    
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        draw(region, x, y, width / 2f, height / 2f, width, height, 0f);
    }
    
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation) {
        if(!enabled) return;
        
        Texture tex = region.texture;
        if(currentTexture != tex){
            flush();
            currentTexture = tex;
            tex.bind();
        }
        
        float u = region.u;
        float v = region.v;
        float u2 = region.u2;
        float v2 = region.v2;
        
        float worldWidth = width;
        float worldHeight = height;
        
        float cos = Mathf.cosDeg(rotation);
        float sin = Mathf.sinDeg(rotation);
        
        float fx = -originX * cos - originY * sin;
        float fy = originX * sin - originY * cos;
        
        float x1 = x + fx;
        float y1 = y + fy;
        float x2 = x1 + worldWidth * cos;
        float y2 = y1 + worldWidth * sin;
        float x3 = x1 - worldHeight * sin;
        float y3 = y1 + worldHeight * cos;
        float x4 = x2 - worldHeight * sin;
        float y4 = y2 + worldHeight * cos;
        
        float color = Draw.getColorPacked();
        float mixColor = Draw.getMixColorPacked();
        
        float[] vertices = new float[24];
        
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mixColor;
        
        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = color;
        vertices[9] = u2;
        vertices[10] = v;
        vertices[11] = mixColor;
        
        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = color;
        vertices[15] = u;
        vertices[16] = v2;
        vertices[17] = mixColor;
        
        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = color;
        vertices[21] = u2;
        vertices[22] = v2;
        vertices[23] = mixColor;
        
        Draw.vert(tex, vertices, 0, 24);
        
        spriteCount++;
        
        if(spriteCount >= MAX_INSTANCES){
            flush();
        }
    }
    
    private void flush() {
        if(spriteCount == 0) return;
        Draw.flush();
        spriteCount = 0;
    }
    
    public void dispose() {
    }
}