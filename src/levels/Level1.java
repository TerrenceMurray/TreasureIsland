package levels;

import entities.Player;
import entities.enemies.Boss;
import entities.enemies.FierceTooth;
import engine.Camera;
import engine.GameConfig;
import engine.GameStateManager;
import rendering.PirateTerrainRenderer;
import java.awt.Graphics2D;

/**
    The Level1 class is the pirate-ship level. It uses the
    pirate terrain renderer, places deck flags, and spawns
    the Fierce Tooth boss.
*/
public class Level1 extends Level {

    // === Level-specific renderer ===
    private PirateTerrainRenderer pirateTerrain;

    public Level1(Player player, Camera camera) {
        super(player, camera, "config/level1.txt",
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/BG Image.png",
            "The Pirate Ship");

        GameConfig cfg = GameConfig.getInstance();
        pirateTerrain = new PirateTerrainRenderer(cfg.getInt("level1.platformLift", 2));
        groundOffset = cfg.getInt("level1.groundOffset", -2);

        // Decorative flags placed directly on the terrain.
        // Entries are { worldX, worldY, spriteIndex }.
        decor.setPlacements(new int[][] {
            {192, 352, 0},    // Upper deck
            {2816, 352, 0},   // Boss arena
        });
    }

    @Override
    protected void drawTerrain(Graphics2D g) {
        pirateTerrain.draw(g, platforms);
    }

    @Override
    protected Boss createBoss(float x, float y) {
        return new FierceTooth(x, y, player);
    }

    @Override
    protected void onBossDefeated() {
        GameStateManager.getInstance().addScore(
            GameConfig.getInstance().getInt("score.fierceTooth", 500));
        bossDefeated = true;
    }
}
