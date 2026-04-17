package interfaces;

import java.awt.Graphics2D;

/**
    The Drawable interface is implemented by anything that renders
    itself to the screen during the render phase of the game loop.
*/
public interface Drawable {

    /**
        Draws this object using the given graphics context.
    */
    void draw(Graphics2D g);
}
