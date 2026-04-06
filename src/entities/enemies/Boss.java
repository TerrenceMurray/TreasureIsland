package entities.enemies;

import entities.Player;

public abstract class Boss extends Enemy {

    protected Player target;
    protected int attackInterval;
    protected int attackTimer;
    protected boolean lunging;
    protected int lungeTick;
    protected static final int LUNGE_DURATION = 20;

    public Boss(float x, float y, int width, int height, int health, int damage, int attackInterval, Player target) {
        super(x, y, width, height, health, damage);
        this.attackInterval = attackInterval;
        this.target = target;
    }

    @Override
    public void update() {
        if (isDead()) return;

        if (lunging) {
            lungeTick++;
            if (lungeTick >= LUNGE_DURATION) {
                lunging = false;
                lungeTick = 0;
            }
            return;
        }

        // Walk toward player
        float dx = target.getX() - x;
        float dist = Math.abs(dx);
        float walkSpeed = getWalkSpeed();

        if (dist > width) {
            x += (dx > 0 ? walkSpeed : -walkSpeed);
        }

        // Attack on interval when close
        attackTimer++;
        if (attackTimer >= attackInterval && dist < width * 2) {
            lunging = true;
            lungeTick = 0;
            attackTimer = 0;
        }
    }

    @Override
    public boolean canDealDamage() {
        return lunging;
    }

    protected abstract float getWalkSpeed();
}
