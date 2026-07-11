package MindustryOptiFine.playback;

import MindustryOptiFine.recording.action.*;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.UnitType;
import mindustry.world.*;

public class ActionExecutor{
    private final ReplayServer server;

    public ActionExecutor(ReplayServer server){
        this.server = server;
    }

    public void execute(Action action){
        switch(action.type()){
            case tileChange -> executeTileChange((ActionTileChange)action);
            case unitCreate -> executeUnitCreate((ActionUnitCreate)action);
            case unitDestroy -> executeUnitDestroy((ActionUnitDestroy)action);
            case unitMove -> executeUnitMove((ActionUnitMove)action);
            case playerJoin -> executePlayerJoin((ActionPlayerJoin)action);
            case playerLeave -> executePlayerLeave((ActionPlayerLeave)action);
            case playerData -> executePlayerData((ActionPlayerData)action);
            case wave -> executeWave((ActionWave)action);
            case gameOver -> executeGameOver((ActionGameOver)action);
            case snapshot -> executeSnapshot((ActionSnapshot)action);
            case chat -> executeChat((ActionChat)action);
            case buildDamage -> executeBuildDamage((ActionBuildDamage)action);
            case unitDamage -> executeUnitDamage((ActionUnitDamage)action);
            case configChange -> executeConfigChange((ActionConfigChange)action);
            case buildTeamChange -> executeBuildTeamChange((ActionBuildTeamChange)action);
            case sectorCapture -> executeSectorCapture((ActionSectorCapture)action);
            case nextTick -> {}
        }
    }

    private void executeTileChange(ActionTileChange action){
        Tile tile = Vars.world.tile(action.x, action.y);
        if(tile != null){
            tile.setBlock(Vars.content.block(action.blockId), Team.get(action.teamId), action.rotation);
        }
    }

    private void executeUnitCreate(ActionUnitCreate action){
        UnitType type = Vars.content.unit(action.typeId);
        if(type != null){
            Unit unit = type.create(Team.get(action.teamId));
            unit.set(action.x, action.y);
            unit.rotation(action.rotation);
        }
    }

    private void executeUnitDestroy(ActionUnitDestroy action){
        Unit unit = Groups.unit.getByID(action.unitId);
        if(unit != null){
            unit.remove();
        }
    }

    private void executeUnitMove(ActionUnitMove action){
        Unit unit = Groups.unit.getByID(action.unitId);
        if(unit != null){
            unit.set(action.x, action.y);
            unit.vel().set(action.vx, action.vy);
            unit.rotation(action.rotation);
        }
    }

    private void executePlayerJoin(ActionPlayerJoin action){}

    private void executePlayerLeave(ActionPlayerLeave action){}

    private void executePlayerData(ActionPlayerData action){
        Player player = Groups.player.find(p -> p.uuid().equals(action.uuid));
        if(player != null){
            player.set(action.x, action.y);
            if(action.unitId != -1){
                Unit unit = Groups.unit.getByID(action.unitId);
                if(unit != null){
                    player.unit(unit);
                }
            }
        }
    }

    private void executeWave(ActionWave action){
        Vars.state.wave = action.wave;
    }

    private void executeGameOver(ActionGameOver action){
        Vars.state.gameOver = true;
        Vars.state.won = action.winnerTeamId == Vars.state.rules.defaultTeam.id;
    }

    private void executeSnapshot(ActionSnapshot action){
        loadSnapshot(action.data);
    }

    private void executeChat(ActionChat action){
        Log.info("[Chat] @: @", action.playerName, action.message);
    }

    private void executeBuildDamage(ActionBuildDamage action){
        Building build = Vars.world.build(action.x, action.y);
        if(build != null){
            build.damage(action.damage);
        }
    }

    private void executeUnitDamage(ActionUnitDamage action){
        Unit unit = Groups.unit.getByID(action.unitId);
        if(unit != null){
            unit.damage(action.damage);
        }
    }

    private void executeConfigChange(ActionConfigChange action){
        Building build = Vars.world.build(action.x, action.y);
        if(build != null){
            build.configure(action.config);
        }
    }

    private void executeBuildTeamChange(ActionBuildTeamChange action){
        Building build = Vars.world.build(action.x, action.y);
        if(build != null){
            build.changeTeam(Team.get(action.newTeamId));
        }
    }

    private void executeSectorCapture(ActionSectorCapture action){}

    private void loadSnapshot(byte[] data){
        try{
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
            java.util.zip.InflaterInputStream iis = new java.util.zip.InflaterInputStream(bais);
            java.io.DataInputStream dis = new java.io.DataInputStream(iis);
            arc.util.io.Reads read = new arc.util.io.Reads(dis);

            readTiles(read);
            readUnits(read);
            readBuildings(read);
            readPlayers(read);
            readState(read);

            dis.close();
        }catch(Exception e){
            Log.err("Snapshot load error: @", e.getMessage());
        }
    }

    private void readTiles(arc.util.io.Reads read){
        int width = read.i();
        int height = read.i();

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Tile tile = Vars.world.tile(x, y);
                if(tile != null){
                    int tx = read.i();
                    if(tx == -1) break;
                    int ty = read.i();
                    int blockId = read.i();
                    int rotation = read.i();
                    int teamId = read.i();

                    Tile targetTile = Vars.world.tile(tx, ty);
                    if(targetTile != null){
                        targetTile.setBlock(Vars.content.block(blockId), Team.get(teamId), rotation);
                    }
                }
            }
        }
    }

    private void readUnits(arc.util.io.Reads read){
        int count = read.i();
        for(int i = 0; i < count; i++){
            int id = read.i();
            int typeId = read.i();
            float x = read.f();
            float y = read.f();
            float rotation = read.f();
            int teamId = read.i();

            UnitType type = Vars.content.unit(typeId);
            if(type != null){
                Unit unit = type.create(Team.get(teamId));
                unit.set(x, y);
                unit.rotation(rotation);
            }
        }
    }

    private void readBuildings(arc.util.io.Reads read){
        int count = read.i();
        for(int i = 0; i < count; i++){
            int x = read.i();
            int y = read.i();
            int blockId = read.i();
            int rotation = read.i();
            int teamId = read.i();
            float health = read.f();

            Tile tile = Vars.world.tile(x, y);
            if(tile != null){
                tile.setBlock(Vars.content.block(blockId), Team.get(teamId), rotation);
                Building build = tile.build;
                if(build != null){
                    build.health(health);
                }
            }
        }
    }

    private void readPlayers(arc.util.io.Reads read){
        int count = read.i();
        for(int i = 0; i < count; i++){
            String name = mindustry.io.TypeIO.readString(read);
            String uuid = mindustry.io.TypeIO.readString(read);
            int teamId = read.i();
            int unitId = read.i();

            Player player = Groups.player.find(p -> p.uuid().equals(uuid));
            if(player != null){
                player.team(Team.get(teamId));
                if(unitId != -1){
                    Unit unit = Groups.unit.getByID(unitId);
                    if(unit != null){
                        player.unit(unit);
                    }
                }
            }
        }
    }

    private void readState(arc.util.io.Reads read){
        Vars.state.wave = read.i();
        int defaultTeamId = read.i();
        int waveTeamId = read.i();
        Vars.state.gameOver = read.b() == 1;
        int winnerId = read.i();

        Vars.state.rules.defaultTeam = Team.get(defaultTeamId);
        Vars.state.rules.waveTeam = Team.get(waveTeamId);
        Vars.state.won = winnerId == defaultTeamId;
    }
}