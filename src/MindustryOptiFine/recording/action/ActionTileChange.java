package MindustryOptiFine.recording.action;

import arc.util.io.*;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.world.*;

public class ActionTileChange implements Action{
    public int x, y;
    public int blockId;
    public int rotation;
    public int teamId;

    public ActionTileChange(){}

    public ActionTileChange(Tile tile){
        this.x = tile.x;
        this.y = tile.y;
        this.blockId = tile.block().id;
        this.rotation = tile.build == null ? 0 : tile.build.rotation;
        this.teamId = tile.build == null ? Team.derelict.id : tile.build.team().id;
    }

    @Override
    public void write(Writes write){
        write.i(x);
        write.i(y);
        write.i(blockId);
        write.i(rotation);
        write.i(teamId);
    }

    @Override
    public void read(Reads read){
        x = read.i();
        y = read.i();
        blockId = read.i();
        rotation = read.i();
        teamId = read.i();
    }

    @Override
    public void execute(){
        Tile tile = Vars.world.tile(x, y);
        if(tile != null){
            tile.setBlock(Vars.content.block(blockId), Team.get(teamId), rotation);
        }
    }

    @Override
    public ActionType type(){
        return ActionType.tileChange;
    }
}