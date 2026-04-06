package entities.enemies;

import entities.Player;
import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Graphics2D;

public class FierceTooth extends Boss {

    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Fierce Tooth/";
    private static final int DRAW_SCALE = 2;

    public FierceTooth(float x, float y, Player target) {
        super(x, y, 56, 64, 6, 1, 150, target);
        initSprite();
    }

    private void initSprite() {
        sprite = new AnimatedSprite(6);
        sprite.loadState("idle", SPRITE_BASE + "01-Idle");
        sprite.loadState("run", SPRITE_BASE + "02-Run");
        sprite.loadState("attack", SPRITE_BASE + "07-Attack");
        sprite.loadState("hit", SPRITE_BASE + "08-Hit");
        sprite.loadState("dead", SPRITE_BASE + "10-Dead Ground");
    }

    @Override
    protected float getWalkSpeed() {
        return 1.2f;
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
