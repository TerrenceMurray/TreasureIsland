package levels;

import entities.Player;
import entities.collectibles.Collectible;
import entities.collectibles.Diamond;
import entities.collectibles.HealthPotion;
import entities.collectibles.TreasureChest;
import entities.enemies.Enemy;
import entities.enemies.EnemyWave;
import entities.enemies.bosses.Boss;
import entities.enemies.PinkStar;
import entities.enemies.Crabby;
import engine.Camera;
import engine.GamePanel;
import engine.managers.GameStateManager;
import engine.managers.SoundManager;
import engine.LevelLoader;
import rendering.background.ScrollingBackground;
import rendering.terrain.TerrainRenderer;
import rendering.effects.EffectManager;
import rendering.decor.PalmTreeRenderer;
import rendering.decor.DecorRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
    The Level class is the abstract base for playable levels.
    It loads the level layout, manages the player, enemies,
    boss, collectibles, and platform collisions, and draws
    the level scene with background, terrain, and effects.
    Subclasses override createBoss, onBossDefeated, and
    drawTerrain to customise each level.
*/
public abstract class Level {

    // === Timing / tuning constants (frames; the game runs at 60 FPS) ===
    // How long the boss death sequence plays before the level reacts.
    private static final int BOSS_DEFEATED_DELAY = 120;   // 2 seconds
    // How long the level-name banner stays on screen.
    private static final int LEVEL_NAME_DURATION = 120;   // 2 seconds
    // During the last 60 frames of the banner it fades from 1.0 to 0.0 alpha.
    private static final int LEVEL_NAME_FADE_FRAMES = 60; // 1 second

    // Screen-shake presets (intensity in pixels, duration in frames)
    private static final float SHAKE_DEATH_INTENSITY = 4f;
    private static final int   SHAKE_DEATH_DURATION  = 15;
    private static final float SHAKE_PLAYER_HURT_INTENSITY = 3f;
    private static final int   SHAKE_PLAYER_HURT_DURATION  = 10;
    private static final float SHAKE_BOSS_HIT_INTENSITY = 5f;
    private static final int   SHAKE_BOSS_HIT_DURATION  = 15;

    // Knockback strengths (pixels of horizontal nudge per hit)
    private static final float KNOCK_BOSS_FROM_PLAYER  = 5f;
    private static final float KNOCK_PLAYER_FROM_BOSS  = 18f;

    // Collision tuning
    // How far below a platform's top we still accept a "land on" contact.
    // Generous window so fast falls don't tunnel through the top.
    private static final int PLATFORM_LAND_SLOP = 12;
    // Height threshold to distinguish solid blocks from thin pass-through
    // platforms. Thin platforms (height <= 30) don't trigger head-bumps so
    // the player can jump up through them.
    private static final int SOLID_PLATFORM_MIN_HEIGHT = 30;
    // Thin strip below the entity used to probe "am I still on ground?".
    private static final int FEET_PROBE_HEIGHT = 4;

    // === Core references ===
    protected Player player;
    protected Camera camera;

    // === World content ===
    protected List<Rectangle> platforms;
    protected List<Collectible> collectibles;
    protected EnemyWave wave;
    protected Boss boss;
    protected int levelWidth;

    // === Renderers ===
    protected ScrollingBackground background;
    protected TerrainRenderer terrain;
    protected EffectManager effects;
    protected PalmTreeRenderer palmTrees;
    protected DecorRenderer decor;

    // === Level state / timers ===
    protected boolean complete;
    protected boolean bossDefeated;
    protected int bossDefeatedTimer;
    protected String levelName;
    protected int levelNameTimer;
    // Pixel shift applied to entities so their feet line up with the
    // rendered terrain surface (levels tweak this via groundOffset).
    protected int groundOffset = 0;
    private boolean playerDeathShakeDone;

    /** Surface label used to pick footstep and landing sounds. */
    protected String surface = "grass";
    private int stepTimer;
    private static final int STEP_PERIOD = 18;  // frames between step sounds while running

    /**
        Creates a new Level, loading the layout from the
        given level file and setting up the background,
        terrain, and any enemies and boss described in it.
    */
    public Level(Player player, Camera camera, String levelFile, String bgPath, String levelName) {
        this.player = player;
        this.camera = camera;
        this.levelName = levelName;
        this.levelNameTimer = LEVEL_NAME_DURATION;
        this.background = new ScrollingBackground(bgPath,
            GamePanel.GAME_WIDTH, GamePanel.GAME_HEIGHT);
        this.terrain = new TerrainRenderer(
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Terrain/Terrain (32x32).png");
        this.effects = new EffectManager();
        this.palmTrees = new PalmTreeRenderer();
        this.decor = new DecorRenderer();

        LevelLoader loader = new LevelLoader(levelFile);
        this.platforms = loader.getPlatforms();
        this.collectibles = loader.getCollectibles();
        this.levelWidth = loader.getLevelWidth();
        player.setLevelWidth(levelWidth);
        camera.setLevelWidth(levelWidth);

        this.wave = new EnemyWave(platforms);
        for (float[] pos : loader.getPinkStarSpawns()) {
            wave.add(new PinkStar(pos[0], pos[1], player, platforms));
        }
        for (float[] pos : loader.getCrabbySpawns()) {
            wave.add(new Crabby(pos[0], pos[1], player));
        }

        float[] bossPos = loader.getBossSpawn();
        if (bossPos != null) {
            boss = createBoss(bossPos[0], bossPos[1]);
        }
    }

    /**
        Creates the boss for this level at the given spawn
        position. Subclasses return their specific boss type,
        or null if the level has no boss.
    */
    protected abstract Boss createBoss(float x, float y);

    /**
        Called after the boss death animation has finished.
        By default the level is marked complete; subclasses
        can override to do something different (e.g. spawning
        a treasure chest).
    */
    protected void onBossAnimationComplete() {
        complete = true;
    }

    /**
        Advances the level one frame: updates the player,
        collectibles, enemies, boss, collisions, camera, and
        effects.
    */
    public void update() {
        if (complete) return;

        if (levelNameTimer > 0) levelNameTimer--;

        if (bossDefeated) {
            bossDefeatedTimer++;
            if (boss != null && boss.isDying()) boss.update();
            camera.update(player);
            background.update();
            effects.update();
            if (bossDefeatedTimer >= BOSS_DEFEATED_DELAY) {
                bossDefeated = false;
                onBossAnimationComplete();
            }
            return;
        }

        boolean wasInAirBefore = player.isInAir();
        float prevY = player.getY();
        player.update();

        if (player.isDying()) {
            // One-shot death shake — triggered only on the first dying frame.
            if (!playerDeathShakeDone) {
                camera.shake(SHAKE_DEATH_INTENSITY, SHAKE_DEATH_DURATION);
                playerDeathShakeDone = true;
            }
            camera.update(player);
            background.update();
            effects.update();
            return;
        }

        applyPlatformCollisions(prevY);

        // Dust + thud on landing
        if (wasInAirBefore && !player.isInAir()) {
            effects.spawnDust(player.getX() + player.getWidth() / 2,
                player.getY() + player.getHeight(), "Fall");
            SoundManager.getInstance().playRandomVariation("land_" + surface, 5);
        }

        // Footsteps while moving on the ground
        if (player.isMoving() && !player.isInAir()) {
            stepTimer++;
            if (stepTimer >= STEP_PERIOD) {
                SoundManager.getInstance().playRandomVariation("footstep_" + surface, 5);
                stepTimer = 0;
            }
        } else {
            stepTimer = 0;
        }

        updateCollectibles();
        checkCollectiblePickups();
        wave.update();
        if (boss != null && !boss.isDead()) {
            boss.applyGravity();
            applyEnemyPlatformCollision(boss);
            boss.update();
        }
        wave.handleCombat(player, camera, effects);
        checkBossCombat();
        camera.update(player);
        background.update();
        effects.update();
        decor.update();
    }

    private void updateCollectibles() {
        for (Collectible c : collectibles) {
            if (!c.isCollected()) c.update();
        }
    }

    private void applyEnemyPlatformCollision(Enemy e) {
        java.awt.geom.Rectangle2D.Double eBounds = e.getBoundingRectangle();
        float eBottom = e.getY() + e.getHeight();
        boolean onPlatform = false;

        for (java.awt.Rectangle plat : platforms) {
            if (eBounds.intersects(plat)) {
                // Land only while falling and only if the feet are within the
                // top "slop" band of the platform — this stops side-wall hits
                // from being treated as landings.
                if (e.getVelocityY() >= 0 && eBottom >= plat.y && eBottom <= plat.y + PLATFORM_LAND_SLOP) {
                    e.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
            }
        }

        if (!onPlatform && !e.isInAir()) {
            // Probe a thin strip just below the feet to decide if the enemy
            // has walked off an edge and should start falling.
            java.awt.Rectangle feet = new java.awt.Rectangle(
                (int) e.getX(), (int) e.getY() + e.getHeight(), e.getWidth(), FEET_PROBE_HEIGHT);
            boolean supported = false;
            for (java.awt.Rectangle plat : platforms) {
                if (feet.intersects(plat)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                e.setInAir(true);
            }
        }
    }

    private void checkBossCombat() {
        if (player.isDying() || player.isDead()) return;
        if (boss == null || boss.isDead() || boss.isDying()) return;

        java.awt.geom.Rectangle2D.Double playerBounds = player.getBoundingRectangle();
        java.awt.geom.Rectangle2D.Double attackBounds = player.getAttackBounds();

        if (attackBounds != null && !player.hasHitEnemy(boss) && attackBounds.intersects(boss.getBoundingRectangle())) {
            boss.takeDamage(1);
            player.markEnemyHit(boss);
            // Boss is heavy — smaller knockback than a regular enemy.
            float knockDir = boss.getX() > player.getX() ? 1 : -1;
            boss.knockback(knockDir * KNOCK_BOSS_FROM_PLAYER);
            camera.shake(SHAKE_PLAYER_HURT_INTENSITY, SHAKE_PLAYER_HURT_DURATION);
            SoundManager.getInstance().playRandomVariation("slime", 10);
            if (boss.isDead() || boss.isDying()) {
                onBossDefeated();
                SoundManager.getInstance().playClip("coin", false);
            }
        }
        if (!boss.isDead() && boss.canDealDamage() && playerBounds.intersects(boss.getBoundingRectangle())) {
            player.takeDamage(boss.getDamage());
            // Boss hits the player hard — bigger knockback and a heavier shake.
            float knockDir = player.getX() > boss.getX() ? 1 : -1;
            player.knockback(knockDir * KNOCK_PLAYER_FROM_BOSS);
            camera.shake(SHAKE_BOSS_HIT_INTENSITY, SHAKE_BOSS_HIT_DURATION);
            SoundManager.getInstance().playRandomVariation("hurt", 2);
        }
    }

    /**
        Called once when the boss is first defeated.
        Subclasses add the score reward and start any boss
        defeat sequence.
    */
    protected abstract void onBossDefeated();

    private void checkCollectiblePickups() {
        java.awt.geom.Rectangle2D.Double playerBounds = player.getBoundingRectangle();
        GameStateManager gsm = GameStateManager.getInstance();
        for (Collectible c : collectibles) {
            if (c.isCollected()) continue;
            if (playerBounds.intersects(c.getBoundingRectangle())) {
                c.collect();
                if (c instanceof Diamond) {
                    int pts = ((Diamond) c).getPoints();
                    gsm.addScore(pts);
                    effects.spawnDiamondEffect(c.getX(), c.getY());
                    effects.spawnScorePopup(c.getX(), c.getY() - 10, pts);
                    SoundManager.getInstance().playClip("diamond", false);
                } else if (c instanceof HealthPotion) {
                    player.heal();
                    effects.spawnPotionEffect(c.getX(), c.getY());
                    effects.spawnTextPopup(c.getX(), c.getY() - 10, "+1 HP", Color.GREEN);
                    SoundManager.getInstance().playClip("bubble", false);
                } else if (c instanceof TreasureChest) {
                    int pts = ((TreasureChest) c).getPoints();
                    gsm.addScore(pts);
                    effects.spawnScorePopup(c.getX(), c.getY() - 10, pts);
                    complete = true;
                }
            }
        }
    }

    private void applyPlatformCollisions(float prevY) {
        java.awt.geom.Rectangle2D.Double playerBounds = player.getBoundingRectangle();
        float playerBottom = player.getY() + player.getHeight();
        float prevBottom = prevY + player.getHeight();
        boolean onPlatform = false;

        for (Rectangle plat : platforms) {
            if (playerBounds.intersects(plat)) {
                // Land on top of a platform: only when falling, and only when
                // the feet crossed the top edge this frame (prevBottom above,
                // playerBottom below). This prevents side collisions from
                // snapping the player onto the surface.
                if (player.getVelocityY() >= 0 && prevBottom <= plat.y + 10 && playerBottom >= plat.y) {
                    player.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
                // Only solid blocks produce head-bumps. Thin (<=30px) platforms
                // are treated as pass-through ledges so the player can jump up
                // through them from below.
                if (plat.height > SOLID_PLATFORM_MIN_HEIGHT) {
                    float platBottom = plat.y + plat.height;
                    // Rising into the underside: previous frame's head was
                    // below platBottom, current frame it's above — bump.
                    if (player.getVelocityY() < 0 && prevY >= platBottom - 2 && player.getY() < platBottom) {
                        player.hitHead(platBottom);
                    }
                }
            }
        }

        if (!onPlatform && !player.isInAir()) {
            // Thin strip just below the feet — if nothing supports it, the
            // player has walked off an edge and should start falling.
            Rectangle feet = new Rectangle((int) player.getX(), (int) player.getY() + player.getHeight(), player.getWidth(), FEET_PROBE_HEIGHT);
            boolean supported = false;
            for (Rectangle plat : platforms) {
                if (feet.intersects(plat)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                player.setInAir(true);
            }
        }
    }

    /**
        Draws the whole level scene: background, terrain,
        palm trees, collectibles, enemies, boss, player,
        effects, and the level-name overlay.
    */
    public void draw(Graphics2D g) {
        float shakeY = camera.getShakeY();
        background.draw(g, camera.getX(), levelWidth);

        int offsetX = (int) camera.getX();
        g.translate(-offsetX, (int) shakeY);

        // Back palm trees
        palmTrees.drawBack(g, levelWidth);

        drawTerrain(g);
        decor.draw(g);

        // Offset entities to align with terrain surface
        g.translate(0, -groundOffset);

        for (Collectible c : collectibles) {
            if (!c.isCollected()) {
                c.draw(g);
            }
        }
        wave.draw(g);
        if (boss != null && (!boss.isDead() || boss.isDying())) {
            boss.draw(g);
        }
        player.draw(g);

        wave.drawEffects(g);
        if (boss != null && !boss.isDead() && !boss.isDying()) {
            boss.drawEffect(g);
        }

        effects.draw(g);

        g.translate(0, groundOffset);

        // Front palm trees
        palmTrees.drawFront(g, levelWidth);

        g.translate(offsetX, (int) -shakeY);

        // Level name overlay
        if (levelNameTimer > 0) {
            // Full opacity for the first half, then linear fade-out across
            // the final LEVEL_NAME_FADE_FRAMES frames.
            float alpha = levelNameTimer > LEVEL_NAME_FADE_FRAMES
                ? 1f
                : levelNameTimer / (float) LEVEL_NAME_FADE_FRAMES;
            java.awt.Composite original = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            g.setFont(new Font("Monospaced", Font.BOLD, 36));
            int textW = g.getFontMetrics().stringWidth(levelName);
            int textX = (GamePanel.GAME_WIDTH - textW) / 2;
            int textY = GamePanel.GAME_HEIGHT / 3;

            // 2px black outline in 8 directions (bigger font → thicker outline)
            g.setColor(Color.BLACK);
            for (int dx = -2; dx <= 2; dx += 2) {
                for (int dy = -2; dy <= 2; dy += 2) {
                    if (dx == 0 && dy == 0) continue;
                    g.drawString(levelName, textX + dx, textY + dy);
                }
            }
            g.setColor(Color.WHITE);
            g.drawString(levelName, textX, textY);

            g.setComposite(original);
        }
    }

    /**
        Draws the terrain for this level. Subclasses can
        override this to use a different terrain renderer.
    */
    protected void drawTerrain(Graphics2D g) {
        terrain.draw(g, platforms);
    }

    public int getGroundOffset() { return groundOffset; }

    public boolean isComplete() {
        return complete;
    }
}
