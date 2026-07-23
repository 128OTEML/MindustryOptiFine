package MindustryOptiFine.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.graphics.g2d.*;
import arc.util.*;
import MindustryOptiFine.shaders.ModShaders;

public class ShaderCache{
    private static FrameBuffer noiseBuffer;
    private static int bufferSize = 256;
    private static float lastUpdateTime = 0f;
    private static float updateInterval = 0.166f;

    public static void init(){
        resize();
    }

    public static void resize(){
        if(noiseBuffer != null) noiseBuffer.dispose();
        noiseBuffer = new FrameBuffer(bufferSize, bufferSize);
    }

    public static void update(){
        float currentTime = Time.time;
        if(currentTime - lastUpdateTime >= updateInterval){
            lastUpdateTime = currentTime;
            renderNoise();
        }
    }

    private static void renderNoise(){
        noiseBuffer.begin(Color.clear);
        ModShaders.panel.bind();
        ModShaders.panel.apply();
        
        Draw.rect(Core.atlas.find("whiteui"), 0, 0, bufferSize, bufferSize);
        
        Gl.useProgram(0);
        noiseBuffer.end();
    }

    public static TextureRegion getNoiseTexture(){
        return new TextureRegion(noiseBuffer.getTexture());
    }

    public static void setBufferSize(int size){
        bufferSize = size;
        resize();
    }

    public static void setUpdateInterval(float interval){
        updateInterval = interval;
    }
}