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
            "assets/Treasure Hunters/Palm Tree Island/Sprites/Background/BG Image.png");
    }

    @Override
    protected Boss createBoss(float x, float y) {
        return new EnragedFierceTooth(x, y, player);
    }

    @Override
    protected void onBossDefeated() {
        GameStateManager.getInstance().addScore(
            GameConfig.getInstance().getInt("score.enragedFierceTooth", 1000));
        complete = true;
    }
}
