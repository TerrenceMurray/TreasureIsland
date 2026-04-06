package entities.collectibles;

import rendering.AnimatedSprite;
import java.awt.Graphics2D;

public class Diamond extends Collectible {

    private static final int DRAW_SCALE = 2;
    private int points = 25;
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
            int drawW = 24 * DRAW_SCALE;
            int drawH = 24 * DRAW_SCALE;
            int drawX = (int) x - (drawW - width) / 2;
            int drawY = (int) y - (drawH - height);
            sprite.draw(g, drawX, drawY, drawW, drawH);
        }
    }

    public int getPoints() { return points; }
}
