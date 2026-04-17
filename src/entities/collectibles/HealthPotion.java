package entities.collectibles;

import rendering.AnimatedSprite;
import java.awt.Graphics2D;

/**
    The HealthPotion class is a collectible that restores one
    point of health to the player when picked up.
*/
public class HealthPotion extends Collectible {

    // === Config ===
    // Each source sprite pixel is drawn as a DRAW_SCALE×DRAW_SCALE block.
    private static final int DRAW_SCALE = 2;
    // Health points restored on pickup.
    private int healAmount = 1;

    // === Animation ===
    private AnimatedSprite sprite;

    public HealthPotion(float x, float y) {
        super(x, y, 16, 16);
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", "assets/Treasure Hunters/Pirate Treasure/Sprites/Red Potion");
    }

    @Override
    public void update() {
        sprite.update();
    }

    @Override
    public void draw(Graphics2D g) {
        if (!collected) {
            // Source sprite is 13×17 px; scale up and centre over the collision box.
            int drawW = 13 * DRAW_SCALE;
            int drawH = 17 * DRAW_SCALE;
            int drawX = (int) x - (drawW - width) / 2;
            int drawY = (int) y - (drawH - height);
            sprite.draw(g, drawX, drawY, drawW, drawH);
        }
    }

    public int getHealAmount() { return healAmount; }
}
