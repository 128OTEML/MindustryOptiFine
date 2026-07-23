package MindustryOptiFine.core.graphics.shaders;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.Vars;
import MindustryOptiFine.core.graphics.automators.ManagedRenderTarget;

public class ShaderManager {
    private static final ObjectMap<String, ManagedShader> shaders = new ObjectMap<>();
    private static final ObjectMap<String, ManagedScreenFilter> filters = new ObjectMap<>();
    private static final Queue<Runnable> postShaderLoadActions = new Queue<>();

    private static boolean hasFinishedLoading = false;

    private static ManagedRenderTarget mainTarget;
    private static ManagedRenderTarget auxiliaryTarget;

    public static final String autoloadDirectoryShaders = "shaders";
    public static final String autoloadDirectoryFilters = "shaders/filters";

    public static void load() {
        if (Core.graphics == null) return;

        mainTarget = new ManagedRenderTarget(true, ManagedRenderTarget::createScreenSizedTarget, true);
        auxiliaryTarget = new ManagedRenderTarget(true, ManagedRenderTarget::createScreenSizedTarget, true);

        shaders.clear();
        filters.clear();
    }

    public static void unload() {
        if (Core.graphics == null) return;

        for (ManagedShader shader : shaders.values()) {
            shader.dispose();
        }
        for (ManagedScreenFilter filter : filters.values()) {
            filter.dispose();
        }
        shaders.clear();
        filters.clear();

        if (mainTarget != null) {
            mainTarget.dispose();
            mainTarget = null;
        }
        if (auxiliaryTarget != null) {
            auxiliaryTarget.dispose();
            auxiliaryTarget = null;
        }
    }

    public static void loadShaders() {
        if (Core.graphics == null) {
            Log.info("ShaderManager: Core.graphics is null, skipping");
            return;
        }

        // Use Vars.tree for mod shaders (Core.files.internal points to game files)
        if (Vars.tree == null) {
            Log.info("ShaderManager: Vars.tree is null, skipping");
            return;
        }

        Fi shaderDir = Vars.tree.get(autoloadDirectoryShaders);
        if (shaderDir == null || !shaderDir.exists()) {
            Log.info("ShaderManager: shader directory not found: " + autoloadDirectoryShaders);
            return;
        }

        Fi[] shaderFiles = shaderDir.list();
        if (shaderFiles == null || shaderFiles.length == 0) {
            Log.info("ShaderManager: no shader files found");
            return;
        }

        Log.info("ShaderManager: found " + shaderFiles.length + " files in shader directory");

        ObjectSet<String> shaderNames = new ObjectSet<>();

        for (Fi file : shaderFiles) {
            if (file.extension().equals("frag") || file.extension().equals("vert")) {
                String shaderName = file.nameWithoutExtension();
                shaderNames.add(shaderName);
            }
        }

        Log.info("ShaderManager: total unique shaders to load: " + shaderNames.size);

        for (String shaderName : shaderNames) {
            if (shaders.containsKey(shaderName)) continue;

            Fi fragPath = Vars.tree.get(autoloadDirectoryShaders + "/" + shaderName + ".frag");
            Fi vertPath = Vars.tree.get(autoloadDirectoryShaders + "/" + shaderName + ".vert");

            if (!fragPath.exists()) {
                Log.info("ShaderManager: fragment shader not found: " + shaderName + ".frag");
                continue;
            }

            try {
                Shader shader;
                if (vertPath.exists()) {
                    shader = new Shader(vertPath, fragPath);
                    Log.info("ShaderManager: loaded shader " + shaderName + " with custom vertex shader");
                } else {
                    // Try screenspace.vert as default for screen-space shaders
                    Fi defaultVert = Vars.tree.get("shaders/screenspace.vert");
                    shader = new Shader(defaultVert, fragPath);
                    Log.info("ShaderManager: loaded shader " + shaderName + " with screenspace vertex shader");
                }

                shaders.put(shaderName, new ManagedShader(shaderName, shader));
                Log.info("ShaderManager: registered shader: " + shaderName);
            } catch (Exception e) {
                Log.err("ShaderManager: failed to load shader " + shaderName, e);
            }
        }

        Log.info("ShaderManager: total loaded shaders: " + shaders.size + "/" + shaderNames.size);
    }

    public static void applyScreenFilters() {
        if (Core.graphics == null) return;

        FrameBuffer target1 = null;
        FrameBuffer target2 = mainTarget.getTarget();

        Seq<ManagedScreenFilter> activeFilters = new Seq<>();
        for (ManagedScreenFilter filter : filters.values()) {
            if (filter.opacity > 0) {
                activeFilters.add(filter);
            }
        }

        for (ManagedScreenFilter filter : activeFilters) {
            target1 = (target2 != mainTarget.getTarget()) ? mainTarget.getTarget() : auxiliaryTarget.getTarget();
            target1.begin();
            Core.graphics.clear(Color.clear);

            filter.apply();
            Draw.rect(new TextureRegion(target2.getTexture()), 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

            target1.end();
            target2 = (target2 != mainTarget.getTarget()) ? mainTarget.getTarget() : auxiliaryTarget.getTarget();
        }

        if (target1 != null) {
            Draw.rect(new TextureRegion(target1.getTexture()), 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }

    public static void update() {
        if (Core.graphics == null) return;

        for (ManagedScreenFilter filter : filters.values()) {
            filter.update();
            filter.deactivate();
        }

        while (!postShaderLoadActions.isEmpty()) {
            postShaderLoadActions.removeFirst().run();
        }
    }

    public static ManagedShader getShader(String name) {
        return shaders.get(name);
    }

    public static ManagedScreenFilter getFilter(String name) {
        return filters.get(name);
    }

    public static boolean tryGetShader(String name, ManagedShader[] result) {
        if (shaders.containsKey(name)) {
            result[0] = shaders.get(name);
            return true;
        }
        return false;
    }

    public static boolean tryGetFilter(String name, ManagedScreenFilter[] result) {
        if (filters.containsKey(name)) {
            result[0] = filters.get(name);
            return true;
        }
        return false;
    }

    public static void setShader(String name, Shader shader) {
        shaders.put(name, new ManagedShader(name, shader));
    }

    public static void setFilter(String name, Shader shader) {
        filters.put(name, new ManagedScreenFilter(shader));
    }

    public static void addPostShaderLoadAction(Runnable action) {
        postShaderLoadActions.add(action);
    }

    public static boolean hasFinishedLoading() {
        return hasFinishedLoading;
    }

    public static void setHasFinishedLoading(boolean value) {
        hasFinishedLoading = value;
    }

    public static ManagedRenderTarget getMainTarget() {
        return mainTarget;
    }

    public static ManagedRenderTarget getAuxiliaryTarget() {
        return auxiliaryTarget;
    }
}