package entities;

import audio.SoundManager;
import combat.DamageNumber;
import rendering.SpriteManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Non-hostile Training Dummy placed in the starting tutorial area.
 * Reacts to melee strikes, dashes, and projectiles with recoil animation, hit flash,
 * particles, and floating damage numbers without interfering with room clear or hurting the player.
 */
public final class TrainingDummy {
    private final SpriteManager sprites;
    private final double x;
    private final double y;
    private final int width = 16;
    private final int height = 18;

    private double hitTimer = 0;
    private boolean recoilRight = true;
    private int hitCount = 0;

    public TrainingDummy(SpriteManager sprites, double x, double y) {
        this.sprites = sprites;
        this.x = x;
        this.y = y;
    }

    public void update(double dt) {
        if (hitTimer > 0) {
            hitTimer = Math.max(0, hitTimer - dt);
        }
    }

    public boolean canTakeHit() {
        return hitTimer <= 0.08;
    }

    public void onHit(int damage, boolean fromLeft, List<Particle> particles, List<DamageNumber> damageNumbers) {
        hitTimer = 0.25;
        recoilRight = fromLeft;
        hitCount++;
        SoundManager.play(SoundManager.Sound.HIT);
        particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.SPARK));
        particles.add(Particle.createDust(sprites, getCenterX(), y + height));
        damageNumbers.add(new DamageNumber(getCenterX(), y - 6, String.valueOf(damage), Color.YELLOW, false));
    }

    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.sprite("training_dummy");
        int frameIdx = (hitTimer > 0.08 && frames.length > 1) ? 1 : 0;
        BufferedImage img = frames[frameIdx];

        if (hitTimer > 0.14) {
            // Brief white silhouette flash on impact
            BufferedImage flash = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int py = 0; py < img.getHeight(); py++) {
                for (int px = 0; px < img.getWidth(); px++) {
                    int argb = img.getRGB(px, py);
                    if ((argb >>> 24) > 50) flash.setRGB(px, py, 0xFFFFFFFF);
                }
            }
            img = flash;
        }

        // Base sits at sprite row 22 -> align bottom with height 18 via ry - 5
        int drawX = rx - 4;
        int drawY = ry - 5;
        if (!recoilRight && frameIdx == 1) {
            g.drawImage(img, drawX + 24, drawY, -24, 24, null);
        } else {
            g.drawImage(img, drawX, drawY, 24, 24, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHitCount() { return hitCount; }
}
