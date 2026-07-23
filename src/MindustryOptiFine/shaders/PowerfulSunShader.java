package MindustryOptiFine.shaders;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;

public class PowerfulSunShader extends Shader {
    public float hoverIntensity = 0f;
    public float speed = 1f;
    public float pixel = 1f;
    public Color inColor = new Color(1f, 1f, 1f, 1f);
    
    public PowerfulSunShader() {
        super(ModShaders.getShaderFi("sun.vert"), ModShaders.getShaderFi("sun.frag"));
    }
    
    @Override
    public void apply() {
        setUniformf("u_time", Time.time);
        setUniformf("u_hoverIntensity", hoverIntensity);
        setUniformf("u_speed", speed);
        setUniformf("u_pixel", pixel);
        setUniformf("u_inColor", inColor.r, inColor.g, inColor.b);
    }
}