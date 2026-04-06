package levels;

import entities.Player;
import entities.enemies.Boss;
import entities.enemies.FierceTooth;
import engine.Camera;
import engine.GameConfig;
import engine.GameStateManager;

public class Level1 extends Level {

    public Level1(Player player, Camera camera) {
        super(player, camera, "config/level1.txt",
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/BG Image.png",
            "The Pirate Ship");

        // Sparse back trees — pirate ship setting, just a few in the distance
        palmTrees.setBackPlacements(new int[][] {
            {2800, 480, 0},
            {3000, 480, 2},
        });
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
