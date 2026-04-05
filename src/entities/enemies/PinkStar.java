package entities.enemies;

import entities.Player;
import java.awt.Color;
import java.awt.Graphics2D;

public class PinkStar extends Enemy {

    private float patrolSpeed = 1.5f;
    private float patrolLeft;
    private float patrolRight;
    private boolean movingRight = true;
    private Player target;
    private int attackDelay;
    private static final int ATTACK_DELAY_MAX = 30;

    public PinkStar(float x, float y, Player target) {
        super(x, y, 34, 30, 3, 1);
        this.patrolLeft = x - 80;
        this.patrolRight = x + 80;
        this.target = target;
    }

    @Override
    public void update() {
        if (isDead()) return;

        float dx = Math.abs(target.getX() - x);
        boolean playerAbove = target.getY() + target.getHeight() < y;

        if (dx < width && !playerAbove) {
            movingRight = target.getX() > x;
            if (attackDelay < ATTACK_DELAY_MAX) attackDelay++;
            return;
        }

        attackDelay = 0;

        if (movingRight) {
            x += patrolSpeed;
            if (x >= patrolRight) movingRight = false;
        } else {
            x -= patrolSpeed;
            if (x <= patrolLeft) movingRight = true;
        }
    }

    @Override
    public boolean canDealDamage() {
        return attackDelay >= ATTACK_DELAY_MAX;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead()) return;
        g.setColor(Color.PINK);
        g.fillRect((int) x, (int) y, width, height);
    }
}
