package MindustryOptiFine;

import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.blocks.environment.TreeBlock;

public class PropShadowHelper {

    public enum PropShadowType { TREE, ORB, SPIKE, GENERIC }

    private static final ObjectMap<Block, PropShadowType> propTypeCache = new ObjectMap<>(64);
    private static final ObjectMap<Block, Float> propHeightCache = new ObjectMap<>(64);

    public static boolean isProp(Block block) {
        return !(block instanceof mindustry.world.blocks.environment.StaticWall)
               && (block instanceof TreeBlock 
                   || block instanceof mindustry.world.blocks.environment.Prop
                   || block.name.contains("tree")
                   || block.name.contains("plant")
                   || block.name.contains("flower"));
    }

    public static PropShadowType getPropType(Block block) {
        return getPropType(block, block.region);
    }

    public static PropShadowType getPropType(Block block, TextureRegion region) {
        if (propTypeCache.containsKey(block)) return propTypeCache.get(block);

        PropShadowType type;
        String name = block.name.toLowerCase();

        if (block instanceof TreeBlock) {
            type = PropShadowType.TREE;
        } else if (name.contains("orb") || name.contains("sphere") || name.contains("ball")) {
            type = PropShadowType.ORB;
        } else if (name.contains("spike") || name.contains("thorn") || name.contains("needle")) {
            type = PropShadowType.SPIKE;
        } else if (region != null && region.found()) {
            float ar = region.height / (float) Math.max(region.width, 1);
            type = (ar > 1.4f) ? PropShadowType.TREE : PropShadowType.GENERIC;
        } else {
            type = PropShadowType.GENERIC;
        }

        propTypeCache.put(block, type);
        return type;
    }

    public static float getPropHeightScale(Block block) {
        return getPropHeightScale(block, block.region);
    }

    public static float getPropHeightScale(Block block, TextureRegion region) {
        if (propHeightCache.containsKey(block)) return propHeightCache.get(block, 0.3f);

        float heightScale;
        PropShadowType type = getPropType(block, region);

        switch (type) {
            case TREE:
                if (region != null && region.found()) {
                    float propH = region.height * Draw.scl;
                    heightScale = Mathf.clamp(propH / (Vars.tilesize * 0.8f), 0.8f, 3.0f);
                } else {
                    heightScale = 1.8f;
                }
                break;
            case SPIKE:
                if (region != null && region.found()) {
                    float propH = region.height * Draw.scl;
                    heightScale = Mathf.clamp(propH / (Vars.tilesize * 1.8f), 0.3f, 1.5f);
                } else {
                    heightScale = 0.8f;
                }
                break;
            case ORB:
                heightScale = 0.4f;
                break;
            default:
                if (region != null && region.found()) {
                    float propH = region.height * Draw.scl;
                    heightScale = Mathf.clamp(propH / (Vars.tilesize * 2f), 0.25f, 1.5f);
                } else {
                    heightScale = 0.6f;
                }
                break;
        }

        propHeightCache.put(block, heightScale);
        return heightScale;
    }

    public static void clearCache() {
        propTypeCache.clear();
        propHeightCache.clear();
    }
}