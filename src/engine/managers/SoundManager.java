package engine.managers;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.util.HashMap;
import java.util.Random;

/**
    The SoundManager class is a singleton that loads and plays
    sound clips (Week 3 pattern). Clips are stored under friendly
    labels and played by name so gameplay code does not need to
    know file paths.
*/
public class SoundManager {

    private HashMap<String, Clip> clips;
    private Random random;
    private static SoundManager instance;

    private SoundManager() {
        clips = new HashMap<String, Clip>();
        random = new Random();
    }

    /**
        Returns the shared SoundManager instance, creating it on
        first use.
    */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
        Returns the Clip stored under the given title, or null if
        no clip has been loaded with that title.
    */
    public Clip getClip(String title) {
        return clips.get(title);
    }

    /**
        Loads an audio file and returns the resulting Clip. Returns
        null if the file cannot be opened.
    */
    public Clip loadClip(String fileName) {
        AudioInputStream audioIn;
        Clip clip = null;
        try {
            File file = new File(fileName);
            audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL());
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (Exception e) {
            System.err.println("Error opening sound file " + fileName + ": " + e);
        }
        return clip;
    }

    /**
        Loads the given audio file and stores it under the given
        title for later playback by name.
    */
    public void loadClip(String title, String fileName) {
        Clip clip = loadClip(fileName);
        if (clip != null) {
            clips.put(title, clip);
        }
    }

    /**
        Plays the clip stored under the given title from the
        beginning. If looping is true, the clip plays continuously
        until stopped. Calling this while the clip is already
        running first stops it so rapid successive triggers restart
        reliably; Clip.start() on its own can be flaky in that case.
    */
    public void playClip(String title, boolean looping) {
        Clip clip = getClip(title);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            if (looping) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        }
    }

    /**
        Stops playback of the clip stored under the given title.
    */
    public void stopClip(String title) {
        Clip clip = getClip(title);
        if (clip != null) {
            clip.stop();
        }
    }

    /**
        Plays one of a set of numbered variations picked at random.
        For example, playRandomVariation("slime", 10) picks one of
        slime1..slime10. Used to avoid a single identical sound
        repeating on every hit.
    */
    public void playRandomVariation(String prefix, int count) {
        if (count <= 0) return;
        int pick = 1 + random.nextInt(count);
        playClip(prefix + pick, false);
    }

    /**
        Sets the clip's playback volume in decibels. 0 dB is the
        clip's natural level; negative values reduce it (-20 dB is
        a soft background level). Silently ignored if the clip does
        not exist or does not support a master gain control.
    */
    public void setClipVolume(String title, float gainDb) {
        Clip clip = getClip(title);
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), gainDb));
            gain.setValue(clamped);
        } catch (IllegalArgumentException ignored) {
            // Clip does not support MASTER_GAIN
        }
    }
}
