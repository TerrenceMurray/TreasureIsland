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

    private BufferedImage[] backTrees;
    private BufferedImage[] frontTops;

    public PalmTreeRenderer() {
        backTrees = new BufferedImage[] {
            loadImage(BACK_BASE + "Back Palm Tree Regular 01.png"),
            loadImage(BACK_BASE + "Back Palm Tree Left 01.png"),
            loadImage(BACK_BASE + "Back Palm Tree Right 01.png"),
            loadImage(BACK_BASE + "Back Palm Tree Regular 02.png"),
        };
        frontTops = new BufferedImage[] {
            loadImage(FRONT_BASE + "Front Palm Tree Top 01.png"),
            loadImage(FRONT_BASE + "Front Palm Tree Top 02.png"),
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

    public void drawBack(Graphics2D g, int levelWidth) {
        // Place back palm trees at intervals
        int spacing = 350;
        for (int i = 0; i < levelWidth / spacing; i++) {
            BufferedImage tree = backTrees[i % backTrees.length];
            if (tree == null) continue;
            int tx = 80 + i * spacing;
            int ty = 480 - tree.getHeight() * SCALE;
            Composite original = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.drawImage(tree, tx, ty, tree.getWidth() * SCALE, tree.getHeight() * SCALE, null);
            g.setComposite(original);
        }
    }

    public void drawFront(Graphics2D g, int levelWidth) {
        // Place front palm tree tops at wider intervals
        int spacing = 600;
        for (int i = 0; i < levelWidth / spacing; i++) {
            BufferedImage top = frontTops[i % frontTops.length];
            if (top == null) continue;
            int tx = 200 + i * spacing;
            int ty = 480 - top.getHeight() * SCALE - 40;
            g.drawImage(top, tx, ty, top.getWidth() * SCALE, top.getHeight() * SCALE, null);
        }
    }
}
