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
import java.awt.Color;
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
    protected ScrollingBackground background;

    public Level(Player player, Camera camera, String levelFile, String bgPath) {
        this.player = player;
        this.camera = camera;
        this.background = new ScrollingBackground(bgPath,
            GameLoop.GAME_WIDTH, GameLoop.GAME_HEIGHT);

        LevelLoader loader = new LevelLoader(levelFile);
        this.platforms = loader.getPlatforms();
        this.collectibles = loader.getCollectibles();
        this.levelWidth = loader.getLevelWidth();
        player.setLevelWidth(levelWidth);
        camera.setLevelWidth(levelWidth);

        this.enemies = new ArrayList<>();
        for (float[] pos : loader.getPinkStarSpawns()) {
            enemies.add(new PinkStar(pos[0], pos[1], player));
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

        float prevY = player.getY();
        player.update();

        if (player.isDying()) {
            camera.update(player);
            background.update();
            return;
        }

        applyPlatformCollisions(prevY);
        updateCollectibles();
        checkCollectiblePickups();
        updateEnemies();
        if (boss != null && !boss.isDead()) {
            boss.update();
        }
        checkCombat();
        camera.update(player);
        background.update();
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
        GameStateManager gsm = GameStateManager.getInstance();
        GameConfig cfg = GameConfig.getInstance();
        Rectangle playerBounds = player.getBounds();
        Rectangle attackBounds = player.getAttackBounds();

        for (Enemy e : enemies) {
            if (e.isDead()) continue;

            // Player attack hits enemy (once per enemy per swing)
            if (attackBounds != null && !player.hasHitEnemy(e) && attackBounds.intersects(e.getBounds())) {
                e.takeDamage(1);
                player.markEnemyHit(e);
                // Knockback
                float knockDir = e.getX() > player.getX() ? 1 : -1;
                e.knockback(knockDir * 10);
                if (e.isDead()) {
                    if (e instanceof PinkStar) {
                        gsm.addScore(cfg.getInt("score.pinkStar", 75));
                    } else if (e instanceof Crabby) {
                        gsm.addScore(cfg.getInt("score.crabby", 100));
                    }
                }
            }

            // Enemy contact damages player
            if (!e.isDead() && e.canDealDamage() && playerBounds.intersects(e.getBounds())) {
                player.takeDamage(e.getDamage());
            }
        }

        // Boss combat
        if (boss != null && !boss.isDead()) {
            if (attackBounds != null && !player.hasHitEnemy(boss) && attackBounds.intersects(boss.getBounds())) {
                boss.takeDamage(1);
                player.markEnemyHit(boss);
                float knockDir = boss.getX() > player.getX() ? 1 : -1;
                boss.knockback(knockDir * 5);
                if (boss.isDead()) {
                    onBossDefeated();
                }
            }
            if (!boss.isDead() && boss.canDealDamage() && playerBounds.intersects(boss.getBounds())) {
                player.takeDamage(boss.getDamage());
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
                    gsm.addScore(((Diamond) c).getPoints());
                } else if (c instanceof HealthPotion) {
                    player.heal();
                } else if (c instanceof TreasureChest) {
                    gsm.addScore(((TreasureChest) c).getPoints());
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
                // Landing — only when falling
                if (player.getVelocityY() >= 0 && prevBottom <= plat.y + 2 && playerBottom >= plat.y) {
                    player.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
                // Head bump — only when moving upward
                float platBottom = plat.y + plat.height;
                if (player.getVelocityY() < 0 && prevY >= platBottom - 2 && player.getY() < platBottom) {
                    player.hitHead(platBottom);
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
        // Background drawn before camera translate (parallax handled internally)
        background.draw(g, camera.getX(), levelWidth);

        int offsetX = (int) camera.getX();
        g.translate(-offsetX, 0);

        g.setColor(new Color(100, 70, 40));
        for (Rectangle plat : platforms) {
            g.fillRect(plat.x, plat.y, plat.width, plat.height);
        }
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

        g.translate(offsetX, 0);
    }

    public boolean isComplete() {
        return complete;
    }
}
