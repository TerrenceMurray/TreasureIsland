package entities;

import interfaces.Attackable;
import engine.GameConfig;
import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

public class Player extends GameEntity implements Attackable {

    private static final GameConfig CFG = GameConfig.getInstance();
    private static final float MOVE_SPEED = CFG.getFloat("player.speed", 4f);
    private static final float JUMP_FORCE = -CFG.getFloat("player.jumpForce", 10f);
    private static final float GRAVITY = CFG.getFloat("player.gravity", 0.5f);
    private static final int MAX_HEALTH = CFG.getInt("player.health", 4);
    private static final int ATTACK_DURATION = CFG.getInt("player.attackDuration", 20);
    private static final int ATTACK_WIDTH = CFG.getInt("player.attackWidth", 45);
    private static final int ATTACK_HEIGHT = CFG.getInt("player.attackHeight", 35);
    private static final int ATTACK_COOLDOWN = CFG.getInt("player.attackCooldown", 25);
    private static final int DRAW_SCALE = 2;
    private static final String SPRITE_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/";

    private int levelWidth;
    private int health;
    private boolean left, right, jumping;
    private boolean facingRight = true;
    private boolean inAir;
    private boolean attacking;
    private int attackTick;
    private int attackCooldownTimer;
    private Set<Object> hitEnemies = new HashSet<>();
    private int damageCooldown;
    private static final int DAMAGE_COOLDOWN_MAX = 90;

    private AnimatedSprite sprite;

    public Player(float x, float y) {
        super(x, y,
            CFG.getInt("player.width", 28),
            CFG.getInt("player.height", 50));
        this.health = MAX_HEALTH;
        this.inAir = true;
        this.levelWidth = CFG.getInt("game.width", 960);
        initSprite();
    }

    private void initSprite() {
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", SPRITE_BASE + "09-Idle Sword");
        sprite.loadState("run", SPRITE_BASE + "10-Run Sword");
        sprite.loadState("jump", SPRITE_BASE + "11-Jump Sword");
        sprite.loadState("fall", SPRITE_BASE + "12-Fall Sword");
        sprite.loadState("attack", SPRITE_BASE + "15-Attack 1");
        sprite.loadState("hit", SPRITE_BASE + "14-Hit Sword");
    }

    public void setLevelWidth(int levelWidth) {
        this.levelWidth = levelWidth;
    }

    @Override
    public void update() {
        // Movement - slower while attacking
        float dx = 0;
        float speed = attacking ? MOVE_SPEED * 0.3f : MOVE_SPEED;
        if (left) dx -= speed;
        if (right) dx += speed;
        x += dx;

        if (right) facingRight = true;
        if (left) facingRight = false;

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
                attackCooldownTimer = ATTACK_COOLDOWN;
                hitEnemies.clear();
            }
        }

        if (attackCooldownTimer > 0) attackCooldownTimer--;
        if (damageCooldown > 0) damageCooldown--;

        if (x < 0) x = 0;
        if (x + width > levelWidth) x = levelWidth - width;

        updateAnimation();
        sprite.update();
    }

    private void updateAnimation() {
        sprite.setFlipped(!facingRight);

        if (attacking) {
            sprite.setState("attack");
        } else if (damageCooldown > DAMAGE_COOLDOWN_MAX - 15) {
            sprite.setState("hit");
        } else if (inAir) {
            sprite.setState(velocityY < 0 ? "jump" : "fall");
        } else if (left || right) {
            sprite.setState("run");
        } else {
            sprite.setState("idle");
        }
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
        int drawW = 64 * DRAW_SCALE;
        int drawH = 40 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + 20;

        if (damageCooldown > 0 && (damageCooldown / 5) % 2 == 0) return;

        sprite.draw(g, drawX, drawY, drawW, drawH);
    }

    public Rectangle getAttackBounds() {
        if (!attacking) return null;
        // Only active during middle frames of swing
        if (attackTick < 4 || attackTick > ATTACK_DURATION - 4) return null;
        int attackX = facingRight ? (int) x + width - 5 : (int) x - ATTACK_WIDTH + 5;
        int attackY = (int) y + (height - ATTACK_HEIGHT) / 2;
        return new Rectangle(attackX, attackY, ATTACK_WIDTH, ATTACK_HEIGHT);
    }

    public boolean hasHitEnemy(Object enemy) {
        return hitEnemies.contains(enemy);
    }

    public void markEnemyHit(Object enemy) {
        hitEnemies.add(enemy);
    }

    @Override
    public void takeDamage(int amount) {
        if (damageCooldown > 0) return;
        health -= amount;
        if (health < 0) health = 0;
        damageCooldown = DAMAGE_COOLDOWN_MAX;
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }

    public void heal() {
        if (health < MAX_HEALTH) health++;
    }

    public void attack() {
        if (!attacking && attackCooldownTimer <= 0) {
            attacking = true;
            attackTick = 0;
            hitEnemies.clear();
        }
    }

    public void setLeft(boolean left) { this.left = left; }
    public void setRight(boolean right) { this.right = right; }
    public void setJumping(boolean jumping) { this.jumping = jumping; }

    public int getHealth() { return health; }
    public int getMaxHealth() { return MAX_HEALTH; }
    public boolean isAttacking() { return attacking; }
}
