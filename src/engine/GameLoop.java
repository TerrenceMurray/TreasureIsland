package engine;

import entities.Player;
import levels.Level;
import levels.Level1;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class GameLoop extends JPanel implements Runnable, KeyListener {

    private static final GameConfig CFG = GameConfig.getInstance();
    public static final int GAME_WIDTH = CFG.getInt("game.width", 960);
    public static final int GAME_HEIGHT = CFG.getInt("game.height", 540);
    private static final int TARGET_FPS = CFG.getInt("game.fps", 60);
    private static final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS;

    private Thread gameThread;
    private boolean running;

    private BufferedImage buffer;

    private Player player;
    private Camera camera;
    private Level currentLevel;

    public GameLoop() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        buffer = new BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        player = new Player(
            CFG.getFloat("player.spawnX", 100),
            CFG.getFloat("player.spawnY", 400)
        );
        camera = new Camera(GAME_WIDTH, GAME_WIDTH);
        currentLevel = new Level1(player, camera);
    }

    public void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stop() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();

            if (now - lastTime >= OPTIMAL_TIME) {
                lastTime = now;
                update();
                render();
                repaint();
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void update() {
        currentLevel.update();
    }

    private void render() {
        Graphics2D g2 = buffer.createGraphics();

        g2.setColor(new Color(135, 206, 235));
        g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        currentLevel.draw(g2);

        // HUD: health hearts
        for (int i = 0; i < player.getMaxHealth(); i++) {
            g2.setColor(i < player.getHealth() ? Color.RED : Color.DARK_GRAY);
            g2.fillOval(10 + i * 30, 10, 20, 20);
        }

        // HUD: score
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(18f));
        g2.drawString("Score: " + GameStateManager.getInstance().getScore(), GAME_WIDTH - 140, 28);

        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, null);
    }

    @Override
    public void keyPressed(KeyEvent e) {
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
