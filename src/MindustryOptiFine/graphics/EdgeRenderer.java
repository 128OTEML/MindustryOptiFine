package MindustryOptiFine.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.FrameBuffer;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

import static arc.Core.atlas;
import static arc.Core.camera;
import static mindustry.Vars.*;

public class EdgeRenderer{
    public static boolean enabled = true;
    
    private static TextureRegion edgeMaskRegion;
    private static Texture edgeMaskTexture;
    private static boolean hasEdgeMask = false;
    private static boolean inited = false;
    
    private static Fi edgeDir;
    private static ObjectMap<String, TextureRegion> edgeRegions = new ObjectMap<>();
    
    public static void init(){
        if(inited) return;
        
        edgeDir = dataDirectory.child("mods").child("MindustryOptiFine").child("edges");
        edgeDir.mkdirs();
        
        Fi maskFile = Vars.tree.get("graphics/-edge.png");
        if(maskFile.exists()){
            try{
                edgeMaskTexture = new Texture(maskFile);
                edgeMaskTexture.setFilter(Texture.TextureFilter.linear);
                edgeMaskRegion = new TextureRegion(edgeMaskTexture);
                hasEdgeMask = true;
                Log.info("Edge mask texture loaded");
            }catch(Exception e){
                hasEdgeMask = false;
                Log.err("Failed to load edge mask: " + e.getMessage());
            }
        }
        
        if(hasEdge()){
            generateAllEdges();
            loadAllEdges();
        }
        
        inited = true;
    }
    
    public static boolean hasEdge(){
        return enabled && hasEdgeMask && edgeMaskRegion != null;
    }
    
    private static void generateAllEdges(){
        Log.info("Generating edge textures...");
        
        int generated = 0;
        int skipped = 0;
        
        for(Block block : content.blocks()){
            if(block instanceof Floor || block instanceof StaticWall){
                Fi edgeFile = getEdgeFile(block);
                
                if(edgeFile.exists()){
                    skipped++;
                    continue;
                }
                
                generateEdgeTexture(block);
                generated++;
            }
        }
        
        Log.info("Edge textures generated: " + generated + ", skipped: " + skipped);
    }
    
    private static Fi getEdgeFile(Block block){
        return edgeDir.child(block.name + "-edge.png");
    }
    
    private static void generateEdgeTexture(Block block){
        Fi edgeFile = getEdgeFile(block);
        
        int size = tilesize * 8;
        int w = size;
        int h = size;
        
        FrameBuffer buffer = new FrameBuffer(w, h);
        buffer.begin();
        
        camera.width = w;
        camera.height = h;
        camera.position.x = w / 2f;
        camera.position.y = h / 2f;
        camera.update();
        Draw.proj(camera);
        Draw.reset();
        
        Draw.color(Color.clear);
        Fill.rect(w / 2f, h / 2f, w, h);
        Draw.reset();
        
        Draw.alpha(1f);
        Draw.rect(edgeMaskRegion, w / 2f, h / 2f, w, h);
        Draw.flush();
        
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        buffer.end();
        buffer.dispose();
        
        Pixmap pixmap = new Pixmap(w, h);
        Buffers.copy(pixels, 0, pixmap.pixels, pixels.length);
        
        PixmapIO.writePng(edgeFile, pixmap);
        pixmap.dispose();
    }
    
    private static void loadAllEdges(){
        edgeRegions.clear();
        
        for(Block block : content.blocks()){
            if(block instanceof Floor || block instanceof StaticWall){
                Fi edgeFile = getEdgeFile(block);
                
                if(edgeFile.exists()){
                    try{
                        Pixmap pixmap = PixmapIO.readPNG(edgeFile);
                        Texture texture = new Texture(pixmap);
                        TextureRegion region = new TextureRegion(texture);
                        
                        String atlasName = block.name + "-edge";
                        atlas.addRegion(atlasName, region);
                        edgeRegions.put(block.name, region);
                    }catch(Exception e){
                        Log.err("Failed to load edge texture for " + block.name + ": " + e.getMessage());
                    }
                }
            }
        }
        
        Log.info("Loaded " + edgeRegions.size + " edge textures");
    }
    
    public static TextureRegion getEdgeRegion(Block block){
        if(!hasEdge()) return null;
        return edgeRegions.get(block.name);
    }
    
    public static void draw(Tile tile){
        if(!hasEdge()) return;
        
        Block current = tile.block();
        
        if(!(current instanceof Floor) && !(current instanceof StaticWall)) return;
        
        TextureRegion edgeRegion = getEdgeRegion(current);
        if(edgeRegion == null) return;
        
        int bits = 0;
        
        for(int i = 0; i < 4; i++){
            Tile otherTile = tile.nearby(Geometry.d4[i]);
            if(otherTile != null){
                Block other = otherTile.block();
                
                if(isDifferentEnvironment(current, other)){
                    bits |= (1 << i);
                }
            }
        }
        
        if(bits == 0) return;
        
        Draw.alpha(1f);
        
        for(int i = 0; i < 4; i++){
            if((bits & (1 << i)) != 0){
                float rotation = i * 90f;
                Point2 d = Geometry.d4[i];
                
                float offsetX = d.x * tilesize * 0.2f;
                float offsetY = d.y * tilesize * 0.2f;
                
                Draw.rect(edgeRegion, tile.worldx() + offsetX, tile.worldy() + offsetY, 
                          tilesize, tilesize, rotation);
            }
        }
        
        Draw.alpha(1f);
    }
    
    private static boolean isDifferentEnvironment(Block current, Block other){
        if(other == Blocks.air) return true;
        
        if(current instanceof Floor && other instanceof Floor){
            return current != other;
        }
        
        if(current instanceof StaticWall && other instanceof StaticWall){
            return current != other;
        }
        
        if((current instanceof Floor && other instanceof StaticWall) || 
           (current instanceof StaticWall && other instanceof Floor)){
            return true;
        }
        
        return false;
    }
    
    public static void dispose(){
        if(edgeMaskTexture != null){
            edgeMaskTexture.dispose();
            edgeMaskTexture = null;
            edgeMaskRegion = null;
            hasEdgeMask = false;
        }
        
        for(TextureRegion region : edgeRegions.values()){
            region.texture.dispose();
        }
        edgeRegions.clear();
        
        inited = false;
    }
}
