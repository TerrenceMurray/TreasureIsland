package engine;

import entities.Player;
import levels.Level;
import levels.Level1;
import levels.Level2;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    private static final GameConfig CFG = GameConfig.getInstance();
    public static final int GAME_WIDTH = CFG.getInt("game.width", 960);
    public static final int GAME_HEIGHT = CFG.getInt("game.height", 540);
    private static final int TARGET_FPS = CFG.getInt("game.fps", 60);
    private static final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS;

    private Thread gameThread;
    private volatile boolean isRunning;
    private volatile boolean isPaused;

    private BufferedImage buffer;

    private Player player;
    private Camera camera;
    private Level currentLevel;
    private int levelNumber = 1;
    private int transitionTimer;
    private static final int TRANSITION_DELAY = 120;

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        // Active rendering: tell Swing not to repaint the panel itself —
        // we draw directly via getGraphics() from gameRender().
        setIgnoreRepaint(true);
        setDoubleBuffered(false);

        buffer = new BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        player = new Player(
            CFG.getFloat("player.spawnX", 100),
            CFG.getFloat("player.spawnY", 400)
        );
        camera = new Camera(GAME_WIDTH, GAME_WIDTH);
        currentLevel = new Level1(player, camera);
    }

    public void startGame() {
        if (isRunning) return;
        isRunning = true;
        isPaused = false;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void endGame() {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void pauseGame() {
        isPaused = !isPaused;
    }

    public void startNewGame() {
        if (isRunning) endGame();
        GameStateManager.getInstance().resetScore();
        GameStateManager.getInstance().setState("PLAYING");
        levelNumber = 1;
        player = new Player(
            CFG.getFloat("player.spawnX", 100),
            CFG.getFloat("player.spawnY", 400)
        );
        camera = new Camera(GAME_WIDTH, GAME_WIDTH);
        currentLevel = new Level1(player, camera);
        startGame();
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                long startTime = System.nanoTime();
                if (!isPaused) gameUpdate();
                gameRender();
                long sleepTime = (OPTIMAL_TIME - (System.nanoTime() - startTime)) / 1_000_000;
                if (sleepTime > 0) Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void gameUpdate() {
        String state = GameStateManager.getInstance().getState();

        if (state.equals("PLAYING")) {
            currentLevel.update();

            if (player.isDead()) {
                GameStateManager.getInstance().setState("GAME_OVER");
            } else if (currentLevel.isComplete()) {
                if (levelNumber == 2) {
                    GameStateManager.getInstance().addScore(
                        CFG.getInt("score.levelComplete2", 500));
                    GameStateManager.getInstance().setState("VICTORY");
                } else {
                    GameStateManager.getInstance().setState("LEVEL_COMPLETE");
                    transitionTimer = 0;
                }
            }
        } else if (state.equals("LEVEL_COMPLETE")) {
            transitionTimer++;
            if (transitionTimer >= TRANSITION_DELAY) {
                levelNumber = 2;
                GameStateManager.getInstance().addScore(
                    CFG.getInt("score.levelComplete1", 200));
                int prevHealth = player.getHealth();
                player = new Player(64, 280);
                player.setHealth(prevHealth);
                currentLevel = new Level2(player, camera);
                GameStateManager.getInstance().setState("PLAYING");
            }
        }
    }

    public void gameRender() {
        // Double buffering: draw the whole frame onto the back buffer first
        Graphics2D imageContext = (Graphics2D) buffer.getGraphics();
        String state = GameStateManager.getInstance().getState();

        imageContext.setColor(new Color(135, 206, 235));
        imageContext.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        if (state.equals("MENU")) {
            drawMenu(imageContext);
        } else if (state.equals("PLAYING") || state.equals("LEVEL_COMPLETE") || state.equals("PAUSED")) {
            currentLevel.draw(imageContext);
            drawHUD(imageContext);

            if (state.equals("LEVEL_COMPLETE")) {
                drawCenteredText(imageContext, "Level Complete!", 48, Color.YELLOW, GAME_HEIGHT / 2 - 20);
            } else if (state.equals("PAUSED")) {
                imageContext.setColor(new Color(0, 0, 0, 120));
                imageContext.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
                drawCenteredText(imageContext, "Paused", 48, Color.WHITE, GAME_HEIGHT / 2 - 10);
                drawCenteredText(imageContext, "Press P to Resume", 18, Color.GRAY, GAME_HEIGHT / 2 + 30);
            }
        } else if (state.equals("GAME_OVER")) {
            drawEndScreen(imageContext, "Game Over");
        } else if (state.equals("VICTORY")) {
            drawEndScreen(imageContext, "Victory!");
        }

        // Blit the completed frame to the panel in a single step
        Graphics2D g2 = (Graphics2D) getGraphics();
        if (g2 != null) {
            g2.drawImage(buffer, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
            Toolkit.getDefaultToolkit().sync();
            g2.dispose();
        }
        imageContext.dispose();
    }

    private void drawMenu(Graphics2D g) {
        drawCenteredText(g, "Treasure Island", 48, Color.WHITE, GAME_HEIGHT / 2 - 40);
        drawCenteredText(g, "Press ENTER to Play", 20, Color.WHITE, GAME_HEIGHT / 2 + 20);
        drawCenteredText(g, "Press Q to Quit", 16, Color.GRAY, GAME_HEIGHT / 2 + 60);
    }

    private void drawEndScreen(Graphics2D g, String title) {
        drawCenteredText(g, title, 48, Color.WHITE, GAME_HEIGHT / 2 - 40);
        drawCenteredText(g, "Score: " + GameStateManager.getInstance().getScore(), 24, Color.YELLOW, GAME_HEIGHT / 2 + 10);
        drawCenteredText(g, "Press Q to Quit", 16, Color.GRAY, GAME_HEIGHT / 2 + 60);
    }

    private void drawHUD(Graphics2D g) {
        // Hearts
        for (int i = 0; i < player.getMaxHealth(); i++) {
            g.setColor(i < player.getHealth() ? Color.RED : Color.DARK_GRAY);
            g.fillOval(10 + i * 30, 10, 20, 20);
        }

        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Score: " + GameStateManager.getInstance().getScore(), GAME_WIDTH - 140, 20);

        // Controls (top-right)
        g.setColor(new Color(255, 255, 255, 180));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int cx = GAME_WIDTH - 140;
        g.drawString("A/D  Move", cx, 45);
        g.drawString("W/Space  Jump", cx, 60);
        g.drawString("Shift  Attack", cx, 75);
        g.drawString("P  Pause", cx, 90);

        // Pause/Play button (bottom-left)
        String state = GameStateManager.getInstance().getState();
        int bx = 15, by = GAME_HEIGHT - 35;
        g.setColor(new Color(255, 255, 255, 160));
        if (state.equals("PAUSED")) {
            // Play triangle
            int[] xp = {bx, bx, bx + 16};
            int[] yp = {by, by + 20, by + 10};
            g.fillPolygon(xp, yp, 3);
        } else {
            // Pause bars
            g.fillRect(bx, by, 6, 20);
            g.fillRect(bx + 10, by, 6, 20);
        }
    }

    private void drawCenteredText(Graphics2D g, String text, int size, Color color, int y) {
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, size));
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (GAME_WIDTH - textWidth) / 2, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String state = GameStateManager.getInstance().getState();

        if (state.equals("MENU")) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                GameStateManager.getInstance().setState("PLAYING");
            } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                System.exit(0);
            }
            return;
        }

        if (state.equals("GAME_OVER") || state.equals("VICTORY")) {
            if (e.getKeyCode() == KeyEvent.VK_Q) {
                System.exit(0);
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_P) {
            if (state.equals("PLAYING")) {
                pauseGame();
                GameStateManager.getInstance().setState("PAUSED");
            } else if (state.equals("PAUSED")) {
                pauseGame();
                GameStateManager.getInstance().setState("PLAYING");
            }
            return;
        }

        if (state.equals("PAUSED")) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                player.setLeft(true);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                player.setRight(true);
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_SPACE:
                player.setJumping(true);
                break;
            case KeyEvent.VK_SHIFT:
                player.attack();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                player.setLeft(false);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                player.setRight(false);
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_SPACE:
                player.setJumping(false);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
