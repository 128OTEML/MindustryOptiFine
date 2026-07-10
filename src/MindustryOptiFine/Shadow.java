package MindustryOptiFine;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.draw.*;
import MindustryOptiFine.utils.*;
import MindustryOptiFine.graphics.ConnectWallHandler.*;

import java.lang.reflect.*;

import static MindustryOptiFine.MindustryOptiFine.*;
import static MindustryOptiFine.graphics.ConnectWallHandler.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class Shadow{
    public static boolean depthTex = false;
    public static boolean shadow = true;
    public static boolean debug = false;
    public static int precision = 8;
    public static boolean zoomPrec = false;
    public static int lightLowPass = 8;
    public static int maxLights = 100;

    public static Field fCircles = RefUtils.getField(LightRenderer.class, "circles"), fSize = RefUtils.getField(LightRenderer.class, "circleIndex"), fCircleX, fCircleY, fCircleR, fCircleC;
    public static int size = 0;
    public static Seq<float[]> floatlights = new Seq<>();

    public static float layer = Layer.block + 3f;

    public static IndexGetterDrawc indexGetter;

    public static TextureRegion[][] normRegions;
    /** Turret base depth textures, indexed by block ID. Null for non-turret blocks or if base not found. */
    public static TextureRegion[] turretBaseNorms;
    
    /** Dynamic prop depth buffer for trees and environment decorations */
    public static FrameBuffer propDepthBuffer;
    public static int frameCount = 0;
    public static final int PROP_UPDATE_INTERVAL = 20;

    public static void init(){
        SSShaders.load();
        indexGetter = new IndexGetterDrawc();
        normRegions = new TextureRegion[content.blocks().size][];

        //load or generate normMaps
        for(int i = 0; i < normRegions.length; i++){
            var block = content.block(i);
            if((block instanceof mindustry.world.blocks.environment.Floor && ((mindustry.world.blocks.environment.Floor)block).isLiquid)) continue;
            int variant = block.variantRegions != null ? block.variantRegions.length : 0;
            normRegions[i] = new TextureRegion[variant + 1];

            for(int v = -1; v < variant; v++){

                Fi file = dataDirectory.child("mods").child("ShadowShader").child(block.name + (v == -1 ? "" : v + 1) + ".png");
                Fi genFile = dataDirectory.child("mods").child("ShadowShader").child(block.name + (v == -1 ? "" : v + 1) + "-auto-v3.png");

                Pixmap norm;

                int w, h;
                if(PropShadowHelper.isProp(block) || block instanceof mindustry.world.blocks.environment.StaticWall){
                    TextureRegion region = v == -1 ? (block.region == null ? block.fullIcon : block.region) : block.variantRegions[v];
                    if(region != null && region.found()){
                        float worldW = region.width * Draw.scl;
                        float worldH = region.height * Draw.scl;
                        w = (int)(worldW * 8);
                        h = (int)(worldH * 8);
                    }else{
                        w = block.size * tilesize * 8;
                        h = block.size * tilesize * 8;
                    }
                }else{
                    w = block.size * tilesize * 8;
                    h = block.size * tilesize * 8;
                }

                try{
                    norm = PixmapIO.readPNG(file);
                }catch(Exception e){
                    try{
                        norm = PixmapIO.readPNG(genFile);
                    }catch(Exception ignore){
                        Log.errTag("ShadowShader", "Error reading depth from file: " + block.localizedName + " & auto gen not found. Using generator......");
                        FrameBuffer buffer = new FrameBuffer(w, h);
                        buffer.begin();
                        camera.width = w;
                        camera.height = h;
                        camera.position.x = 0;
                        camera.position.y = 0;
                        camera.update();
                        Draw.proj(camera);
                        Draw.reset();
                        Draw.color(Color.black);
                        Fill.rect(0, 0, w, h);
                        Draw.reset();
                        Draw.rect(v == -1 ? (block.region == null ? block.fullIcon : block.region): block.variantRegions[v], 0, 0, w, h);
                        Draw.flush();
                        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
                        buffer.end();
                        buffer.dispose();

                        for(int j = 0; j < lines.length; j += 4){
                            lines[j + 3] = (byte)255;//alpha
                        }
                        Pixmap shot = new Pixmap(w, h);
                        norm = new Pixmap(w, h);
                        Buffers.copy(lines, 0, shot.pixels, lines.length);
                        Buffers.copy(lines, 0, norm.pixels, lines.length);
                        //normal mapping - 使用高度缩放因子
                        float heightScale;
                        if(PropShadowHelper.isProp(block) || block instanceof mindustry.world.blocks.environment.StaticWall){
                            // 道具和墙体使用基于类型和区域尺寸的高度缩放
                            heightScale = PropShadowHelper.getPropHeightScale(block, v == -1 ? (block.region == null ? block.fullIcon : block.region) : block.variantRegions[v]);
                        }else{
                            // 普通方块使用block.size/4.0作为高度缩放因子
                            heightScale = Mathf.clamp(block.size / 4.0f, 0.25f, 1.0f);
                        }
                        Generators.check(shot, norm, heightScale);

                        if(block instanceof Floor){
                            for(int x = 0; x < norm.width; x++){
                                for(int y = 0; y < norm.height; y++){
                                    norm.set(x, y, Tmp.c1.set(0f, 0f, Tmp.c2.set(norm.get(x, y)).b * 0.2f, Tmp.c2.set(norm.get(x, y)).a));
                                }
                            }
                        }

                        PixmapIO.writePng(genFile, norm);
                    }
                }

                var texture = new Texture(norm);
                var normRegion = new TextureRegion(texture, w, h);
                atlas.addRegion(block.name + "-normmap", normRegion);
                normRegions[i][v + 1] = normRegion;
            }

            // --- 炮台底座深度贴图独立生成 ---
            if(block instanceof Turret turret && turret.drawer instanceof DrawTurret dt){
                if(turretBaseNorms == null){
                    turretBaseNorms = new TextureRegion[content.blocks().size];
                }

                TextureRegion baseRegion = dt.base;
                if(baseRegion != null && baseRegion.found()){
                    int bw = block.size * tilesize * 8, bh = block.size * tilesize * 8;
                    Fi baseFile = dataDirectory.child("mods").child("ShadowShader").child(block.name + "-base.png");
                    Fi baseGenFile = dataDirectory.child("mods").child("ShadowShader").child(block.name + "-base-auto-v3.png");

                    Pixmap baseNorm;
                    try{
                        baseNorm = PixmapIO.readPNG(baseFile);
                    }catch(Exception e){
                        try{
                            baseNorm = PixmapIO.readPNG(baseGenFile);
                        }catch(Exception ignore){
                            Log.errTag("ShadowShader", "Error reading base depth from file: " + block.localizedName + " & auto gen not found. Using generator......");
                            FrameBuffer buffer = new FrameBuffer(bw, bh);
                            buffer.begin();
                            camera.width = bw;
                            camera.height = bh;
                            camera.position.x = 0;
                            camera.position.y = 0;
                            camera.update();
                            Draw.proj(camera);
                            Draw.reset();

                            // 透明背景，保留 alpha
                            Draw.color(Color.clear);
                            Fill.rect(0, 0, bw, bh);
                            Draw.reset();

                            Draw.rect(baseRegion, 0, 0, bw, bh);
                            Draw.flush();
                            byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, bw, bh, true);
                            buffer.end();
                            buffer.dispose();

                            Pixmap shot = new Pixmap(bw, bh);
                            baseNorm = new Pixmap(bw, bh);
                            Buffers.copy(lines, 0, shot.pixels, lines.length);
                            Buffers.copy(lines, 0, baseNorm.pixels, lines.length);
                            //normal mapping - 使用block.size/4.0作为高度缩放因子
                            float heightScale = Mathf.clamp(block.size / 4.0f, 0.25f, 1.0f);
                            Generators.check(shot, baseNorm, heightScale);

                            // 清除透明区域，确保底座深度贴图严格限定在底座轮廓内
                            for(int px = 0; px < baseNorm.width; px++){
                                for(int py = 0; py < baseNorm.height; py++){
                                    int shotPixel = shot.get(px, py);
                                    int shotAlpha = (shotPixel >>> 24) & 0xFF;
                                    if(shotAlpha < 10){
                                        baseNorm.set(px, py, Color.clear);
                                    }
                                }
                            }

                            PixmapIO.writePng(baseGenFile, baseNorm);
                        }
                    }

                    var texture = new Texture(baseNorm);
                    var normRegion = new TextureRegion(texture, bw, bh);
                    atlas.addRegion(block.name + "-normmap-base", normRegion);
                    turretBaseNorms[i] = normRegion;
                }
            }
        }

        propDepthBuffer = new FrameBuffer();
    }


    public static void updatePropDepthMap(){
        if(propDepthBuffer == null) return;
        
        frameCount++;
        if(frameCount % PROP_UPDATE_INTERVAL != 0) return;
        
        propDepthBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        propDepthBuffer.begin(Color.clear);
        
        Draw.proj(camera);
        Draw.color();
        Draw.mixcol();
        
        var r = camera.bounds(Tmp.r1);
        int minX = Math.max(0, Mathf.floor(r.x / tilesize));
        int maxX = Math.min(world.width(), Mathf.ceil((r.x + r.width) / tilesize));
        int minY = Math.max(0, Mathf.floor(r.y / tilesize));
        int maxY = Math.min(world.height(), Mathf.ceil((r.y + r.height) / tilesize));
        
        if(minX >= maxX || minY >= maxY){
            propDepthBuffer.end();
            return;
        }
        
        float bs = tilesize;
        
        for(int x = minX; x < maxX; x++){
            for(int y = minY; y < maxY; y++){
                var tile = world.tile(x, y);
                if(tile == null || tile.build != null) continue;
                
                Block todraw = tile.block();
                if(todraw == Blocks.air || !PropShadowHelper.isProp(todraw)) continue;
                
                float xw = tile.worldx();
                float yw = tile.worldy();
                
                TextureRegion region = todraw.region;
                int variantIndex = 0;
                if(todraw.variantRegions != null && todraw.variantRegions.length > 0){
                    variantIndex = Mathf.randomSeed(tile.pos(), 0, todraw.variantRegions.length - 1);
                    if(variantIndex >= 0 && variantIndex < todraw.variantRegions.length && todraw.variantRegions[variantIndex] != null && todraw.variantRegions[variantIndex].found()){
                        region = todraw.variantRegions[variantIndex];
                    }
                }
                
                if(region != null && region.found()){
                    float propW = region.width * Draw.scl;
                    float propH = region.height * Draw.scl;
                    
                    float heightScale = PropShadowHelper.getPropHeightScale(todraw, region);
                    float depthValue = Math.min(1.0f, Math.max(0.2f, heightScale * 0.6f * 3f));
                    
                    Draw.mixcol(new Color(0f, 0f, depthValue, 1f), 1f);
                    Draw.rect(region, xw, yw, propW, propH, 0f);
                }
            }
        }
        
        propDepthBuffer.end();
        Draw.color();
    }


    public static void getIndex(){
        size = RefUtils.getValue(fSize, renderer.lights);
    }

    public static void deepReflectObject(float[] tmpc, Object circle){
        if(fCircleX == null) fCircleX = RefUtils.getField(circle.getClass(), "x");
        if(fCircleY == null) fCircleY = RefUtils.getField(circle.getClass(), "y");
        if(fCircleR == null) fCircleR = RefUtils.getField(circle.getClass(), "radius");
        if(fCircleC == null) fCircleC = RefUtils.getField(circle.getClass(), "color");

        tmpc[0] = fCircleX == null ? -1f : RefUtils.getValue(fCircleX, circle);
        tmpc[1] = fCircleY == null ? -1f : RefUtils.getValue(fCircleY, circle);
        var tile = world.tileWorld(tmpc[0], tmpc[1]);
        tmpc[2] = tile == null ? 0f : tile.build != null && Mathf.dst(tile.build.x, tile.build.y, tmpc[0], tmpc[1]) < 0.1f ? tile.block().size * tilesize : 0f;
        tmpc[3] = fCircleR == null ? -1f : RefUtils.getValue(fCircleR, circle);
        tmpc[3] *= fCircleC == null ? 1f : Tmp.c1.abgr8888(RefUtils.getValue(fCircleC, circle)).a;
    }

    public static float getLayer(){
        return depthTex?layer:layer-4f;
    }

    public static void draw(Seq<Tile> tiles){
        if(!shadow && !debug) return;

        for(Tile tile : tiles){
            float bs = tile.block().size * tilesize;
            if(state.rules.fog && tile.build != null && !tile.build.wasVisible) continue;
            Draw.z(getLayer());
            Draw.color();
            Draw.mixcol();

            float x = tile.build == null ? tile.worldx() : tile.build.x;
            float y = tile.build == null ? tile.worldy() : tile.build.y;

            if(tile.build instanceof BaseTurret.BaseTurretBuild){
                // ==================== 炮台：仅绘制底座层 ====================
                // 炮管阴影/深度贴图完全由原版 DrawTurret 处理，Shadow 系统不再绘制任何炮管内容

                TextureRegion baseRegion = null;
                if(tile.block() instanceof Turret turret && turret.drawer instanceof DrawTurret dt){
                    baseRegion = dt.base;
                }

                float baseRot = 0f;
                if(!depthTex){
                    Draw.mixcol(Color.white, 1f);
                    // 白模模式下使用纯底座贴图，避免绘制完整炮塔轮廓
                    if(baseRegion != null && baseRegion.found()){
                        Draw.rect(baseRegion, x, y, bs, bs, baseRot);
                    }else{
                        Draw.rect(tile.block().fullIcon, x, y, bs, bs, baseRot);
                    }
                }else{
                    // 法线模式下使用底座深度贴图（透明区域已清除，不越界）
                    if(turretBaseNorms != null && turretBaseNorms[tile.block().id] != null){
                        Draw.rect(turretBaseNorms[tile.block().id], x, y, bs, bs, baseRot);
                    }else{
                        Draw.rect(normRegions[tile.block().id][0], x, y, bs, bs, baseRot);
                    }
                }
                // 注意：此处已删除所有炮管层绘制代码

            }else{
                // ==================== 普通建筑（原逻辑） ====================
                float rot = 0f;
                if(tile.build != null){
                    rot = tile.build.drawrot();
                }

                if(!depthTex){
                    Draw.mixcol(Color.white, 1f);
                    Draw.rect(tile.block().fullIcon, x, y, bs, bs, rot);
                }else{
                    if(tile.build != null && tile.build.block instanceof mindustry.world.blocks.defense.Wall && enabled){
                        continue;
                    }
                    Draw.rect(normRegions[tile.block().id][0], x, y, bs, bs, rot);
                }
            }
        }
        
        if(depthTex){
            for(Tile tile : tiles){
                if(tile.build != null && tile.build.block instanceof mindustry.world.blocks.defense.Wall){
                    drawConnectWallDepth(tile.build);
                }
            }
        }
    }

    public static void drawMap(){
        if(!shadow && !debug) return;
        
        Draw.z(getLayer());
        Draw.color();
        
        var r = camera.bounds(Tmp.r1);
        int minX = Math.max(0, Mathf.floor(r.x / tilesize));
        int maxX = Math.min(world.width(), Mathf.ceil((r.x + r.width) / tilesize));
        int minY = Math.max(0, Mathf.floor(r.y / tilesize));
        int maxY = Math.min(world.height(), Mathf.ceil((r.y + r.height) / tilesize));
        
        if(minX >= maxX || minY >= maxY) return;
        
        float bs = tilesize;
        
        for(int x = minX; x < maxX; x++){
            for(int y = minY; y < maxY; y++){
                var tile = world.tile(x, y);
                if(tile == null || tile.build != null) continue;
                
                Block todraw = tile.block();
                if(todraw == Blocks.air || (todraw instanceof mindustry.world.blocks.environment.Floor && ((mindustry.world.blocks.environment.Floor)todraw).isLiquid)) continue;
                
                Draw.mixcol();
                
                boolean isProp = PropShadowHelper.isProp(todraw);
                
                if(depthTex){
                    Mathf.rand.setSeed(tile.pos());
                    if(isProp){
                        continue;
                    }else if(todraw.variantRegions == null){
                        if(todraw instanceof mindustry.world.blocks.environment.StaticWall){
                            TextureRegion region = todraw.region;
                            if(region != null && region.found()){
                                Draw.rect(normRegions[todraw.id][0], tile.worldx(), tile.worldy(), region.width * Draw.scl, region.height * Draw.scl, 0f);
                            }else{
                                Draw.rect(normRegions[todraw.id][0], tile.worldx(), tile.worldy(), bs, bs, 0f);
                            }
                        }else{
                            Draw.rect(normRegions[todraw.id][0], tile.worldx(), tile.worldy(), bs, bs, 0f);
                        }
                    }else{
                        int idx = 1 + Mathf.randomSeed(tile.pos(), 0, Math.max(0, todraw.variantRegions.length - 1));
                        if(todraw instanceof mindustry.world.blocks.environment.StaticWall){
                            TextureRegion region = todraw.variantRegions[idx - 1];
                            if(region != null && region.found()){
                                Draw.rect(normRegions[todraw.id][idx], tile.worldx(), tile.worldy(), region.width * Draw.scl, region.height * Draw.scl, 0f);
                            }else{
                                Draw.rect(normRegions[todraw.id][idx], tile.worldx(), tile.worldy(), bs, bs, 0f);
                            }
                        }else{
                            Draw.rect(normRegions[todraw.id][idx], tile.worldx(), tile.worldy(), bs, bs, 0f);
                        }
                    }
                }else if(todraw.cacheLayer == CacheLayer.walls || isProp || todraw instanceof mindustry.world.blocks.environment.StaticWall){
                    Draw.mixcol(Color.white, 1f);
                    
                    if(isProp){
                        TextureRegion region = todraw.region;
                        if(todraw.variantRegions != null && todraw.variantRegions.length > 0){
                            int index = Mathf.randomSeed(tile.pos(), 0, todraw.variantRegions.length - 1);
                            if(index >= 0 && index < todraw.variantRegions.length && todraw.variantRegions[index] != null && todraw.variantRegions[index].found()){
                                region = todraw.variantRegions[index];
                            }
                        }
                        if(region != null && region.found()){
                            float propW = region.width * Draw.scl;
                            float propH = region.height * Draw.scl;
                            Draw.rect(region, tile.worldx(), tile.worldy(), propW, propH, 0f);
                        }else{
                            Draw.rect(todraw.fullIcon, tile.worldx(), tile.worldy(), bs, bs, 0f);
                        }
                    }else{
                        if(todraw instanceof mindustry.world.blocks.environment.StaticWall){
                            TextureRegion region = todraw.region;
                            if(todraw.variantRegions != null && todraw.variantRegions.length > 0){
                                int index = Mathf.randomSeed(tile.pos(), 0, Math.max(0, todraw.variantRegions.length - 1));
                                if(index >= 0 && index < todraw.variantRegions.length && todraw.variantRegions[index] != null && todraw.variantRegions[index].found()){
                                    region = todraw.variantRegions[index];
                                }
                            }
                            if(region != null && region.found()){
                                Draw.rect(region, tile.worldx(), tile.worldy(), region.width * Draw.scl, region.height * Draw.scl, 0f);
                            }else{
                                Draw.rect(todraw.fullIcon, tile.worldx(), tile.worldy(), bs, bs, 0f);
                            }
                        }else{
                            Draw.rect(todraw.fullIcon, tile.worldx(), tile.worldy(), bs, bs, 0f);
                        }
                    }
                }
            }
        }
        
        if(depthTex && propDepthBuffer != null){
            Draw.rect(String.valueOf(propDepthBuffer.getTexture()), Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static void applyShader(){
        if(!shadow || debug || SSShaders.shadow == null) return;
        Draw.drawRange(getLayer(), 0.1f, () -> renderer.effectBuffer.begin(Color.clear), () -> {
            renderer.effectBuffer.end();
            renderer.effectBuffer.blit(SSShaders.shadow);
        });
    }

    public static void lightsUniformData(FloatSeq data){
        data.clear();
        if(size == 0) return;
        Seq<Object> seq = RefUtils.getValue(fCircles, renderer.lights);
        if(seq == null) return;

        for(int i = 0; i < size; i++){
            if(i >= floatlights.size){
                floatlights.add(new float[4]);
            }
            deepReflectObject(floatlights.get(i), seq.get(i));
        }

        floatlights.sort(fs -> -fs[3]);

        if(floatlights.isEmpty()) return;
        float minR = lightLowPass;
        float maxLight = maxLights;
        for(int i = 0; i < Math.min(Math.min(floatlights.size, 400), maxLight); i++){
            if(floatlights.get(i)[3] < minR) break;
            pack(floatlights.get(i));
            data.addAll(Tmp.v3.x, Tmp.v3.y);
        }
    }

    public static void pack(float[] values){
        if(values[0] < 0 || values[1] < 0 || values[2] < 0 || values[3] < 0 || values[3] > 100000f){
            Tmp.v3.set(-10000f, 0f);
            return;
        }
        Tmp.v3.set(Mathf.floor((values[0] + 100f) * 5)
                        + Mathf.floor(values[2]) * 50000f,
                Mathf.floor((values[1] + 100f) * 5)
                        + Mathf.floor(values[3]) * 50000f);
    }

    public static class IndexGetterDrawc implements Drawc{
        public transient boolean added = false;

        @Override
        public float clipSize() {
            return 100000000f;
        }

        @Override
        public void draw() {
            getIndex();
        }

        @Override
        public Floor floorOn() {
            return null;
        }

        @Override
        public Building buildOn() {
            return null;
        }

        @Override
        public boolean onSolid() {
            return false;
        }

        @Override
        public float getX() {
            return 0;
        }

        @Override
        public float getY() {
            return 0;
        }

        @Override
        public float x() {
            return 0;
        }

        @Override
        public float y() {
            return 0;
        }

        @Override
        public int tileX() {
            return 0;
        }

        @Override
        public int tileY() {
            return 0;
        }

        @Override
        public Block blockOn() {
            return null;
        }

        @Override
        public Tile tileOn() {
            return null;
        }

        @Override
        public void set(Position position) {

        }

        @Override
        public void set(float v, float v1) {

        }

        @Override
        public void trns(Position position) {

        }

        @Override
        public void trns(float v, float v1) {

        }

        @Override
        public void x(float v) {

        }

        @Override
        public void y(float v) {

        }

        @Override
        public <T extends Entityc> T self() {
            return null;
        }

        @Override
        public <T> T as() {
            return null;
        }

        @Override
        public boolean isAdded() {
            return added;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        public boolean isNull() {
            return false;
        }

        @Override
        public boolean isRemote() {
            return false;
        }

        @Override
        public boolean serialize() {
            return false;
        }

        @Override
        public int classId() {
            return 0;
        }

        @Override
        public int id() {
            return 0;
        }

        @Override
        public void add() {
            if (!this.added) {
                Groups.draw.add(this);
                this.added = true;
            }
        }

        @Override
        public void afterRead() {

        }

        @Override
        public void id(int i) {

        }

        @Override
        public void read(Reads reads) {

        }

        @Override
        public void remove() {
            if (this.added) {
                Groups.draw.remove(this);
                this.added = false;
            }
        }

        @Override
        public void update() {

        }

        @Override
        public void write(Writes writes) {

        }

        @Override
        public void beforeWrite() {

        }

        @Override
        public void afterReadAll() {

        }
    }
}
