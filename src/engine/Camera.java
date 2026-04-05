package engine;

import entities.Player;

public class Camera {

    private float x;
    private int levelWidth;
    private int screenWidth;

    public Camera(int screenWidth, int levelWidth) {
        this.screenWidth = screenWidth;
        this.levelWidth = levelWidth;
    }

    public void update(Player player) {
        x = player.getX() + player.getWidth() / 2f - screenWidth / 2f;
        if (x < 0) x = 0;
        if (x > levelWidth - screenWidth) x = levelWidth - screenWidth;
    }

    public float getX() { return x; }

    public void setLevelWidth(int levelWidth) {
        this.levelWidth = levelWidth;
    }
}
