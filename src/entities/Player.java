package entities;

import interfaces.Attackable;
import engine.GameConfig;
import rendering.AnimatedSprite;
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
    // Empty pixels below the character's feet in source frames (8 src × scale)
    private static final int FOOT_PADDING = 8 * DRAW_SCALE;
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
    private static final int DAMAGE_COOLDOWN_MAX = 60;
    private int hurtTimer;
    private static final int HURT_DURATION = 12;
    private float knockbackVel;
    private boolean dying;
    private int deathTimer;
    private static final int DEATH_HIT_DURATION = 36;
    private static final int DEATH_GROUND_DURATION = 60;
    private static final int DEATH_TOTAL = DEATH_HIT_DURATION + DEATH_GROUND_DURATION;
    private static final String DEATH_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose without Sword/";

    private AnimatedSprite sprite;
    private AnimatedSprite attackEffect;
    private static final String EFFECT_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Sword Effects/";

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

        attackEffect = new AnimatedSprite(6);
        attackEffect.loadState("slash", EFFECT_BASE + "24-Attack 1");

        sprite.loadState("deadhit", DEATH_BASE + "07-Dead Hit");
        sprite.loadState("dead", DEATH_BASE + "08-Dead Ground");
    }

    public void setLevelWidth(int levelWidth) {
        this.levelWidth = levelWidth;
    }

    @Override
    public void update() {
        if (health <= 0) {
            if (dying) {
                deathTimer--;
                String deathState = deathTimer > DEATH_GROUND_DURATION ? "deadhit" : "dead";
                sprite.setState(deathState);
                sprite.update();
                if (deathTimer <= 0) dying = false;
            }
            return;
        }

        float dx = 0;
        float speed = attacking ? MOVE_SPEED * 0.3f : MOVE_SPEED;
        if (left) dx -= speed;
        if (right) dx += speed;
        x += dx;

        if (knockbackVel != 0) {
            x += knockbackVel;
            knockbackVel *= 0.8f;
            if (Math.abs(knockbackVel) < 0.5f) knockbackVel = 0;
        }

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
            attackEffect.setState("slash");
            attackEffect.setFlipped(!facingRight);
            attackEffect.update();
            if (attackTick >= ATTACK_DURATION) {
                attacking = false;
                attackTick = 0;
                attackCooldownTimer = ATTACK_COOLDOWN;
                hitEnemies.clear();
            }
        }

        if (attackCooldownTimer > 0) attackCooldownTimer--;
        if (damageCooldown > 0) damageCooldown--;
        if (hurtTimer > 0) hurtTimer--;

        if (x < 0) x = 0;
        if (x + width > levelWidth) x = levelWidth - width;

        updateAnimation();
        sprite.update();
    }

    private void updateAnimation() {
        sprite.setFlipped(!facingRight);

        if (hurtTimer > 0) {
            sprite.setState("hit");
        } else if (attacking) {
            sprite.setState("attack");
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
        if (isDead() && !dying) return;
        int drawW = 64 * DRAW_SCALE;
        int drawH = 40 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + FOOT_PADDING;

        if (dying) {
            float alpha = deathTimer > DEATH_GROUND_DURATION ? 1f : (float) deathTimer / DEATH_GROUND_DURATION;
            java.awt.Composite original = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            sprite.draw(g, drawX, drawY, drawW, drawH);
            g.setComposite(original);
            return;
        }

        if (hurtTimer > 0) {
            java.awt.Composite original = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.3f));
            sprite.draw(g, drawX, drawY, drawW, drawH);
            g.setComposite(original);
            return;
        }

        sprite.draw(g, drawX, drawY, drawW, drawH);

        if (attacking && attackTick >= 4) {
            int effectW = 28 * DRAW_SCALE;
            int effectH = 17 * DRAW_SCALE;
            int effectX = facingRight ? (int) x + width : (int) x - effectW;
            int effectY = (int) y + height / 2 - effectH / 2;
            attackEffect.draw(g, effectX, effectY, effectW, effectH);
        }
    }

    public Rectangle getAttackBounds() {
        if (!attacking) return null;
        if (attackTick < 6 || attackTick > 12) return null;
        // Position from sprite center, not narrow hitbox edge
        int spriteCenterX = (int) x + width / 2;
        int attackX = facingRight ? spriteCenterX + 10 : spriteCenterX - ATTACK_WIDTH - 10;
        int attackY = (int) y + (height - ATTACK_HEIGHT) / 2;
        return new Rectangle(attackX, attackY, ATTACK_WIDTH, ATTACK_HEIGHT);
    }

    public boolean hasHitEnemy(Object enemy) {
        return hitEnemies.contains(enemy);
    }

    public void markEnemyHit(Object enemy) {
        hitEnemies.add(enemy);
    }

    public void knockback(float amount) {
        knockbackVel = amount;
    }

    @Override
    public void takeDamage(int amount) {
        if (damageCooldown > 0 || dying) return;
        health -= amount;
        if (health < 0) health = 0;
        if (health == 0) {
            dying = true;
            deathTimer = DEATH_TOTAL;
        } else {
            damageCooldown = DAMAGE_COOLDOWN_MAX;
            hurtTimer = HURT_DURATION;
        }
    }

    @Override
    public boolean isDead() {
        return health <= 0 && !dying;
    }

    public boolean isDying() { return dying; }

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
    public void setHealth(int health) { this.health = Math.min(health, MAX_HEALTH); }
    public int getMaxHealth() { return MAX_HEALTH; }
    public boolean isAttacking() { return attacking; }
}
