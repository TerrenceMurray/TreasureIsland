package entities.enemies;

import interfaces.Updatable;
import interfaces.Drawable;
import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;

public class EnemyWave implements Updatable, Drawable {

    private List<Enemy> enemies;

    public EnemyWave() {
        enemies = new ArrayList<>();
    }

    public void add(Enemy e) {
        enemies.add(e);
    }

    @Override
    public void update() {
        for (Enemy e : enemies) {
            e.update();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        for (Enemy e : enemies) {
            if (!e.isDead()) {
                e.draw(g);
            }
        }
    }

    public boolean isDefeated() {
        for (Enemy e : enemies) {
            if (!e.isDead()) return false;
        }
        return true;
    }

    public List<Enemy> getEnemies() { return enemies; }
}
