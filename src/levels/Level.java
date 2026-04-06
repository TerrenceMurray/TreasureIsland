package levels;

import entities.Player;
import entities.collectibles.Collectible;
import entities.collectibles.Diamond;
import entities.collectibles.HealthPotion;
import entities.collectibles.TreasureChest;
import entities.enemies.Enemy;
import entities.enemies.Boss;
import entities.enemies.PinkStar;
import entities.enemies.Crabby;
import engine.Camera;
import engine.GameConfig;
import engine.GameLoop;
import engine.GameStateManager;
import engine.LevelLoader;
import rendering.ScrollingBackground;
import rendering.TerrainRenderer;
import rendering.EffectManager;
import rendering.PalmTreeRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;

public abstract class Level {

    protected Player player;
    protected List<Rectangle> platforms;
    protected List<Collectible> collectibles;
    protected List<Enemy> enemies;
    protected Boss boss;
    protected Camera camera;
    protected int levelWidth;
    protected boolean complete;
    protected boolean bossDefeated;
    protected int bossDefeatedTimer;
    private static final int BOSS_DEFEATED_DELAY = 120;
    protected ScrollingBackground background;
    protected TerrainRenderer terrain;
    protected EffectManager effects;
    protected PalmTreeRenderer palmTrees;
    protected String levelName;
    protected int levelNameTimer;
    private static final int LEVEL_NAME_DURATION = 120;

    public Level(Player player, Camera camera, String levelFile, String bgPath, String levelName) {
        this.player = player;
        this.camera = camera;
        this.levelName = levelName;
        this.levelNameTimer = LEVEL_NAME_DURATION;
        this.background = new ScrollingBackground(bgPath,
            GameLoop.GAME_WIDTH, GameLoop.GAME_HEIGHT);
        this.terrain = new TerrainRenderer(
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Terrain/Terrain (32x32).png");
        this.effects = new EffectManager();
        this.palmTrees = new PalmTreeRenderer();

        LevelLoader loader = new LevelLoader(levelFile);
        this.platforms = loader.getPlatforms();
        this.collectibles = loader.getCollectibles();
        this.levelWidth = loader.getLevelWidth();
        player.setLevelWidth(levelWidth);
        camera.setLevelWidth(levelWidth);

        this.enemies = new ArrayList<>();
        for (float[] pos : loader.getPinkStarSpawns()) {
            enemies.add(new PinkStar(pos[0], pos[1], player, platforms));
        }
        for (float[] pos : loader.getCrabbySpawns()) {
            enemies.add(new Crabby(pos[0], pos[1], player));
        }

        float[] bossPos = loader.getBossSpawn();
        if (bossPos != null) {
            boss = createBoss(bossPos[0], bossPos[1]);
        }
    }

    protected abstract Boss createBoss(float x, float y);

    public void update() {
        if (complete) return;

        if (levelNameTimer > 0) levelNameTimer--;

        if (bossDefeated) {
            bossDefeatedTimer++;
            // Let boss death animation play, then update enemies/effects
            if (boss != null && boss.isDying()) boss.update();
            camera.update(player);
            background.update();
            effects.update();
            if (bossDefeatedTimer >= BOSS_DEFEATED_DELAY) {
                complete = true;
            }
            return;
        }

        boolean wasInAirBefore = player.isInAir();
        float prevY = player.getY();
        player.update();

        if (player.isDying()) {
            camera.shake(4f, 15);
            camera.update(player);
            background.update();
            effects.update();
            return;
        }

        applyPlatformCollisions(prevY);

        // Dust on landing
        if (wasInAirBefore && !player.isInAir()) {
            effects.spawnDust(player.getX() + player.getWidth() / 2,
                player.getY() + player.getHeight(), "Fall");
        }

        updateCollectibles();
        checkCollectiblePickups();
        updateEnemies();
        if (boss != null && !boss.isDead()) {
            boss.update();
        }
        checkCombat();
        camera.update(player);
        background.update();
        effects.update();
    }

    private void updateCollectibles() {
        for (Collectible c : collectibles) {
            if (!c.isCollected()) c.update();
        }
    }

    private void updateEnemies() {
        for (Enemy e : enemies) {
            if (!e.isDead() || e.isDying()) e.update();
        }
    }

    private void checkCombat() {
        if (player.isDying() || player.isDead()) return;

        GameStateManager gsm = GameStateManager.getInstance();
        GameConfig cfg = GameConfig.getInstance();
        Rectangle playerBounds = player.getBounds();
        Rectangle attackBounds = player.getAttackBounds();

        for (Enemy e : enemies) {
            if (e.isDead()) continue;

            if (attackBounds != null && !player.hasHitEnemy(e) && attackBounds.intersects(e.getBounds())) {
                e.takeDamage(1);
                player.markEnemyHit(e);
                float knockDir = e.getX() > player.getX() ? 1 : -1;
                e.knockback(knockDir * 10);
                camera.shake(2f, 8);
                if (e.isDead() || e.isDying()) {
                    int score = 0;
                    if (e instanceof PinkStar) {
                        score = cfg.getInt("score.pinkStar", 75);
                    } else if (e instanceof Crabby) {
                        score = cfg.getInt("score.crabby", 100);
                    }
                    gsm.addScore(score);
                    effects.spawnScorePopup(e.getX(), e.getY() - 10, score);
                }
            }

            if (!e.isDead() && e.canDealDamage() && playerBounds.intersects(e.getBounds())) {
                player.takeDamage(e.getDamage());
                float knockDir = player.getX() > e.getX() ? 1 : -1;
                player.knockback(knockDir * 12);
                camera.shake(3f, 10);
            }
        }

        if (boss != null && !boss.isDead()) {
            if (attackBounds != null && !player.hasHitEnemy(boss) && attackBounds.intersects(boss.getBounds())) {
                boss.takeDamage(1);
                player.markEnemyHit(boss);
                float knockDir = boss.getX() > player.getX() ? 1 : -1;
                boss.knockback(knockDir * 5);
                camera.shake(3f, 10);
                if (boss.isDead() || boss.isDying()) {
                    onBossDefeated();
                }
            }
            if (!boss.isDead() && boss.canDealDamage() && playerBounds.intersects(boss.getBounds())) {
                player.takeDamage(boss.getDamage());
                float knockDir = player.getX() > boss.getX() ? 1 : -1;
                player.knockback(knockDir * 18);
                camera.shake(5f, 15);
            }
        }
    }

    protected abstract void onBossDefeated();

    private void checkCollectiblePickups() {
        Rectangle playerBounds = player.getBounds();
        GameStateManager gsm = GameStateManager.getInstance();
        for (Collectible c : collectibles) {
            if (c.isCollected()) continue;
            if (playerBounds.intersects(c.getBounds())) {
                c.collect();
                if (c instanceof Diamond) {
                    int pts = ((Diamond) c).getPoints();
                    gsm.addScore(pts);
                    effects.spawnDiamondEffect(c.getX(), c.getY());
                    effects.spawnScorePopup(c.getX(), c.getY() - 10, pts);
                } else if (c instanceof HealthPotion) {
                    player.heal();
                    effects.spawnPotionEffect(c.getX(), c.getY());
                    effects.spawnTextPopup(c.getX(), c.getY() - 10, "+1 HP", Color.GREEN);
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
        Rectangle playerBounds = player.getBounds();
        float playerBottom = player.getY() + player.getHeight();
        float prevBottom = prevY + player.getHeight();
        boolean onPlatform = false;

        for (Rectangle plat : platforms) {
            if (playerBounds.intersects(plat)) {
                if (player.getVelocityY() >= 0 && prevBottom <= plat.y + 10 && playerBottom >= plat.y) {
                    player.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
                if (plat.height > 30) {
                    float platBottom = plat.y + plat.height;
                    if (player.getVelocityY() < 0 && prevY >= platBottom - 2 && player.getY() < platBottom) {
                        player.hitHead(platBottom);
                    }
                }
            }
        }

        if (!onPlatform && !player.isInAir()) {
            Rectangle feet = new Rectangle((int) player.getX(), (int) player.getY() + player.getHeight(), player.getWidth(), 4);
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

    public void draw(Graphics2D g) {
        float shakeY = camera.getShakeY();
        background.draw(g, camera.getX(), levelWidth);

        int offsetX = (int) camera.getX();
        g.translate(-offsetX, (int) shakeY);

        // Back palm trees
        palmTrees.drawBack(g, levelWidth);

        terrain.draw(g, platforms);
        for (Collectible c : collectibles) {
            if (!c.isCollected()) {
                c.draw(g);
            }
        }
        for (Enemy e : enemies) {
            if (!e.isDead() || e.isDying()) {
                e.draw(g);
            }
        }
        if (boss != null && (!boss.isDead() || boss.isDying())) {
            boss.draw(g);
        }
        player.draw(g);

        for (Enemy e : enemies) {
            if (!e.isDead() && !e.isDying()) {
                e.drawEffect(g);
            }
        }
        if (boss != null && !boss.isDead() && !boss.isDying()) {
            boss.drawEffect(g);
        }

        effects.draw(g);

        // Front palm trees
        palmTrees.drawFront(g, levelWidth);

        g.translate(offsetX, (int) -shakeY);

        // Level name overlay
        if (levelNameTimer > 0) {
            float alpha = levelNameTimer > 60 ? 1f : levelNameTimer / 60f;
            java.awt.Composite original = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            int textW = g.getFontMetrics().stringWidth(levelName);
            g.drawString(levelName, (GameLoop.GAME_WIDTH - textW) / 2, GameLoop.GAME_HEIGHT / 3);
            g.setComposite(original);
        }
    }

    public boolean isComplete() {
        return complete;
    }
}
