package application.managers;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {
    private static SoundManager instance;
    private MediaPlayer bgPlayer;
    private String currentBGM = ""; // Tracks the current track to prevent 60FPS restarting

    // SFX
    private final AudioClip footstep;
    private final AudioClip pop;
    private final AudioClip victory;

    private SoundManager() {
        // Load SFX and reduce latency by pre-loading
        footstep = loadSFX("footstep.mp3");
        pop = loadSFX("pop.mp3");
        victory = loadSFX("victory.mp3");

        // Pre-warm the audio engine by playing a silent clip
        // This forces JavaFX to initialize the audio thread immediately, removing first-play delay
        if (footstep != null) footstep.play(0.0);
        if (pop != null) pop.play(0.0);
        if (victory != null) victory.play(0.0);
    }

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    private AudioClip loadSFX(String fileName) {
        URL resource = getClass().getResource("/sfx/" + fileName);
        if (resource != null) {
            return new AudioClip(resource.toExternalForm());
        }
        return null;
    }

    public void playBGM(String fileName) {
        // Prevent restarting the media player if the same song is already playing
        if (fileName.equals(currentBGM) && bgPlayer != null) {
            return;
        }

        // Stop current music before starting new one
        if (bgPlayer != null) {
            bgPlayer.stop();
            bgPlayer.dispose(); // Release resources to prevent memory leaks
        }

        currentBGM = fileName;
        URL resource = getClass().getResource("/sfx/" + fileName);

        if (resource != null) {
            Media media = new Media(resource.toExternalForm());
            bgPlayer = new MediaPlayer(media);

            // Decrease background volume by half (0.0 to 1.0)
            bgPlayer.setVolume(0.5);
            bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            // Directly play instead of waiting for setOnReady to avoid event listener delays
            bgPlayer.play();
        }
    }

    public void playFootstep() {
        if (footstep != null && !footstep.isPlaying()) {
            footstep.play();
        }
    }

    public void playPop() { if (pop != null) pop.play(); }
    public void playVictory() { if (victory != null) victory.play(); }
}