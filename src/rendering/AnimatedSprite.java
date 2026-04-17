package rendering;

import engine.ImageManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
    The AnimatedSprite class manages a set of named animation states
    (for example "idle", "run", "attack") for a single entity. Each
    state is a sequence of frames. Frame advancement is counter-based:
    the current frame is changed after a fixed number of update() calls
    rather than by elapsed time.
*/
public class AnimatedSprite {

    // === Animation data ===
    // Maps a state name (e.g. "idle") to the array of frame images.
    private Map<String, BufferedImage[]> animations = new HashMap<>();

    // === Current playback state ===
    private String currentState;
    private int currentFrame;
    private int frameTimer;
    // Number of update() calls each frame is displayed for before advancing.
    private int frameDuration;

    // === Rendering options ===
    // When true, the sprite is mirrored horizontally when drawn (e.g. facing left).
    private boolean flipped;

    /**
        Creates a new AnimatedSprite. The frameDuration is the number
        of update() calls each frame will be displayed for.
    */
    public AnimatedSprite(int frameDuration) {
        this.frameDuration = frameDuration;
    }

    /**
        Loads an animation state by reading every PNG file in the
        given folder (sorted by filename) as a frame.
    */
    public void loadState(String state, String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Animation folder not found: " + folderPath);
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));
        if (files == null || files.length == 0) return;

        // Sort by filename so numbered frames (e.g. 01, 02, 03) play in order.
        java.util.Arrays.sort(files);
        BufferedImage[] frames = new BufferedImage[files.length];
        for (int i = 0; i < files.length; i++) {
            frames[i] = ImageManager.loadBufferedImage(files[i].getPath());
        }
        animations.put(state, frames);

        // If this is the first state loaded, make it the active one.
        if (currentState == null) {
            currentState = state;
        }
    }

    /**
        Loads an animation state from a numbered set of files using the
        pattern "prefix NN.png" (for example "Run 01.png" through
        "Run 08.png").
    */
    public void loadStateFromFiles(String state, String folderPath, String prefix, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            // Files are 1-indexed and zero-padded to two digits.
            String filename = String.format("%s/%s %02d.png", folderPath, prefix, i + 1);
            frames[i] = ImageManager.loadBufferedImage(filename);
        }
        animations.put(state, frames);
        if (currentState == null) currentState = state;
    }

    /**
        Switches to a different animation state and resets the frame
        counter. Does nothing if the state is already active or has
        not been loaded.
    */
    public void setState(String state) {
        if (state.equals(currentState)) return;
        if (!animations.containsKey(state)) return;
        currentState = state;
        currentFrame = 0;
        frameTimer = 0;
    }

    /**
        Advances the frame counter for the current state. Call once
        per game update.
    */
    public void update() {
        BufferedImage[] frames = animations.get(currentState);
        if (frames == null) return;

        frameTimer++;
        if (frameTimer >= frameDuration) {
            frameTimer = 0;
            // Wrap back to the first frame so animations loop.
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    /**
        Draws the current frame at the given position and size. If
        flipped is true, the image is mirrored horizontally.
    */
    public void draw(Graphics2D g, int x, int y, int width, int height) {
        BufferedImage[] frames = animations.get(currentState);
        if (frames == null || frames[currentFrame] == null) return;

        if (flipped) {
            // Shift origin to the right edge and pass negative width so the image
            // is mirrored horizontally in place.
            g.drawImage(frames[currentFrame], x + width, y, -width, height, null);
        } else {
            g.drawImage(frames[currentFrame], x, y, width, height, null);
        }
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public String getState() { return currentState; }
}
