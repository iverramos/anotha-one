package entities;

import audio.SoundManager;
import rendering.SpriteManager;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Green bouncy slime. Squashes flat on the ground and leaps towards the player!
 */
public final class SlimeEnemy extends Enemy {
    private double jumpCooldown;
    private int animState = 0; // 0: Idle, 1: Squash, 2: Jump, 3: Land

    public SlimeEnemy(SpriteManager sprites, double x, double y) {
        super(sprites, x, y, 16, 12, 35, 12, 10);
        this.jumpCooldown = 0.5 + Math.random() * 0.8;
    }

    @Override
    public void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles) {
        jumpCooldown -= dt;

        if (onGround) {
            animState = (jumpCooldown < 0.2) ? 1 : 0; // squashing before leap
            if (jumpCooldown <= 0) {
                jumpCooldown = 1.2 + Math.random() * 0.6;
                animState = 2; // leap
                vy = -190.0;
                double dir = (player.getCenterX() > getCenterX()) ? 1.0 : -1.0;
                facingRight = dir > 0;
                vx = dir * 110.0;
                onGround = false;
                particles.add(Particle.createImpact(sprites, getCenterX(), y + height, Particle.Type.SLIME));
            }
        } else {
            animState = (vy < 0) ? 2 : 3;
            vy += 600 * dt; // gravity
        }
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.slice("enemies/slime.png", 20, 20);
        int f = Math.min(frames.length - 1, Math.max(0, animState));
        drawSpriteWithFlash(g, frames[f], rx - 2, ry - 7, 20, 20, !facingRight);
    }
}
