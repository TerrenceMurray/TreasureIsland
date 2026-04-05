package engine;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class GameLoop extends JPanel implements Runnable {

    public static final int GAME_WIDTH = 960;
    public static final int GAME_HEIGHT = 540;
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS;

    private Thread gameThread;
    private boolean running;

    private BufferedImage buffer;

    public GameLoop() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        requestFocusInWindow();

        buffer = new BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
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
        long fpsTimer = System.nanoTime();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;

            if (elapsed >= OPTIMAL_TIME) {
                lastTime = now;
                update();
                render();
                repaint();
                frames++;
            } else {
                try {
                    long sleepTime = (OPTIMAL_TIME - elapsed) / 1_000_000;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (now - fpsTimer >= 1_000_000_000) {
                System.out.println("FPS: " + frames);
                frames = 0;
                fpsTimer = now;
            }
        }
    }

    private void update() {
    }

    private void render() {
        Graphics2D g2 = buffer.createGraphics();
        g2.clearRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, null);
    }
}
