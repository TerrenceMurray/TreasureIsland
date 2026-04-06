package rendering;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TerrainRenderer {

    private static final int TILE = 32;
    private static final int DRAW_TILE = TILE * 2;

    private BufferedImage topLeft, topMid, topRight;
    private BufferedImage midLeft, midMid, midRight;

    public TerrainRenderer(String sheetPath) {
        try {
            BufferedImage sheet = ImageIO.read(new File(sheetPath));
            topLeft = sheet.getSubimage(0, 0, TILE, TILE);
            topMid = sheet.getSubimage(TILE, 0, TILE, TILE);
            topRight = sheet.getSubimage(TILE * 2, 0, TILE, TILE);
            midLeft = sheet.getSubimage(0, TILE, TILE, TILE);
            midMid = sheet.getSubimage(TILE, TILE, TILE, TILE);
            midRight = sheet.getSubimage(TILE * 2, TILE, TILE, TILE);
        } catch (IOException e) {
            System.err.println("Could not load terrain: " + sheetPath);
        }
    }

    public void draw(Graphics2D g, List<Rectangle> platforms) {
        for (Rectangle plat : platforms) {
            drawPlatform(g, plat);
        }
    }

    private void drawPlatform(Graphics2D g, Rectangle plat) {
        if (topMid == null) return;

        int cols = Math.max(1, plat.width / DRAW_TILE);
        int rows = Math.max(1, plat.height / DRAW_TILE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int dx = plat.x + col * DRAW_TILE;
                int dy = plat.y + row * DRAW_TILE;

                BufferedImage tile;
                if (row == 0) {
                    if (cols == 1) {
                        tile = topMid;
                    } else if (col == 0) {
                        tile = topLeft;
                    } else if (col == cols - 1) {
                        tile = topRight;
                    } else {
                        tile = topMid;
                    }
                } else {
                    if (cols == 1) {
                        tile = midMid;
                    } else if (col == 0) {
                        tile = midLeft;
                    } else if (col == cols - 1) {
                        tile = midRight;
                    } else {
                        tile = midMid;
                    }
                }

                g.drawImage(tile, dx, dy, DRAW_TILE, DRAW_TILE, null);
            }
        }
    }
}
