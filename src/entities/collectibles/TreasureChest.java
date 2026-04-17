package entities.collectibles;

import rendering.AnimatedSprite;
import java.awt.Graphics2D;

/**
    The TreasureChest class is the final collectible of a
    level. Picking it up ends the level and awards a large
    number of points.
*/
public class TreasureChest extends Collectible {

    // === Config ===
    // Each source sprite pixel is drawn as a DRAW_SCALE×DRAW_SCALE block.
    private static final int DRAW_SCALE = 2;
    // Large bonus score for reaching the end of the level.
    private int points = 200;

    // === Animation ===
    private AnimatedSprite sprite;

    public TreasureChest(float x, float y) {
        super(x, y, 32, 28);
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", "assets/Treasure Hunters/Palm Tree Island/Sprites/Objects/Chest");
        sprite.setState("idle");
    }

    @Override
    public void update() {
        sprite.update();
    }

    @Override
    public void draw(Graphics2D g) {
        if (!collected) {
            // Source sprite is 64×35 px; scale up and centre over the collision box.
            int drawW = 64 * DRAW_SCALE;
            int drawH = 35 * DRAW_SCALE;
            int drawX = (int) x - (drawW - width) / 2;
            int drawY = (int) y - (drawH - height);
            sprite.draw(g, drawX, drawY, drawW, drawH);
        }
    }

    public int getPoints() { return points; }
}
