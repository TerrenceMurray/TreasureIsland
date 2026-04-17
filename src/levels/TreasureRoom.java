package levels;

import entities.Player;
import entities.collectibles.TreasureChest;
import entities.enemies.Boss;
import engine.Camera;

/**
    The TreasureRoom class is a short reward level that
    contains only a treasure chest and some palm-tree decor.
    Work in progress: not yet wired into the main game flow.
*/
public class TreasureRoom extends Level {

    public TreasureRoom(Player player, Camera camera) {
        super(player, camera, "config/treasure.txt",
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/BG Image.png",
            "The Treasure");
        // Chest sits on the ground roughly centred on screen.
        collectibles.add(new TreasureChest(460, 452));

        // Back trees — { worldX, worldY, spriteIndex }.
        palmTrees.setBackPlacements(new int[][] {
            {150, 480, 0},
            {700, 480, 3},
        });
        // Front trees — { worldX, worldY, spriteIndex, variation }.
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
