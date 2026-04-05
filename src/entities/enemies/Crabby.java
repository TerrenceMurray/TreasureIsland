package entities.enemies;

import java.awt.Color;
import java.awt.Graphics2D;

public class Crabby extends Enemy {

    private float detectionRange = 150f;

    public Crabby(float x, float y) {
        super(x, y, 40, 30, 5, 1);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.ORANGE);
        g.fillRect((int) x, (int) y, width, height);
    }
}
