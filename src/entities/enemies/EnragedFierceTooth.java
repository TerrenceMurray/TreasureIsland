package entities.enemies;

import java.awt.Color;
import java.awt.Graphics2D;

public class EnragedFierceTooth extends Boss {

    public EnragedFierceTooth(float x, float y) {
        super(x, y, 56, 64, 8, 3, 72);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(150, 0, 0));
        g.fillRect((int) x, (int) y, width, height);
    }
}
