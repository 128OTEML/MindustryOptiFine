package MindustryOptiFine.graphics;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import mindustry.ctype.ContentType;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.blocks.defense.Wall;
import mindustry.Vars;
import mindustry.mod.Mods;

public class ConnectWallHandler {
    public static final ObjectMap<String, TextureRegion[]> tiledRegions = new ObjectMap<>();
    public static final ObjectMap<String, TextureRegion[]> innerTiledRegions = new ObjectMap<>();
    public static final IntMap<Integer> drawIndices = new IntMap<>();
    public static final IntMap<Integer> innerDrawIndices = new IntMap<>();
    public static boolean enabled = true;

    public static int depthFrameCount = 0;
    public static final int DEPTH_UPDATE_INTERVAL = 20;

    // 存储已加载的墙体名称，用于延迟替换
    private static final Seq<String> pendingWallNames = new Seq<>();
    private static boolean initialized = false;
    private static boolean loading = false;
    private static int retryCount = 0;
    private static final int MAX_RETRIES = 15;
    private static final int RETRY_DELAY = 30; // ticks

    /**
     * 获取所有需要替换的墙体名称列表（即有 tiled 贴图的墙体）
     */
    public static Seq<String> getReplacedWallNames() {
        Seq<String> result = new Seq<>();
        for (String wallName : tiledRegions.keys()) {
            result.add(wallName);
        }
        return result;
    }

    /**
     * 获取所有可用的墙体贴图映射
     */
    public static ObjectMap<String, String> getWallTextureMap() {
        ObjectMap<String, String> map = new ObjectMap<>();
        String prefix = getModPrefix();
        for (String wallName : tiledRegions.keys()) {
            map.put(wallName, prefix + wallName + "-tiled");
        }
        return map;
    }

    /**
     * 初始化：扫描所有墙体并加载对应的贴图
     * 应该在所有MOD加载完毕后调用
     */
    public static void init() {
        if (initialized || loading) return;

        loading = true;
        Log.info("ConnectWallHandler: starting initialization...");

        // 清除旧数据
        tiledRegions.clear();
        innerTiledRegions.clear();
        pendingWallNames.clear();
        retryCount = 0;

        // 开始尝试加载
        attemptLoad();
    }

    /**
     * 尝试加载，如果贴图未准备好则重试
     */
    private static void attemptLoad() {
        // 扫描并加载贴图
        boolean allLoaded = loadTexturesWithRetry();

        if (allLoaded) {
            // 所有贴图加载成功，进行替换
            replaceVanillaWalls();
            initialized = true;
            loading = false;
            Log.info("ConnectWallHandler: initialized successfully, loaded " + tiledRegions.size + " wall textures");
        } else if (retryCount < MAX_RETRIES) {
            retryCount++;
            Log.info("ConnectWallHandler: some textures not ready, retry " + retryCount + "/" + MAX_RETRIES + "...");
            // 延迟重试
            Time.runTask(RETRY_DELAY, ConnectWallHandler::attemptLoad);
        } else {
            loading = false;
            Log.warn("ConnectWallHandler: initialization failed after " + MAX_RETRIES + " retries, some textures may be missing");
            // 即使部分失败，也尝试替换已加载的
            if (!pendingWallNames.isEmpty()) {
                replaceVanillaWalls();
                initialized = true;
                Log.info("ConnectWallHandler: partially initialized, loaded " + tiledRegions.size + " wall textures");
            }
        }
    }

    /**
     * 获取MOD资源前缀
     */
    private static String getModPrefix() {
        // 查找当前MOD的名称
        for (Mods.LoadedMod mod : Vars.mods.list()) {
            if (mod.main instanceof MindustryOptiFine.MindustryOptiFine) {
                return mod.name + "-";
            }
        }
        return "mindustry-optifine-";
    }

    /**
     * 加载所有墙体的贴图（带重试检测）
     * @return true 如果所有已发现贴图都已加载完成
     */
    private static boolean loadTexturesWithRetry() {
        String modPrefix = getModPrefix();
        int loadedCount = 0;
        int missingCount = 0;
        boolean allLoaded = true;

        // 扫描所有墙体
        for (Block block : Vars.content.blocks()) {
            if (!(block instanceof Wall)) continue;

            String blockName = block.name;
            int stateSize = block.size * 32;

            // 加载主贴图
            String tiledName = modPrefix + blockName + "-tiled";
            if (Core.atlas.has(tiledName)) {
                TextureRegion tiledRegion = Core.atlas.find(tiledName);
                TextureRegion[] regions = SpriteUtil.splitRegionArray(tiledRegion, stateSize, stateSize);
                tiledRegions.put(blockName, regions);
                if (!pendingWallNames.contains(blockName)) {
                    pendingWallNames.add(blockName);
                }
                loadedCount++;
                Log.info("ConnectWallHandler: loaded tiled texture for " + blockName +
                        " (size: " + tiledRegion.width + "x" + tiledRegion.height + ")");
            } else {
                // 检查贴图文件是否存在，如果存在但未加载，标记为需要重试
                if (checkTextureExists(blockName + "-tiled")) {
                    allLoaded = false;
                    missingCount++;
                    Log.info("ConnectWallHandler: texture not yet loaded for " + blockName + ", will retry");
                }
            }

            // 加载内部贴图
            String innerName = modPrefix + blockName + "-inner-tiled";
            if (Core.atlas.has(innerName)) {
                TextureRegion innerRegion = Core.atlas.find(innerName);
                TextureRegion[] regions = SpriteUtil.splitRegionArray(innerRegion, stateSize, stateSize);
                innerTiledRegions.put(blockName, regions);
                Log.info("ConnectWallHandler: loaded inner-tiled texture for " + blockName);
            }
        }

        Log.info("ConnectWallHandler: loaded " + loadedCount + " wall textures, " + missingCount + " missing");

        // 如果没有任何墙体需要加载，返回 true
        if (loadedCount == 0 && missingCount == 0) {
            return true;
        }

        return allLoaded;
    }

    /**
     * 检查贴图文件是否存在（在MOD的resources中）
     */
    private static boolean checkTextureExists(String textureName) {
        // 检查所有已加载的MOD资源
        for (Mods.LoadedMod mod : Vars.mods.list()) {
            arc.files.Fi file = mod.root.child("sprites/blocks/walls/" + textureName + ".png");
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 替换所有墙体的 buildType
     */
    private static void replaceVanillaWalls() {
        int replacedCount = 0;

        for (String wallName : pendingWallNames) {
            if (!tiledRegions.containsKey(wallName)) continue;

            Block originalBlock = Vars.content.getByName(ContentType.block, wallName);
            if (originalBlock == null || !(originalBlock instanceof Wall)) continue;

            try {
                java.lang.reflect.Field buildTypeField = Block.class.getDeclaredField("buildType");
                buildTypeField.setAccessible(true);

                Wall finalWall = (Wall) originalBlock;
                arc.func.Prov<mindustry.gen.Building> newBuildType = () -> {
                    try {
                        ConnectWallBuild build = new ConnectWallBuild();
                        java.lang.reflect.Field blockField = mindustry.gen.Building.class.getDeclaredField("block");
                        blockField.setAccessible(true);
                        blockField.set(build, finalWall);
                        return build;
                    } catch (Exception e) {
                        Log.err("ConnectWall: failed to create ConnectWallBuild for '" + wallName + "': " + e.getMessage());
                        return finalWall.newBuilding();
                    }
                };

                buildTypeField.set(originalBlock, newBuildType);
                replacedCount++;
                Log.info("ConnectWall: replaced buildType for '" + wallName + "'");
            } catch (Exception e) {
                Log.err("ConnectWall: failed to replace buildType for '" + wallName + "': " + e.getMessage());
            }
        }

        Log.info("ConnectWall: replaced buildType for " + replacedCount + " walls");
    }

    /**
     * 重新加载（用于热重载或配置更改）
     */
    public static void reload() {
        initialized = false;
        loading = false;
        retryCount = 0;
        init();
    }

    /**
     * 加载贴图（兼容旧接口）
     */
    public static void load() {
        // 延迟到 init 时统一处理
        if (!initialized && !loading) {
            init();
        }
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

        float stateSize = build.block.size * 32f;

        Draw.rect(regions[drawIndex], build.x, build.y, stateSize, stateSize);

        TextureRegion[] innerRegions = innerTiledRegions.get(blockName);
        if (innerRegions != null && drawIndex == 13) {
            if (innerDrawIndex >= innerRegions.length) innerDrawIndex = 0;
            Draw.rect(innerRegions[innerDrawIndex], build.x, build.y, stateSize, stateSize);
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