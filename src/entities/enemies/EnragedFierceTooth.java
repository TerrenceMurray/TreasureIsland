package entities.enemies;

import entities.Player;
import java.awt.Color;
import java.awt.Graphics2D;

public class EnragedFierceTooth extends Boss {

    public EnragedFierceTooth(float x, float y, Player target) {
        super(x, y, 56, 64, 8, 2, 90, target);
    }

    @Override
    protected float getWalkSpeed() {
        return 1.8f;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead()) return;
        g.setColor(lunging ? new Color(255, 50, 50) : new Color(150, 0, 0));
        g.fillRect((int) x, (int) y, width, height);

        // Health bar
        g.setColor(Color.RED);
        int barWidth = (int) (width * ((float) health / 8));
        g.fillRect((int) x, (int) y - 8, barWidth, 4);
        g.setColor(Color.DARK_GRAY);
        g.drawRect((int) x, (int) y - 8, width, 4);
    }
}
