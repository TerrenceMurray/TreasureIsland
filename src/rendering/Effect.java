package rendering;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;

public class Effect {

    private float x, y;
    private int timer;
    private int duration;
    private AnimatedSprite sprite;
    private String text;
    private Color textColor;
    private boolean done;
    private int drawW, drawH;
    private float vy;

    // Sprite effect
    public Effect(float x, float y, AnimatedSprite sprite, int drawW, int drawH, int duration) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
        this.drawW = drawW;
        this.drawH = drawH;
        this.duration = duration;
    }

    // Text popup effect
    public Effect(float x, float y, String text, Color color, int duration) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.textColor = color;
        this.duration = duration;
        this.vy = -1.5f;
    }

    public void update() {
        timer++;
        if (sprite != null) sprite.update();
        if (text != null) {
            y += vy;
            vy *= 0.95f;
        }
        if (timer >= duration) done = true;
    }

    public void draw(Graphics2D g) {
        if (done) return;
        float alpha = timer > duration * 0.6f ? 1f - (float)(timer - duration * 0.6f) / (duration * 0.4f) : 1f;
        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));

        if (sprite != null) {
            sprite.draw(g, (int) x, (int) y, drawW, drawH);
        }
        if (text != null) {
            g.setColor(textColor);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(text, (int) x, (int) y);
        }

        g.setComposite(original);
    }

    public boolean isDone() { return done; }
}
