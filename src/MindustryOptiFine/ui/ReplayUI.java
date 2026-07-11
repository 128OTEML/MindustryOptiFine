package MindustryOptiFine.ui;

import MindustryOptiFine.recording.*;
import MindustryOptiFine.playback.*;
import arc.*;
import arc.files.*;
import arc.input.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType.*;

public class ReplayUI{
    private static Recorder recorder;
    private static ReplayServer replayServer;
    private static RecordingIndicator indicator;
    private static ReplayDialog dialog;

    public static void init(){
        recorder = new Recorder();
        replayServer = new ReplayServer();
        indicator = new RecordingIndicator(recorder);

        Events.run(Trigger.update, () -> {
            if(replayServer.isPlaying()){
                replayServer.update();
            }
        });
    }

    public static void toggleRecording(){
        if(recorder.isRecording()){
            recorder.stopRecording();
        }else{
            Fi replayDir = Vars.dataDirectory.child("RePlay");
            if(!replayDir.exists()){
                replayDir.mkdirs();
            }
            Fi file = replayDir.child(System.currentTimeMillis() + ".mrpl");
            recorder.startRecording(file);
        }
    }

    public static void showDialog(){
        if(dialog == null){
            dialog = new ReplayDialog();
        }
        dialog.show();
    }

    public static Recorder getRecorder(){
        return recorder;
    }

    public static ReplayServer getReplayServer(){
        return replayServer;
    }

    public static void dispose(){
        if(indicator != null){
            indicator.dispose();
        }
        if(dialog != null){
            dialog.hide();
            dialog = null;
        }
        if(replayServer != null){
            replayServer.stop();
        }
        if(recorder != null){
            recorder.stopRecording();
        }
    }
}