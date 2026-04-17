package entities.enemies;

import entities.Player;
import engine.ImageManager;
import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
    The Boss class is the abstract base for end-of-level
    bosses. It tracks the player, walks toward them, lunges
    when close, and can jump to reach higher platforms. It
    also draws a name tag and a skull icon above the sprite.
    Vertical motion uses the kinematic equation s = ut + ½at².
*/
public abstract class Boss extends Enemy {

    // === AI target and timing ===
    protected Player target;
    // Frames between allowed lunge attacks.
    protected int attackInterval;
    // Counts up each frame; lunges when it reaches attackInterval.
    protected int attackTimer;

    // === Attack state ===
    protected boolean lunging;
    // Current frame within the lunge animation.
    protected int lungeTick;
    // Total length of a lunge in frames.
    protected static final int LUNGE_DURATION = 20;

    // === Facing and animation ===
    protected boolean facingRight;
    protected AnimatedSprite sprite;
    protected String bossName;

    // === Skull icon ===
    private static final String SKULL_SHEET = "assets/Skull_Icons/PNG/Skull_Icons_128x96.png";
    // Tile size within the 4×3 skull tileset (128/4 = 32, 96/3 = 32).
    private static final int SKULL_TILE = 32;
    // Loaded once, shared by all bosses.
    private static BufferedImage skullSheet;
    // Per-boss sub-image carved out of the sheet.
    private BufferedImage skullIcon;

    // === Jump physics ===
    // Prevents the boss from jumping repeatedly; counts down each frame.
    private int jumpCooldown;
    // 1.5 seconds at 60fps between jumps.
    private static final int JUMP_COOLDOWN_MAX = 90;
    // Negative = upward (Java y grows downward).
    private static final float JUMP_FORCE = -9f;
    // Kinematic airborne state: y(t) = airStartY + airStartVel*t + ½·GRAVITY·t²
    private int airTime;
    private float airStartY;
    private float airStartVel;

    /**
        Creates a new Boss with the given stats, target, and
        display name. The skull column and row pick a sub-image
        from the shared skull icon tileset.
    */
    public Boss(float x, float y, int width, int height, int health, int damage, int attackInterval, Player target, String bossName, int skullCol, int skullRow) {
        super(x, y, width, height, health, damage);
        this.attackInterval = attackInterval;
        this.target = target;
        this.bossName = bossName;

        if (skullSheet == null) skullSheet = ImageManager.loadBufferedImage(SKULL_SHEET);
        if (skullSheet != null) {
            skullIcon = skullSheet.getSubimage(skullCol * SKULL_TILE, skullRow * SKULL_TILE, SKULL_TILE, SKULL_TILE);
        }
    }

    /**
        Advances the boss AI one frame. Chases the player,
        jumps up to higher platforms, and starts a lunge
        attack when close enough.
    */
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
            // 3 px/frame forward dash during the lunge.
            float lungeSpeed = facingRight ? 3f : -3f;
            // Only move during the first half of the lunge; the back half is recovery.
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

        // Jump toward player if they're on a higher platform. 60 px up is roughly
        // one platform height — below that we just walk toward them.
        if (dy < -60 && !inAir && jumpCooldown <= 0) {
            velocityY = JUMP_FORCE;
            setInAir(true);  // captures airStartY and airStartVel from current velocityY
            jumpCooldown = JUMP_COOLDOWN_MAX;
        }

        // Stop walking once we're within one body-width of the player so we don't
        // overshoot and jitter on top of them.
        if (dist > this.width) {
            x += (dx > 0 ? walkSpeed : -walkSpeed);
            sprite.setState(inAir ? "run" : "run");
        } else {
            sprite.setState("idle");
        }

        attackTimer++;
        // Lunge when: cooldown elapsed, player is in range (within 2 body-widths),
        // and we're on the ground.
        if (attackTimer >= attackInterval && dist < this.width * 2 && !inAir) {
            lunging = true;
            lungeTick = 0;
            attackTimer = 0;
        }

        sprite.setFlipped(facingRight);
        sprite.update();
    }

    /**
        Returns the boss's bounding box. While lunging, the
        box is extended forward so the sword reach counts as
        part of the hitbox.
    */
    @Override
    public java.awt.geom.Rectangle2D.Double getBoundingRectangle() {
        if (lunging) {
            // Extend hitbox forward by half a body-width to cover the sword reach.
            int attackReach = width / 2;
            int bx = facingRight ? (int) x : (int) x - attackReach;
            return new java.awt.geom.Rectangle2D.Double(bx, y, width + attackReach, height);
        }
        return super.getBoundingRectangle();
    }

    @Override
    public boolean canDealDamage() {
        return lunging;
    }

    /**
        Draws the boss name tag in white with a black outline
        above the boss sprite.
    */
    protected void drawBossName(Graphics2D g) {
        if (dying) return;
        g.setFont(new Font("Monospaced", Font.BOLD, 14));

        int nameW = g.getFontMetrics().stringWidth(bossName);
        // Centre the name above the boss (18 px above the top of the hitbox).
        int textX = (int) x + width / 2 - nameW / 2;
        int textY = (int) y - 18;

        // 1px black outline: draw the text shifted by (-1..1, -1..1), skipping
        // the centre, so the white fill on top reads clearly on any background.
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                g.drawString(bossName, textX + dx, textY + dy);
            }
        }

        g.setColor(Color.WHITE);
        g.drawString(bossName, textX, textY);
    }

    /**
        Draws this boss's skull icon above the name tag.
    */
    protected void drawSkull(Graphics2D g) {
        if (dying || isDead() || skullIcon == null) return;
        int skullW = 32;
        int skullH = 32;
        // Centre horizontally over the boss. The first -32 lifts above the
        // name tag, and -skullH stacks the skull on top of that.
        int skullX = (int) x + width / 2 - skullW / 2;
        int skullY = (int) y - 32 - skullH;
        g.drawImage(skullIcon, skullX, skullY, skullW, skullH, null);
    }

    /**
        Returns how many pixels the boss moves per frame
        while walking. Each boss subclass picks its own pace.
    */
    protected abstract float getWalkSpeed();

    /**
        Applies one frame of gravity using the kinematic
        equation s = ut + ½at², overriding the simpler
        per-frame accumulation in Enemy. This gives bosses a
        smooth parabolic jump arc.
    */
    @Override
    public void applyGravity() {
        if (!inAir) return;
        airTime++;
        y = airStartY + airStartVel * airTime + 0.5f * GRAVITY * airTime * airTime;
        velocityY = airStartVel + GRAVITY * airTime;
    }

    /**
        Sets whether the boss is airborne. When it first
        leaves the ground, the kinematic jump state is
        captured from the current velocity.
    */
    @Override
    public void setInAir(boolean newInAir) {
        if (newInAir && !inAir) {
            airStartY = y;
            airTime = 0;
            airStartVel = velocityY;
        }
        super.setInAir(newInAir);
    }
}
