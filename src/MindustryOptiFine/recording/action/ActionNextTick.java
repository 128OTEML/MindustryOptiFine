package MindustryOptiFine.recording.action;

import arc.util.io.*;

public class ActionNextTick implements Action{
    public int tick;

    public ActionNextTick(){}

    public ActionNextTick(int tick){
        this.tick = tick;
    }

    @Override
    public void write(Writes write){
        write.i(tick);
    }

    @Override
    public void read(Reads read){
        tick = read.i();
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.nextTick;
    }
}