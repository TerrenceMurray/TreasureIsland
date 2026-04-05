package levels;

import entities.Player;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;

public abstract class Level {

    protected Player player;
    protected List<Rectangle> platforms;

    public Level(Player player) {
        this.player = player;
        this.platforms = new ArrayList<>();
        initPlatforms();
    }

    protected abstract void initPlatforms();

    public void update() {
        float prevY = player.getY();
        player.update();
        applyPlatformCollisions(prevY);
    }

    private void applyPlatformCollisions(float prevY) {
        Rectangle playerBounds = player.getBounds();
        float playerBottom = player.getY() + player.getHeight();
        float prevBottom = prevY + player.getHeight();
        boolean onPlatform = false;

        for (Rectangle plat : platforms) {
            if (playerBounds.intersects(plat)) {
                // Landing: player was above the platform top last frame
                if (prevBottom <= plat.y + 2 && playerBottom >= plat.y) {
                    player.landOn(plat.y);
                    onPlatform = true;
                    break;
                }
                // Head bump: player was below the platform bottom last frame
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
        drawPlatforms(g);
        player.draw(g);
    }

    protected void drawPlatforms(Graphics2D g) {
        g.setColor(new java.awt.Color(100, 70, 40));
        for (Rectangle plat : platforms) {
            g.fillRect(plat.x, plat.y, plat.width, plat.height);
        }
    }

    public abstract boolean isComplete();
}
