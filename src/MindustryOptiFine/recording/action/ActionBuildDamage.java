package MindustryOptiFine.recording.action;

import arc.util.io.*;

public class ActionBuildDamage implements Action{
    public int x, y;
    public float damage;
    public int bulletType;

    public ActionBuildDamage(){}

    public ActionBuildDamage(int x, int y, float damage, int bulletType){
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.bulletType = bulletType;
    }

    @Override
    public void write(Writes write){
        write.i(x);
        write.i(y);
        write.f(damage);
        write.i(bulletType);
    }

    @Override
    public void read(Reads read){
        x = read.i();
        y = read.i();
        damage = read.f();
        bulletType = read.i();
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.buildDamage;
    }
}