package levels;

import entities.Player;
import entities.collectibles.TreasureChest;
import entities.enemies.Boss;
import engine.Camera;

public class TreasureRoom extends Level {

    public TreasureRoom(Player player, Camera camera) {
        super(player, camera, "config/treasure.txt",
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/BG Image.png",
            "The Treasure");
        collectibles.add(new TreasureChest(460, 452));

        palmTrees.setBackPlacements(new int[][] {
            {150, 480, 0},
            {700, 480, 3},
        });
        palmTrees.setFrontPlacements(new int[][] {
            {50, 480, 4, 0},
            {800, 480, 4, 2},
        });
    }

    @Override
    protected Boss createBoss(float x, float y) {
        return null;
    }

    @Override
    protected void onBossDefeated() {
    }
}
