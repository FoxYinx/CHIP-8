package fr.yinxfox;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.nio.file.Paths;

public class SoundMaker {

    //Fixme: le son ne se "joue pas" s'il doit être joué pendant un temps très court

    private static final String URL = "src/main/resources/audio/";

    private final MediaPlayer mediaPlayer;
    private final Media media;

    public SoundMaker() {
        this.media = new Media(Paths.get(URL + "buzzer.mp3").toUri().toString());
        this.mediaPlayer = new MediaPlayer(media);
        this.mediaPlayer.setVolume(0.2);
    }

    public void playBuzzer() {
        this.mediaPlayer.play();
    }

    public void stopBuzzer() {
        this.mediaPlayer.stop();
    }

}
