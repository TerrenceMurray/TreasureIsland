package rendering;

import engine.ImageManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class PalmTreeRenderer {

    private static final String BACK_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Back Palm Trees/";
    private static final String FRONT_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Front Palm Trees/";
    private static final int SCALE = 2;
    private static final int TILE = 32;

    private BufferedImage[] backTrees;
    private BufferedImage[] frontTops;
    private BufferedImage trunkStraight;
    private int[][] backTreePlacements;
    private int[][] frontTreePlacements;

    public PalmTreeRenderer() {
        backTrees = new BufferedImage[] {
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Regular 01.png"),
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Regular 02.png"),
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Regular 03.png"),
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Regular 04.png"),
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Left 01.png"),
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Left 02.png"),
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Right 01.png"),
            ImageManager.loadBufferedImage(BACK_BASE + "Back Palm Tree Right 02.png"),
        };
        frontTops = new BufferedImage[] {
            ImageManager.loadBufferedImage(FRONT_BASE + "Front Palm Tree Top 01.png"),
            ImageManager.loadBufferedImage(FRONT_BASE + "Front Palm Tree Top 02.png"),
            ImageManager.loadBufferedImage(FRONT_BASE + "Front Palm Tree Top 03.png"),
            ImageManager.loadBufferedImage(FRONT_BASE + "Front Palm Tree Top 04.png"),
        };

        BufferedImage trunkSheet = ImageManager.loadBufferedImage(FRONT_BASE + "Front Palm Bottom and Grass (32x32).png");
        if (trunkSheet != null) {
            trunkStraight = trunkSheet.getSubimage(0, 0, TILE, TILE);
            trunkSheet.getSubimage(TILE, 0, TILE, TILE);
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

        for (int[] tree : backTreePlacements) {
            int tx = tree[0];
            int groundY = tree[1];
            int variant = tree[2] % backTrees.length;
            BufferedImage img = backTrees[variant];
            if (img == null) continue;
            int w = img.getWidth() * SCALE;
            int h = img.getHeight() * SCALE;
            g.drawImage(img, tx, groundY - h + 16, w, h, null);
        }
    }

    // Front tree placements: {x, groundY, trunkSegments, topVariant}
    public void drawFront(Graphics2D g, int levelWidth) {
        if (frontTreePlacements == null) return;

        for (int[] tree : frontTreePlacements) {
            int tx = tree[0];
            int groundY = tree[1];
            int trunkSegments = tree[2];
            int topVariant = tree[3] % frontTops.length;

            int segH = TILE * SCALE;
            int segW = TILE * SCALE;
            int baseOffset = 16;

            for (int s = 0; s < trunkSegments; s++) {
                BufferedImage seg = trunkStraight;
                if (seg == null) continue;
                int sy = groundY - (s + 1) * segH + baseOffset;
                g.drawImage(seg, tx, sy, segW, segH, null);
            }

            // Canopy on top
            BufferedImage top = frontTops[topVariant];
            if (top != null) {
                int topW = top.getWidth() * SCALE;
                int topH = top.getHeight() * SCALE;
                int topX = tx + segW / 2 - topW / 2;
                int topY = groundY - trunkSegments * segH - topH + baseOffset + 8;
                g.drawImage(top, topX, topY, topW, topH, null);
            }
        }
    }
}
