package entities.enemies;

import entities.Player;
import java.awt.Color;
import java.awt.Graphics2D;

public class Crabby extends Enemy {

    private float detectionRange = 100f;
    private float leashRange = 180f;
    private float idleSpeed = 0.5f;
    private float chargeSpeed = 2f;
    private boolean charging;
    private int aggroDelay;
    private static final int AGGRO_DELAY_MAX = 40;
    private Player target;
    private float spawnX;

    public Crabby(float x, float y, Player target) {
        super(x, y, 40, 30, 5, 1);
        this.target = target;
        this.spawnX = x;
    }

    @Override
    public void update() {
        if (isDead()) return;

        float playerDistFromSpawn = Math.abs(target.getX() - spawnX);
        boolean playerAbove = target.getY() + target.getHeight() < y;

        if (!charging && playerDistFromSpawn < detectionRange && !playerAbove) {
            if (aggroDelay < AGGRO_DELAY_MAX) {
                aggroDelay++;
            } else {
                charging = true;
            }
        } else if (charging && (playerDistFromSpawn > leashRange || playerAbove)) {
            charging = false;
            aggroDelay = 0;
        } else if (!charging) {
            aggroDelay = 0;
        }

        if (charging) {
            float dx = target.getX() - x;
            float dist = Math.abs(dx);
            if (dist > width) {
                x += (dx > 0 ? chargeSpeed : -chargeSpeed);
            }
        } else {
            float toSpawn = spawnX - x;
            if (Math.abs(toSpawn) > idleSpeed) {
                x += (toSpawn > 0 ? idleSpeed : -idleSpeed);
            } else {
                x = spawnX;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (isDead()) return;
        g.setColor(charging ? Color.RED : Color.ORANGE);
        g.fillRect((int) x, (int) y, width, height);
    }
}
