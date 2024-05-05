package fr.yinxfox;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundMaker {

    private static final String URL = "C:\\Users\\tolle\\IdeaProjects\\CHIP-8\\src\\main\\resources\\audio";

    private final MediaPlayer mediaPlayer;
    private final Media media;

    public SoundMaker() {
        this.media = new Media(URL + "buzzer.mp3");
        this.mediaPlayer = new MediaPlayer(media);
    }

    public void playBuzzer() {
        this.mediaPlayer.play();
    }

    public void stopBuzzer() {
        this.mediaPlayer.stop();
    }

}
