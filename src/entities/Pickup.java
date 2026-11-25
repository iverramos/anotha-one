package entities;

import combat.Weapon;
import rendering.SpriteManager;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Floor drops: Bouncing gold coins, healing hearts, and dropped weapons.
 * Coins and hearts are magnetically attracted to the player when close!
 */
public final class Pickup {
    public enum Type { COIN, HEART, WEAPON }

    private double x, y;
    private double vx, vy;
    private final Type type;
    private final int value; // gold amount or heal amount
    private final Weapon weapon;
    private boolean onGround;
    private boolean collected;
    private double bobTimer;
    private final SpriteManager sprites;

    public Pickup(SpriteManager sprites, double x, double y, Type type, int value, Weapon weapon) {
        this.sprites = sprites;
        this.x = x;
        this.y = y;
        this.type = type;
        this.value = value;
        this.weapon = weapon;
        this.vx = (Math.random() - 0.5) * 60;
        this.vy = -60 - Math.random() * 50;
        this.bobTimer = Math.random() * Math.PI * 2;
    }

    public static Pickup createCoin(SpriteManager sm, double x, double y, int value) {
        return new Pickup(sm, x, y, Type.COIN, value, null);
    }

    public static Pickup createHeart(SpriteManager sm, double x, double y, int heal) {
        return new Pickup(sm, x, y, Type.HEART, heal, null);
    }

    public static Pickup createWeapon(SpriteManager sm, double x, double y, Weapon w) {
        return new Pickup(sm, x, y, Type.WEAPON, 0, w);
    }

    public void update(double dt, double playerX, double playerY) {
        if (collected) return;
        bobTimer += dt * 4;

        // Magnet attraction toward player
        double dx = playerX - x;
        double dy = playerY - y;
        double distSq = dx * dx + dy * dy;
        if (type == Type.COIN && distSq < 70 * 70) {
            double dist = Math.sqrt(distSq);
            double speed = 180;
            vx += (dx / dist) * speed * dt * 5;
            vy += (dy / dist) * speed * dt * 5;
            x += vx * dt;
            y += vy * dt;
            return;
        }

        // Standard physics
        if (!onGround) {
            vy += 280 * dt; // gravity
            x += vx * dt;
            y += vy * dt;
        } else {
            vx *= 0.85;
            x += vx * dt;
        }
    }

    public void render(Graphics2D g, int camX, int camY) {
        if (collected) return;
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY + (onGround ? Math.sin(bobTimer) * 2 : 0));

        if (type == Type.COIN) {
            BufferedImage hud = sprites.sprite("hud")[0];
            BufferedImage coin = hud.getSubimage(60, 0, 10, 10);
            g.drawImage(coin, rx - 5, ry - 5, null);
        } else if (type == Type.HEART) {
            BufferedImage hud = sprites.sprite("hud")[0];
            BufferedImage heart = hud.getSubimage(0, 0, 12, 12);
            g.drawImage(heart, rx - 6, ry - 6, null);
        } else if (type == Type.WEAPON && weapon != null) {
            BufferedImage[] wImgs = sprites.sprite("weapons");
            if (weapon.getId() < wImgs.length) {
                g.drawImage(wImgs[weapon.getId()], rx - 12, ry - 12, null);
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x - 6, (int) y - 6, 12, 12);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setOnGround(boolean og) { this.onGround = og; if (og) vy = 0; }
    public boolean isOnGround() { return onGround; }
    public Type getType() { return type; }
    public int getValue() { return value; }
    public Weapon getWeapon() { return weapon; }
    public boolean isCollected() { return collected; }
    public void collect() { this.collected = true; }
}
