package MindustryOptiFine;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import mindustry.Vars;

public class SSShaders{
    public static ShadowShader shadow;

    public static void load(){
        shadow = new ShadowShader();
    }

    public static class SSShader extends Shader{
        public SSShader(String frag, boolean treeFrag, String vert, boolean treeVert){
            super(getShaderFi(vert, treeVert), getShaderFi(frag, treeFrag));
        }
        public SSShader(String frag, String vert){
            this(frag, true, vert, false);
        }
    }

    public static Fi getShaderFi(String name, boolean tree){
        if(tree) return Vars.tree.get("shaders/" + name);
        return Core.files.internal("shaders/" + name);
    }

    public static class ShadowShader extends SSShader {
        public FloatSeq data = new FloatSeq();
        public ShadowShader(){
            super("shadow.frag", "screenspace.vert");
        }

        @Override
        public void apply(){
            Shadow.lightsUniformData(data);
            setUniformf("u_EDGE_PRECISION", 8f / (float)Shadow.precision / (Shadow.zoomPrec ? Mathf.pow(Vars.renderer.getDisplayScale(), 0.4f):1f));
            setUniformf("u_offset",
                    Core.camera.position.x - Core.camera.width / 2,
                    Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_texsize", Core.camera.width, Core.camera.height);
            setUniformf("u_invsize", 1f/Core.camera.width, 1f/Core.camera.height);

            Color ambient = Vars.state.rules.ambientLight;
            float ambientAlpha = Vars.state.rules.lighting ? ambient.a : 1f;
            
            setUniformf("u_ambientLight", ambientAlpha);
            setUniformf("u_ambientColor", ambient.r, ambient.g, ambient.b);
            
            setUniformi("u_lightcount", data.size/2);
            setUniform2fv("u_lights", data.items, 0, data.size);
        }
    }
}
