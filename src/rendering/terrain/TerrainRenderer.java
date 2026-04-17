package rendering.terrain;

import engine.managers.ImageManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
    The TerrainRenderer class draws platforms using the Palm Tree
    Island tileset. Tall platforms are drawn as a block of tiles
    with grass-topped edges, while thin platforms (height 20 pixels
    or less) are drawn as just the grass strip so sprites appear
    resting on top of them.
*/
public class TerrainRenderer {

    // === Tile sizes ===
    // Size of a source tile in the sprite sheet.
    private static final int TILE = 32;
    // On-screen size of a drawn tile (sprites are scaled 2x).
    private static final int DRAW_TILE = TILE * 2;
    // Height in source pixels of just the grass strip at the top of a tile.
    private static final int GRASS_SRC_H = 12;
    // Platforms with collision height at or below this are treated as thin grass strips.
    private static final int THIN_PLATFORM_HEIGHT = 20;
    // Extra pixels of grass drawn below the collision rect so the strip isn't razor-thin.
    private static final int THIN_PLATFORM_EXTRA_HEIGHT = 4;

    // === Tiles ===
    private BufferedImage topLeft, topMid, topRight;
    private BufferedImage midLeft, midMid, midRight;

    // === Configuration ===
    // Pixels to raise thin platforms when drawing so sprites rest on top.
    private int platformLift;

    /**
        Creates a renderer with no platform lift.
    */
    public TerrainRenderer(String sheetPath) {
        this(sheetPath, 0);
    }

    /**
        Creates a renderer that raises thin grass platforms by the
        given number of pixels when drawing.
    */
    public TerrainRenderer(String sheetPath, int platformLift) {
        this.platformLift = platformLift;
        BufferedImage sheet = ImageManager.loadBufferedImage(sheetPath);
        if (sheet != null) {
            // Row 0 is the grass-topped edge tiles, row 1 is the solid body.
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

    /**
        Draws every platform. Platforms with a height of 20 pixels
        or less are drawn as a thin grass strip, others as a full
        block of terrain tiles.
    */
    public void draw(Graphics2D g, List<Rectangle> platforms) {
        for (Rectangle plat : platforms) {
            if (plat.height <= THIN_PLATFORM_HEIGHT) {
                drawThinPlatform(g, plat);
            } else {
                drawPlatform(g, plat);
            }
        }
    }

    private void drawPlatform(Graphics2D g, Rectangle plat) {
        if (topMid == null) return;

        // How many tiles fit along each axis; always at least one.
        int cols = Math.max(1, plat.width / DRAW_TILE);
        int rows = Math.max(1, plat.height / DRAW_TILE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int dx = plat.x + col * DRAW_TILE;
                int dy = plat.y + row * DRAW_TILE;

                // Pick the correct tile based on whether this cell is on the
                // top edge vs body, and on left/middle/right.
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
        // Shift the grass strip up so the visible top aligns with the collision top.
        int destY = plat.y - platformLift;
        // Stretch the strip to cover the collision height plus the lift and a small overhang.
        int destH = plat.height + platformLift + THIN_PLATFORM_EXTRA_HEIGHT;

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

            // Source rect is just the top GRASS_SRC_H pixels of the tile; destination
            // rect stretches it across the whole drawn area.
            g.drawImage(tile,
                dx, destY, dx + DRAW_TILE, destY + destH,
                0, 0, TILE, GRASS_SRC_H,
                null);
        }
    }
}
