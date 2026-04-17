package entities.collectibles;

import rendering.AnimatedSprite;
import java.awt.Graphics2D;

/**
    The Diamond class is a collectible that awards the player
    points when picked up.
*/
public class Diamond extends Collectible {

    // === Config ===
    // Each source sprite pixel is drawn as a DRAW_SCALE×DRAW_SCALE block.
    private static final int DRAW_SCALE = 2;
    // Score value awarded on pickup.
    private int points = 25;

    // === Animation ===
    // Animated sparkle sprite; tick rate 6 = advance one frame every 6 updates.
    private AnimatedSprite sprite;

    public Diamond(float x, float y) {
        super(x, y, 16, 16);
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", "assets/Treasure Hunters/Pirate Treasure/Sprites/Blue Diamond");
    }

    @Override
    public void update() {
        sprite.update();
    }

    @Override
    public void draw(Graphics2D g) {
        if (!collected) {
            // Drawn size (24 source px × scale). The drawn sprite is wider/taller
            // than the collision box, so shift it so the box stays centred inside.
            int drawW = 24 * DRAW_SCALE;
            int drawH = 24 * DRAW_SCALE;
            int drawX = (int) x - (drawW - width) / 2;
            int drawY = (int) y - (drawH - height);
            sprite.draw(g, drawX, drawY, drawW, drawH);
        }
    }

    public int getPoints() { return points; }
}
