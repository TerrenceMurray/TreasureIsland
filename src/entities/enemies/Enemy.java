package entities.enemies;

import entities.GameEntity;
import interfaces.Attackable;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
    The Enemy class is the abstract base for hostile
    creatures. It holds the shared logic for health, taking
    damage, the two-phase death animation, knockback, and
    gravity.
*/
public abstract class Enemy extends GameEntity implements Attackable {

    // === Health ===
    protected int health;
    protected int maxHealth;
    // Damage dealt to the player on contact (when canDealDamage() is true).
    protected int damage;

    // === Combat timers (in frames, 60fps) ===
    // Counts down while playing the brief flinch/hit animation.
    protected int hurtTimer;
    // How long the hit flinch animation plays.
    protected static final int HURT_DURATION = 15;
    // Counts down through the full death animation sequence.
    protected int deathTimer;
    // First phase: enemy takes the fatal blow (airborne/flinching).
    protected static final int DEATH_HIT_DURATION = 36;
    // Second phase: enemy lies on the ground and fades out.
    protected static final int DEATH_GROUND_DURATION = 60;
    protected static final int DEATH_TOTAL = DEATH_HIT_DURATION + DEATH_GROUND_DURATION;

    // === State flags ===
    // True while the death animation is playing; isDead() becomes true once it finishes.
    protected boolean dying;
    // Horizontal push-back velocity after being hit; decays toward 0 each frame.
    protected float knockbackVel;
    // True while the enemy is not standing on a platform. Starts true so gravity
    // drops newly spawned enemies onto the ground.
    protected boolean inAir = true;

    // === Physics ===
    // Downward acceleration applied each frame while airborne.
    protected static final float GRAVITY = 0.5f;

    public Enemy(float x, float y, int width, int height, int health, int damage) {
        super(x, y, width, height);
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
    }

    /**
        Reduces health by the given amount. Starts the death
        animation if health reaches zero, otherwise starts a
        short hurt animation.
    */
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

    /**
        Returns true while the enemy is in the first phase
        of the death animation (being hit), before it falls
        to the ground.
    */
    public boolean inDeathHitPhase() {
        return dying && deathTimer > DEATH_GROUND_DURATION;
    }

    private float getDeathAlpha() {
        if (!dying) return 1f;
        if (deathTimer > DEATH_GROUND_DURATION) return 1f;
        return (float) deathTimer / DEATH_GROUND_DURATION;
    }

    /**
        Advances the death animation timer. Should be called
        once per frame while the enemy is dying.
    */
    public void updateDeath() {
        if (dying) {
            deathTimer--;
            if (deathTimer <= 0) {
                dying = false;
            }
        }
    }

    /**
        Sets a horizontal knockback velocity that decays on
        each update.
    */
    public void knockback(float amount) {
        knockbackVel = amount;
    }

    /**
        Applies one frame of gravity to the enemy's vertical
        velocity and position.
    */
    public void applyGravity() {
        velocityY += GRAVITY;
        y += velocityY;
    }

    /**
        Places the enemy on top of a floor at the given y
        coordinate and clears vertical velocity.
    */
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
            // Friction: each frame we keep 80% of the previous push, so the
            // knockback decays exponentially.
            knockbackVel *= 0.8f;
            // Snap very small velocities to 0 so the enemy doesn't drift forever.
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
        // Default: 10 px above the top of the collision box.
        drawHealthBar(g, (int) y - 10);
    }

    protected void drawHealthBar(Graphics2D g, int barY) {
        // Hide bar at full health (no clutter) and while dying.
        if (health >= maxHealth || dying) return;
        int barW = width;
        int barH = 4;  // thin bar
        int barX = (int) x;
        // Dark grey background behind the red fill.
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);
        g.setColor(Color.RED);
        int fillW = (int) (barW * ((float) health / maxHealth));
        g.fillRect(barX, barY, fillW, barH);
    }

    /**
        Draws any extra visual effect for the enemy (such as
        an attack swing). Subclasses override as needed; by
        default no effect is drawn.
    */
    public void drawEffect(Graphics2D g) {}

    public int getDamage() { return damage; }

    /**
        Returns whether the enemy can currently damage the
        player. Subclasses override to restrict damage to
        their attacking frames.
    */
    public boolean canDealDamage() { return true; }
}
