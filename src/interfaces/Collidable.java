package interfaces;

import java.awt.geom.Rectangle2D;

/**
    The Collidable interface is implemented by entities that
    participate in collision checks. Implementers return the
    axis-aligned bounding box used for overlap tests.
*/
public interface Collidable {

    /**
        Returns the axis-aligned bounding rectangle of this
        entity in world coordinates.
    */
    Rectangle2D.Double getBoundingRectangle();
}
