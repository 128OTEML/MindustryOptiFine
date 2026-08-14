package MindustryOptiFine.io;

import arc.files.Fi;
import arc.struct.*;
import arc.util.Log;
import arc.util.Strings;

import static arc.Core.settings;

/** Reads .mofs settings preset files, a plain text format identical to locale bundle files. */
public class MofsReader{
    public static final String EXTENSION = "mofs";

    /** Parses the preset text into a key-value map. Lines and comma-separated entries are both supported. */
    public static ObjectMap<String, String> parse(String text){
        ObjectMap<String, String> out = new ObjectMap<>();

        if(text == null) return out;

        for(String line : text.split("[,\\r\\n]+")){
            int eq = line.indexOf('=');
            if(eq == -1) continue;

            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();

            if(key.isEmpty() || key.startsWith("#")) continue;

            out.put(key, value);
        }

        return out;
    }

    /** Reads a .mofs file into a key-value map. */
    public static ObjectMap<String, String> read(Fi file){
        if(file == null || !file.exists()){
            Log.warn("MofsReader: preset file does not exist: " + (file == null ? "<null>" : file.path()));
            return new ObjectMap<>();
        }
        return parse(file.readString());
    }

    /** Reads a .mofs file and applies all entries to the current settings store. */
    public static void load(Fi file){
        apply(read(file));
    }

    /** Parses preset text and applies all entries to the current settings store. */
    public static void load(String text){
        apply(parse(text));
    }

    /** Applies the given key-value map to the current settings store, converting each value to its proper type. */
    public static void apply(ObjectMap<String, String> presets){
        for(var entry : presets){
            settings.put(entry.key, convert(entry.value));
        }
    }

    /** Converts a preset value string to boolean/int/float/String based on its content. */
    public static Object convert(String value){
        if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
            return Boolean.parseBoolean(value);
        }
        if(Strings.canParseInt(value)){
            return Strings.parseInt(value);
        }
        if(Strings.canParseFloat(value)){
            return Strings.parseFloat(value);
        }
        return value;
    }

    /** Serializes the current settings store into preset text format, always including every given key. */
    public static String export(String... keys){
        StringBuilder sb = new StringBuilder();
        for(String key : keys){
            Object value = settings.has(key) ? settings.get(key, null) : settings.getDefault(key);
            if(value == null) continue;
            sb.append(key).append(" = ").append(value).append("\n");
        }
        return sb.toString();
    }

    /** Writes the current settings store for the given keys into a .mofs file. */
    public static void save(Fi file, String... keys){
        file.writeString(export(keys));
    }

    /** All setting keys handled by the mod, in display order. */
    public static final String[] KEYS = {
        "al-bloom-enabled", "al-bloom-quality", "al-bloom-intensity", "al-bloom-threshold", "al-bloom-saturation",
        "al-bloom-blur-amount", "al-bloom-flare-amount", "al-bloom-flare-length", "al-bloom-flare-direction",
        "al-bloom-blur-size", "al-bloom-blur-feedback", "al-bloom-flare-feedback", "al-bloom-diffuse-amount",
        "al-bloom-diffuse-size", "al-bloom-diffuse-feedback", "al-environment-enabled", "al-hide-lights", "al-auto-quality",
        "shadow", "shadows_enabled", "depthTex", "precision", "zoomPrec", "lightLowPass", "maxLights", "debug",
        "day_night_cycle", "unit_shadows", "graphics_quality", "shadow_length", "prop_shadow_scale",
        "shadow_opacity_percent", "blur_radius", "shadow_tint_percent", "contact_shadow_percent", "dark_fade_percent",
        "connect-wall-enabled", "advanced-camera", "camera-sensitivity"
    };

    /** The built-in preset directory (assets/mofs) of the given mod root. */
    public static Fi presetDir(Fi modRoot){
        return modRoot == null ? null : modRoot.child("mofs");
    }

    /** The external preset directory (data root/Mofs) for user-imported presets. */
    public static Fi externalPresetDir(Fi dataRoot){
        return dataRoot == null ? null : dataRoot.child("Mofs");
    }

    /** Returns all .mofs preset files in the given directory, sorted by name. */
    public static Seq<Fi> listFromDir(Fi dir){
        Seq<Fi> out = new Seq<>();
        if(dir != null && dir.exists() && dir.isDirectory()){
            for(Fi file : dir.list()){
                if(file.extEquals(EXTENSION)){
                    out.add(file);
                }
            }
            out.sort((a, b) -> a.name().compareTo(b.name()));
        }
        return out;
    }

    /** Returns all .mofs preset files bundled with the given mod root, sorted by name. */
    public static Seq<Fi> listPresets(Fi modRoot){
        return listFromDir(presetDir(modRoot));
    }

    /** Returns all .mofs preset files in the external data directory, sorted by name. */
    public static Seq<Fi> listExternalPresets(Fi dataRoot){
        return listFromDir(externalPresetDir(dataRoot));
    }

    /** Returns all built-in and external presets, external ones taking precedence on name conflicts. */
    public static Seq<Fi> listAllPresets(Fi modRoot, Fi dataRoot){
        Seq<Fi> all = listPresets(modRoot);
        all.addAll(listExternalPresets(dataRoot));
        return all;
    }

    /** Finds a preset file by exact name in the given mod root. */
    public static Fi findPreset(Fi modRoot, String name){
        Fi dir = presetDir(modRoot);
        return dir == null ? null : dir.child(name);
    }

    /** Finds a preset file by exact name, checking the external data directory first, then the mod root. */
    public static Fi findPreset(Fi modRoot, Fi dataRoot, String name){
        Fi ext = externalPresetDir(dataRoot);
        if(ext != null){
            Fi file = ext.child(name);
            if(file.exists()) return file;
        }
        return findPreset(modRoot, name);
    }
}
