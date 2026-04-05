package levels;

import entities.Player;
import engine.Camera;

public class Level2 extends Level {

    public Level2(Player player, Camera camera) {
        super(player, camera, "config/level2.txt");
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
