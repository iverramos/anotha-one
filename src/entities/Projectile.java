package entities;

import rendering.SpriteManager;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Ranged combat projectiles: Arrows, Bullets, Fireballs, and Boss Dark Orbs.
 */
public final class Projectile {
    public enum Type { ARROW, BULLET, FIREBALL, DARK_ORB }
    public enum Owner { PLAYER, ENEMY }

    private double x, y;
    private double vx, vy;
    private final Type type;
    private final Owner owner;
    private final int damage;
    private final double maxLife;
    private double life;
    private boolean active = true;
    private final SpriteManager sprites;

    public Projectile(SpriteManager sprites, double x, double y, double angle, double speed, Type type, Owner owner, int damage) {
        this.sprites = sprites;
        this.x = x;
        this.y = y;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.type = type;
        this.owner = owner;
        this.damage = damage;
        this.maxLife = (type == Type.BULLET) ? 0.6 : 2.5;
    }

    public void update(double dt) {
        if (!active) return;
        life += dt;
        x += vx * dt;
        y += vy * dt;
        if (type == Type.ARROW) {
            vy += 90 * dt; // slight arrow drop
        }
        if (life >= maxLife) {
            active = false;
        }
    }

    public void render(Graphics2D g, int camX, int camY) {
        if (!active) return;
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        double angle = Math.atan2(vy, vx);
        BufferedImage[] projs = sprites.slice("effects/projectiles.png", 16, 16);
        int idx = 0;
        switch (type) {
            case ARROW: idx = 0; break;
            case BULLET: idx = 1; break;
            case FIREBALL: idx = 2; break;
            case DARK_ORB: idx = 3; break;
        }

        if (idx < projs.length) {
            AffineTransform old = g.getTransform();
            g.translate(rx, ry);
            g.rotate(angle);
            g.drawImage(projs[idx], -8, -8, null);
            g.setTransform(old);
        }
    }

    public Rectangle getBounds() {
        int r = (type == Type.DARK_ORB || type == Type.FIREBALL) ? 6 : 4;
        return new Rectangle((int) (x - r), (int) (y - r), r * 2, r * 2);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public Type getType() { return type; }
    public Owner getOwner() { return owner; }
    public int getDamage() { return damage; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
}
