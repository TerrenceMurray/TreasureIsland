package engine;

import entities.Player;

public class Camera {

    private float x;
    private float shakeX, shakeY;
    private int shakeDuration;
    private float shakeIntensity;
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

        if (shakeDuration > 0) {
            shakeDuration--;
            shakeX = (float) (Math.random() * 2 - 1) * shakeIntensity;
            shakeY = (float) (Math.random() * 2 - 1) * shakeIntensity;
            shakeIntensity *= 0.9f;
        } else {
            shakeX = 0;
            shakeY = 0;
        }
    }

    public void shake(float intensity, int duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
    }

    public float getX() { return x + shakeX; }
    public float getShakeY() { return shakeY; }

    public void setLevelWidth(int levelWidth) {
        this.levelWidth = levelWidth;
    }
}
