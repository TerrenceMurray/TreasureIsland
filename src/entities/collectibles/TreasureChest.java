package entities.collectibles;

import rendering.AnimatedSprite;
import java.awt.Graphics2D;

public class TreasureChest extends Collectible {

    private static final int DRAW_SCALE = 2;
    private int points = 200;
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
            int drawW = 64 * DRAW_SCALE;
            int drawH = 35 * DRAW_SCALE;
            int drawX = (int) x - (drawW - width) / 2;
            int drawY = (int) y - (drawH - height);
            sprite.draw(g, drawX, drawY, drawW, drawH);
        }
    }

    public int getPoints() { return points; }
}
