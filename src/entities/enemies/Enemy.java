package entities.enemies;

import entities.GameEntity;
import interfaces.Attackable;
import java.awt.Graphics2D;

public abstract class Enemy extends GameEntity implements Attackable {

    protected int health;
    protected int damage;

    public Enemy(float x, float y, int width, int height, int health, int damage) {
        super(x, y, width, height);
        this.health = health;
        this.damage = damage;
    }

    @Override
    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }

    public int getDamage() { return damage; }
}
