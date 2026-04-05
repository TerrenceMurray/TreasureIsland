package levels;

import entities.Player;
import engine.GameLoop;
import java.awt.Rectangle;

public class Level2 extends Level {

    public Level2(Player player) {
        super(player);
    }

    @Override
    protected void initPlatforms() {
        platforms.add(new Rectangle(0, 480, GameLoop.GAME_WIDTH, 60));
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
