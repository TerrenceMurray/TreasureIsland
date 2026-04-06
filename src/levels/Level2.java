package levels;

import entities.Player;
import entities.enemies.Boss;
import entities.enemies.EnragedFierceTooth;
import engine.Camera;
import engine.GameConfig;
import engine.GameStateManager;

public class Level2 extends Level {

    public Level2(Player player, Camera camera) {
        super(player, camera, "config/level2.txt",
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/BG Image.png",
            "Palm Tree Island");

        // Back trees: 0-3 = regular (on ground), 4-5 = left lean, 6-7 = right lean
        palmTrees.setBackPlacements(new int[][] {
            // Regular — on ground between platforms
            {80, 480, 0},
            {400, 480, 1},
            {900, 480, 2},
            {1400, 480, 3},
            {1850, 480, 0},
            {2450, 480, 1},
            {3600, 480, 2},
            // Right-leaning — on left edges (leans outward right)
            {960, 448, 6},
            {1984, 384, 7},
            {3008, 384, 6},
            // Left-leaning — on right edges (leans outward left)
            {224, 448, 4},
            {1312, 448, 5},
            {2272, 384, 4},
        });

        // Front trees — 2x scale, 2-3 segments
        palmTrees.setFrontPlacements(new int[][] {
            {160, 480, 2, 0},
            {640, 480, 3, 1},
            {1350, 480, 2, 2},
            {1950, 480, 3, 3},
            {2500, 480, 2, 0},
            {3600, 480, 3, 1},
        });

    }

    @Override
    protected Boss createBoss(float x, float y) {
        return new EnragedFierceTooth(x, y, player);
    }

    @Override
    protected void onBossDefeated() {
        GameStateManager.getInstance().addScore(
            GameConfig.getInstance().getInt("score.enragedFierceTooth", 1000));
        bossDefeated = true;
    }
}
