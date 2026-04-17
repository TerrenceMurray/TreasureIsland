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
import engine.GamePanel;
import engine.GameStateManager;
import engine.LevelLoader;
import rendering.ScrollingBackground;
import rendering.TerrainRenderer;
import rendering.EffectManager;
import rendering.PalmTreeRenderer;
import rendering.DecorRenderer;
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
    protected DecorRenderer decor;
    protected String levelName;
    protected int levelNameTimer;
    private static final int LEVEL_NAME_DURATION = 120;
    protected int groundOffset = 0;

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
    protected void onBossAnimationComplete() {
        complete = true;
    }

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
            boss.applyGravity();
            applyEnemyPlatformCollision(boss);
            boss.update();
        }
        checkCombat();
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

    private void updateEnemies() {
        for (Enemy e : enemies) {
            if (!e.isDead() || e.isDying()) {
                e.applyGravity();
                applyEnemyPlatformCollision(e);
                e.update();
            }
        }
    }

    private void applyEnemyPlatformCollision(Enemy e) {
        java.awt.geom.Rectangle2D.Double eBounds = e.getBoundingRectangle();
        float eBottom = e.getY() + e.getHeight();
        boolean onPlatform = false;

        for (java.awt.Rectangle plat : platforms) {
            if (eBounds.intersects(plat)) {
                if (e.getVelocityY() >= 0 && eBottom >= plat.y && eBottom <= plat.y + 12) {
                    e.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
            }
        }

        if (!onPlatform && !e.isInAir()) {
            java.awt.Rectangle feet = new java.awt.Rectangle(
                (int) e.getX(), (int) e.getY() + e.getHeight(), e.getWidth(), 4);
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

    private void checkCombat() {
        if (player.isDying() || player.isDead()) return;

        GameStateManager gsm = GameStateManager.getInstance();
        GameConfig cfg = GameConfig.getInstance();
        java.awt.geom.Rectangle2D.Double playerBounds = player.getBoundingRectangle();
        Rectangle attackBounds = player.getAttackBounds();

        for (Enemy e : enemies) {
            if (e.isDead() || e.isDying()) continue;

            if (attackBounds != null && !player.hasHitEnemy(e) && attackBounds.intersects(e.getBoundingRectangle())) {
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

            if (!e.isDead() && e.canDealDamage() && playerBounds.intersects(e.getBoundingRectangle())) {
                player.takeDamage(e.getDamage());
                float knockDir = player.getX() > e.getX() ? 1 : -1;
                player.knockback(knockDir * 12);
                camera.shake(3f, 10);
            }
        }

        if (boss != null && !boss.isDead() && !boss.isDying()) {
            if (attackBounds != null && !player.hasHitEnemy(boss) && attackBounds.intersects(boss.getBoundingRectangle())) {
                boss.takeDamage(1);
                player.markEnemyHit(boss);
                float knockDir = boss.getX() > player.getX() ? 1 : -1;
                boss.knockback(knockDir * 5);
                camera.shake(3f, 10);
                if (boss.isDead() || boss.isDying()) {
                    onBossDefeated();
                }
            }
            if (!boss.isDead() && boss.canDealDamage() && playerBounds.intersects(boss.getBoundingRectangle())) {
                player.takeDamage(boss.getDamage());
                float knockDir = player.getX() > boss.getX() ? 1 : -1;
                player.knockback(knockDir * 18);
                camera.shake(5f, 15);
            }
        }
    }

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
        java.awt.geom.Rectangle2D.Double playerBounds = player.getBoundingRectangle();
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

        drawTerrain(g);
        decor.draw(g);

        // Offset entities to align with terrain surface
        g.translate(0, -groundOffset);

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

        g.translate(0, groundOffset);

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
            g.drawString(levelName, (GamePanel.GAME_WIDTH - textW) / 2, GamePanel.GAME_HEIGHT / 3);
            g.setComposite(original);
        }
    }

    protected void drawTerrain(Graphics2D g) {
        terrain.draw(g, platforms);
    }

    public int getGroundOffset() { return groundOffset; }

    public boolean isComplete() {
        return complete;
    }
}
