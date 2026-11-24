package entities;

import audio.SoundManager;
import combat.DamageNumber;
import rendering.SpriteManager;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Base enemy class providing AI hooks, contact damage, hit flashes, and loot drops.
 */
public abstract class Enemy extends Entity {
    protected final SpriteManager sprites;
    protected final int contactDamage;
    protected final int goldReward;
    protected boolean facingRight = true;
    protected double animTimer;

    public Enemy(SpriteManager sprites, double x, double y, int width, int height, int maxHealth, int contactDamage, int goldReward) {
        super(x, y, width, height, maxHealth);
        this.sprites = sprites;
        this.contactDamage = contactDamage;
        this.goldReward = goldReward;
    }

    public abstract void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles);

    @Override
    public void update(double dt) {
        if (hitFlashTimer > 0) hitFlashTimer -= dt;
        vx *= 0.88; // horizontal friction
    }

    public void checkContactPlayer(Player player, List<DamageNumber> damageNumbers) {
        if (dead || player.isDead() || player.isInvulnerable()) return;
        if (getBounds().intersects(player.getBounds())) {
            player.takeDamage(contactDamage);
            damageNumbers.add(new DamageNumber(player.getCenterX(), player.getY() - 4,
                    "-" + contactDamage, new Color(0xe62e3d), false));
        }
    }

    public List<Pickup> spawnDeathLoot() {
        List<Pickup> loot = new ArrayList<>();
        int coinCount = Math.max(1, goldReward / 5);
        for (int i = 0; i < coinCount; i++) {
            loot.add(Pickup.createCoin(sprites, x + width / 2.0 + (i - coinCount / 2.0) * 4, y, 5));
        }
        // 25% chance of health heart drop
        if (Math.random() < 0.25) {
            loot.add(Pickup.createHeart(sprites, x + width / 2.0, y - 4, 20));
        }
        return loot;
    }

    protected void drawSpriteWithFlash(Graphics2D g, BufferedImage img, int drawX, int drawY, int w, int h, boolean flipH) {
        if (hitFlashTimer > 0) {
            // White silhouette hit flash
            BufferedImage flashImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int argb = img.getRGB(x, y);
                    if ((argb >>> 24) > 50) {
                        flashImg.setRGB(x, y, 0xFFFFFFFF);
                    }
                }
            }
            img = flashImg;
        }

        if (flipH) {
            g.drawImage(img, drawX + w, drawY, -w, h, null);
        } else {
            g.drawImage(img, drawX, drawY, w, h, null);
        }
    }

    public double getHitFlashTimer() { return hitFlashTimer; }
    public int getContactDamage() { return contactDamage; }
}
