package MindustryOptiFine.core.graphics.atlases;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;

public class Atlas {
    private final Seq<AtlasTexture> textures = new Seq<>();

    public Texture texture;
    public final String name;
    public final String atlasPath;

    public Atlas(String name, String atlasPath) {
        this.name = name;
        this.atlasPath = atlasPath;
        loadTexture();
        loadAtlasData();
    }

    private void loadTexture() {
        Fi texFile = Core.files.internal(atlasPath + ".png");
        if (texFile.exists()) {
            texture = new Texture(texFile);
        }
    }

    private void loadAtlasData() {
        Fi filePath = Core.files.internal(atlasPath + ".json");
        if (!filePath.exists()) {
            Log.warn("Atlas data file not found: " + filePath.path());
            if (texture != null) {
                AtlasTexture atlasTexture = new AtlasTexture(this, name, texture);
                textures.add(atlasTexture);
            }
            return;
        }

        if (texture == null) {
            return;
        }

        String jsonContent = filePath.readString();
        try {
            int framesStart = jsonContent.indexOf("\"frames\"");
            if (framesStart == -1) {
                AtlasTexture atlasTexture = new AtlasTexture(this, name, texture);
                textures.add(atlasTexture);
                return;
            }

            int braceStart = jsonContent.indexOf('{', framesStart);
            int braceEnd = findMatchingBrace(jsonContent, braceStart);
            
            String framesContent = jsonContent.substring(braceStart + 1, braceEnd);
            String[] entries = splitJsonObjects(framesContent);
            
            for (String entry : entries) {
                if (entry.trim().isEmpty()) continue;
                
                int colonIndex = entry.indexOf(':');
                if (colonIndex == -1) continue;
                
                String key = entry.substring(0, colonIndex).replace("\"", "").trim();
                String value = entry.substring(colonIndex + 1).trim();
                
                if (!value.startsWith("{")) continue;
                
                int frameStart = value.indexOf("\"frame\"");
                if (frameStart == -1) continue;
                
                int frameBraceStart = value.indexOf('{', frameStart);
                int frameBraceEnd = findMatchingBrace(value, frameBraceStart);
                
                String frameContent = value.substring(frameBraceStart + 1, frameBraceEnd);
                
                int x = parseIntFromJson(frameContent, "x");
                int y = parseIntFromJson(frameContent, "y");
                int w = parseIntFromJson(frameContent, "w");
                int h = parseIntFromJson(frameContent, "h");
                
                if (x >= 0 && y >= 0 && w > 0 && h > 0) {
                    TextureRegion region = new TextureRegion(texture, x, y, w, h);
                    AtlasTexture atlasTexture = new AtlasTexture(this, key, region.texture);
                    textures.add(atlasTexture);
                }
            }
        } catch (Exception e) {
            Log.err("Failed to parse atlas data: " + filePath.path(), e);
            if (texture != null) {
                AtlasTexture atlasTexture = new AtlasTexture(this, name, texture);
                textures.add(atlasTexture);
            }
        }
    }

    private int findMatchingBrace(String str, int start) {
        int depth = 1;
        for (int i = start + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            if (depth == 0) return i;
        }
        return str.length();
    }

    private String[] splitJsonObjects(String content) {
        Seq<String> result = new Seq<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            else if (c == ',' && depth == 0) {
                result.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        
        if (current.length() > 0) {
            result.add(current.toString());
        }
        
        return result.toArray(String.class);
    }

    private int parseIntFromJson(String content, String key) {
        int keyIndex = content.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return -1;
        
        int colonIndex = content.indexOf(':', keyIndex);
        if (colonIndex == -1) return -1;
        
        int valueStart = colonIndex + 1;
        while (valueStart < content.length() && (content.charAt(valueStart) == ' ' || content.charAt(valueStart) == '\n')) {
            valueStart++;
        }
        
        int valueEnd = valueStart;
        while (valueEnd < content.length() && Character.isDigit(content.charAt(valueEnd))) {
            valueEnd++;
        }
        
        try {
            return Integer.parseInt(content.substring(valueStart, valueEnd));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public Seq<AtlasTexture> getTextures() {
        return textures;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}