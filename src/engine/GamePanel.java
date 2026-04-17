package engine;

import engine.managers.GameConfig;
import engine.managers.GameStateManager;
import entities.Player;
import levels.Level;
import levels.Level1;
import levels.Level2;

import rendering.ui.HUD;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Color;
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
    private HUD hud;

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

        player = new Player(
            CFG.getFloat("player.spawnX", 100),
            CFG.getFloat("player.spawnY", 400)
        );
        camera = new Camera(GAME_WIDTH, GAME_WIDTH);
        currentLevel = new Level1(player, camera);
        hud = new HUD(player, GAME_WIDTH, GAME_HEIGHT);
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
        hud.setPlayer(player);
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
                hud.setPlayer(player);
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
            hud.drawMenu(imageContext);
        } else if (state.equals("PLAYING") || state.equals("LEVEL_COMPLETE") || state.equals("PAUSED")) {
            currentLevel.draw(imageContext);
            hud.drawInGame(imageContext);

            if (state.equals("LEVEL_COMPLETE")) {
                hud.drawLevelCompleteOverlay(imageContext);
            } else if (state.equals("PAUSED")) {
                hud.drawPauseOverlay(imageContext);
            }
        } else if (state.equals("GAME_OVER")) {
            hud.drawEndScreen(imageContext, "Game Over");
        } else if (state.equals("VICTORY")) {
            hud.drawEndScreen(imageContext, "Victory!");
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
