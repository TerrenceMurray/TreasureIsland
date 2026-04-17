package rendering;

import engine.ImageManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
    The PalmTreeRenderer class draws palm trees in two layers to
    give the scene a sense of depth. Back trees are drawn behind
    the terrain as silhouettes, while front trees are drawn in front
    with a stacked trunk and a canopy on top.
*/
public class PalmTreeRenderer {

    // === Asset paths ===
    private static final String BACK_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Back Palm Trees/";
    private static final String FRONT_BASE = "assets/Treasure Hunters/Palm Tree Island/Sprites/Front Palm Trees/";

    // === Tile / scale constants ===
    // Source sprites are drawn at 2x their pixel size to match the rest of the game.
    private static final int SCALE = 2;
    // Size of a source tile in the trunk sprite sheet.
    private static final int TILE = 32;
    // How far the trunk base overlaps into the ground so trees aren't hovering.
    private static final int TRUNK_GROUND_OFFSET = 16;
    // Extra drop applied to the canopy so it nests nicely over the top trunk segment.
    private static final int CANOPY_DROP = 8;

    // === Sprites ===
    private BufferedImage[] backTrees;
    private BufferedImage[] frontTops;
    private BufferedImage trunkStraight;

    // === Placements ===
    private int[][] backTreePlacements;
    private int[][] frontTreePlacements;

    /**
        Loads the back and front palm tree sprites from disk.
    */
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
            // First tile in the sheet is the straight trunk segment.
            trunkStraight = trunkSheet.getSubimage(0, 0, TILE, TILE);
            trunkSheet.getSubimage(TILE, 0, TILE, TILE);
        }
    }

    /**
        Sets the back-layer tree placements. Each entry is
        {x, groundY, variantIndex}.
    */
    public void setBackPlacements(int[][] placements) {
        this.backTreePlacements = placements;
    }

    /**
        Sets the front-layer tree placements. Each entry is
        {x, groundY, trunkSegments, topVariant}.
    */
    public void setFrontPlacements(int[][] placements) {
        this.frontTreePlacements = placements;
    }

    /**
        Draws the back layer of palm trees. Intended to be called
        before the main terrain is drawn.
    */
    public void drawBack(Graphics2D g, int levelWidth) {
        if (backTreePlacements == null) return;

        for (int[] tree : backTreePlacements) {
            int tx = tree[0];
            int groundY = tree[1];
            // Wrap variant index so callers don't have to worry about bounds.
            int variant = tree[2] % backTrees.length;
            BufferedImage img = backTrees[variant];
            if (img == null) continue;
            int w = img.getWidth() * SCALE;
            int h = img.getHeight() * SCALE;
            // Anchor the tree so its base sits just below groundY (overlap of TRUNK_GROUND_OFFSET).
            g.drawImage(img, tx, groundY - h + TRUNK_GROUND_OFFSET, w, h, null);
        }
    }

    /**
        Draws the front layer of palm trees. Each tree has a stack
        of trunk segments with a canopy resting on top. Intended to
        be called after the main terrain is drawn so trees appear
        in front.
    */
    public void drawFront(Graphics2D g, int levelWidth) {
        if (frontTreePlacements == null) return;

        for (int[] tree : frontTreePlacements) {
            int tx = tree[0];
            int groundY = tree[1];
            int trunkSegments = tree[2];
            int topVariant = tree[3] % frontTops.length;

            int segH = TILE * SCALE;
            int segW = TILE * SCALE;
            int baseOffset = TRUNK_GROUND_OFFSET;

            // Stack trunk segments upward from the ground.
            for (int s = 0; s < trunkSegments; s++) {
                BufferedImage seg = trunkStraight;
                if (seg == null) continue;
                // Each segment sits directly above the previous one.
                int sy = groundY - (s + 1) * segH + baseOffset;
                g.drawImage(seg, tx, sy, segW, segH, null);
            }

            // Canopy rests on top of the highest trunk segment.
            BufferedImage top = frontTops[topVariant];
            if (top != null) {
                int topW = top.getWidth() * SCALE;
                int topH = top.getHeight() * SCALE;
                // Centre the canopy horizontally over the trunk.
                int topX = tx + segW / 2 - topW / 2;
                // Position the canopy just above the top segment, dropped slightly so it
                // nests over the trunk rather than floating above it.
                int topY = groundY - trunkSegments * segH - topH + baseOffset + CANOPY_DROP;
                g.drawImage(top, topX, topY, topW, topH, null);
            }
        }
    }
}
