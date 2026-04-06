package levels;

import entities.Player;
import entities.collectibles.TreasureChest;
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

        // Back trees — in open ground areas
        palmTrees.setBackPlacements(new int[][] {
            {100, 480, 0},
            {500, 480, 1},
            {1200, 480, 2},
            {1850, 480, 3},
            {2500, 480, 0},
            {3450, 480, 1},
            // Leaning off terrain
            {352, 448, 4},
            {896, 448, 6},
            {1792, 448, 4},
        });

        // Front trees — between terrain features
        palmTrees.setFrontPlacements(new int[][] {
            {150, 480, 2, 0},
            {550, 480, 3, 1},
            {1250, 480, 2, 2},
            {1850, 480, 3, 3},
            {3450, 480, 2, 0},
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

    @Override
    protected void onBossAnimationComplete() {
        // Spawn treasure chest where boss died — player must collect it
        collectibles.add(new TreasureChest(boss.getX(), 480 - 28));
    }
}
