package MindustryOptiFine.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import mindustry.Vars;

public class PostEffectQueue{
    private Seq<EffectPass> passes = new Seq<>();
    private FrameBuffer buffer1, buffer2;
    private boolean enabled = true;

    public void add(EffectPass pass){
        passes.add(pass);
    }

    public void remove(EffectPass pass){
        passes.remove(pass);
    }

    public void clear(){
        passes.clear();
    }

    public void resize(int width, int height){
        if(buffer1 != null) buffer1.dispose();
        if(buffer2 != null) buffer2.dispose();
        
        buffer1 = new FrameBuffer(width, height);
        buffer2 = new FrameBuffer(width, height);
    }

    public void begin(){
        if(!enabled || passes.isEmpty()) return;
        
        if(buffer1 == null || buffer1.getWidth() != Core.graphics.getWidth() || buffer1.getHeight() != Core.graphics.getHeight()){
            resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        }
        
        buffer1.begin(Color.clear);
    }

    public void end(){
        if(!enabled || passes.isEmpty()) return;
        
        buffer1.end();
        
        FrameBuffer current = buffer1;
        FrameBuffer next = buffer2;
        
        for(int i = 0; i < passes.size; i++){
            EffectPass pass = passes.get(i);
            
            next.begin(Color.clear);
            pass.shader.bind();
            pass.shader.apply();
            
            current.getTexture().bind(0);
            Draw.rect(new TextureRegion(current.getTexture()), 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
            
            next.end();
            
            FrameBuffer temp = current;
            current = next;
            next = temp;
        }
        
        Draw.blend(Blending.disabled);
        Draw.rect(new TextureRegion(current.getTexture()), 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        Draw.blend();
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public static class EffectPass{
        public Shader shader;
        public boolean active = true;

        public EffectPass(Shader shader){
            this.shader = shader;
        }

        public EffectPass active(boolean active){
            this.active = active;
            return this;
        }
    }
}