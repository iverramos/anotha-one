package entities;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Base game entity with physics, health, hit flash, and knockback.
 */
public abstract class Entity {
    protected double x, y;
    protected double vx, vy;
    protected int width, height;
    protected int health, maxHealth;
    protected boolean dead;
    protected double hitFlashTimer;
    protected boolean onGround;

    public Entity(double x, double y, int width, int height, int maxHealth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = maxHealth;
        this.maxHealth = maxHealth;
    }

    public abstract void update(double dt);
    public abstract void render(Graphics2D g, int camX, int camY);

    public void takeDamage(int amount) {
        if (dead) return;
        health = Math.max(0, health - amount);
        hitFlashTimer = 0.10;
        if (health <= 0) {
            dead = true;
        }
    }

    public void heal(int amount) {
        if (dead) return;
        health = Math.min(maxHealth, health + amount);
    }

    public void applyKnockback(double kx, double ky) {
        vx += kx;
        vy += ky;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }

    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVelocity(double vx, double vy) { this.vx = vx; this.vy = vy; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int mh) { this.maxHealth = mh; }
    public boolean isDead() { return dead; }
    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean og) { this.onGround = og; }
}
