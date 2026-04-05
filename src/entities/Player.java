package entities;

import interfaces.Attackable;
import engine.GameLoop;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Player extends GameEntity implements Attackable {

    private static final float MOVE_SPEED = 4f;
    private static final float JUMP_FORCE = -10f;
    private static final float GRAVITY = 0.5f;
    private static final int MAX_HEALTH = 4;

    private int health;
    private boolean left, right, jumping;
    private boolean inAir;
    private boolean attacking;
    private int attackTick;
    private static final int ATTACK_DURATION = 15;

    public Player(float x, float y) {
        super(x, y, 40, 56);
        this.health = MAX_HEALTH;
        this.inAir = true;
    }

    @Override
    public void update() {
        float dx = 0;
        if (left) dx -= MOVE_SPEED;
        if (right) dx += MOVE_SPEED;
        x += dx;

        if (jumping && !inAir) {
            velocityY = JUMP_FORCE;
            inAir = true;
        }

        velocityY += GRAVITY;
        y += velocityY;

        if (attacking) {
            attackTick++;
            if (attackTick >= ATTACK_DURATION) {
                attacking = false;
                attackTick = 0;
            }
        }

        // Screen boundary clamping (horizontal)
        if (x < 0) x = 0;
        if (x + width > GameLoop.GAME_WIDTH) x = GameLoop.GAME_WIDTH - width;
    }

    public void landOn(float floorY) {
        y = floorY - height;
        velocityY = 0;
        inAir = false;
    }

    public void hitHead(float ceilingBottom) {
        y = ceilingBottom;
        velocityY = 0;
    }

    public boolean isInAir() { return inAir; }
    public void setInAir(boolean inAir) { this.inAir = inAir; }

    @Override
    public void draw(Graphics2D g) {
        // Placeholder: colored rectangle
        g.setColor(Color.BLUE);
        g.fillRect((int) x, (int) y, width, height);

        if (attacking) {
            g.setColor(Color.CYAN);
            int attackX = right || !left ? (int) x + width : (int) x - 20;
            g.fillRect(attackX, (int) y + 10, 20, 20);
        }
    }

    public Rectangle getAttackBounds() {
        if (!attacking) return null;
        int attackX = right || !left ? (int) x + width : (int) x - 20;
        return new Rectangle(attackX, (int) y + 10, 20, 20);
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

    public void heal() {
        if (health < MAX_HEALTH) health++;
    }

    public void attack() {
        if (!attacking) {
            attacking = true;
            attackTick = 0;
        }
    }

    public void setLeft(boolean left) { this.left = left; }
    public void setRight(boolean right) { this.right = right; }
    public void setJumping(boolean jumping) { this.jumping = jumping; }

    public int getHealth() { return health; }
    public int getMaxHealth() { return MAX_HEALTH; }
    public boolean isAttacking() { return attacking; }
}
