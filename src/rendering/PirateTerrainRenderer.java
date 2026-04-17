package rendering;

import engine.ImageManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

public class PirateTerrainRenderer {

    private static final String TERRAIN_PATH = "assets/Treasure Hunters/Pirate Ship/Sprites/Tilesets/Terrain and Back Wall (32x32).png";
    private static final String PLATFORM_PATH = "assets/Treasure Hunters/Pirate Ship/Sprites/Tilesets/Platforms (32x32).png";
    private static final int TILE = 32;
    private static final int DRAW_TILE = TILE * 2;

    // Main terrain tiles (brick)
    private BufferedImage topLeft, topMid, topRight;
    private BufferedImage midLeft, midMid, midRight;

    // Platform tiles (wooden planks)
    private BufferedImage platLeft, platMid, platRight;

    private final int platformLift;

    public PirateTerrainRenderer() {
        this(10);
    }

    public PirateTerrainRenderer(int platformLift) {
        this.platformLift = platformLift;

        BufferedImage sheet = ImageManager.loadBufferedImage(TERRAIN_PATH);
        if (sheet != null) {
            // (1,1)=top-left, (5,1)=top-mid, (3,1)=top-right (gold trim)
            // (1,2)=left body, (5,2)=mid body, (3,2)=right body
            topLeft = sheet.getSubimage(TILE, TILE, TILE, TILE);
            topMid = sheet.getSubimage(TILE * 2, TILE, TILE, TILE);
            topRight = sheet.getSubimage(TILE * 3, TILE, TILE, TILE);
            midLeft = sheet.getSubimage(TILE, TILE * 2, TILE, TILE);
            midMid = sheet.getSubimage(TILE * 5, TILE * 2, TILE, TILE);
            midRight = sheet.getSubimage(TILE * 3, TILE * 2, TILE, TILE);
        }

        BufferedImage platSheet = ImageManager.loadBufferedImage(PLATFORM_PATH);
        if (platSheet != null) {
            // Row 2 is the thicker wooden plank (cols 1-4)
            platLeft = platSheet.getSubimage(TILE, TILE * 2, TILE, TILE);
            platMid = platSheet.getSubimage(TILE * 2, TILE * 2, TILE, TILE);
            platRight = platSheet.getSubimage(TILE * 4, TILE * 2, TILE, TILE);
        }
    }

    public void draw(Graphics2D g, List<Rectangle> platforms) {
        for (Rectangle plat : platforms) {
            if (plat.height <= 20) {
                drawPlatform(g, plat);
            } else {
                drawTerrain(g, plat);
            }
        }
    }

    private void drawTerrain(Graphics2D g, Rectangle plat) {
        if (topMid == null) return;

        int cols = Math.max(1, plat.width / DRAW_TILE);
        int rows = Math.max(1, plat.height / DRAW_TILE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int dx = plat.x + col * DRAW_TILE;
                int dy = plat.y + row * DRAW_TILE;

                BufferedImage tile;
                if (row == 0) {
                    if (cols == 1) tile = topMid;
                    else if (col == 0) tile = topLeft;
                    else if (col == cols - 1) tile = topRight;
                    else tile = topMid;
                } else {
                    if (cols == 1) tile = midMid;
                    else if (col == 0) tile = midLeft;
                    else if (col == cols - 1) tile = midRight;
                    else tile = midMid;
                }

                g.drawImage(tile, dx, dy, DRAW_TILE, DRAW_TILE, null);
            }
        }
    }

    private void drawPlatform(Graphics2D g, Rectangle plat) {
        if (platMid == null) return;

        int cols = Math.max(1, plat.width / DRAW_TILE);
        for (int col = 0; col < cols; col++) {
            int dx = plat.x + col * DRAW_TILE;

            BufferedImage tile;
            if (cols == 1) tile = platMid;
            else if (col == 0) tile = platLeft;
            else if (col == cols - 1) tile = platRight;
            else tile = platMid;

            g.drawImage(tile, dx, plat.y - platformLift, DRAW_TILE, DRAW_TILE, null);
        }
    }
}
