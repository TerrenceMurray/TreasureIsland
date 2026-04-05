package levels;

import entities.Player;
import engine.Camera;

public class Level1 extends Level {

    public Level1(Player player, Camera camera) {
        super(player, camera, "config/level1.txt");
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
