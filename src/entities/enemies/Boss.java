package entities.enemies;

public abstract class Boss extends Enemy {

    protected int attackInterval;
    protected long lastAttackTime;

    public Boss(float x, float y, int width, int height, int health, int damage, int attackInterval) {
        super(x, y, width, height, health, damage);
        this.attackInterval = attackInterval;
    }
}
