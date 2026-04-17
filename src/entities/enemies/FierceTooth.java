package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Graphics2D;

public class FierceTooth extends Boss {

    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Fierce Tooth/";
    private static final int DRAW_SCALE = 2;
    private static final int FOOT_PADDING = 2 * DRAW_SCALE;
    private AnimatedSprite attackEffect;

    public FierceTooth(float x, float y, Player target) {
        super(x, y, 56, 64, 6, 1, 150, target, "Fierce Tooth");
        initSprite();
    }

    private void initSprite() {
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", SPRITE_BASE + "01-Idle");
        sprite.loadState("run", SPRITE_BASE + "02-Run");
        sprite.loadState("attack", SPRITE_BASE + "07-Attack");
        sprite.loadState("hit", SPRITE_BASE + "08-Hit");
        sprite.loadState("deadhit", SPRITE_BASE + "09-Dead Hit");
        sprite.loadState("dead", SPRITE_BASE + "10-Dead Ground");

        attackEffect = new AnimatedSprite(6);
        attackEffect.loadState("effect", SPRITE_BASE + "11-Attack Effect");
    }

    @Override
    protected float getWalkSpeed() {
        return 1.2f;
    }

    @Override
    public void update() {
        super.update();
        if (lunging) {
            attackEffect.setState("effect");
            attackEffect.setFlipped(facingRight);
            attackEffect.update();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead() && !dying) return;
        int drawW = 34 * DRAW_SCALE;
        int drawH = 30 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + FOOT_PADDING;
        drawWithDeathFade(g, () -> sprite.draw(g, drawX, drawY, drawW, drawH));
        drawHealthBar(g);
        drawBossName(g);
    }

    @Override
    public void drawEffect(Graphics2D g) {
        if (lunging) {
            int effectW = 22 * DRAW_SCALE;
            int effectH = 24 * DRAW_SCALE;
            int effectX = facingRight ? (int) x + width : (int) x - effectW;
            int effectY = (int) y + height / 2 - effectH / 2;
            attackEffect.draw(g, effectX, effectY, effectW, effectH);
        }
    }
}
