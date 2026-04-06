package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Graphics2D;

public class Crabby extends Enemy {

    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Crabby/";
    private static final int DRAW_SCALE = 2;

    private float detectionRange = 100f;
    private float leashRange = 180f;
    private float idleSpeed = 0.5f;
    private float approachSpeed = 1f;
    private float lungeSpeed = 6f;
    private boolean charging;
    private boolean lunging;
    private int lungeTick;
    private static final int LUNGE_DURATION = 15;
    private static final int LUNGE_COOLDOWN = 60;
    private int lungeCooldownTimer;
    private int aggroDelay;
    private static final int AGGRO_DELAY_MAX = 90;
    private Player target;
    private float spawnX;
    private boolean facingRight;

    private AnimatedSprite sprite;
    private AnimatedSprite attackEffect;

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
        int drawW = 72 * DRAW_SCALE;
        int drawH = 32 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + 10;
        drawWithDeathFade(g, () -> sprite.draw(g, drawX, drawY, drawW, drawH));
        drawHealthBar(g);
    }

    @Override
    public void drawEffect(Graphics2D g) {
        if (lunging) {
            int drawW = 72 * DRAW_SCALE;
            int drawX = (int) x - (drawW - width) / 2;
            int drawY = (int) y + height - 32 * DRAW_SCALE + 10;
            int effectW = 118 * DRAW_SCALE;
            int effectH = 24 * DRAW_SCALE;
            int effectX = drawX + (drawW - effectW) / 2;
            attackEffect.draw(g, effectX, drawY, effectW, effectH);
        }
    }
}
