package rendering.decor;

import engine.managers.ImageManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
    The DecorRenderer class draws animated decoration items, such
    as waving flags planted on platforms. Placements are provided
    externally by the level and rendered behind or on top of the
    world as needed.
*/
public class DecorRenderer {

    // === Asset paths and scale ===
    private static final String FLAG_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Objects/Flag/";
    // Source sprites are drawn at 2x their pixel size to match the rest of the game.
    private static final int SCALE = 2;

    // === Flag animation tuning ===
    // Total number of frames in the flag waving animation.
    private static final int FLAG_FRAME_COUNT = 9;
    // Update ticks between flag frame changes.
    private static final int FLAG_FRAME_DURATION = 6;
    // Pixels the flag overlaps down into its platform so it doesn't appear to float.
    private static final int FLAG_PLATFORM_OVERLAP = 8;

    // === Decor type identifiers ===
    private static final int TYPE_FLAG = 0;

    // === Sprites ===
    private BufferedImage[] flagFrames;
    private BufferedImage flagPlatform;

    // === Animation state ===
    private int flagFrame;
    private int flagTimer;

    // === Placements ===
    // Each entry is {x, groundY, type} where type 0 is a flag.
    private int[][] placements;

    /**
        Loads the flag animation frames and its small platform base.
    */
    public DecorRenderer() {
        flagFrames = new BufferedImage[FLAG_FRAME_COUNT];
        for (int i = 0; i < FLAG_FRAME_COUNT; i++) {
            // Flag files are named "Flag 01.png" ... "Flag 09.png".
            flagFrames[i] = ImageManager.loadBufferedImage(FLAG_BASE + String.format("Flag %02d.png", i + 1));
        }
        flagPlatform = ImageManager.loadBufferedImage(FLAG_BASE + "Platform.png");
    }

    /**
        Sets the array of decor placements. Each entry is
        {x, groundY, type} where type 0 is a flag.
    */
    public void setPlacements(int[][] placements) {
        this.placements = placements;
    }

    /**
        Advances the flag animation. Call once per game update.
    */
    public void update() {
        flagTimer++;
        if (flagTimer >= FLAG_FRAME_DURATION) {
            flagTimer = 0;
            flagFrame = (flagFrame + 1) % FLAG_FRAME_COUNT;
        }
    }

    /**
        Draws every configured decoration item at its placement.
    */
    public void draw(Graphics2D g) {
        if (placements == null) return;

        for (int[] decor : placements) {
            int dx = decor[0];
            int groundY = decor[1];
            int type = decor[2];

            if (type == TYPE_FLAG && flagFrames[flagFrame] != null) {
                // Small platform base, centred on dx and sitting on the ground line.
                if (flagPlatform != null) {
                    int pw = flagPlatform.getWidth() * SCALE;
                    int ph = flagPlatform.getHeight() * SCALE;
                    g.drawImage(flagPlatform, dx - pw / 2, groundY - ph, pw, ph, null);
                }
                // Flag, centred on dx and sitting just above the platform with a small overlap
                // so the flagpole visually connects to the base.
                int fw = flagFrames[flagFrame].getWidth() * SCALE;
                int fh = flagFrames[flagFrame].getHeight() * SCALE;
                int fx = dx - fw / 2;
                int fy = groundY - fh - (flagPlatform != null ? flagPlatform.getHeight() * SCALE - FLAG_PLATFORM_OVERLAP : 0);
                g.drawImage(flagFrames[flagFrame], fx, fy, fw, fh, null);
            }
        }
    }
}
