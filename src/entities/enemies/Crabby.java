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
    private float chargeSpeed = 1f;
    private boolean charging;
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
            } else {
                charging = true;
            }
        } else if (charging && (playerDistFromSpawn > leashRange || playerAbove)) {
            charging = false;
            aggroDelay = 0;
        } else if (!charging) {
            aggroDelay = 0;
        }

        if (charging) {
            float dx = target.getX() - x;
            float dist = Math.abs(dx);
            facingRight = dx > 0;
            if (dist > width) {
                x += (dx > 0 ? chargeSpeed : -chargeSpeed);
            }
            sprite.setState("run");
            attackEffect.setState("effect");
            attackEffect.setFlipped(facingRight);
            attackEffect.update();
        } else {
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
        return charging;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead() && !dying) return;
        int drawW = 72 * DRAW_SCALE;
        int drawH = 32 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + 10;
        drawWithDeathFade(g, () -> sprite.draw(g, drawX, drawY, drawW, drawH));

        if (charging && !dying) {
            int effectW = 118 * DRAW_SCALE;
            int effectH = 24 * DRAW_SCALE;
            int effectX = drawX + (drawW - effectW) / 2;
            int effectY = drawY;
            attackEffect.draw(g, effectX, effectY, effectW, effectH);
        }

        drawHealthBar(g);
    }
}
