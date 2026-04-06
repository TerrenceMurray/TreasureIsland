package entities.enemies;

import entities.GameEntity;
import interfaces.Attackable;
import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Enemy extends GameEntity implements Attackable {

    protected int health;
    protected int maxHealth;
    protected int damage;
    protected int hurtTimer;
    protected static final int HURT_DURATION = 15;

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
        hurtTimer = HURT_DURATION;
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }

    public void knockback(float amount) {
        x += amount;
    }

    protected void drawHealthBar(Graphics2D g) {
        if (health >= maxHealth) return;
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

    public int getDamage() { return damage; }
    public boolean canDealDamage() { return true; }
}
