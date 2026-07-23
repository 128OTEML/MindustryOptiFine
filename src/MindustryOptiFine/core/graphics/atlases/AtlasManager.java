package MindustryOptiFine.core.graphics.atlases;

import arc.*;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.struct.*;
import arc.util.*;

public class AtlasManager {
    private static final Seq<Atlas> atlases = new Seq<>();
    private static final ObjectMap<String, AtlasTexture> allTexturesByName = new ObjectMap<>();

    private static void addAtlas(Atlas atlas) {
        for (AtlasTexture texture : atlas.getTextures()) {
            String storedName = "MindustryOptiFine." + texture.name;

            if (allTexturesByName.containsKey(storedName)) {
                throw new RuntimeException("Duplicate atlas texture name '" + storedName + "' found! All atlas texture names must be unique!");
            }

            allTexturesByName.put(storedName, texture);
        }
        atlases.add(atlas);
    }

    public static Texture getTexture(String textureName) {
        AtlasTexture atlasTexture = allTexturesByName.get(textureName);
        if (atlasTexture != null) {
            return atlasTexture.texture;
        }

        TextureRegion region = Core.atlas.find(textureName);
        if (region != null) {
            return region.texture;
        }
        return null;
    }

    public static Atlas getAtlas(String atlasName) {
        for (Atlas atlas : atlases) {
            if (atlas.name.equals(atlasName)) {
                return atlas;
            }
        }

        Log.info("Atlas '" + atlasName + "' is not registered!");
        return null;
    }

    public static boolean atlasIsRegistered(String atlasName) {
        for (Atlas atlas : atlases) {
            if (atlas.name.equals(atlasName)) {
                return true;
            }
        }
        return false;
    }

    public static Atlas registerAtlas(String name, String atlasPath) {
        if (atlasIsRegistered(name)) {
            return getAtlas(name);
        }

        Atlas atlas = new Atlas(name, atlasPath);
        addAtlas(atlas);
        return atlas;
    }

    public static void load() {
    }

    public static void unload() {
        for (Atlas atlas : atlases) {
            atlas.dispose();
        }
        atlases.clear();
        allTexturesByName.clear();
    }
}