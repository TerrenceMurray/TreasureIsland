package entities;

import interfaces.Drawable;
import interfaces.Updatable;
import interfaces.Collidable;
import java.awt.geom.Rectangle2D;

/**
    The GameEntity class is the abstract base for every object
    in the world that can be drawn, updated, and collided with.
    It stores position, size, and vertical velocity.
*/
public abstract class GameEntity implements Drawable, Updatable, Collidable {

    // === Position and size ===
    // Top-left corner of the collision box, in world pixels.
    protected float x, y;
    protected int width, height;

    // === Physics ===
    // Downward speed in pixels/frame. Positive = falling (Java y grows downward).
    protected float velocityY;

    public GameEntity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public float getVelocityY() { return velocityY; }
}
