package MindustryOptiFine.recording.action;

import arc.util.io.*;
import mindustry.gen.*;

public class ActionUnitMove implements Action{
    public int unitId;
    public float x, y;
    public float vx, vy;
    public float rotation;

    public ActionUnitMove(){}

    public ActionUnitMove(Unit unit){
        this.unitId = unit.id();
        this.x = unit.x();
        this.y = unit.y();
        this.vx = unit.vel().x;
        this.vy = unit.vel().y;
        this.rotation = unit.rotation();
    }

    @Override
    public void write(Writes write){
        write.i(unitId);
        write.f(x);
        write.f(y);
        write.f(vx);
        write.f(vy);
        write.f(rotation);
    }

    @Override
    public void read(Reads read){
        unitId = read.i();
        x = read.f();
        y = read.f();
        vx = read.f();
        vy = read.f();
        rotation = read.f();
    }

    @Override
    public void execute(){
        Unit unit = Groups.unit.getByID(unitId);
        if(unit != null){
            unit.set(x, y);
            unit.vel().set(vx, vy);
            unit.rotation(rotation);
        }
    }

    @Override
    public ActionType type(){
        return ActionType.unitMove;
    }
}