package rendering;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScrollingBackground {

    private static final String ASSET_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/";

    private BufferedImage bgImage;
    private BufferedImage bigClouds;
    private BufferedImage[] smallClouds;
    private int screenWidth;
    private int screenHeight;
    private float cloudDrift;

    public ScrollingBackground(String imagePath, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        bgImage = loadImage(imagePath);
        bigClouds = loadImage(ASSET_BASE + "Big Clouds.png");
        smallClouds = new BufferedImage[] {
            loadImage(ASSET_BASE + "Small Cloud 1.png"),
            loadImage(ASSET_BASE + "Small Cloud 2.png"),
            loadImage(ASSET_BASE + "Small Cloud 3.png")
        };
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load: " + path);
            return null;
        }
    }

    public void update() {
        cloudDrift += 0.3f;
    }

    public void draw(Graphics2D g, float cameraX, int levelWidth) {
        // Base background
        if (bgImage != null) {
            float parallax = cameraX * 0.2f;
            int startX = (int) (-parallax % screenWidth);
            if (startX > 0) startX -= screenWidth;
            for (int x = startX; x < screenWidth; x += screenWidth) {
                g.drawImage(bgImage, x, 0, screenWidth, screenHeight, null);
            }
        }

        // Big clouds — horizon strip, slow parallax tiled across screen
        if (bigClouds != null) {
            int cw = bigClouds.getWidth() * 2;
            int ch = bigClouds.getHeight() * 2;
            int horizonY = screenHeight / 2;
            float parallax = cameraX * 0.1f + cloudDrift * 0.15f;
            int startX = -((int) parallax % cw);
            for (int x = startX; x < screenWidth; x += cw) {
                g.drawImage(bigClouds, x, horizonY - ch / 2, cw, ch, null);
            }
        }

        // Small clouds drifting in the sky
        float[] speeds = {0.4f, 0.3f, 0.5f};
        int[] heights = {30, 70, 50};
        int[] offsets = {0, 300, 150};
        for (int i = 0; i < smallClouds.length; i++) {
            if (smallClouds[i] == null) continue;
            int cw = smallClouds[i].getWidth() * 2;
            int ch = smallClouds[i].getHeight() * 2;
            int spacing = cw + 250;

            float pos = offsets[i] + cloudDrift * speeds[i] - cameraX * 0.05f;
            pos = ((pos % spacing) + spacing) % spacing;

            for (int x = (int) pos - spacing; x < screenWidth + cw; x += spacing) {
                g.drawImage(smallClouds[i], x, heights[i], cw, ch, null);
            }
        }
    }
}
