package MindustryOptiFine.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.*;

import static mindustry.Vars.*;

public class AdvancedCamera{
    public static boolean enabled = false;
    public static float sensitivity = 1f;
    
    private static CustomCamera customCamera;
    private static Camera originalCamera;
    private static boolean replaced = false;
    
    private static Vec2 tmpMove = new Vec2();
    private static Vec2 targetCenter = new Vec2();
    private static float unitDirection = 0f;
    
    public static void init(){
        originalCamera = Core.camera;
        customCamera = new CustomCamera();
        customCamera.width = Core.camera.width;
        customCamera.height = Core.camera.height;
        customCamera.position.set(Core.camera.position);
        
        Events.run(EventType.Trigger.preDraw, AdvancedCamera::updateRotation);
        Events.run(EventType.Trigger.update, AdvancedCamera::transformMovement);
    }
    
    public static void enable(){
        if(!replaced && customCamera != null){
            customCamera.width = Core.camera.width;
            customCamera.height = Core.camera.height;
            customCamera.position.set(Core.camera.position);
            Core.camera = customCamera;
            replaced = true;
        }
        enabled = true;
    }
    
    public static void disable(){
        enabled = false;
        if(replaced && originalCamera != null){
            originalCamera.width = Core.camera.width;
            originalCamera.height = Core.camera.height;
            originalCamera.position.set(Core.camera.position);
            Core.camera = originalCamera;
            replaced = false;
        }
    }
    
    public static void setEnabled(boolean value){
        if(value){
            enable();
        }else{
            disable();
        }
    }
    
    private static void updateRotation(){
        if(!enabled || customCamera == null) return;
        
        if(state.isGame() && player.unit() != null && player.unit().isValid()){
            Unit unit = player.unit();
            
            unitDirection = unit.rotation;
            
            customCamera.targetRotation = unitDirection * sensitivity;
            targetCenter.set(unit.x, unit.y);
        }else{
            unitDirection = 0f;
            customCamera.targetRotation = 0f;
            targetCenter.setZero();
        }
    }
    
    private static void transformMovement(){
        if(!enabled || customCamera == null) return;
        
        if(state.isGame() && player.unit() != null && player.unit().isValid()){
            float rotation = customCamera.rotation;
            
            if(Math.abs(rotation) > 0.01f){
                if(control.input instanceof DesktopInput){
                    DesktopInput desktopInput = (DesktopInput)control.input;
                    Vec2 movement = desktopInput.movement;
                    
                    if(movement != null && !movement.isZero()){
                        tmpMove.set(movement);
                        tmpMove.rotate(-rotation);
                        movement.set(tmpMove);
                    }
                }
            }
        }
    }
    
    public static void dispose(){
        if(replaced){
            disable();
        }
        customCamera = null;
        originalCamera = null;
    }
    
    public static float getRotation(){
        return customCamera != null ? customCamera.rotation : 0f;
    }
    
    public static float getUnitDirection(){
        return unitDirection;
    }
    
    private static float lerpAngle(float from, float to, float alpha){
        float diff = to - from;
        
        while(diff > 180f) diff -= 360f;
        while(diff < -180f) diff += 360f;
        
        return from + diff * alpha;
    }
    
    public static class CustomCamera extends Camera{
        public float rotation = 0f;
        public float targetRotation = 0f;
        
        @Override
        public void update(){
            if(AdvancedCamera.enabled){
                rotation = lerpAngle(rotation, targetRotation, 0.1f);
                
                mat.idt();
                mat.setOrtho(position.x - width / 2f, position.y - height / 2f, width, height);
                
                if(rotation != 0f){
                    float centerX = targetCenter.x;
                    float centerY = targetCenter.y;
                    
                    mat.translate(centerX, centerY);
                    mat.rotate(rotation);
                    mat.translate(-centerX, -centerY);
                }
            }else{
                super.update();
            }
        }
    }
}
