package entities.collectibles;

import entities.GameEntity;

/**
    The Collectible class is the abstract base for items the
    player can pick up, such as diamonds, potions, and chests.
*/
public abstract class Collectible extends GameEntity {

    // === State ===
    // Becomes true once the player walks over the item. A collected item
    // stops drawing and will no longer be checked for collisions.
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
