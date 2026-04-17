package levels;

import entities.Player;
import entities.collectibles.TreasureChest;
import entities.enemies.bosses.Boss;
import entities.enemies.bosses.EnragedFierceTooth;
import engine.Camera;
import engine.managers.GameConfig;
import engine.managers.GameStateManager;

/**
    The Level2 class is the palm-tree island level. It
    places background and foreground palm trees and spawns
    the Enraged Fierce Tooth final boss. When the boss dies,
    a treasure chest appears for the player to collect.
*/
public class Level2 extends Level {

    // Ground surface Y in world pixels — used to seat palm trees and the
    // treasure chest on the main terrain plane.
    private static final int GROUND_Y = 480;
    // Treasure chest sprite is 28px tall — offset it so its base sits on
    // the ground line at GROUND_Y.
    private static final int CHEST_HEIGHT = 28;

    public Level2(Player player, Camera camera) {
        super(player, camera, "config/level2.txt",
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/BG Image.png",
            "Palm Tree Island");

        GameConfig cfg = GameConfig.getInstance();
        groundOffset = cfg.getInt("level2.groundOffset", -4);
        terrain.setPlatformLift(cfg.getInt("level2.platformLift", 0));

        // Back trees — in open ground areas.
        // Entries are { worldX, worldY, spriteIndex }.
        palmTrees.setBackPlacements(new int[][] {
            {100, 480, 0},
            {500, 480, 1},
            {1200, 480, 2},
            {1850, 480, 3},
            {2500, 480, 0},
            {3450, 480, 1},
            // Leaning off terrain (y=448 raises them onto higher ground)
            {352, 448, 4},
            {896, 448, 6},
            {1792, 448, 4},
        });

        // Front trees — between terrain features.
        // Entries are { worldX, worldY, spriteIndex, variation }.
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
        // Spawn treasure chest where boss died. The player has to walk over
        // and pick it up to actually end the level.
        collectibles.add(new TreasureChest(boss.getX(), GROUND_Y - CHEST_HEIGHT));
    }
}
