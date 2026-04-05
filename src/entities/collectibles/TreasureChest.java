package entities.collectibles;

import java.awt.Color;
import java.awt.Graphics2D;

public class TreasureChest extends Collectible {

    private int points = 200;

    public TreasureChest(float x, float y) {
        super(x, y, 32, 28);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        if (!collected) {
            g.setColor(new Color(180, 140, 50));
            g.fillRect((int) x, (int) y, width, height);
        }
    }

    public int getPoints() { return points; }
}
