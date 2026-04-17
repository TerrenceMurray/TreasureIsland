package entities;

import interfaces.Attackable;
import engine.GameConfig;
import rendering.AnimatedSprite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

/**
    The Player class represents the pirate the user controls.
    It handles movement, jumping, attacking, taking damage,
    death animation, and loading the player's sprite states.
    Vertical motion uses the kinematic equation s = ut + ½at²
    so falls and jumps follow a smooth parabola.
*/
public class Player extends GameEntity implements Attackable {

    // === Config-loaded constants ===
    private static final GameConfig CFG = GameConfig.getInstance();
    // Horizontal speed in pixels/frame while running.
    private static final float MOVE_SPEED = CFG.getFloat("player.speed", 4f);
    // Negative because Java's y-axis grows downward; jump shoots the player up.
    private static final float JUMP_FORCE = -CFG.getFloat("player.jumpForce", 10f);
    // Downward acceleration in pixels/frame² applied while airborne.
    private static final float GRAVITY = CFG.getFloat("player.gravity", 0.5f);
    private static final int MAX_HEALTH = CFG.getInt("player.health", 4);
    // Length of the full attack animation, in frames.
    private static final int ATTACK_DURATION = CFG.getInt("player.attackDuration", 20);
    // Size of the sword swing hitbox.
    private static final int ATTACK_WIDTH = CFG.getInt("player.attackWidth", 45);
    private static final int ATTACK_HEIGHT = CFG.getInt("player.attackHeight", 35);
    // Frames the player must wait between attacks.
    private static final int ATTACK_COOLDOWN = CFG.getInt("player.attackCooldown", 25);

    // === Draw constants ===
    // Each source sprite pixel is drawn as a DRAW_SCALE×DRAW_SCALE block.
    private static final int DRAW_SCALE = 2;
    // Empty pixels below the character's feet in source frames (8 src × scale).
    // Used to line up the sprite's visible feet with the collision box bottom.
    private static final int FOOT_PADDING = 8 * DRAW_SCALE;
    private static final String SPRITE_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose with Sword/";
    private static final String DEATH_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Captain Clown Nose without Sword/";
    private static final String EFFECT_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Captain Clown Nose/Sword Effects/";

    // === Combat timers (frames) ===
    // Grace period after taking a hit so rapid enemy contact can't chain-damage.
    private static final int DAMAGE_COOLDOWN_MAX = 60;
    // Length of the hurt flinch animation.
    private static final int HURT_DURATION = 12;
    // Two-phase death animation lengths.
    private static final int DEATH_HIT_DURATION = 36;     // airborne/flinch
    private static final int DEATH_GROUND_DURATION = 60;  // grounded fade-out
    private static final int DEATH_TOTAL = DEATH_HIT_DURATION + DEATH_GROUND_DURATION;

    // === Level state ===
    private int levelWidth;
    private int health;

    // === Input / facing flags ===
    private boolean left, right, jumping;
    private boolean facingRight = true;

    // === Movement / physics ===
    private boolean inAir;
    // Kinematic airborne state: y(t) = airStartY + airStartVel*t + ½·GRAVITY·t²
    private int airTime;
    private float airStartY;
    private float airStartVel;

    // === Combat state ===
    private boolean attacking;
    // Current frame within the active attack animation.
    private int attackTick;
    // Frames left before the next attack is allowed.
    private int attackCooldownTimer;
    // Enemies already hit by the current swing, so one swing = one hit per enemy.
    private Set<Object> hitEnemies = new HashSet<>();
    // Counts down after taking damage; prevents another hit until it reaches 0.
    private int damageCooldown;
    // Counts down while the hurt flinch animation plays.
    private int hurtTimer;
    // Horizontal push-back applied after taking a hit; decays each frame.
    private float knockbackVel;
    private boolean dying;
    private int deathTimer;

    // === Animation ===
    private AnimatedSprite sprite;
    private AnimatedSprite attackEffect;

    /**
        Creates a new Player at the given location. Health
        starts at the maximum and the player begins in the
        air so gravity pulls it down to the ground.
    */
    public Player(float x, float y) {
        super(x, y,
            CFG.getInt("player.width", 28),
            CFG.getInt("player.height", 50));
        this.health = MAX_HEALTH;
        this.inAir = true;
        this.airStartY = y;
        this.airTime = 0;
        this.airStartVel = 0;
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

    /**
        Updates the player's position, handles input flags
        (left, right, jumping), applies gravity, advances the
        attack and hurt timers, and advances the animation.
    */
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
        // Slow the player to 30% speed while swinging the sword so attacks feel
        // rooted rather than a run-by strike.
        float speed = attacking ? MOVE_SPEED * 0.3f : MOVE_SPEED;
        if (left) dx -= speed;
        if (right) dx += speed;
        x += dx;

        if (knockbackVel != 0) {
            x += knockbackVel;
            // Friction: keep 80% of the push each frame (exponential decay).
            knockbackVel *= 0.8f;
            // Snap very small velocities to 0 so the player doesn't drift forever.
            if (Math.abs(knockbackVel) < 0.5f) knockbackVel = 0;
        }

        if (right) facingRight = true;
        if (left) facingRight = false;

        if (jumping && !inAir) {
            inAir = true;
            airStartY = y;
            airTime = 0;
            airStartVel = JUMP_FORCE;  // JUMP_FORCE is negative (upward in Java coords)
        }

        if (inAir) {
            airTime++;
            // s = ut + ½at² (Java y grows downward, JUMP_FORCE is negative)
            y = airStartY + airStartVel * airTime + 0.5f * GRAVITY * airTime * airTime;
            velocityY = airStartVel + GRAVITY * airTime;
        }

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

    /**
        Places the player on top of a floor at the given y
        coordinate and clears vertical velocity.
    */
    public void landOn(float floorY) {
        y = floorY - height;
        velocityY = 0;
        inAir = false;
    }

    /**
        Stops the player after their head hits the underside
        of a ceiling. Restarts the kinematic fall from here
        with zero vertical velocity so they drop back down.
    */
    public void hitHead(float ceilingBottom) {
        y = ceilingBottom;
        velocityY = 0;
        airStartY = y;
        airTime = 0;
        airStartVel = 0;
    }

    public boolean isInAir() { return inAir; }

    /**
        Sets whether the player is airborne. When the player
        first leaves the ground (e.g. walking off a ledge)
        the kinematic fall is restarted from rest.
    */
    public void setInAir(boolean newInAir) {
        if (newInAir && !inAir) {
            airStartY = y;
            airTime = 0;
            airStartVel = 0;
        }
        this.inAir = newInAir;
    }

    /**
        Draws the player sprite. Applies a fade during the
        death animation and a transparency flash while hurt,
        and draws the sword effect during an attack.
    */
    @Override
    public void draw(Graphics2D g) {
        if (isDead() && !dying) return;
        // Source frames are 64×40 px; scale up and centre horizontally over the
        // collision box, then line up the feet using FOOT_PADDING.
        int drawW = 64 * DRAW_SCALE;
        int drawH = 40 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + FOOT_PADDING;

        if (dying) {
            // First phase (hit) draws at full opacity; second phase (ground)
            // fades linearly from 1 to 0 as the timer runs out.
            float alpha = deathTimer > DEATH_GROUND_DURATION ? 1f : (float) deathTimer / DEATH_GROUND_DURATION;
            java.awt.Composite original = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            sprite.draw(g, drawX, drawY, drawW, drawH);
            g.setComposite(original);
            return;
        }

        if (hurtTimer > 0) {
            // Flash 30% opacity during hurt frames so the player can see they got hit.
            java.awt.Composite original = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.3f));
            sprite.draw(g, drawX, drawY, drawW, drawH);
            g.setComposite(original);
            return;
        }

        sprite.draw(g, drawX, drawY, drawW, drawH);

        // Only draw the sword slash effect once the swing has started (frame 4+),
        // so the effect appears at the moment of impact rather than on wind-up.
        if (attacking && attackTick >= 4) {
            int effectW = 28 * DRAW_SCALE;
            int effectH = 17 * DRAW_SCALE;
            // Place the slash at the hand on the facing side, centred vertically.
            int effectX = facingRight ? (int) x + width : (int) x - effectW;
            int effectY = (int) y + height / 2 - effectH / 2;
            attackEffect.draw(g, effectX, effectY, effectW, effectH);
        }
    }

    /**
        Returns the rectangle that represents the sword swing
        during the active frames of an attack, or null if the
        player is not attacking or the swing is not active.
    */
    public Rectangle2D.Double getAttackBounds() {
        if (!attacking) return null;
        // Active hit window is frames 6-12 of the swing (wind-up/recovery
        // frames don't damage).
        if (attackTick < 6 || attackTick > 12) return null;
        // Position from sprite center, not narrow hitbox edge. The +10 offset
        // lines the box up with the visible sword tip.
        int spriteCenterX = (int) x + width / 2;
        int attackX = facingRight ? spriteCenterX + 10 : spriteCenterX - ATTACK_WIDTH - 10;
        int attackY = (int) y + (height - ATTACK_HEIGHT) / 2;
        return new Rectangle2D.Double(attackX, attackY, ATTACK_WIDTH, ATTACK_HEIGHT);
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

    /**
        Reduces the player's health by the given amount.
        Starts the death animation if health reaches zero,
        otherwise starts a brief damage cooldown and hurt
        animation so rapid hits cannot chain.
    */
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

    /**
        Starts a sword attack if no attack is in progress
        and the cooldown has elapsed.
    */
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
}
