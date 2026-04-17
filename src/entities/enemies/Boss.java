package entities.enemies;

import entities.Player;
import engine.ImageManager;
import rendering.AnimatedSprite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class Boss extends Enemy {

    protected Player target;
    protected int attackInterval;
    protected int attackTimer;
    protected boolean lunging;
    protected int lungeTick;
    protected static final int LUNGE_DURATION = 20;
    protected boolean facingRight;
    protected AnimatedSprite sprite;
    protected String bossName;
    private static final String SKULL_SHEET = "assets/Skull_Icons/PNG/Skull_Icons_128x96.png";
    private static final int SKULL_TILE = 32;  // 4×3 tileset
    private static BufferedImage skullSheet;  // loaded once, shared by all bosses
    private BufferedImage skullIcon;          // per-boss sub-image
    private int jumpCooldown;
    private static final int JUMP_COOLDOWN_MAX = 90;
    private static final float JUMP_FORCE = -9f;
    // Kinematic airborne state: y(t) = airStartY + airStartVel*t + ½·GRAVITY·t²
    private int airTime;
    private float airStartY;
    private float airStartVel;

    public Boss(float x, float y, int width, int height, int health, int damage, int attackInterval, Player target, String bossName, int skullCol, int skullRow) {
        super(x, y, width, height, health, damage);
        this.attackInterval = attackInterval;
        this.target = target;
        this.bossName = bossName;

        if (skullSheet == null) skullSheet = ImageManager.loadBufferedImage(SKULL_SHEET);
        if (skullSheet != null) {
            skullIcon = skullSheet.getSubimage(skullCol * SKULL_TILE, skullRow * SKULL_TILE, SKULL_TILE, SKULL_TILE);
        }
    }

    @Override
    public void update() {
        if (dying) {
            sprite.setState(inDeathHitPhase() ? "deadhit" : "dead");
            sprite.update();
            updateDeath();
            return;
        }
        if (isDead()) return;
        applyKnockback();

        if (hurtTimer > 0) {
            hurtTimer--;
            sprite.setState("hit");
            sprite.update();
            return;
        }

        if (jumpCooldown > 0) jumpCooldown--;

        if (lunging) {
            lungeTick++;
            float lungeSpeed = facingRight ? 3f : -3f;
            if (lungeTick < LUNGE_DURATION / 2) {
                x += lungeSpeed;
            }
            if (lungeTick >= LUNGE_DURATION) {
                lunging = false;
                lungeTick = 0;
            }
            sprite.setState("attack");
            sprite.update();
            return;
        }

        float dx = target.getX() - x;
        float dy = target.getY() - y;
        float dist = Math.abs(dx);
        float walkSpeed = getWalkSpeed();
        facingRight = dx > 0;

        // Jump toward player if they're on a higher platform
        if (dy < -60 && !inAir && jumpCooldown <= 0) {
            velocityY = JUMP_FORCE;
            setInAir(true);  // captures airStartY and airStartVel from current velocityY
            jumpCooldown = JUMP_COOLDOWN_MAX;
        }

        if (dist > this.width) {
            x += (dx > 0 ? walkSpeed : -walkSpeed);
            sprite.setState(inAir ? "run" : "run");
        } else {
            sprite.setState("idle");
        }

        attackTimer++;
        if (attackTimer >= attackInterval && dist < this.width * 2 && !inAir) {
            lunging = true;
            lungeTick = 0;
            attackTimer = 0;
        }

        sprite.setFlipped(facingRight);
        sprite.update();
    }

    @Override
    public java.awt.geom.Rectangle2D.Double getBoundingRectangle() {
        if (lunging) {
            int attackReach = width / 2;
            int bx = facingRight ? (int) x : (int) x - attackReach;
            return new java.awt.geom.Rectangle2D.Double(bx, y, width + attackReach, height);
        }
        return super.getBoundingRectangle();
    }

    @Override
    public boolean canDealDamage() {
        return lunging;
    }

    protected void drawBossName(Graphics2D g) {
        if (dying) return;
        g.setFont(new Font("Monospaced", Font.BOLD, 14));

        int nameW = g.getFontMetrics().stringWidth(bossName);
        int textX = (int) x + width / 2 - nameW / 2;
        int textY = (int) y - 18;

        // 1px black outline in 8 directions
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                g.drawString(bossName, textX + dx, textY + dy);
            }
        }

        g.setColor(Color.WHITE);
        g.drawString(bossName, textX, textY);
    }

    protected void drawSkull(Graphics2D g) {
        if (dying || isDead() || skullIcon == null) return;
        int skullW = 32;
        int skullH = 32;
        int skullX = (int) x + width / 2 - skullW / 2;
        int skullY = (int) y - 32 - skullH;  // above the name
        g.drawImage(skullIcon, skullX, skullY, skullW, skullH, null);
    }

    protected abstract float getWalkSpeed();

    @Override
    public void applyGravity() {
        if (!inAir) return;
        airTime++;
        // s = ut + ½at²
        y = airStartY + airStartVel * airTime + 0.5f * GRAVITY * airTime * airTime;
        velocityY = airStartVel + GRAVITY * airTime;
    }

    @Override
    public void setInAir(boolean newInAir) {
        if (newInAir && !inAir) {
            airStartY = y;
            airTime = 0;
            airStartVel = velocityY;
        }
        super.setInAir(newInAir);
    }
}
