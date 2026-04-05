package levels;

import entities.Player;
import engine.GameLoop;
import java.awt.Rectangle;

public class Level1 extends Level {

    public Level1(Player player) {
        super(player);
    }

    @Override
    protected void initPlatforms() {
        // Ground
        platforms.add(new Rectangle(0, 480, GameLoop.GAME_WIDTH, 60));

        // Platforms — staircase with ~70px height gaps (jump reaches ~100px)
        platforms.add(new Rectangle(120, 410, 150, 20));
        platforms.add(new Rectangle(350, 345, 150, 20));
        platforms.add(new Rectangle(570, 280, 150, 20));
        platforms.add(new Rectangle(300, 215, 180, 20));
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
