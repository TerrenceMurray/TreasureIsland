package engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.HashMap;

/**
    The SoundManager class is a singleton that loads and plays
    sound clips (Week 3 pattern). Clips are stored under friendly
    labels and played by name so gameplay code does not need to
    know file paths.
*/
public class SoundManager {

    private HashMap<String, Clip> clips;
    private static SoundManager instance;

    private SoundManager() {
        clips = new HashMap<String, Clip>();
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
        until stopped.
    */
    public void playClip(String title, boolean looping) {
        Clip clip = getClip(title);
        if (clip != null) {
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
}
