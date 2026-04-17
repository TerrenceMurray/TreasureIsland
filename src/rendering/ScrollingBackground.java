package rendering;

import engine.ImageManager;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
    The ScrollingBackground class draws the parallax sky scene for
    the Palm Tree Island levels. It combines a tiled base image with
    big and small clouds and animated water reflections in the lower
    half of the screen. Cloud drift is independent of the camera so
    the sky keeps moving even when the player stands still.
*/
public class ScrollingBackground {

    // === Asset paths ===
    private static final String ASSET_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/";

    // === Tuning constants ===
    // How fast the clouds drift sideways per update tick (pixels of phase).
    private static final float CLOUD_DRIFT_SPEED = 0.3f;
    // Update ticks between water reflection frame changes.
    private static final int WATER_FRAME_DURATION = 12;
    // Number of water reflection frames in the animation.
    private static final int WATER_FRAME_COUNT = 4;

    // Parallax factors: smaller = layer moves less with the camera (feels farther away).
    private static final float BG_PARALLAX = 0.2f;
    private static final float BIG_CLOUD_PARALLAX = 0.1f;
    // Fraction of cloud-drift applied to big clouds so they move slower than small ones.
    private static final float BIG_CLOUD_DRIFT_FACTOR = 0.15f;
    private static final float SMALL_CLOUD_PARALLAX = 0.05f;
    private static final float WATER_BIG_PARALLAX = 0.05f;
    private static final float WATER_SMALL_PARALLAX = 0.03f;

    // Source sprites are drawn at 2x their pixel size to match the rest of the game.
    private static final int SCALE = 2;
    // Extra horizontal gap between repeating small cloud copies, in pixels.
    private static final int SMALL_CLOUD_SPACING_GAP = 250;
    // Water reflection layer sits this many pixels from the bottom of the screen.
    private static final int WATER_Y_OFFSET_FROM_BOTTOM = 120;
    // Alpha used when compositing water reflections over the background.
    private static final float WATER_ALPHA = 0.6f;

    // === Sprites ===
    private BufferedImage bgImage;
    private BufferedImage bigClouds;
    private BufferedImage[] smallClouds;
    private BufferedImage[] waterReflectBig;
    private BufferedImage[] waterReflectSmall;

    // === Screen dimensions ===
    private int screenWidth;
    private int screenHeight;

    // === Animation state ===
    // Camera-independent cloud scroll offset; keeps advancing every tick.
    private float cloudDrift;
    private int waterFrame;
    private int waterTimer;

    /**
        Loads the base background image along with the cloud and
        water reflection sprites used for the parallax layers.
    */
    public ScrollingBackground(String imagePath, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        bgImage = ImageManager.loadBufferedImage(imagePath);
        bigClouds = ImageManager.loadBufferedImage(ASSET_BASE + "Big Clouds.png");
        smallClouds = new BufferedImage[] {
            ImageManager.loadBufferedImage(ASSET_BASE + "Small Cloud 1.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Small Cloud 2.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Small Cloud 3.png")
        };
        waterReflectBig = new BufferedImage[] {
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Big 01.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Big 02.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Big 03.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Big 04.png"),
        };
        waterReflectSmall = new BufferedImage[] {
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Small 01.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Small 02.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Small 03.png"),
            ImageManager.loadBufferedImage(ASSET_BASE + "Water Reflect Small 04.png"),
        };
    }

    /**
        Advances the cloud drift and the water reflection animation.
        Call once per game update.
    */
    public void update() {
        cloudDrift += CLOUD_DRIFT_SPEED;
        waterTimer++;
        if (waterTimer >= WATER_FRAME_DURATION) {
            waterTimer = 0;
            waterFrame = (waterFrame + 1) % WATER_FRAME_COUNT;
        }
    }

    /**
        Draws the background, cloud layers, and water reflections.
        Each layer scrolls at a different fraction of the camera
        position to produce a parallax effect.
    */
    public void draw(Graphics2D g, float cameraX, int levelWidth) {
        // Base background: slowest parallax. Tile horizontally so it covers the view.
        if (bgImage != null) {
            float parallax = cameraX * BG_PARALLAX;
            // Modulo wrap the offset so startX is always in the range (-screenWidth, 0].
            int startX = (int) (-parallax % screenWidth);
            if (startX > 0) startX -= screenWidth;
            for (int x = startX; x < screenWidth; x += screenWidth) {
                g.drawImage(bgImage, x, 0, screenWidth, screenHeight, null);
            }
        }

        // Big clouds along the horizon. Subtract cloudDrift so the clouds drift
        // in the same direction as the small clouds below.
        if (bigClouds != null) {
            int cw = bigClouds.getWidth() * SCALE;
            int ch = bigClouds.getHeight() * SCALE;
            int horizonY = screenHeight / 2;
            float parallax = cameraX * BIG_CLOUD_PARALLAX - cloudDrift * BIG_CLOUD_DRIFT_FACTOR;
            // Same wrap trick as above so the tiled clouds seamlessly repeat.
            int startX = -((int) parallax % cw);
            if (startX > 0) startX -= cw;
            for (int x = startX; x < screenWidth; x += cw) {
                // Centre the strip vertically on the horizon.
                g.drawImage(bigClouds, x, horizonY - ch / 2, cw, ch, null);
            }
        }

        // Small clouds — three variants at different heights and speeds.
        float[] speeds = {0.4f, 0.3f, 0.5f};
        int[] heights = {30, 70, 50};          // y positions on screen for each variant
        int[] offsets = {0, 300, 150};         // initial horizontal phase offsets
        for (int i = 0; i < smallClouds.length; i++) {
            if (smallClouds[i] == null) continue;
            int cw = smallClouds[i].getWidth() * SCALE;
            int ch = smallClouds[i].getHeight() * SCALE;
            int spacing = cw + SMALL_CLOUD_SPACING_GAP;
            float pos = offsets[i] + cloudDrift * speeds[i] - cameraX * SMALL_CLOUD_PARALLAX;
            // Force pos into [0, spacing) so the loop always starts just off-screen left.
            pos = ((pos % spacing) + spacing) % spacing;
            for (int x = (int) pos - spacing; x < screenWidth + cw; x += spacing) {
                g.drawImage(smallClouds[i], x, heights[i], cw, ch, null);
            }
        }

        // Water reflections sit in the bottom strip and are drawn semi-transparent.
        int waterY = screenHeight - WATER_Y_OFFSET_FROM_BOTTOM;
        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WATER_ALPHA));

        if (waterReflectBig[waterFrame] != null) {
            int rw = waterReflectBig[waterFrame].getWidth() * SCALE;
            int rh = waterReflectBig[waterFrame].getHeight() * SCALE;
            float parallax = cameraX * WATER_BIG_PARALLAX;
            // Two copies at different x/y positions and phase-shifted frames for variety.
            g.drawImage(waterReflectBig[waterFrame], (int)(80 - parallax), waterY, rw, rh, null);
            g.drawImage(waterReflectBig[(waterFrame + 2) % WATER_FRAME_COUNT], (int)(640 - parallax), waterY + 15, rw, rh, null);
        }

        if (waterReflectSmall[waterFrame] != null) {
            int rw = waterReflectSmall[waterFrame].getWidth() * SCALE;
            int rh = waterReflectSmall[waterFrame].getHeight() * SCALE;
            float parallax = cameraX * WATER_SMALL_PARALLAX;
            // Three copies staggered in x and y, each showing a different animation frame
            // so the ripples do not all move in lockstep.
            g.drawImage(waterReflectSmall[waterFrame], (int)(360 - parallax), waterY + 10, rw, rh, null);
            g.drawImage(waterReflectSmall[(waterFrame + 1) % WATER_FRAME_COUNT], (int)(880 - parallax), waterY + 25, rw, rh, null);
            g.drawImage(waterReflectSmall[(waterFrame + 3) % WATER_FRAME_COUNT], (int)(220 - parallax), waterY + 30, rw, rh, null);
        }

        g.setComposite(original);
    }
}
