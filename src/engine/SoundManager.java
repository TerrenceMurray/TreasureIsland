package engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.HashMap;

// Singleton sound manager (Week 3 pattern).
// Clips are stored under friendly labels and played by name so gameplay
// code doesn't need to know file paths.
public class SoundManager {

    private HashMap<String, Clip> clips;
    private static SoundManager instance;

    private SoundManager() {
        clips = new HashMap<String, Clip>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public Clip getClip(String title) {
        return clips.get(title);
    }

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

    public void loadClip(String title, String fileName) {
        Clip clip = loadClip(fileName);
        if (clip != null) {
            clips.put(title, clip);
        }
    }

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

    public void stopClip(String title) {
        Clip clip = getClip(title);
        if (clip != null) {
            clip.stop();
        }
    }
}
