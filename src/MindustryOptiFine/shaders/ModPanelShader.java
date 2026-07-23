package MindustryOptiFine.shaders;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;

public class ModPanelShader extends Shader {
    public float hoverIntensity = 0f;
    public float speed = 1f;
    public Color inColor = new Color(1f, 0.47f, 0.01f, 1f);
    
    public ModPanelShader() {
        super(ModShaders.getShaderFi("modpanel.vert"), ModShaders.getShaderFi("modpanel.frag"));
    }
    
    @Override
    public void apply() {
        setUniformf("u_time", Time.time);
        setUniformf("u_hoverIntensity", hoverIntensity);
        setUniformf("u_speed", speed);
        setUniformf("u_inColor", inColor.r, inColor.g, inColor.b);
    }
}