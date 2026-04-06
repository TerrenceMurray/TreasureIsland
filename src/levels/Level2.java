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

        // Back trees — faded, behind ground level
        palmTrees.setBackPlacements(new int[][] {
            {300, 480, 0},
            {900, 480, 3},
            {1600, 480, 1},
            {2400, 480, 4},
            {3500, 480, 2},
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
