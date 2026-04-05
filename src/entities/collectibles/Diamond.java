package entities.collectibles;

import java.awt.Color;
import java.awt.Graphics2D;

public class Diamond extends Collectible {

    private int points = 25;

    public Diamond(float x, float y) {
        super(x, y, 16, 16);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        if (!collected) {
            g.setColor(Color.CYAN);
            g.fillRect((int) x, (int) y, width, height);
        }
    }

    public int getPoints() { return points; }
}
