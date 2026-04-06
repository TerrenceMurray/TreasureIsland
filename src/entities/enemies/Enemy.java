package entities.enemies;

import entities.GameEntity;
import interfaces.Attackable;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

public abstract class Enemy extends GameEntity implements Attackable {

    protected int health;
    protected int maxHealth;
    protected int damage;
    protected int hurtTimer;
    protected static final int HURT_DURATION = 15;
    protected int deathTimer;
    protected static final int DEATH_HIT_DURATION = 36;
    protected static final int DEATH_GROUND_DURATION = 60;
    protected static final int DEATH_TOTAL = DEATH_HIT_DURATION + DEATH_GROUND_DURATION;
    protected boolean dying;
    protected float knockbackVel;
    protected boolean inAir = true;
    protected static final float GRAVITY = 0.5f;

    public Enemy(float x, float y, int width, int height, int health, int damage) {
        super(x, y, width, height);
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
    }

    @Override
    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
        if (health == 0) {
            dying = true;
            deathTimer = DEATH_TOTAL;
        } else {
            hurtTimer = HURT_DURATION;
        }
    }

    @Override
    public boolean isDead() {
        return health <= 0 && !dying;
    }

    public boolean isDying() { return dying; }

    public boolean inDeathHitPhase() {
        return dying && deathTimer > DEATH_GROUND_DURATION;
    }

    public float getDeathAlpha() {
        if (!dying) return 1f;
        if (deathTimer > DEATH_GROUND_DURATION) return 1f;
        return (float) deathTimer / DEATH_GROUND_DURATION;
    }

    public void updateDeath() {
        if (dying) {
            deathTimer--;
            if (deathTimer <= 0) {
                dying = false;
            }
        }
    }

    public void knockback(float amount) {
        knockbackVel = amount;
    }

    public void applyGravity() {
        velocityY += GRAVITY;
        y += velocityY;
    }

    public void landOn(float floorY) {
        y = floorY - height;
        velocityY = 0;
        inAir = false;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public boolean isInAir() { return inAir; }

    protected void applyKnockback() {
        if (knockbackVel != 0) {
            x += knockbackVel;
            knockbackVel *= 0.8f;
            if (Math.abs(knockbackVel) < 0.5f) knockbackVel = 0;
        }
    }

    protected void drawWithDeathFade(Graphics2D g, Runnable drawSprite) {
        if (dying) {
            float alpha = getDeathAlpha();
            Composite original = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            drawSprite.run();
            g.setComposite(original);
        } else {
            drawSprite.run();
        }
    }

    protected void drawHealthBar(Graphics2D g) {
        if (health >= maxHealth || dying) return;
        int barW = width;
        int barH = 4;
        int barX = (int) x;
        int barY = (int) y - 10;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);
        g.setColor(Color.RED);
        int fillW = (int) (barW * ((float) health / maxHealth));
        g.fillRect(barX, barY, fillW, barH);
    }

    public void drawEffect(Graphics2D g) {}

    public int getDamage() { return damage; }
    public boolean canDealDamage() { return true; }
}
