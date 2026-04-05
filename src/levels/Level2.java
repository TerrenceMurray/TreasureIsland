package levels;

import entities.Player;

public class Level2 extends Level {

    public Level2(Player player) {
        super(player, "config/level2.txt");
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
