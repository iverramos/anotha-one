package entities;

import audio.SoundManager;
import rendering.SpriteManager;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Skeleton Cultist Mage hovering on high ledges and conjuring tracking dark magic orbs.
 */
public final class MageEnemy extends Enemy {
    private double castCooldown;
    private double hoverTimer;

    public MageEnemy(SpriteManager sprites, double x, double y) {
        super(sprites, x, y, 14, 20, 42, 12, 20);
        this.castCooldown = 1.5 + Math.random();
    }

    @Override
    public void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles) {
        hoverTimer += dt * 4.0;
        animTimer += dt * 6.0;
        castCooldown -= dt;

        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();
        double dist = Math.hypot(dx, dy);
        facingRight = dx > 0;

        // Teleport retreat if player gets too close
        if (dist < 36) {
            particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
            x += (facingRight ? -70 : 70);
            particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
            castCooldown = Math.max(castCooldown, 0.8);
        }

        // Cast Dark Magic Orb
        if (castCooldown <= 0 && dist < 220) {
            castCooldown = 2.4;
            SoundManager.play(SoundManager.Sound.MAGIC_CAST);
            double angle = Math.atan2(dy, dx);
            projectiles.add(new Projectile(sprites, getCenterX(), getCenterY(), angle, 130.0,
                    Projectile.Type.DARK_ORB, Projectile.Owner.ENEMY, 15));
            particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
        }

        // Slight hover bob
        vy = Math.sin(hoverTimer) * 15.0;
        y += vy * dt;
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.sprite("mage");
        int f = ((int) animTimer) % frames.length;
        drawSpriteWithFlash(g, frames[f], rx - 5, ry - 4, 24, 24, !facingRight);
    }
}
