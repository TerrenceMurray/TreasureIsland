package entities.enemies;

import engine.Camera;
import engine.managers.GameConfig;
import engine.managers.GameStateManager;
import engine.managers.SoundManager;
import entities.Player;
import interfaces.Drawable;
import interfaces.Updatable;
import rendering.effects.EffectManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
    An EnemyWave is a Composite of Enemy instances: it exposes
    update and draw on the group as a whole, and owns the per-enemy
    physics, drawing, and combat resolution so Level can treat the
    wave uniformly alongside other world objects.
*/
public class EnemyWave implements Updatable, Drawable {

    // === Screen-shake / knockback tuning for wave combat ===
    private static final float SHAKE_HIT_ENEMY_INTENSITY = 2f;
    private static final int   SHAKE_HIT_ENEMY_DURATION  = 8;
    private static final float SHAKE_PLAYER_HURT_INTENSITY = 3f;
    private static final int   SHAKE_PLAYER_HURT_DURATION  = 10;
    private static final float KNOCK_ENEMY_FROM_PLAYER = 10f;
    private static final float KNOCK_PLAYER_FROM_ENEMY = 12f;

    // === Platform collision tuning ===
    // How far below a platform's top we still accept a "land on" contact.
    private static final int PLATFORM_LAND_SLOP = 12;
    // Thin strip below the entity used to probe "am I still on ground?".
    private static final int FEET_PROBE_HEIGHT = 4;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Rectangle> platforms;

    public EnemyWave(List<Rectangle> platforms) {
        this.platforms = platforms;
    }

    public void add(Enemy e) {
        enemies.add(e);
    }

    /**
        Advances every enemy in the wave by one frame, applying
        gravity and resolving platform collisions first so enemies
        sit on terrain before their own update runs.
    */
    @Override
    public void update() {
        for (Enemy e : enemies) {
            if (!e.isDead() || e.isDying()) {
                e.applyGravity();
                applyPlatformCollision(e);
                e.update();
            }
        }
    }

    /**
        Draws every enemy that is still alive or playing its death
        animation.
    */
    @Override
    public void draw(Graphics2D g) {
        for (Enemy e : enemies) {
            if (!e.isDead() || e.isDying()) {
                e.draw(g);
            }
        }
    }

    /**
        Draws the per-enemy overlay effects (e.g. attack swings)
        for every enemy that is still alive.
    */
    public void drawEffects(Graphics2D g) {
        for (Enemy e : enemies) {
            if (!e.isDead() && !e.isDying()) {
                e.drawEffect(g);
            }
        }
    }

    /**
        Resolves combat between the player and every enemy in the
        wave: applies player attack hits (with knockback, shake,
        score), then contact damage back onto the player.
    */
    public void handleCombat(Player player, Camera camera, EffectManager effects) {
        if (player.isDying() || player.isDead()) return;

        GameStateManager gsm = GameStateManager.getInstance();
        GameConfig cfg = GameConfig.getInstance();
        SoundManager sm = SoundManager.getInstance();
        Rectangle2D.Double playerBounds = player.getBoundingRectangle();
        Rectangle2D.Double attackBounds = player.getAttackBounds();

        for (Enemy e : enemies) {
            if (e.isDead() || e.isDying()) continue;

            if (attackBounds != null && !player.hasHitEnemy(e) && attackBounds.intersects(e.getBoundingRectangle())) {
                e.takeDamage(1);
                player.markEnemyHit(e);
                float knockDir = e.getX() > player.getX() ? 1 : -1;
                e.knockback(knockDir * KNOCK_ENEMY_FROM_PLAYER);
                camera.shake(SHAKE_HIT_ENEMY_INTENSITY, SHAKE_HIT_ENEMY_DURATION);
                sm.playRandomVariation("slime", 10);
                if (e.isDead() || e.isDying()) {
                    int score = scoreFor(e, cfg);
                    gsm.addScore(score);
                    effects.spawnScorePopup(e.getX(), e.getY() - 10, score);
                    sm.playClip("coin", false);
                }
            }

            if (!e.isDead() && e.canDealDamage() && playerBounds.intersects(e.getBoundingRectangle())) {
                player.takeDamage(e.getDamage());
                float knockDir = player.getX() > e.getX() ? 1 : -1;
                player.knockback(knockDir * KNOCK_PLAYER_FROM_ENEMY);
                camera.shake(SHAKE_PLAYER_HURT_INTENSITY, SHAKE_PLAYER_HURT_DURATION);
                sm.playRandomVariation("hurt", 2);
            }
        }
    }

    private int scoreFor(Enemy e, GameConfig cfg) {
        if (e instanceof PinkStar) return cfg.getInt("score.pinkStar", 75);
        if (e instanceof Crabby)   return cfg.getInt("score.crabby", 100);
        return 0;
    }

    /**
        Lands the enemy on a platform when it falls into the top
        "slop" band, and detects when it has walked off an edge so
        gravity can resume.
    */
    private void applyPlatformCollision(Enemy e) {
        Rectangle2D.Double eBounds = e.getBoundingRectangle();
        float eBottom = e.getY() + e.getHeight();
        boolean onPlatform = false;

        for (Rectangle plat : platforms) {
            if (eBounds.intersects(plat)) {
                if (e.getVelocityY() >= 0 && eBottom >= plat.y && eBottom <= plat.y + PLATFORM_LAND_SLOP) {
                    e.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
            }
        }

        if (!onPlatform && !e.isInAir()) {
            Rectangle feet = new Rectangle(
                (int) e.getX(), (int) e.getY() + e.getHeight(), e.getWidth(), FEET_PROBE_HEIGHT);
            boolean supported = false;
            for (Rectangle plat : platforms) {
                if (feet.intersects(plat)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) e.setInAir(true);
        }
    }
}
