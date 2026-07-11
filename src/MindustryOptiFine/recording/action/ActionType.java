package MindustryOptiFine.recording.action;

public enum ActionType{
    nextTick,
    tileChange,
    unitCreate,
    unitDestroy,
    unitMove,
    playerJoin,
    playerLeave,
    playerData,
    wave,
    gameOver,
    snapshot,
    chat,
    buildDamage,
    unitDamage,
    configChange,
    buildTeamChange,
    sectorCapture,
    keyframe;

    public static final ActionType[] all = values();
}