package MindustryOptiFine.graphics.metaballs;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import MindustryOptiFine.shaders.ModShaders;

public class MetaballManager{
    private static Seq<MetaballInstance> metaballs = new Seq<>();
    private static FrameBuffer buffer;
    private static int maxMetaballs = 32;

    public static void init(){
        Events.run(Trigger.update, MetaballManager::update);
        Events.run(Trigger.postDraw, MetaballManager::draw);
    }

    public static void add(MetaballInstance metaball){
        if(metaballs.size >= maxMetaballs){
            metaballs.remove(0);
        }
        metaballs.add(metaball);
    }

    public static void remove(MetaballInstance metaball){
        metaballs.remove(metaball);
    }

    public static void clear(){
        metaballs.clear();
    }

    private static void update(){
        if(Vars.state.isPaused()) return;
        
        for(int i = metaballs.size - 1; i >= 0; i--){
            MetaballInstance m = metaballs.get(i);
            m.update();
            
            if(m.isDead()){
                metaballs.remove(i);
            }
        }
    }

    private static void draw(){
        if(metaballs.isEmpty()) return;
        
        if(buffer == null || buffer.getWidth() != Core.graphics.getWidth() || buffer.getHeight() != Core.graphics.getHeight()){
            if(buffer != null) buffer.dispose();
            buffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
        }
        
        buffer.begin(Color.clear);
        
        for(MetaballInstance m : metaballs){
            Draw.color(m.color.r, m.color.g, m.color.b, m.intensity);
            Fill.circle(m.position.x, m.position.y, m.radius);
        }
        
        Draw.color();
        buffer.end();
        
        buffer.getTexture().bind(0);
        
        ModShaders.metaball.bind();
        ModShaders.metaball.apply();
        
        Draw.rect(new TextureRegion(buffer.getTexture()), 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        
        Gl.useProgram(0);
    }

    public static void setMaxMetaballs(int max){
        maxMetaballs = max;
    }

    public static int getMetaballCount(){
        return metaballs.size;
    }

    public static Seq<MetaballInstance> getMetaballs(){
        return metaballs;
    }
}