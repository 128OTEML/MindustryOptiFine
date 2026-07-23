package MindustryOptiFine.core.graphics.atlases;

import arc.graphics.Texture;

public class AtlasTexture {
    public final Atlas atlas;
    public final String name;
    public final Texture texture;

    public AtlasTexture(Atlas atlas, String name, Texture texture) {
        this.atlas = atlas;
        this.name = name;
        this.texture = texture;
    }
}