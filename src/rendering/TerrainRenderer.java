package rendering;

import engine.ImageManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

public class TerrainRenderer {

    private static final int TILE = 32;
    private static final int DRAW_TILE = TILE * 2;
    // Top portion of source tile containing the grass strip
    private static final int GRASS_SRC_H = 12;

    private BufferedImage topLeft, topMid, topRight;
    private BufferedImage midLeft, midMid, midRight;

    private int platformLift;

    public TerrainRenderer(String sheetPath) {
        this(sheetPath, 0);
    }

    public TerrainRenderer(String sheetPath, int platformLift) {
        this.platformLift = platformLift;
        BufferedImage sheet = ImageManager.loadBufferedImage(sheetPath);
        if (sheet != null) {
            topLeft = sheet.getSubimage(0, 0, TILE, TILE);
            topMid = sheet.getSubimage(TILE, 0, TILE, TILE);
            topRight = sheet.getSubimage(TILE * 2, 0, TILE, TILE);
            midLeft = sheet.getSubimage(0, TILE, TILE, TILE);
            midMid = sheet.getSubimage(TILE, TILE, TILE, TILE);
            midRight = sheet.getSubimage(TILE * 2, TILE, TILE, TILE);
        }
    }

    public void setPlatformLift(int platformLift) {
        this.platformLift = platformLift;
    }

    public void draw(Graphics2D g, List<Rectangle> platforms) {
        for (Rectangle plat : platforms) {
            if (plat.height <= 20) {
                drawThinPlatform(g, plat);
            } else {
                drawPlatform(g, plat);
            }
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

    // Thin platforms: draw only the grass strip of the source tile, stretched over the
    // platform's collision height plus a small visual overhang so sprites appear on top.
    private void drawThinPlatform(Graphics2D g, Rectangle plat) {
        if (topMid == null) return;

        int cols = Math.max(1, plat.width / DRAW_TILE);
        int destY = plat.y - platformLift;
        int destH = plat.height + platformLift + 4;

        for (int col = 0; col < cols; col++) {
            int dx = plat.x + col * DRAW_TILE;

            BufferedImage tile;
            if (cols == 1) {
                tile = topMid;
            } else if (col == 0) {
                tile = topLeft;
            } else if (col == cols - 1) {
                tile = topRight;
            } else {
                tile = topMid;
            }

            g.drawImage(tile,
                dx, destY, dx + DRAW_TILE, destY + destH,
                0, 0, TILE, GRASS_SRC_H,
                null);
        }
    }
}
