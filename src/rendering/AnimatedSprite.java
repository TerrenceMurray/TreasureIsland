package rendering;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnimatedSprite {

    private Map<String, BufferedImage[]> animations = new HashMap<>();
    private String currentState;
    private int currentFrame;
    private int frameTimer;
    private int frameDuration;
    private boolean flipped;

    public AnimatedSprite(int frameDuration) {
        this.frameDuration = frameDuration;
    }

    public void loadState(String state, String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Animation folder not found: " + folderPath);
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));
        if (files == null || files.length == 0) return;

        java.util.Arrays.sort(files);
        BufferedImage[] frames = new BufferedImage[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                frames[i] = ImageIO.read(files[i]);
            } catch (IOException e) {
                System.err.println("Could not load frame: " + files[i]);
            }
        }
        animations.put(state, frames);

        if (currentState == null) {
            currentState = state;
        }
    }

    public void loadStateFromFiles(String state, String folderPath, String prefix, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            String filename = String.format("%s/%s %02d.png", folderPath, prefix, i + 1);
            try {
                frames[i] = ImageIO.read(new File(filename));
            } catch (IOException e) {
                System.err.println("Could not load frame: " + filename);
            }
        }
        animations.put(state, frames);
        if (currentState == null) currentState = state;
    }

    public void setState(String state) {
        if (state.equals(currentState)) return;
        if (!animations.containsKey(state)) return;
        currentState = state;
        currentFrame = 0;
        frameTimer = 0;
    }

    public void update() {
        BufferedImage[] frames = animations.get(currentState);
        if (frames == null) return;

        frameTimer++;
        if (frameTimer >= frameDuration) {
            frameTimer = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    public void draw(Graphics2D g, int x, int y, int width, int height) {
        BufferedImage[] frames = animations.get(currentState);
        if (frames == null || frames[currentFrame] == null) return;

        if (flipped) {
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
