package rendering;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScrollingBackground {

    private static final String ASSET_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/";

    private BufferedImage bgImage;
    private BufferedImage bigClouds;
    private BufferedImage[] smallClouds;
    private BufferedImage[] waterReflectBig;
    private BufferedImage[] waterReflectSmall;
    private int screenWidth;
    private int screenHeight;
    private float cloudDrift;
    private int waterFrame;
    private int waterTimer;

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
        waterReflectBig = new BufferedImage[] {
            loadImage(ASSET_BASE + "Water Reflect Big 01.png"),
            loadImage(ASSET_BASE + "Water Reflect Big 02.png"),
            loadImage(ASSET_BASE + "Water Reflect Big 03.png"),
            loadImage(ASSET_BASE + "Water Reflect Big 04.png"),
        };
        waterReflectSmall = new BufferedImage[] {
            loadImage(ASSET_BASE + "Water Reflect Small 01.png"),
            loadImage(ASSET_BASE + "Water Reflect Small 02.png"),
            loadImage(ASSET_BASE + "Water Reflect Small 03.png"),
            loadImage(ASSET_BASE + "Water Reflect Small 04.png"),
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
        waterTimer++;
        if (waterTimer >= 12) {
            waterTimer = 0;
            waterFrame = (waterFrame + 1) % 4;
        }
    }

    public void draw(Graphics2D g, float cameraX, int levelWidth) {
        if (bgImage != null) {
            float parallax = cameraX * 0.2f;
            int startX = (int) (-parallax % screenWidth);
            if (startX > 0) startX -= screenWidth;
            for (int x = startX; x < screenWidth; x += screenWidth) {
                g.drawImage(bgImage, x, 0, screenWidth, screenHeight, null);
            }
        }

        // Big clouds — horizon strip
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

        // Small clouds
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

        // Water reflections in the lower half
        int waterY = screenHeight * 2 / 3;
        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

        if (waterReflectBig[waterFrame] != null) {
            int rw = waterReflectBig[waterFrame].getWidth() * 2;
            int rh = waterReflectBig[waterFrame].getHeight() * 2;
            float parallax = cameraX * 0.05f;
            g.drawImage(waterReflectBig[waterFrame], (int)(120 - parallax), waterY, rw, rh, null);
            g.drawImage(waterReflectBig[(waterFrame + 2) % 4], (int)(500 - parallax), waterY + 30, rw, rh, null);
        }

        if (waterReflectSmall[waterFrame] != null) {
            int rw = waterReflectSmall[waterFrame].getWidth() * 2;
            int rh = waterReflectSmall[waterFrame].getHeight() * 2;
            float parallax = cameraX * 0.03f;
            g.drawImage(waterReflectSmall[waterFrame], (int)(300 - parallax), waterY + 15, rw, rh, null);
            g.drawImage(waterReflectSmall[(waterFrame + 1) % 4], (int)(700 - parallax), waterY + 45, rw, rh, null);
            g.drawImage(waterReflectSmall[(waterFrame + 3) % 4], (int)(50 - parallax), waterY + 50, rw, rh, null);
        }

        g.setComposite(original);
    }
}
