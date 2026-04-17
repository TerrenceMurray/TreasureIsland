package interfaces;

/**
    The Updatable interface is implemented by anything that
    advances its state once per frame during the update phase of
    the game loop.
*/
public interface Updatable {

    /**
        Advances this object's state by one frame.
    */
    void update();
}
