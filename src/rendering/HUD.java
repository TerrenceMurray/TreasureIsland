package rendering;

import engine.GameStateManager;
import engine.ImageManager;
import entities.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
    The HUD class draws everything that sits on top of the game
    world: the in-game heads-up display (hearts, score, controls,
    pause button), the menu and end screens, and the pause /
    level-complete overlays. All UI text is drawn in a pixel-style
    Monospaced bold font with a black outline.
*/
public class HUD {

    // === Asset paths ===
    private static final String UI_BASE = "assets/Treasure Hunters/Wood and Paper UI/Sprites/";

    // === Layout constants ===
    private static final int HEART_DRAW = 44;       // pixels drawn per heart
    private static final int HEART_OVERLAP = 6;     // pixels of overlap in the heart row
    private static final int MARGIN = 12;           // generic HUD padding
    private static final int CONTROLS_LINE_H = 15;
    private static final int CONTROLS_COLUMN_GAP = 70;
    private static final int PAUSE_BTN_W = 16;
    private static final int PAUSE_BTN_H = 20;

    // === Colors ===
    private static final Color GOLD = new Color(255, 230, 150);
    private static final Color SEPARATOR = new Color(180, 180, 180);
    private static final Color DIMMED = new Color(220, 220, 220);
    private static final Color OVERLAY_DIM = new Color(0, 0, 0, 120);
    private static final Color OVERLAY_DARK = new Color(0, 0, 0, 140);

    // === Screen dimensions ===
    private final int screenWidth;
    private final int screenHeight;

    // === Sprites (Wood and Paper UI asset pack) ===
    private final BufferedImage heartFull;
    private final BufferedImage heartEmpty;
    private final BufferedImage uiBoard;

    // === Player reference for the hearts ===
    private Player player;

    public HUD(Player player, int screenWidth, int screenHeight) {
        this.player = player;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.heartFull = ImageManager.loadBufferedImage(UI_BASE + "Life Bars/Big Bars/1.png");
        this.heartEmpty = toGrayscale(heartFull);
        this.uiBoard = ImageManager.loadBufferedImage(UI_BASE + "Prefabs/1.png");
    }

    /**
        Updates the player reference. Needed when the player is
        rebuilt between levels.
    */
    public void setPlayer(Player player) {
        this.player = player;
    }

    // ---------- Top-level screens ----------

    /**
        Draws the main menu: wooden board with title and prompts.
    */
    public void drawMenu(Graphics2D g) {
        drawUiPanel(g, 220, 360);
        drawCentered(g, "Treasure Island", 40, GOLD, screenHeight / 2 - 40);
        drawCentered(g, "Press ENTER to Play", 18, Color.WHITE, screenHeight / 2 + 20);
        drawCentered(g, "Press Q to Quit", 14, DIMMED, screenHeight / 2 + 50);
    }

    /**
        Draws the Game Over / Victory end screen with a dark overlay
        and the final score.
    */
    public void drawEndScreen(Graphics2D g, String title) {
        g.setColor(OVERLAY_DARK);
        g.fillRect(0, 0, screenWidth, screenHeight);
        drawUiPanel(g, 260, 320);
        drawCentered(g, title, 40, GOLD, screenHeight / 2 - 40);
        drawCentered(g, "Score: " + GameStateManager.getInstance().getScore(), 22, Color.WHITE, screenHeight / 2 + 10);
        drawCentered(g, "Press Q to Quit", 14, DIMMED, screenHeight / 2 + 50);
    }

    /**
        Draws the in-game HUD: hearts, score, controls list, and
        the bottom-left pause / play indicator.
    */
    public void drawInGame(Graphics2D g) {
        drawHearts(g);
        drawScore(g);
        drawControls(g);
        drawPauseIndicator(g);
    }

    /**
        Draws the translucent "Paused" overlay on top of the game
        world.
    */
    public void drawPauseOverlay(Graphics2D g) {
        g.setColor(OVERLAY_DIM);
        g.fillRect(0, 0, screenWidth, screenHeight);
        drawCentered(g, "Paused", 48, Color.WHITE, screenHeight / 2 - 10);
        drawCentered(g, "Press P to Resume", 18, Color.GRAY, screenHeight / 2 + 30);
    }

    /**
        Draws the "Level Complete!" banner.
    */
    public void drawLevelCompleteOverlay(Graphics2D g) {
        drawCentered(g, "Level Complete!", 48, Color.YELLOW, screenHeight / 2 - 20);
    }

    // ---------- HUD pieces ----------

    private void drawHearts(Graphics2D g) {
        int maxHealth = player.getMaxHealth();
        int currentHealth = player.getHealth();
        for (int i = 0; i < maxHealth; i++) {
            BufferedImage sprite = (i < currentHealth) ? heartFull : heartEmpty;
            if (sprite == null) continue;
            int hx = MARGIN + i * (HEART_DRAW - HEART_OVERLAP);
            g.drawImage(sprite, hx, MARGIN, HEART_DRAW, HEART_DRAW, null);
        }
    }

    private void drawScore(Graphics2D g) {
        String scoreText = "Score: " + GameStateManager.getInstance().getScore();
        int scoreX = screenWidth - MARGIN - measureWidth(g, scoreText, 18);
        drawOutlined(g, scoreText, scoreX, 30, 18, GOLD);
    }

    private void drawControls(Graphics2D g) {
        // Two-column layout: keys right-aligned at a shared boundary so
        // the separators line up vertically.
        int controlsRight = screenWidth - MARGIN;
        int keysRight = controlsRight - CONTROLS_COLUMN_GAP;
        int actionsLeft = keysRight + 14;
        int cy = 58;
        drawControlRow(g, "A/D",     "Move",   keysRight, actionsLeft, cy);
        drawControlRow(g, "W/Space", "Jump",   keysRight, actionsLeft, cy + CONTROLS_LINE_H);
        drawControlRow(g, "Shift",   "Attack", keysRight, actionsLeft, cy + CONTROLS_LINE_H * 2);
        drawControlRow(g, "P",       "Pause",  keysRight, actionsLeft, cy + CONTROLS_LINE_H * 3);
    }

    private void drawControlRow(Graphics2D g, String key, String action, int keysRight, int actionsLeft, int y) {
        int size = 12;
        int keyX = keysRight - measureWidth(g, key, size);
        drawOutlined(g, key, keyX, y, size, GOLD);
        drawOutlined(g, ">", keysRight + 2, y, size, SEPARATOR);
        drawOutlined(g, action, actionsLeft, y, size, Color.WHITE);
    }

    private void drawPauseIndicator(Graphics2D g) {
        String state = GameStateManager.getInstance().getState();
        int bx = 15, by = screenHeight - 35;
        g.setColor(new Color(255, 255, 255, 160));
        if (state.equals("PAUSED")) {
            int[] xp = {bx, bx, bx + PAUSE_BTN_W};
            int[] yp = {by, by + PAUSE_BTN_H, by + PAUSE_BTN_H / 2};
            g.fillPolygon(xp, yp, 3);
        } else {
            g.fillRect(bx, by, 6, PAUSE_BTN_H);
            g.fillRect(bx + 10, by, 6, PAUSE_BTN_H);
        }
    }

    // ---------- Drawing helpers ----------

    private void drawUiPanel(Graphics2D g, int w, int h) {
        if (uiBoard == null) return;
        int px = (screenWidth - w) / 2;
        int py = (screenHeight - h) / 2;
        g.drawImage(uiBoard, px, py, w, h, null);
    }

    private void drawCentered(Graphics2D g, String text, int size, Color color, int y) {
        int x = (screenWidth - measureWidth(g, text, size)) / 2;
        drawOutlined(g, text, x, y, size, color);
    }

    /**
        Draws text in bold Monospaced font with a 1px black outline
        in 8 directions, matching the in-world pixel-style banners.
    */
    private static void drawOutlined(Graphics2D g, String text, int x, int y, int size, Color color) {
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

    private static int measureWidth(Graphics2D g, String text, int size) {
        g.setFont(new Font("Monospaced", Font.BOLD, size));
        return g.getFontMetrics().stringWidth(text);
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
}
