package entities.enemies;

import java.awt.Color;
import java.awt.Graphics2D;

public class PinkStar extends Enemy {

    private float patrolSpeed = 1.5f;

    public PinkStar(float x, float y) {
        super(x, y, 34, 30, 3, 1);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.PINK);
        g.fillRect((int) x, (int) y, width, height);
    }
}
