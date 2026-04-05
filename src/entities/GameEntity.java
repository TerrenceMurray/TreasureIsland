package entities;

import interfaces.Drawable;
import interfaces.Updatable;
import interfaces.Collidable;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class GameEntity implements Drawable, Updatable, Collidable {

    protected float x, y;
    protected int width, height;
    protected float velocityX, velocityY;

    public GameEntity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
