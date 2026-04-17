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

/**
    The GamePanel class is the main game surface. It runs the game
    loop on its own thread, handles keyboard input, dispatches
    update/render calls to the current Level, and draws menus and
    the HUD. It also manages transitions between levels and the
    top-level game states (MENU, PLAYING, PAUSED, etc.).
*/
public class GamePanel extends JPanel implements Runnable, KeyListener {

    // === Timing / display constants ===
    private static final GameConfig CFG = GameConfig.getInstance();
    public static final int GAME_WIDTH = CFG.getInt("game.width", 960);
    public static final int GAME_HEIGHT = CFG.getInt("game.height", 540);
    private static final int TARGET_FPS = CFG.getInt("game.fps", 60);
    // Nanoseconds per frame at the target FPS (used to pace the game loop)
    private static final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS;
    // 120 frames = 2 seconds at 60 FPS — "Level Complete" banner hold time
    private static final int TRANSITION_DELAY = 120;
    // Level 2 respawn coordinates (keep player near the left edge, on the ground)
    private static final int LEVEL2_SPAWN_X = 64;
    private static final int LEVEL2_SPAWN_Y = 280;

    // === Thread / lifecycle ===
    private Thread gameThread;
    private volatile boolean isRunning;
    private volatile boolean isPaused;

    // === Rendering ===
    private BufferedImage buffer;

    // === UI sprites (Wood and Paper UI asset pack) ===
    private BufferedImage heartFull;
    private BufferedImage heartEmpty;
    private BufferedImage uiBoard;
    private static final String UI_BASE = "assets/Treasure Hunters/Wood and Paper UI/Sprites/";
    private static final int HEART_DRAW = 44;     // pixels drawn per heart on screen
    private static final int HUD_MARGIN = 12;

    // === Game state ===
    private Player player;
    private Camera camera;
    private Level currentLevel;
    private int levelNumber = 1;
    private int transitionTimer;

    /**
        Creates the game panel, sets up active rendering, the back
        buffer, and the initial Player, Camera, and Level1.
    */
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

        heartFull = ImageManager.loadBufferedImage(UI_BASE + "Life Bars/Big Bars/1.png");
        heartEmpty = toGrayscale(heartFull);
        uiBoard = ImageManager.loadBufferedImage(UI_BASE + "Prefabs/1.png");

        player = new Player(
            CFG.getFloat("player.spawnX", 100),
            CFG.getFloat("player.spawnY", 400)
        );
        camera = new Camera(GAME_WIDTH, GAME_WIDTH);
        currentLevel = new Level1(player, camera);
    }

    /**
        Starts the game loop on a new thread. Does nothing if the
        loop is already running.
    */
    public void startGame() {
        if (isRunning) return;
        isRunning = true;
        isPaused = false;
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
        Stops the game loop and waits for the game thread to finish.
    */
    public void endGame() {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
        Toggles the paused flag on the game loop.
    */
    public void pauseGame() {
        isPaused = !isPaused;
    }

    /**
        Resets the score, the player, and the camera, then loads
        Level 1 and starts the loop from a clean state.
    */
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

    /**
        Advances the current level and handles game-state
        transitions such as death, level completion, and victory.
    */
    public void gameUpdate() {
        String state = GameStateManager.getInstance().getState();

        if (state.equals("PLAYING")) {
            currentLevel.update();

            if (player.isDead()) {
                GameStateManager.getInstance().setState("GAME_OVER");
            } else if (currentLevel.isComplete()) {
                // Level 2 is the final level — completing it goes to VICTORY,
                // otherwise show the LEVEL_COMPLETE banner and transition.
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
                // Rebuild the player so input/velocity state is fresh, but
                // preserve the current health so damage carries over into L2.
                int prevHealth = player.getHealth();
                player = new Player(LEVEL2_SPAWN_X, LEVEL2_SPAWN_Y);
                player.setHealth(prevHealth);
                currentLevel = new Level2(player, camera);
                GameStateManager.getInstance().setState("PLAYING");
            }
        }
    }

    /**
        Renders the current frame. Draws into a back buffer first,
        then blits the finished frame to the panel in one step.
    */
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
        drawUiPanel(g, 220, 360);
        drawCenteredText(g, "Treasure Island", 40, new Color(255, 230, 150), GAME_HEIGHT / 2 - 40);
        drawCenteredText(g, "Press ENTER to Play", 18, Color.WHITE, GAME_HEIGHT / 2 + 20);
        drawCenteredText(g, "Press Q to Quit", 14, new Color(220, 220, 220), GAME_HEIGHT / 2 + 50);
    }

    private void drawEndScreen(Graphics2D g, String title) {
        // Darken the world behind the board
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        drawUiPanel(g, 260, 320);
        drawCenteredText(g, title, 40, new Color(255, 230, 150), GAME_HEIGHT / 2 - 40);
        drawCenteredText(g, "Score: " + GameStateManager.getInstance().getScore(), 22, Color.WHITE, GAME_HEIGHT / 2 + 10);
        drawCenteredText(g, "Press Q to Quit", 14, new Color(220, 220, 220), GAME_HEIGHT / 2 + 50);
    }

    /**
        Draws the wooden UI board centred on the screen at the
        given size. The board is a 64x64 prefab — we stretch it
        to the requested size.
    */
    private void drawUiPanel(Graphics2D g, int w, int h) {
        if (uiBoard == null) return;
        int px = (GAME_WIDTH - w) / 2;
        int py = (GAME_HEIGHT - h) / 2;
        g.drawImage(uiBoard, px, py, w, h, null);
    }

    private void drawHUD(Graphics2D g) {
        // Hearts — one brass-frame heart per health slot, grayed out when lost
        int maxHealth = player.getMaxHealth();
        int currentHealth = player.getHealth();
        for (int i = 0; i < maxHealth; i++) {
            BufferedImage sprite = (i < currentHealth) ? heartFull : heartEmpty;
            if (sprite == null) continue;
            int hx = HUD_MARGIN + i * (HEART_DRAW - 6);  // slight overlap for a tighter row
            g.drawImage(sprite, hx, HUD_MARGIN, HEART_DRAW, HEART_DRAW, null);
        }

        // Score — outlined pixel font, right-aligned to match the controls column
        String scoreText = "Score: " + GameStateManager.getInstance().getScore();
        int scoreX = GAME_WIDTH - HUD_MARGIN - measureWidth(g, scoreText, 18);
        drawOutlinedString(g, scoreText, scoreX, 30, 18, new Color(255, 230, 150));

        // Controls (top-right). Two-column layout: keys right-aligned at a shared
        // column boundary so the arrow separators line up neatly.
        int controlsRight = GAME_WIDTH - HUD_MARGIN;
        int keysRight = controlsRight - 70;   // right edge of the keys column
        int actionsLeft = keysRight + 14;     // leaves room for the " > " separator
        int cy = 58;
        int lineH = 15;
        drawControlRow(g, "A/D",     "Move",   keysRight, actionsLeft, cy);
        drawControlRow(g, "W/Space", "Jump",   keysRight, actionsLeft, cy + lineH);
        drawControlRow(g, "Shift",   "Attack", keysRight, actionsLeft, cy + lineH * 2);
        drawControlRow(g, "P",       "Pause",  keysRight, actionsLeft, cy + lineH * 3);

        // Pause/Play button (bottom-left)
        String state = GameStateManager.getInstance().getState();
        int bx = 15, by = GAME_HEIGHT - 35;
        g.setColor(new Color(255, 255, 255, 160));
        if (state.equals("PAUSED")) {
            int[] xp = {bx, bx, bx + 16};
            int[] yp = {by, by + 20, by + 10};
            g.fillPolygon(xp, yp, 3);
        } else {
            g.fillRect(bx, by, 6, 20);
            g.fillRect(bx + 10, by, 6, 20);
        }
    }

    private void drawCenteredText(Graphics2D g, String text, int size, Color color, int y) {
        int textX = centeredX(g, text, size);
        drawOutlinedString(g, text, textX, y, size, color);
    }

    /**
        Draws text in bold Monospaced font with a 1px black outline
        in 8 directions, matching the in-world pixel-style banners.
    */
    private void drawOutlinedString(Graphics2D g, String text, int x, int y, int size, Color color) {
        g.setFont(new Font("Monospaced", Font.BOLD, size));
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                g.drawString(text, x + dx, y + dy);
            }
        }
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private int centeredX(Graphics2D g, String text, int size) {
        return (GAME_WIDTH - measureWidth(g, text, size)) / 2;
    }

    private int measureWidth(Graphics2D g, String text, int size) {
        g.setFont(new Font("Monospaced", Font.BOLD, size));
        return g.getFontMetrics().stringWidth(text);
    }

    /**
        Draws one row of the controls panel: key right-aligned at
        keysRight, a small separator, then the action left-aligned
        at actionsLeft. Keeps all rows in two clean columns.
    */
    private void drawControlRow(Graphics2D g, String key, String action, int keysRight, int actionsLeft, int y) {
        int keySize = 12;
        int actionSize = 12;
        int keyX = keysRight - measureWidth(g, key, keySize);
        drawOutlinedString(g, key, keyX, y, keySize, new Color(255, 230, 150));
        drawOutlinedString(g, ">", keysRight + 2, y, keySize, new Color(180, 180, 180));
        drawOutlinedString(g, action, actionsLeft, y, actionSize, Color.WHITE);
    }

    /**
        Returns a grayscale copy of the given image, preserving
        alpha. Uses the Week 6 pixel-tint pattern.
    */
    private static BufferedImage toGrayscale(BufferedImage src) {
        if (src == null) return null;
        BufferedImage copy = ImageManager.copyImage(src);
        for (int py = 0; py < copy.getHeight(); py++) {
            for (int px = 0; px < copy.getWidth(); px++) {
                int pixel = copy.getRGB(px, py);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha == 0) continue;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int gray = (red + green + blue) / 3;
                copy.setRGB(px, py, (alpha << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }
        return copy;
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
