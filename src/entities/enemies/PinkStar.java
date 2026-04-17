package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
    The PinkStar class is a platform-bound enemy that patrols
    back and forth within the bounds of the platform it spawns
    on. When the player comes within range, it pauses with an
    exclamation mark and then performs a short attack.
*/
public class PinkStar extends Enemy {

    // === Config ===
    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Pink Star/";
    private static final String DIALOGUE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Dialogue/Exclamation/";
    // Each source sprite pixel is drawn as a DRAW_SCALE×DRAW_SCALE block.
    private static final int DRAW_SCALE = 2;
    // Empty pixels below the feet in the source frames (1 src × scale).
    private static final int FOOT_PADDING = 1 * DRAW_SCALE;

    // === AI tuning ===
    // 0.6 px/frame patrol pace — slower than the crab so it reads as a different creature.
    private float patrolSpeed = 0.6f;
    // Left/right patrol boundaries, clipped to the hosting platform.
    private float patrolLeft;
    private float patrolRight;

    // === Attack timing ===
    // Anticipation window (in frames) before a close-range strike; ~0.5s at 60fps.
    private static final int ATTACK_DELAY_MAX = 30;

    // === State flags and timers ===
    private boolean movingRight = true;
    private int attackDelay;
    // Counts down while the slash effect is on screen.
    private int attackEffectTimer;

    // === References ===
    private Player target;

    // === Animation ===
    private AnimatedSprite sprite;
    private AnimatedSprite attackEffect;
    private AnimatedSprite exclamation;

    public PinkStar(float x, float y, Player target, List<Rectangle> platforms) {
        super(x, y, 34, 30, 3, 1);
        this.target = target;
        // Default patrol: 80 px either side of spawn.
        this.patrolLeft = x - 80;
        this.patrolRight = x + 80;
        // Narrow the patrol to the platform this star is standing on, so it
        // can't walk off the edge. The ±5/+10 y tolerance is because the
        // spawner rounds coordinates; anything within 10 px of the platform
        // top is counted as standing on it.
        for (Rectangle plat : platforms) {
            if (x >= plat.x && x <= plat.x + plat.width && y + height >= plat.y - 5 && y + height <= plat.y + 10) {
                patrolLeft = Math.max(patrolLeft, plat.x);
                patrolRight = Math.min(patrolRight, plat.x + plat.width - width);
                break;
            }
        }
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

        float dx = Math.abs(target.getX() - x);
        // "Above" means the player is standing higher than this enemy, so we
        // won't engage (would just run off the platform).
        boolean playerAbove = target.getY() + target.getHeight() < y;

        // Trigger range: body width plus 20 px buffer.
        if (dx < width + 20 && !playerAbove) {
            movingRight = target.getX() > x;
            if (attackDelay < ATTACK_DELAY_MAX) {
                attackDelay++;
                // First half of windup stays idle, second half shows the
                // anticipation crouch for clarity.
                sprite.setState(attackDelay > ATTACK_DELAY_MAX / 2 ? "anticipation" : "idle");
                exclamation.setState("show");
                exclamation.update();
            } else {
                sprite.setState("idle");
            }
            sprite.setFlipped(movingRight);
            sprite.update();
            return;
        }

        attackDelay = 0;

        if (movingRight) {
            x += patrolSpeed;
            if (x >= patrolRight) movingRight = false;
        } else {
            x -= patrolSpeed;
            if (x <= patrolLeft) movingRight = true;
        }

        sprite.setState("run");
        sprite.setFlipped(movingRight);
        sprite.update();
    }

    @Override
    public java.awt.geom.Rectangle2D.Double getBoundingRectangle() {
        if (canDealDamage()) {
            // Extend hitbox forward when attacking
            int reach = width;
            int bx = movingRight ? (int) x : (int) x - reach;
            return new java.awt.geom.Rectangle2D.Double(bx, y, width + reach, height);
        }
        return super.getBoundingRectangle();
    }

    @Override
    public boolean canDealDamage() {
        // First frame the attack becomes active: kick off the slash effect
        // animation (18 frames = 3 sprite ticks at frameDelay 6).
        if (attackDelay >= ATTACK_DELAY_MAX && attackEffectTimer == 0) {
            attackEffectTimer = 18;
            attackEffect.setState("effect");
            attackEffect.setFlipped(movingRight);
        }
        if (attackEffectTimer > 0) {
            attackEffectTimer--;
            attackEffect.update();
        }
        return attackDelay >= ATTACK_DELAY_MAX;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead() && !dying) return;
        // Source sprite is 34×30 px; scale and centre over the collision box.
        int drawW = 34 * DRAW_SCALE;
        int drawH = 30 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + FOOT_PADDING;
        drawWithDeathFade(g, () -> sprite.draw(g, drawX, drawY, drawW, drawH));
        drawHealthBar(g);

        // "!" icon during anticipation (only before the strike, not after).
        if (attackDelay > 0 && attackDelay < ATTACK_DELAY_MAX && !dying) {
            // Source icon is 14×12 px; drawn at 1.75× so it reads clearly.
            int exW = (int)(14 * 1.75f);
            int exH = (int)(12 * 1.75f);
            int exX = movingRight ? (int) x + width + 2 : (int) x - exW - 2;
            int exY = (int) y - exH;
            exclamation.draw(g, exX, exY, exW, exH);
        }
    }

    @Override
    public void drawEffect(Graphics2D g) {
        if (attackEffectTimer > 0) {
            // Slash effect source is 16×12 px.
            int effectW = 16 * DRAW_SCALE;
            int effectH = 12 * DRAW_SCALE;
            // Place the slash on the facing side, centred vertically over the body.
            int effectX = movingRight ? (int) x + width : (int) x - effectW;
            int effectY = (int) y + height / 2 - effectH / 2;
            attackEffect.draw(g, effectX, effectY, effectW, effectH);
        }
    }
}
