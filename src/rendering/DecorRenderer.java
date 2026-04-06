package rendering;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DecorRenderer {

    private static final String FLAG_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Objects/Flag/";
    private static final int SCALE = 2;

    private BufferedImage[] flagFrames;
    private BufferedImage flagPlatform;
    private int flagFrame;
    private int flagTimer;

    // Decor placements: {x, groundY, type}  type: 0=flag
    private int[][] placements;

    public DecorRenderer() {
        flagFrames = new BufferedImage[9];
        for (int i = 0; i < 9; i++) {
            flagFrames[i] = loadImage(FLAG_BASE + String.format("Flag %02d.png", i + 1));
        }
        flagPlatform = loadImage(FLAG_BASE + "Platform.png");
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load: " + path);
            return null;
        }
    }

    public void setPlacements(int[][] placements) {
        this.placements = placements;
    }

    public void update() {
        flagTimer++;
        if (flagTimer >= 6) {
            flagTimer = 0;
            flagFrame = (flagFrame + 1) % 9;
        }
    }

    public void draw(Graphics2D g) {
        if (placements == null) return;

        for (int[] decor : placements) {
            int dx = decor[0];
            int groundY = decor[1];
            int type = decor[2];

            if (type == 0 && flagFrames[flagFrame] != null) {
                // Flag platform
                if (flagPlatform != null) {
                    int pw = flagPlatform.getWidth() * SCALE;
                    int ph = flagPlatform.getHeight() * SCALE;
                    g.drawImage(flagPlatform, dx - pw / 2, groundY - ph, pw, ph, null);
                }
                // Flag
                int fw = flagFrames[flagFrame].getWidth() * SCALE;
                int fh = flagFrames[flagFrame].getHeight() * SCALE;
                int fx = dx - fw / 2;
                int fy = groundY - fh - (flagPlatform != null ? flagPlatform.getHeight() * SCALE - 8 : 0);
                g.drawImage(flagFrames[flagFrame], fx, fy, fw, fh, null);
            }
        }
    }
}
