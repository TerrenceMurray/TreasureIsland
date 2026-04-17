package rendering.effects;

import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
    The EffectManager class owns every active transient visual effect
    (landing dust, diamond sparkles, score popups, text popups) and
    updates and draws them each frame. Effects that have expired are
    removed automatically.
*/
public class EffectManager {

    // === Asset paths ===
    private static final String TREASURE_BASE = "assets/Treasure Hunters/Pirate Treasure/Sprites/";
    private static final String DUST_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Dust Particles/";

    // === Animation frame durations (update ticks per frame) ===
    private static final int DIAMOND_FRAME_DURATION = 6;
    private static final int POTION_FRAME_DURATION = 6;
    private static final int DUST_FRAME_DURATION = 4;

    // === Effect lifetimes (update ticks) ===
    private static final int SPRITE_EFFECT_DURATION = 24;
    private static final int DUST_DURATION = 20;
    private static final int POPUP_DURATION = 45;

    // === Draw sizes for each sprite effect (width x height in pixels) ===
    private static final int DIAMOND_DRAW_W = 48;
    private static final int DIAMOND_DRAW_H = 48;
    private static final int POTION_DRAW_W = 32;
    private static final int POTION_DRAW_H = 78;
    private static final int DUST_DRAW_W = 52;
    private static final int DUST_DRAW_H = 20;

    // === Centring offsets so spawn point maps to roughly the centre of the effect ===
    private static final int DIAMOND_X_OFFSET = 16;
    private static final int DIAMOND_Y_OFFSET = 16;
    private static final int POTION_X_OFFSET = 8;
    private static final int POTION_Y_OFFSET = 20;
    private static final int DUST_X_OFFSET = 13;
    private static final int DUST_Y_OFFSET = 10;

    // === Active effects ===
    private List<Effect> effects = new ArrayList<>();

    /**
        Spawns the sparkle animation shown when a diamond is
        collected.
    */
    public void spawnDiamondEffect(float x, float y) {
        AnimatedSprite s = new AnimatedSprite(DIAMOND_FRAME_DURATION);
        s.loadState("play", TREASURE_BASE + "Diamond Effect");
        s.setState("play");
        // Shift so (x, y) maps to the centre of the effect.
        effects.add(new Effect(x - DIAMOND_X_OFFSET, y - DIAMOND_Y_OFFSET, s, DIAMOND_DRAW_W, DIAMOND_DRAW_H, SPRITE_EFFECT_DURATION));
    }

    /**
        Spawns the burst animation shown when a potion is collected.
    */
    public void spawnPotionEffect(float x, float y) {
        AnimatedSprite s = new AnimatedSprite(POTION_FRAME_DURATION);
        s.loadState("play", TREASURE_BASE + "Potion Effect");
        s.setState("play");
        effects.add(new Effect(x - POTION_X_OFFSET, y - POTION_Y_OFFSET, s, POTION_DRAW_W, POTION_DRAW_H, SPRITE_EFFECT_DURATION));
    }

    /**
        Spawns a dust puff, typically when the player lands on the
        ground. The type parameter is reserved for future variants.
    */
    public void spawnDust(float x, float y, String type) {
        AnimatedSprite s = new AnimatedSprite(DUST_FRAME_DURATION);
        s.loadState("play", DUST_BASE);
        s.setState("play");
        effects.add(new Effect(x - DUST_X_OFFSET, y - DUST_Y_OFFSET, s, DUST_DRAW_W, DUST_DRAW_H, DUST_DURATION));
    }

    /**
        Spawns a yellow "+N" number that floats up from the given
        position.
    */
    public void spawnScorePopup(float x, float y, int points) {
        effects.add(new Effect(x, y, "+" + points, Color.YELLOW, POPUP_DURATION));
    }

    /**
        Spawns a generic floating text popup in the given color.
    */
    public void spawnTextPopup(float x, float y, String text, Color color) {
        effects.add(new Effect(x, y, text, color, POPUP_DURATION));
    }

    /**
        Advances every active effect and removes those that have
        finished.
    */
    public void update() {
        Iterator<Effect> it = effects.iterator();
        while (it.hasNext()) {
            Effect e = it.next();
            e.update();
            if (e.isDone()) it.remove();
        }
    }

    /**
        Draws every active effect.
    */
    public void draw(Graphics2D g) {
        for (Effect e : effects) {
            e.draw(g);
        }
    }
}
