package entities.enemies;

import entities.Player;
import java.awt.Color;
import java.awt.Graphics2D;

public class FierceTooth extends Boss {

    public FierceTooth(float x, float y, Player target) {
        super(x, y, 56, 64, 6, 1, 150, target);
    }

    @Override
    protected float getWalkSpeed() {
        return 1.2f;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead()) return;
        g.setColor(lunging ? new Color(180, 0, 180) : new Color(100, 0, 100));
        g.fillRect((int) x, (int) y, width, height);

        // Health bar
        g.setColor(Color.RED);
        int barWidth = (int) (width * ((float) health / 6));
        g.fillRect((int) x, (int) y - 8, barWidth, 4);
        g.setColor(Color.DARK_GRAY);
        g.drawRect((int) x, (int) y - 8, width, 4);
    }
}
