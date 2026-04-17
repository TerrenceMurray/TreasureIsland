package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Graphics2D;

/**
    The Crabby class is a ground enemy that patrols near its
    spawn point. When the player gets close, it pauses to
    show an exclamation mark, then charges and lunges. It
    returns to its spawn point if the player leaves its
    leash range.
*/
public class Crabby extends Enemy {

    // === Config ===
    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Crabby/";
    private static final String DIALOGUE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Dialogue/Exclamation/";
    // Each source sprite pixel is drawn as a DRAW_SCALE×DRAW_SCALE block.
    private static final int DRAW_SCALE = 2;
    // Empty pixels below the feet in the source frames (3 src × scale).
    private static final int FOOT_PADDING = 3 * DRAW_SCALE;

    // === AI tuning (pixels / pixels per frame) ===
    // How close the player must get (horizontally, from spawn) before aggro.
    private float detectionRange = 100f;
    // If the player goes beyond this distance from spawn, disengage.
    private float leashRange = 180f;
    // Patrol back to spawn at 0.5 px/frame.
    private float idleSpeed = 0.5f;
    // Walk speed while chasing the player.
    private float approachSpeed = 1f;
    // Speed during the forward lunge attack.
    private float lungeSpeed = 6f;

    // === Attack timing (frames) ===
    private static final int LUNGE_DURATION = 15;
    // 1 second at 60fps between lunges.
    private static final int LUNGE_COOLDOWN = 60;
    // 1.5 seconds of "!!" anticipation before first lunge.
    private static final int AGGRO_DELAY_MAX = 90;

    // === State flags and timers ===
    private boolean charging;
    private boolean lunging;
    private int lungeTick;
    private int lungeCooldownTimer;
    private int aggroDelay;
    private boolean facingRight;

    // === References and spawn anchor ===
    private Player target;
    // Remembered spawn x so the crab can patrol back when the player leaves.
    private float spawnX;

    // === Animation ===
    private AnimatedSprite sprite;
    private AnimatedSprite attackEffect;
    private AnimatedSprite exclamation;

    public Crabby(float x, float y, Player target) {
        super(x, y, 40, 30, 5, 1);
        this.target = target;
        this.spawnX = x;
        initSprite();
    }

    private void initSprite() {
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", SPRITE_BASE + "01-Idle");
        sprite.loadState("anticipation", SPRITE_BASE + "06-Anticipation");
        sprite.loadState("run", SPRITE_BASE + "02-Run");
        sprite.loadState("hit", SPRITE_BASE + "08-Hit");
        sprite.loadState("deadhit", SPRITE_BASE + "09-Dead Hit");
        sprite.loadState("dead", SPRITE_BASE + "10-Dead Ground");

        attackEffect = new AnimatedSprite(6);
        attackEffect.loadState("effect", SPRITE_BASE + "11-Attack Effect");

        exclamation = new AnimatedSprite(6);
        exclamation.loadState("show", DIALOGUE_BASE);
    }

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

        float playerDistFromSpawn = Math.abs(target.getX() - spawnX);
        boolean playerAbove = target.getY() + target.getHeight() < y;

        if (!charging && playerDistFromSpawn < detectionRange && !playerAbove) {
            if (aggroDelay < AGGRO_DELAY_MAX) {
                aggroDelay++;
                facingRight = target.getX() > x;
                sprite.setState("anticipation");
                sprite.setFlipped(facingRight);
                exclamation.setState("show");
                exclamation.update();
            } else {
                charging = true;
            }
        } else if (charging && (playerDistFromSpawn > leashRange || playerAbove)) {
            charging = false;
            aggroDelay = 0;
        } else if (!charging) {
            aggroDelay = 0;
        }

        if (lungeCooldownTimer > 0) lungeCooldownTimer--;

        if (charging) {
            float dx = target.getX() - x;
            float dist = Math.abs(dx);
            facingRight = dx > 0;

            if (lunging) {
                // Lunge in progress
                lungeTick++;
                x += (facingRight ? lungeSpeed : -lungeSpeed);
                sprite.setState("run");
                attackEffect.setState("effect");
                attackEffect.setFlipped(facingRight);
                attackEffect.update();
                if (lungeTick >= LUNGE_DURATION) {
                    lunging = false;
                    lungeCooldownTimer = LUNGE_COOLDOWN;
                }
            } else if (dist < width * 2 && lungeCooldownTimer <= 0) {
                // Close enough — start lunge
                lunging = true;
                lungeTick = 0;
            } else if (dist > width) {
                // Approach the player
                x += (dx > 0 ? approachSpeed : -approachSpeed);
                sprite.setState("run");
            } else {
                sprite.setState("idle");
            }
        } else {
            lunging = false;
            float toSpawn = spawnX - x;
            if (Math.abs(toSpawn) > idleSpeed) {
                facingRight = toSpawn > 0;
                x += (toSpawn > 0 ? idleSpeed : -idleSpeed);
                sprite.setState("run");
            } else {
                x = spawnX;
                sprite.setState("idle");
            }
        }

        sprite.setFlipped(facingRight);
        sprite.update();
    }

    @Override
    public boolean canDealDamage() {
        return lunging;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead() && !dying) return;
        // Source sprite is 72×32 px; scale and centre over the collision box.
        int drawW = 72 * DRAW_SCALE;
        int drawH = 32 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + FOOT_PADDING;
        drawWithDeathFade(g, () -> sprite.draw(g, drawX, drawY, drawW, drawH));

        // Health bar above sprite (Crabby's sprite is much taller than its
        // collision box, so the default position would be inside the sprite).
        // 6 px above the top of the drawn sprite.
        drawHealthBar(g, drawY - 6);

        // "!!" exclamation during aggro windup.
        if (aggroDelay > 0 && !charging && !dying) {
            // Source icon is 14×12 px; drawn at 1.75× to read clearly without scale doubling.
            int exW = (int)(14 * 1.75f);
            int exH = (int)(12 * 1.75f);
            // Float the icon just off the facing side of the crab, above its head.
            int exX = facingRight ? (int) x + width + 2 : (int) x - exW - 2;
            int exY = (int) y - exH;
            exclamation.draw(g, exX, exY, exW, exH);
        }
    }

    @Override
    public void drawEffect(Graphics2D g) {
        if (lunging) {
            // Recompute the sprite's drawn footprint so we can centre the
            // wide attack dust cloud over it.
            int drawW = 72 * DRAW_SCALE;
            int drawX = (int) x - (drawW - width) / 2;
            // +10 lowers the dust cloud so it reads as ground-level spray
            // rather than floating around the torso.
            int drawY = (int) y + height - 32 * DRAW_SCALE + 10;
            // Effect sprite is 118×24 px.
            int effectW = 118 * DRAW_SCALE;
            int effectH = 24 * DRAW_SCALE;
            int effectX = drawX + (drawW - effectW) / 2;
            attackEffect.draw(g, effectX, drawY, effectW, effectH);
        }
    }
}
