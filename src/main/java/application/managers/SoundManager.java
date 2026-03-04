package application.managers;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {
    private static SoundManager instance;
    private MediaPlayer bgPlayer;
    private String currentBGM = "";

    // SFX
    private final AudioClip footstep;
    private final AudioClip pop;
    private final AudioClip victory;
    private final AudioClip fuse;
    private final AudioClip booom;
    private final AudioClip count;

    private SoundManager() {
        // Load SFX
        footstep = loadSFX("footstep.mp3");
        pop = loadSFX("pop.mp3");
        victory = loadSFX("victory.mp3");
        fuse = loadSFX("fuse.mp3");
        booom = loadSFX("booom.mp3");
        count = loadSFX("count.mp3");

        // Pre-warm the audio engine to eliminate first-play delay
        if (footstep != null) footstep.play(0.0);
        if (pop != null) pop.play(0.0);
        if (victory != null) victory.play(0.0);
        if (fuse != null) fuse.play(0.0);
        if (booom != null) booom.play(0.0);
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
        if (fileName.equals(currentBGM) && bgPlayer != null) {
            return;
        }

        if (bgPlayer != null) {
            bgPlayer.stop();
            bgPlayer.dispose();
        }

        currentBGM = fileName;
        URL resource = getClass().getResource("/sfx/" + fileName);

        if (resource != null) {
            Media media = new Media(resource.toExternalForm());
            bgPlayer = new MediaPlayer(media);

            bgPlayer.setVolume(0.5);
            bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
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
    public void playFuse() { if (fuse != null) fuse.play(); }
    public void playBoom() { if (booom != null) booom.play(); }
    public void playCount() {if (count != null) count.play(); }
}