package rendering;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PalmTreeRenderer {

    private static final String BACK_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Back Palm Trees/";
    private static final String FRONT_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Front Palm Trees/";
    private static final int SCALE = 2;
    private static final int TILE = 32;

    private BufferedImage[] backTrees;
    private BufferedImage[] frontTops;
    private BufferedImage trunkStraight;
    private BufferedImage trunkCurveLeft;
    private BufferedImage trunkBase;

    // Tree placement: x position, ground y, trunk height (in segments), type (0=straight, 1=left lean, 2=right lean)
    private int[][] backTreePlacements;
    private int[][] frontTreePlacements;

    public PalmTreeRenderer() {
        backTrees = new BufferedImage[] {
            loadImage(BACK_BASE + "Back Palm Tree Regular 01.png"),
            loadImage(BACK_BASE + "Back Palm Tree Left 01.png"),
            loadImage(BACK_BASE + "Back Palm Tree Right 01.png"),
            loadImage(BACK_BASE + "Back Palm Tree Regular 02.png"),
            loadImage(BACK_BASE + "Back Palm Tree Left 02.png"),
            loadImage(BACK_BASE + "Back Palm Tree Right 02.png"),
        };
        frontTops = new BufferedImage[] {
            loadImage(FRONT_BASE + "Front Palm Tree Top 01.png"),
            loadImage(FRONT_BASE + "Front Palm Tree Top 02.png"),
            loadImage(FRONT_BASE + "Front Palm Tree Top 03.png"),
            loadImage(FRONT_BASE + "Front Palm Tree Top 04.png"),
        };

        // Extract trunk tiles from the tileset
        BufferedImage trunkSheet = loadImage(FRONT_BASE + "Front Palm Bottom and Grass (32x32).png");
        if (trunkSheet != null) {
            trunkStraight = trunkSheet.getSubimage(0, 0, TILE, TILE);
            trunkCurveLeft = trunkSheet.getSubimage(TILE, 0, TILE, TILE);
            trunkBase = trunkStraight;
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load: " + path);
            return null;
        }
    }

    public void setBackPlacements(int[][] placements) {
        this.backTreePlacements = placements;
    }

    public void setFrontPlacements(int[][] placements) {
        this.frontTreePlacements = placements;
    }

    public void drawBack(Graphics2D g, int levelWidth) {
        if (backTreePlacements == null) return;
        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        for (int[] tree : backTreePlacements) {
            int tx = tree[0];
            int groundY = tree[1];
            int variant = tree[2] % backTrees.length;
            BufferedImage img = backTrees[variant];
            if (img == null) continue;
            int w = img.getWidth() * SCALE;
            int h = img.getHeight() * SCALE;
            g.drawImage(img, tx, groundY - h, w, h, null);
        }

        g.setComposite(original);
    }

    public void drawFront(Graphics2D g, int levelWidth) {
        if (frontTreePlacements == null) return;

        for (int[] tree : frontTreePlacements) {
            int tx = tree[0];
            int groundY = tree[1];
            int trunkSegments = tree[2];
            int topVariant = tree[3] % frontTops.length;

            int segH = TILE * SCALE;
            int segW = TILE * SCALE;

            // Draw trunk from ground up — base sits on ground
            for (int s = 0; s < trunkSegments; s++) {
                BufferedImage seg = (s == 0) ? trunkBase : trunkStraight;
                if (seg == null) continue;
                int sy = groundY - (s + 1) * segH;
                g.drawImage(seg, tx, sy, segW, segH, null);
            }

            // Canopy on top of trunk
            BufferedImage top = frontTops[topVariant];
            if (top != null) {
                int topW = top.getWidth() * SCALE;
                int topH = top.getHeight() * SCALE;
                int topX = tx + segW / 2 - topW / 2;
                int topY = groundY - trunkSegments * segH - topH + 8;
                g.drawImage(top, topX, topY, topW, topH, null);
            }
        }
    }
}
