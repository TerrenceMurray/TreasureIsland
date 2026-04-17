package rendering;

import engine.ImageManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
    The PirateTerrainRenderer class draws platforms using the Pirate
    Ship brick tileset for solid ground and the wooden plank tileset
    for thin platforms. Thin plank platforms can be visually lifted
    by a configurable number of pixels so the player sprite appears
    to stand on the visible plank surface rather than below it.
*/
public class PirateTerrainRenderer {

    // === Asset paths ===
    private static final String TERRAIN_PATH = "assets/Treasure Hunters/Pirate Ship/Sprites/Tilesets/Terrain and Back Wall (32x32).png";
    private static final String PLATFORM_PATH = "assets/Treasure Hunters/Pirate Ship/Sprites/Tilesets/Platforms (32x32).png";

    // === Tile sizes ===
    // Size of a source tile in the sprite sheet.
    private static final int TILE = 32;
    // On-screen size of a drawn tile (sprites are scaled 2x).
    private static final int DRAW_TILE = TILE * 2;
    // Platforms with a collision height at or below this are treated as thin planks.
    private static final int THIN_PLATFORM_HEIGHT = 20;
    // Default visual lift in pixels applied to thin plank platforms.
    private static final int DEFAULT_PLATFORM_LIFT = 10;

    // === Main terrain tiles (brick) ===
    private BufferedImage topLeft, topMid, topRight;
    private BufferedImage midLeft, midMid, midRight;

    // === Platform tiles (wooden planks) ===
    private BufferedImage platLeft, platMid, platRight;

    // === Configuration ===
    // Pixels to raise thin plank platforms when drawing so sprites appear on top.
    private final int platformLift;

    /**
        Creates a renderer with the default visual plank lift.
    */
    public PirateTerrainRenderer() {
        this(DEFAULT_PLATFORM_LIFT);
    }

    /**
        Creates a renderer that raises thin plank platforms by the
        given number of pixels when drawing, so sprites align with
        the visible plank surface.
    */
    public PirateTerrainRenderer(int platformLift) {
        this.platformLift = platformLift;

        BufferedImage sheet = ImageManager.loadBufferedImage(TERRAIN_PATH);
        if (sheet != null) {
            // Slice the 32x32 tiles out of the sheet at known grid positions.
            // Row 1 is the gold-trimmed top edge, row 2 is the solid body.
            // (1,1)=top-left, (2,1)=top-mid, (3,1)=top-right
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
            // Row 2 of the plank sheet holds the thicker wooden plank tiles (cols 1-4).
            platLeft = platSheet.getSubimage(TILE, TILE * 2, TILE, TILE);
            platMid = platSheet.getSubimage(TILE * 2, TILE * 2, TILE, TILE);
            platRight = platSheet.getSubimage(TILE * 4, TILE * 2, TILE, TILE);
        }
    }

    /**
        Draws every platform. Platforms with a height of 20 pixels
        or less are drawn as thin wooden planks, others as full
        brick terrain.
    */
    public void draw(Graphics2D g, List<Rectangle> platforms) {
        for (Rectangle plat : platforms) {
            if (plat.height <= THIN_PLATFORM_HEIGHT) {
                drawPlatform(g, plat);
            } else {
                drawTerrain(g, plat);
            }
        }
    }

    private void drawTerrain(Graphics2D g, Rectangle plat) {
        if (topMid == null) return;

        // How many tiles fit along each axis; always at least one.
        int cols = Math.max(1, plat.width / DRAW_TILE);
        int rows = Math.max(1, plat.height / DRAW_TILE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int dx = plat.x + col * DRAW_TILE;
                int dy = plat.y + row * DRAW_TILE;

                // Pick the correct tile based on whether this cell is
                // on the top edge vs body, and on left/middle/right.
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

            // Pick left / middle / right plank tile based on column index.
            BufferedImage tile;
            if (cols == 1) tile = platMid;
            else if (col == 0) tile = platLeft;
            else if (col == cols - 1) tile = platRight;
            else tile = platMid;

            // Subtract platformLift so the visible plank surface sits at plat.y
            // rather than below it, which keeps sprites standing on the plank.
            g.drawImage(tile, dx, plat.y - platformLift, DRAW_TILE, DRAW_TILE, null);
        }
    }
}
