package levels;

import entities.Player;

public class Level1 extends Level {

    public Level1(Player player) {
        super(player, "config/level1.txt");
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
