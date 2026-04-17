package interfaces;

/**
    The Attackable interface is implemented by entities that can
    take damage and be destroyed, such as the player and enemies.
*/
public interface Attackable {

    /**
        Applies the given amount of damage to this entity.
    */
    void takeDamage(int amount);

    /**
        Returns true if this entity has been destroyed.
    */
    boolean isDead();
}
