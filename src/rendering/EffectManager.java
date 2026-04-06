package rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EffectManager {

    private static final String TREASURE_BASE = "assets/Treasure Hunters/Pirate Treasure/Sprites/";
    private static final String DUST_BASE = "assets/Treasure Hunters/Captain Clown Nose/Sprites/Dust Particles/";

    private List<Effect> effects = new ArrayList<>();

    public void spawnDiamondEffect(float x, float y) {
        AnimatedSprite s = new AnimatedSprite(6);
        s.loadState("play", TREASURE_BASE + "Diamond Effect");
        s.setState("play");
        effects.add(new Effect(x - 16, y - 16, s, 48, 48, 24));
    }

    public void spawnPotionEffect(float x, float y) {
        AnimatedSprite s = new AnimatedSprite(6);
        s.loadState("play", TREASURE_BASE + "Potion Effect");
        s.setState("play");
        effects.add(new Effect(x - 8, y - 20, s, 32, 78, 24));
    }

    public void spawnDust(float x, float y, String type) {
        AnimatedSprite s = new AnimatedSprite(4);
        s.loadState("play", DUST_BASE);
        s.setState("play");
        effects.add(new Effect(x - 13, y - 10, s, 52, 20, 20));
    }

    public void spawnScorePopup(float x, float y, int points) {
        effects.add(new Effect(x, y, "+" + points, Color.YELLOW, 45));
    }

    public void spawnTextPopup(float x, float y, String text, Color color) {
        effects.add(new Effect(x, y, text, color, 45));
    }

    public void update() {
        Iterator<Effect> it = effects.iterator();
        while (it.hasNext()) {
            Effect e = it.next();
            e.update();
            if (e.isDone()) it.remove();
        }
    }

    public void draw(Graphics2D g) {
        for (Effect e : effects) {
            e.draw(g);
        }
    }
}
