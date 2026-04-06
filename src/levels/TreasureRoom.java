package levels;

import entities.Player;
import entities.collectibles.TreasureChest;
import entities.enemies.Boss;
import engine.Camera;

public class TreasureRoom extends Level {

    public TreasureRoom(Player player, Camera camera) {
        super(player, camera, "config/treasure.txt");
        collectibles.add(new TreasureChest(460, 452));
    }

    @Override
    protected Boss createBoss(float x, float y) {
        return null;
    }

    @Override
    protected void onBossDefeated() {
    }
}
