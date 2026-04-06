package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;

public abstract class Boss extends Enemy {

    protected Player target;
    protected int attackInterval;
    protected int attackTimer;
    protected boolean lunging;
    protected int lungeTick;
    protected static final int LUNGE_DURATION = 20;
    protected boolean facingRight;
    protected AnimatedSprite sprite;

    public Boss(float x, float y, int width, int height, int health, int damage, int attackInterval, Player target) {
        super(x, y, width, height, health, damage);
        this.attackInterval = attackInterval;
        this.target = target;
    }

    @Override
    public void update() {
        if (isDead()) return;

        if (hurtTimer > 0) {
            hurtTimer--;
            sprite.setState("hit");
            sprite.update();
            return;
        }

        if (lunging) {
            lungeTick++;
            if (lungeTick >= LUNGE_DURATION) {
                lunging = false;
                lungeTick = 0;
            }
            sprite.setState("attack");
            sprite.update();
            return;
        }

        float dx = target.getX() - x;
        float dist = Math.abs(dx);
        float walkSpeed = getWalkSpeed();
        facingRight = dx > 0;

        if (dist > this.width) {
            x += (dx > 0 ? walkSpeed : -walkSpeed);
            sprite.setState("run");
        } else {
            sprite.setState("idle");
        }

        attackTimer++;
        if (attackTimer >= attackInterval && dist < this.width * 2) {
            lunging = true;
            lungeTick = 0;
            attackTimer = 0;
        }

        sprite.setFlipped(!facingRight);
        sprite.update();
    }

    @Override
    public boolean canDealDamage() {
        return lunging;
    }

    protected abstract float getWalkSpeed();
}
