package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

public class PinkStar extends Enemy {

    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Pink Star/";
    private static final int DRAW_SCALE = 2;

    private float patrolSpeed = 0.8f;
    private float patrolLeft;
    private float patrolRight;
    private boolean movingRight = true;
    private Player target;
    private int attackDelay;
    private static final int ATTACK_DELAY_MAX = 30;

    private AnimatedSprite sprite;
    private AnimatedSprite attackEffect;

    public PinkStar(float x, float y, Player target, List<Rectangle> platforms) {
        super(x, y, 34, 30, 3, 1);
        this.target = target;
        // Find the platform this enemy stands on and clamp patrol to its edges
        this.patrolLeft = x - 80;
        this.patrolRight = x + 80;
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
        boolean playerAbove = target.getY() + target.getHeight() < y;

        if (dx < width && !playerAbove) {
            movingRight = target.getX() > x;
            if (attackDelay < ATTACK_DELAY_MAX) attackDelay++;
            sprite.setState(attackDelay > ATTACK_DELAY_MAX / 2 ? "anticipation" : "idle");
            sprite.setFlipped(movingRight);
            sprite.update();
            if (canDealDamage()) {
                attackEffect.setState("effect");
                attackEffect.setFlipped(movingRight);
                attackEffect.update();
            }
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
    public boolean canDealDamage() {
        return attackDelay >= ATTACK_DELAY_MAX;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead() && !dying) return;
        int drawW = 34 * DRAW_SCALE;
        int drawH = 30 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + 10;
        drawWithDeathFade(g, () -> sprite.draw(g, drawX, drawY, drawW, drawH));
        drawHealthBar(g);
    }

    @Override
    public void drawEffect(Graphics2D g) {
        if (canDealDamage()) {
            int effectW = 16 * DRAW_SCALE;
            int effectH = 12 * DRAW_SCALE;
            int effectX = movingRight ? (int) x + width : (int) x - effectW;
            int effectY = (int) y + height / 2 - effectH / 2;
            attackEffect.draw(g, effectX, effectY, effectW, effectH);
        }
    }
}
