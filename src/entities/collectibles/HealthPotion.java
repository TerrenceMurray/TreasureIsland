package entities.collectibles;

import java.awt.Color;
import java.awt.Graphics2D;

public class HealthPotion extends Collectible {

    private int healAmount = 1;

    public HealthPotion(float x, float y) {
        super(x, y, 16, 16);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        if (!collected) {
            g.setColor(Color.RED);
            g.fillRect((int) x, (int) y, width, height);
        }
    }

    public int getHealAmount() { return healAmount; }
}
