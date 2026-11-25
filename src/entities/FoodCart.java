package entities;

import rendering.SpriteManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Han's Dungeon Diner stall.
 * Allows buying delicious food buffs with gold.
 */
public final class FoodCart {
    private final double x, y;
    private final SpriteManager sprites;

    public FoodCart(SpriteManager sprites, double x, double y) {
        this.sprites = sprites;
        this.x = x;
        this.y = y;
    }

    public boolean isPlayerNearby(double playerX, double playerY) {
        double dist = Math.hypot(playerX - (x + 24), playerY - (y + 18));
        return dist < 48;
    }

    public void render(Graphics2D g, int camX, int camY, double playerX, double playerY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage img = sprites.sprite("food_cart")[0];
        g.drawImage(img, rx, ry, null);
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, 48, 36);
    }
}
