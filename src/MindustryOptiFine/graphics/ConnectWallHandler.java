package MindustryOptiFine.graphics;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.blocks.defense.Wall;
import mindustry.Vars;

public class ConnectWallHandler {
    public static final ObjectMap<String, TextureRegion[]> tiledRegions = new ObjectMap<>();
    public static final ObjectMap<String, TextureRegion[]> innerTiledRegions = new ObjectMap<>();
    public static final IntMap<Integer> drawIndices = new IntMap<>();
    public static final IntMap<Integer> innerDrawIndices = new IntMap<>();
    public static boolean enabled = true;
    
    public static int depthFrameCount = 0;
    public static final int DEPTH_UPDATE_INTERVAL = 20;

    public static void load() {
        tiledRegions.clear();
        innerTiledRegions.clear();
        
        String modPrefix = "mindustry-optifine-";
        
        String[] wallNames = {
            "beryllium-wall", "plastanium-wall", "carbide-wall", "copper-wall",
            "phase-wall", "surge-wall", "reinforced-surge-wall", "thorium-wall",
            "titanium-wall", "tungsten-wall"
        };
        
        int loadedCount = 0;
        
        for (String blockName : wallNames) {
            String tiledName = modPrefix + blockName + "-tiled";
            if (Core.atlas.has(tiledName)) {
                TextureRegion tiledRegion = Core.atlas.find(tiledName);
                TextureRegion[] regions = SpriteUtil.splitRegionArray(tiledRegion, 32, 32);
                tiledRegions.put(blockName, regions);
                loadedCount++;
                Log.info("ConnectWallHandler: loaded tiled texture for " + blockName + " (name: " + tiledName + ", size: " + tiledRegion.width + "x" + tiledRegion.height + ")");
            }
            
            String innerName = modPrefix + blockName + "-inner-tiled";
            if (Core.atlas.has(innerName)) {
                TextureRegion innerRegion = Core.atlas.find(innerName);
                TextureRegion[] regions = SpriteUtil.splitRegionArray(innerRegion, 32, 32);
                innerTiledRegions.put(blockName, regions);
                Log.info("ConnectWallHandler: loaded inner-tiled texture for " + blockName + " (name: " + innerName + ", size: " + innerRegion.width + "x" + innerRegion.height + ")");
            }
        }
        
        Log.info("ConnectWallHandler: loaded " + loadedCount + " connect wall textures");
    }

    public static void updateDrawIndex(Building build) {
        int pos = build.pos();
        Tile tile = build.tile;
        
        int drawIndex = 0;
        int innerDrawIndex = 0;
        
        for (int i = 0; i < 8; i++) {
            Tile other = tile.nearby(Geometry.d8[i]);
            if (other != null && other.build != null && other.build.block == build.block) {
                drawIndex |= (1 << i);
            }
        }
        
        drawIndex = TileBitmask.values[drawIndex];
        
        if (drawIndex == 13 && innerTiledRegions.containsKey(build.block.name)) {
            for (int i = 0; i < 4; i++) {
                Tile other1 = tile.nearby(Geometry.d4[i]);
                if (other1 != null && other1.build != null && other1.build.block == build.block) {
                    Building otherBuild = other1.build;
                    int otherPos = otherBuild.pos();
                    int otherIndex = drawIndices.get(otherPos, -1);
                    if (otherIndex == 13) {
                        innerDrawIndex |= (1 << i);
                    }
                }
            }
        }
        
        drawIndices.put(pos, drawIndex);
        innerDrawIndices.put(pos, innerDrawIndex);
    }

    public static boolean hasConnectTexture(Building build) {
        return enabled && build != null && tiledRegions.containsKey(build.block.name);
    }

    public static void drawConnectWall(Building build) {
        String blockName = build.block.name;
        TextureRegion[] regions = tiledRegions.get(blockName);
        if (regions == null) return;
        
        int pos = build.pos();
        int drawIndex = drawIndices.get(pos, 0);
        int innerDrawIndex = innerDrawIndices.get(pos, 0);
        
        if (drawIndex >= regions.length) drawIndex = 0;
        
        Draw.rect(regions[drawIndex], build.x, build.y);
        
        TextureRegion[] innerRegions = innerTiledRegions.get(blockName);
        if (innerRegions != null && drawIndex == 13) {
            if (innerDrawIndex >= innerRegions.length) innerDrawIndex = 0;
            Draw.rect(innerRegions[innerDrawIndex], build.x, build.y);
        }
    }

    public static void drawConnectWallDepth(Building build) {
        if (!enabled) return;
        
        depthFrameCount++;
        if (depthFrameCount % DEPTH_UPDATE_INTERVAL != 0) return;
        
        String blockName = build.block.name;
        TextureRegion[] regions = tiledRegions.get(blockName);
        if (regions == null) return;
        
        int pos = build.pos();
        int drawIndex = drawIndices.get(pos, 0);
        
        if (drawIndex >= regions.length) drawIndex = 0;
        
        float bs = build.block.size * Vars.tilesize;
        Draw.rect(regions[drawIndex], build.x, build.y, bs, bs);
        
        TextureRegion[] innerRegions = innerTiledRegions.get(blockName);
        if (innerRegions != null && drawIndex == 13) {
            int innerDrawIndex = innerDrawIndices.get(pos, 0);
            if (innerDrawIndex >= innerRegions.length) innerDrawIndex = 0;
            Draw.rect(innerRegions[innerDrawIndex], build.x, build.y, bs, bs);
        }
    }

    public static void updateAllConnectedWalls() {
        for (Building build : Groups.build) {
            if (build != null && build.block instanceof Wall && hasConnectTexture(build)) {
                updateDrawIndex(build);
            }
        }
    }

    public static TextureRegion[] getTiledRegions(String blockName) {
        return tiledRegions.get(blockName);
    }

    public static TextureRegion[] getInnerTiledRegions(String blockName) {
        return innerTiledRegions.get(blockName);
    }

    public static boolean hasInnerTexture(String blockName) {
        return innerTiledRegions.containsKey(blockName);
    }
}