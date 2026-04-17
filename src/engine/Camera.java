package engine;

import entities.Player;

/**
    The Camera class tracks the player horizontally and provides
    the scroll offset used when drawing the world. It also supports
    a temporary screen shake effect for impact feedback.
*/
public class Camera {

    // Per-frame multiplier applied to shakeIntensity so the shake fades out.
    // 0.9 = smooth decay over ~20 frames; higher values shake longer.
    private static final float SHAKE_DECAY = 0.9f;

    // === Position / bounds ===
    private float x;
    private int levelWidth;
    private int screenWidth;

    // === Screen shake state ===
    private float shakeX, shakeY;
    private int shakeDuration;
    private float shakeIntensity;

    /**
        Creates a new Camera for a level of the given width.
    */
    public Camera(int screenWidth, int levelWidth) {
        this.screenWidth = screenWidth;
        this.levelWidth = levelWidth;
    }

    /**
        Centers the camera on the player, clamps it to the level
        bounds, and advances any active shake effect.
    */
    public void update(Player player) {
        x = player.getX() + player.getWidth() / 2f - screenWidth / 2f;
        if (x < 0) x = 0;
        if (x > levelWidth - screenWidth) x = levelWidth - screenWidth;

        if (shakeDuration > 0) {
            shakeDuration--;
            // Random offset in the range [-intensity, +intensity] each axis.
            shakeX = (float) (Math.random() * 2 - 1) * shakeIntensity;
            shakeY = (float) (Math.random() * 2 - 1) * shakeIntensity;
            shakeIntensity *= SHAKE_DECAY;
        } else {
            shakeX = 0;
            shakeY = 0;
        }
    }

    /**
        Starts a screen shake effect with the given intensity (in
        pixels) that decays over the specified number of frames.
    */
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
