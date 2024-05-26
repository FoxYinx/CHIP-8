package fr.yinxfox.emulator;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.nio.file.Paths;

public class SoundMaker {

    //fixme: sound is not being played if sound timer is too small

    private static final String URL = "src/main/resources/audio/";

    private final MediaPlayer mediaPlayer;
    private int pitch;

    public SoundMaker() {
        Media media = new Media(Paths.get(URL + "beep.mp3").toUri().toString());
        this.mediaPlayer = new MediaPlayer(media);
        this.mediaPlayer.setVolume(0.2);
        this.pitch = 64;
    }

    public void playBuzzer() {
        this.mediaPlayer.play();
    }

    public void stopBuzzer() {
        this.mediaPlayer.stop();
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }
}
