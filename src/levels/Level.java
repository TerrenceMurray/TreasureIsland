package levels;

import entities.Player;
import entities.collectibles.Collectible;
import entities.collectibles.Diamond;
import entities.collectibles.HealthPotion;
import engine.Camera;
import engine.GameStateManager;
import engine.LevelLoader;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

public abstract class Level {

    protected Player player;
    protected List<Rectangle> platforms;
    protected List<Collectible> collectibles;
    protected Camera camera;
    protected int levelWidth;

    public Level(Player player, Camera camera, String levelFile) {
        this.player = player;
        this.camera = camera;
        LevelLoader loader = new LevelLoader(levelFile);
        this.platforms = loader.getPlatforms();
        this.collectibles = loader.getCollectibles();
        this.levelWidth = loader.getLevelWidth();
        player.setLevelWidth(levelWidth);
        camera.setLevelWidth(levelWidth);
    }

    public void update() {
        float prevY = player.getY();
        player.update();
        applyPlatformCollisions(prevY);
        checkCollectiblePickups();
        camera.update(player);
    }

    private void checkCollectiblePickups() {
        Rectangle playerBounds = player.getBounds();
        for (Collectible c : collectibles) {
            if (c.isCollected()) continue;
            if (playerBounds.intersects(c.getBounds())) {
                c.collect();
                if (c instanceof Diamond) {
                    GameStateManager.getInstance().addScore(((Diamond) c).getPoints());
                } else if (c instanceof HealthPotion) {
                    player.heal();
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
                if (prevBottom <= plat.y + 2 && playerBottom >= plat.y) {
                    player.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
                float platBottom = plat.y + plat.height;
                if (prevY >= platBottom - 2 && player.getY() < platBottom) {
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
        int offsetX = (int) camera.getX();
        g.translate(-offsetX, 0);

        g.setColor(new java.awt.Color(100, 70, 40));
        for (Rectangle plat : platforms) {
            g.fillRect(plat.x, plat.y, plat.width, plat.height);
        }
        for (Collectible c : collectibles) {
            if (!c.isCollected()) {
                c.draw(g);
            }
        }
        player.draw(g);

        g.translate(offsetX, 0);
    }

    public abstract boolean isComplete();
}
