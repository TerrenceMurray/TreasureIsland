package rendering;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;

/**
    The Effect class is a short-lived visual, either an animated
    sprite (for example a dust puff or diamond sparkle) or a rising
    text popup (for example a score number). Effects fade out during
    the last portion of their lifetime and then mark themselves done.
*/
public class Effect {

    // === Tuning constants ===
    // Fraction of the effect's duration that plays at full opacity; after this point
    // the alpha ramps linearly down to zero.
    private static final float FADE_START_FRACTION = 0.6f;
    // Initial upward velocity applied to text popups (negative = up).
    private static final float TEXT_INITIAL_VY = -1.5f;
    // Per-tick damping on text popup velocity so they decelerate as they rise.
    private static final float TEXT_VY_DAMPING = 0.95f;
    // Font size for text popups, in points.
    private static final int TEXT_FONT_SIZE = 14;

    // === Position and lifetime ===
    private float x, y;
    private int timer;
    // Total lifetime in update ticks.
    private int duration;
    private boolean done;

    // === Sprite-mode fields ===
    private AnimatedSprite sprite;
    private int drawW, drawH;

    // === Text-mode fields ===
    private String text;
    private Color textColor;
    // Vertical drift, only used by text popups.
    private float vy;

    /**
        Creates an effect that displays an animated sprite for the
        given duration at the given position and draw size.
    */
    public Effect(float x, float y, AnimatedSprite sprite, int drawW, int drawH, int duration) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
        this.drawW = drawW;
        this.drawH = drawH;
        this.duration = duration;
    }

    /**
        Creates a floating text popup that drifts upward and fades.
    */
    public Effect(float x, float y, String text, Color color, int duration) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.textColor = color;
        this.duration = duration;
        this.vy = TEXT_INITIAL_VY;
    }

    /**
        Advances the effect's timer and animation. Call once per
        game update.
    */
    public void update() {
        timer++;
        if (sprite != null) sprite.update();
        if (text != null) {
            y += vy;
            // Damp the velocity each tick so the popup slows as it rises.
            vy *= TEXT_VY_DAMPING;
        }
        if (timer >= duration) done = true;
    }

    /**
        Draws the effect. The effect fades out over the final 40%
        of its duration.
    */
    public void draw(Graphics2D g) {
        if (done) return;

        // Full opacity until FADE_START_FRACTION of the lifetime, then ramp linearly to 0.
        float fadeStart = duration * FADE_START_FRACTION;
        float fadeLength = duration * (1f - FADE_START_FRACTION);
        float alpha = timer > fadeStart ? 1f - (timer - fadeStart) / fadeLength : 1f;

        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));

        if (sprite != null) {
            sprite.draw(g, (int) x, (int) y, drawW, drawH);
        }
        if (text != null) {
            g.setColor(textColor);
            g.setFont(new Font("Arial", Font.BOLD, TEXT_FONT_SIZE));
            g.drawString(text, (int) x, (int) y);
        }

        g.setComposite(original);
    }

    public boolean isDone() { return done; }
}
