package entities.enemies;

import java.awt.Color;
import java.awt.Graphics2D;

public class FierceTooth extends Boss {

    public FierceTooth(float x, float y) {
        super(x, y, 56, 64, 6, 2, 120);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(100, 0, 100));
        g.fillRect((int) x, (int) y, width, height);
    }
}
