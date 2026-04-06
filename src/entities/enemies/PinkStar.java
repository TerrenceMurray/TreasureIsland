package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Graphics2D;

public class PinkStar extends Enemy {

    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Pink Star/";
    private static final int DRAW_SCALE = 2;

    private float patrolSpeed = 1.5f;
    private float patrolLeft;
    private float patrolRight;
    private boolean movingRight = true;
    private Player target;
    private int attackDelay;
    private static final int ATTACK_DELAY_MAX = 30;

    private AnimatedSprite sprite;

    public PinkStar(float x, float y, Player target) {
        super(x, y, 34, 30, 3, 1);
        this.patrolLeft = x - 80;
        this.patrolRight = x + 80;
        this.target = target;
        initSprite();
    }

    private void initSprite() {
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", SPRITE_BASE + "01-Idle");
        sprite.loadState("run", SPRITE_BASE + "02-Run");
        sprite.loadState("hit", SPRITE_BASE + "08-Hit");
        sprite.loadState("dead", SPRITE_BASE + "10-Dead Ground");
    }

    @Override
    public void update() {
        if (isDead()) return;
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
            sprite.setState("idle");
            sprite.setFlipped(!movingRight);
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
        sprite.setFlipped(!movingRight);
        sprite.update();
    }

    @Override
    public boolean canDealDamage() {
        return attackDelay >= ATTACK_DELAY_MAX;
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead()) return;
        int drawW = 34 * DRAW_SCALE;
        int drawH = 30 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + 10;
        sprite.draw(g, drawX, drawY, drawW, drawH);
        drawHealthBar(g);
    }
}
