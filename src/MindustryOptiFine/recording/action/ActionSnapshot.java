package MindustryOptiFine.recording.action;

import arc.util.io.*;

public class ActionSnapshot implements Action{
    public int tick;
    public byte[] data;

    public ActionSnapshot(){}

    public ActionSnapshot(int tick, byte[] data){
        this.tick = tick;
        this.data = data;
    }

    @Override
    public void write(Writes write){
        write.i(tick);
        write.s((short)data.length);
        write.b(data);
    }

    @Override
    public void read(Reads read){
        tick = read.i();
        data = read.b(read.s());
    }

    @Override
    public void execute(){}

    @Override
    public ActionType type(){
        return ActionType.snapshot;
    }
}