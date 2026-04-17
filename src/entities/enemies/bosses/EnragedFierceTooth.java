package entities.enemies.bosses;

import entities.enemies.Enemy;
import entities.Player;
import rendering.AnimatedSprite;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
    The EnragedFierceTooth class is the final boss. It is a
    larger, faster, red-tinted version of Fierce Tooth with
    a shorter attack interval and higher damage.
*/
public class EnragedFierceTooth extends Boss {

    // === Config ===
    // Shares the Fierce Tooth sprite sheet; the red tint is applied at draw time.
    private static final String SPRITE_BASE = "assets/Treasure Hunters/The Crusty Crew/Sprites/Fierce Tooth/";
    // Bigger scale than regular Fierce Tooth (3× vs 2×) to sell "enraged".
    private static final int DRAW_SCALE = 3;
    // Empty pixels below the feet in the source frames (2 src × scale).
    private static final int FOOT_PADDING = 2 * DRAW_SCALE;

    // === Animation ===
    private AnimatedSprite attackEffect;

    public EnragedFierceTooth(float x, float y, Player target) {
        // 70×80 hitbox (larger), 6 HP, 2 damage, 90-frame attack interval
        // (~1.5s, faster than Fierce Tooth), skull tile col 3 row 2.
        super(x, y, 70, 80, 6, 2, 90, target, "Enraged Fierce Tooth", 3, 2);
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
        return 1.8f;
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
        // Source sprite is 34×30 px; scale and centre over the collision box.
        int drawW = 34 * DRAW_SCALE;
        int drawH = 30 * DRAW_SCALE;
        int drawX = (int) x - (drawW - width) / 2;
        int drawY = (int) y + height - drawH + FOOT_PADDING;

        drawWithDeathFade(g, () -> {
            // Draw sprite to a temp buffer so we can tint only the opaque
            // pixels, without colouring the transparent background.
            BufferedImage temp = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = temp.createGraphics();
            sprite.draw(tg, 0, 0, drawW, drawH);

            // SRC_ATOP: the red fill is kept only where the destination
            // (the sprite) has alpha > 0. 30% strength so the sprite detail
            // still shows through.
            tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.3f));
            tg.setColor(new Color(255, 0, 0));
            tg.fillRect(0, 0, drawW, drawH);
            tg.dispose();

            g.drawImage(temp, drawX, drawY, null);
        });
        drawHealthBar(g);
        drawBossName(g);
        drawSkull(g);
    }

    @Override
    public void drawEffect(Graphics2D g) {
        if (lunging) {
            // Sword swing effect source is 22×24 px.
            int effectW = 22 * DRAW_SCALE;
            int effectH = 24 * DRAW_SCALE;
            // Place on the facing side, centred vertically over the body.
            int effectX = facingRight ? (int) x + width : (int) x - effectW;
            int effectY = (int) y + height / 2 - effectH / 2;
            attackEffect.draw(g, effectX, effectY, effectW, effectH);
        }
    }
}
