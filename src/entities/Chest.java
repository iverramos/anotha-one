package entities;

import audio.SoundManager;
import combat.Weapon;
import rendering.SpriteManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactable Dungeon Treasure Chest.
 * Opens with [E] to shower weapons, gold coins, and health!
 */
public final class Chest {
    private final double x, y;
    private int state; // 0: Closed, 1: Opening, 2: Open
    private double animTimer;
    private boolean opened;
    private final SpriteManager sprites;
    private final Weapon rewardWeapon;

    public Chest(SpriteManager sprites, double x, double y, Weapon rewardWeapon) {
        this.sprites = sprites;
        this.x = x;
        this.y = y;
        this.rewardWeapon = rewardWeapon;
        this.state = 0;
    }

    public void update(double dt) {
        if (state == 1) {
            animTimer += dt;
            if (animTimer > 0.15) {
                state = 2;
            }
        }
    }

    public List<Pickup> tryOpen(double playerX, double playerY) {
        List<Pickup> loot = new ArrayList<>();
        if (opened) return loot;

        double dist = Math.hypot(playerX - (x + 12), playerY - (y + 10));
        if (dist < 32) {
            opened = true;
            state = 1;
            SoundManager.play(SoundManager.Sound.CHEST_OPEN);

            // Spawn loot
            if (rewardWeapon != null) {
                loot.add(Pickup.createWeapon(sprites, x + 12, y - 4, rewardWeapon));
            }
            loot.add(Pickup.createHeart(sprites, x + 8, y - 6, 25));
            for (int i = 0; i < 6; i++) {
                loot.add(Pickup.createCoin(sprites, x + 6 + i * 2, y - 8, 5));
            }
        }
        return loot;
    }

    public void render(Graphics2D g, int camX, int camY, double playerX, double playerY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.sprite("chest");
        int frameIdx = Math.min(frames.length - 1, state);
        g.drawImage(frames[frameIdx], rx, ry, null);
    }

    public boolean isPlayerNearby(double playerX, double playerY) {
        return !opened && Math.hypot(playerX - (x + 12), playerY - (y + 10)) < 34;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, 24, 20);
    }

    public boolean isOpened() { return opened; }
}
