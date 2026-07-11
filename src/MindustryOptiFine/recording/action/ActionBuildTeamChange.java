package MindustryOptiFine.recording.action;

import arc.util.io.*;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.*;

public class ActionBuildTeamChange implements Action{
    public int x, y;
    public int previousTeamId;
    public int newTeamId;

    public ActionBuildTeamChange(){}

    public ActionBuildTeamChange(int x, int y, int previousTeamId, int newTeamId){
        this.x = x;
        this.y = y;
        this.previousTeamId = previousTeamId;
        this.newTeamId = newTeamId;
    }

    @Override
    public void write(Writes write){
        write.i(x);
        write.i(y);
        write.i(previousTeamId);
        write.i(newTeamId);
    }

    @Override
    public void read(Reads read){
        x = read.i();
        y = read.i();
        previousTeamId = read.i();
        newTeamId = read.i();
    }

    @Override
    public void execute(){
        Building build = Vars.world.build(x, y);
        if(build != null){
            build.team(Team.get(newTeamId));
        }
    }

    @Override
    public ActionType type(){
        return ActionType.buildTeamChange;
    }
}