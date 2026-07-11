package MindustryOptiFine.ui;

import MindustryOptiFine.recording.*;
import MindustryOptiFine.recording.io.*;
import MindustryOptiFine.playback.*;
import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.text.*;
import java.util.*;

public class ReplayDialog extends BaseDialog{
    private final Recorder recorder = new Recorder();
    private final ReplayServer replayServer = new ReplayServer();
    private Table recordingTable, playbackTable, listTable;
    private Label recordingStatus, playbackTime;
    private Slider progressSlider, speedSlider;
    private Button playButton, pauseButton, stopButton;
    private boolean showRecording = true, showPlayback = false, showList = false;
    private boolean playVisible = true, pauseVisible = false, stopVisible = false;

    public ReplayDialog(){
        super("@replay.title");
        addCloseButton();

        Table tabs = new Table();
        tabs.defaults().size(200f, 64f);
        tabs.button("@replay.record", Icon.refresh, () -> showRecordingTab()).row();
        tabs.button("@replay.play", Icon.play, () -> showPlaybackTab()).row();
        tabs.button("@replay.list", Icon.list, () -> showListTab()).row();
        add(tabs).size(200f).left();

        Table content = new Table();
        recordingTable = createRecordingTable();
        playbackTable = createPlaybackTable();
        listTable = createListTable();

        content.add(recordingTable).grow();
        content.add(playbackTable).grow();
        content.add(listTable).grow();

        add(content).grow();

        showRecordingTab();
    }

    private void showRecordingTab(){
        showRecording = true;
        showPlayback = false;
        showList = false;
    }

    private void showPlaybackTab(){
        showRecording = false;
        showPlayback = true;
        showList = false;
    }

    private void showListTab(){
        showRecording = false;
        showPlayback = false;
        showList = true;
        refreshReplayList();
    }

    private Table createRecordingTable(){
        Table table = new Table();
        table.defaults().pad(4f);

        table.add("@replay.record.status").left();
        recordingStatus = table.add("@replay.record.idle").color(Color.green).left().get();
        table.row();

        table.add("@replay.record.info").left().row();

        Table infoTable = new Table();
        infoTable.add("@replay.record.frames").left();
        infoTable.add(new Label(() -> recorder.getFrameCount() + "")).left().row();
        table.add(infoTable).left().row();

        table.row();

        table.button("@replay.record.start", Icon.play, () -> {
            if(!recorder.isRecording()){
                Fi replayDir = Vars.dataDirectory.child("RePlay");
                if(!replayDir.exists()){
                    replayDir.mkdirs();
                }
                Fi file = replayDir.child(System.currentTimeMillis() + ".mrpl");
                recorder.startRecording(file);
                recordingStatus.setText("@replay.record.recording");
                recordingStatus.setColor(Color.red);
            }
        }).size(200f, 64f).row();

        table.button("@replay.record.stop", Icon.cancel, () -> {
            if(recorder.isRecording()){
                recorder.stopRecording();
                recordingStatus.setText("@replay.record.idle");
                recordingStatus.setColor(Color.green);
            }
        }).size(200f, 64f).row();

        table.button("@replay.record.cancel", Icon.cancel, () -> {
            if(recorder.isRecording()){
                recorder.stopRecording();
                recordingStatus.setText("@replay.record.idle");
                recordingStatus.setColor(Color.green);
            }
        }).size(200f, 64f).row();

        table.visible(() -> showRecording);

        return table;
    }

    private Table createPlaybackTable(){
        Table table = new Table();
        table.defaults().pad(4f);

        table.add("@replay.play.info").left().row();

        playbackTime = table.add("00:00 / 00:00").left().get();
        table.row();

        progressSlider = new Slider(0f, 100f, 1f, false);
        progressSlider.changed(() -> {
            if(replayServer.isPlaying()){
                replayServer.jumpToTick((int)(progressSlider.getValue()));
            }
        });
        table.add(progressSlider).growX().height(32f).row();

        Table controlTable = new Table();
        controlTable.defaults().size(64f, 64f);

        stopButton = controlTable.button(Icon.cancel, () -> {
            replayServer.stop();
            updatePlaybackUI();
        }).visible(() -> stopVisible).get();

        playButton = controlTable.button(Icon.play, () -> {
            if(!replayServer.isPlaying()){
                replayServer.start();
            }
            updatePlaybackUI();
        }).visible(() -> playVisible).get();

        pauseButton = controlTable.button(Icon.refresh, () -> {
            replayServer.pause();
            updatePlaybackUI();
        }).visible(() -> pauseVisible).get();

        table.add(controlTable).row();

        table.add("@replay.play.speed").left().row();
        speedSlider = new Slider(0.1f, 5f, 0.1f, false);
        speedSlider.setValue(1f);
        speedSlider.changed(() -> {
            if(replayServer.getTimer() != null){
                replayServer.getTimer().setTickRate(speedSlider.getValue());
            }
        });
        table.add(speedSlider).growX().height(32f).row();

        table.add(new Label(() -> speedSlider.getValue() + "x")).left().row();

        table.row();

        table.button("@replay.play.load", Icon.download, () -> {
            FileChooser.open("mrpl").submit(file -> {
                replayServer.loadReplay(file);
                updatePlaybackUI();
            });
        }).size(200f, 64f).row();

        Events.run(EventType.Trigger.update, () -> {
            if(replayServer.isPlaying()){
                updatePlaybackUI();
            }
        });

        table.visible(() -> showPlayback);

        return table;
    }

    private Table createListTable(){
        Table table = new Table();
        table.defaults().pad(4f);

        table.add("@replay.list.title").left().row();

        Table list = new Table();
        list.defaults().growX().height(64f).pad(2f);
        table.add(list).grow().row();

        table.visible(() -> showList);

        return table;
    }

    private void refreshReplayList(){
        Table list = (Table) listTable.getChildren().get(1);
        list.clear();

        Fi dir = Vars.dataDirectory.child("RePlay");
        if(!dir.exists()){
            dir.mkdirs();
            return;
        }

        Fi[] files = dir.list();
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        for(Fi file : files){
            if(file.extension().equals("mrpl")){
                list.button(file.nameWithoutExtension(), () -> {
                    replayServer.loadReplay(file);
                    showPlaybackTab();
                }).growX().height(64f).row();
            }
        }

        if(files.length == 0){
            list.add("@replay.list.empty").left().row();
        }
    }

    private void updatePlaybackUI(){
        if(replayServer.getMeta() != null){
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String current = sdf.format(new Date((long)(replayServer.getCurrentTick() / 60f * 1000)));
            String duration = sdf.format(new Date((long)(replayServer.getTotalFrames() / 60f * 1000)));
            playbackTime.setText(current + " / " + duration);

            progressSlider.setValue(replayServer.getCurrentTick());
        }

        playVisible = !replayServer.isPlaying();
        pauseVisible = replayServer.isPlaying() && !replayServer.isPaused();
        stopVisible = replayServer.isPlaying();
    }
}