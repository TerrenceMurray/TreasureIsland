package entities.collectibles;

import entities.GameEntity;

public abstract class Collectible extends GameEntity {

    protected boolean collected;

    public Collectible(float x, float y, int width, int height) {
        super(x, y, width, height);
        this.collected = false;
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() { return collected; }
}
