package MindustryOptiFine.recording.action;

import arc.func.*;
import arc.struct.*;

public class ActionRegistry{
    private static final IntMap<Prov<Action>> providers = new IntMap<>();

    static{
        register(ActionType.nextTick, ActionNextTick::new);
        register(ActionType.tileChange, ActionTileChange::new);
        register(ActionType.unitCreate, ActionUnitCreate::new);
        register(ActionType.unitDestroy, ActionUnitDestroy::new);
        register(ActionType.unitMove, ActionUnitMove::new);
        register(ActionType.playerJoin, ActionPlayerJoin::new);
        register(ActionType.playerLeave, ActionPlayerLeave::new);
        register(ActionType.playerData, ActionPlayerData::new);
        register(ActionType.wave, ActionWave::new);
        register(ActionType.gameOver, ActionGameOver::new);
        register(ActionType.snapshot, ActionSnapshot::new);
        register(ActionType.chat, ActionChat::new);
        register(ActionType.buildDamage, ActionBuildDamage::new);
        register(ActionType.unitDamage, ActionUnitDamage::new);
        register(ActionType.configChange, ActionConfigChange::new);
        register(ActionType.buildTeamChange, ActionBuildTeamChange::new);
        register(ActionType.sectorCapture, ActionSectorCapture::new);
        register(ActionType.keyframe, ActionKeyframe::new);
    }

    public static void register(ActionType type, Prov<Action> provider){
        providers.put(type.ordinal(), provider);
    }

    public static Action create(ActionType type){
        Prov<Action> provider = providers.get(type.ordinal());
        return provider == null ? null : provider.get();
    }

    public static Action create(int ordinal){
        Prov<Action> provider = providers.get(ordinal);
        return provider == null ? null : provider.get();
    }

    public static Seq<ActionType> getRegisteredTypes(){
        Seq<ActionType> types = new Seq<>();
        for(ActionType type : ActionType.all){
            if(providers.containsKey(type.ordinal())){
                types.add(type);
            }
        }
        return types;
    }
}