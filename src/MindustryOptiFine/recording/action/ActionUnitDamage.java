package MindustryOptiFine.recording.action;

import arc.util.io.*;

public class ActionUnitDamage implements Action{
    public int unitId;
    public float damage;
    public int bulletType;

    public ActionUnitDamage(){}

    public ActionUnitDamage(int unitId, float damage, int bulletType){
        this.unitId = unitId;
        this.damage = damage;
        this.bulletType = bulletType;
    }

    @Override
    public void write(Writes write){
        write.i(unitId);
        write.f(damage);
        write.i(bulletType);
    }

    @Override
    public void read(Reads read){
        unitId = read.i();
        damage = read.f();
        bulletType = read.i();
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.unitDamage;
    }
}