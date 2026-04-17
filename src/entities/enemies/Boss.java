package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Boss extends Enemy {

    protected Player target;
    protected int attackInterval;
    protected int attackTimer;
    protected boolean lunging;
    protected int lungeTick;
    protected static final int LUNGE_DURATION = 20;
    protected boolean facingRight;
    protected AnimatedSprite sprite;
    protected String bossName;
    private int jumpCooldown;
    private static final int JUMP_COOLDOWN_MAX = 90;
    private static final float JUMP_FORCE = -9f;

    public Boss(float x, float y, int width, int height, int health, int damage, int attackInterval, Player target, String bossName) {
        super(x, y, width, height, health, damage);
        this.attackInterval = attackInterval;
        this.target = target;
        this.bossName = bossName;
    }

    @Override
    public void update() {
        if (dying) {
            sprite.setState(inDeathHitPhase() ? "deadhit" : "dead");
            sprite.update();
            updateDeath();
            return;
        }
        if (isDead()) return;
        applyKnockback();

        if (hurtTimer > 0) {
            hurtTimer--;
            sprite.setState("hit");
            sprite.update();
            return;
        }

        if (jumpCooldown > 0) jumpCooldown--;

        if (lunging) {
            lungeTick++;
            float lungeSpeed = facingRight ? 3f : -3f;
            if (lungeTick < LUNGE_DURATION / 2) {
                x += lungeSpeed;
            }
            if (lungeTick >= LUNGE_DURATION) {
                lunging = false;
                lungeTick = 0;
            }
            sprite.setState("attack");
            sprite.update();
            return;
        }

        float dx = target.getX() - x;
        float dy = target.getY() - y;
        float dist = Math.abs(dx);
        float walkSpeed = getWalkSpeed();
        facingRight = dx > 0;

        // Jump toward player if they're on a higher platform
        if (dy < -60 && !inAir && jumpCooldown <= 0) {
            velocityY = JUMP_FORCE;
            inAir = true;
            jumpCooldown = JUMP_COOLDOWN_MAX;
        }

        if (dist > this.width) {
            x += (dx > 0 ? walkSpeed : -walkSpeed);
            sprite.setState(inAir ? "run" : "run");
        } else {
            sprite.setState("idle");
        }

        attackTimer++;
        if (attackTimer >= attackInterval && dist < this.width * 2 && !inAir) {
            lunging = true;
            lungeTick = 0;
            attackTimer = 0;
        }

        sprite.setFlipped(facingRight);
        sprite.update();
    }

    @Override
    public Rectangle getBounds() {
        if (lunging) {
            int attackReach = width / 2;
            int bx = facingRight ? (int) x : (int) x - attackReach;
            return new Rectangle(bx, (int) y, width + attackReach, height);
        }
        return super.getBounds();
    }

    @Override
    public boolean canDealDamage() {
        return lunging;
    }

    protected void drawBossName(Graphics2D g) {
        if (dying) return;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        int nameW = g.getFontMetrics().stringWidth(bossName);
        g.drawString(bossName, (int) x + width / 2 - nameW / 2, (int) y - 16);
    }

    protected abstract float getWalkSpeed();
}
