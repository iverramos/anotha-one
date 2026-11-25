package entities;

import audio.SoundManager;
import rendering.SpriteManager;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Skeleton Warrior patrolling platforms and charging with an iron sword!
 */
public final class SkeletonEnemy extends Enemy {
    private double patrolTimer;
    private int patrolDir = 1;
    private double attackWindup;

    public SkeletonEnemy(SpriteManager sprites, double x, double y) {
        super(sprites, x, y, 14, 20, 50, 16, 15);
        this.patrolTimer = 2.0;
    }

    @Override
    public void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles) {
        animTimer += dt * 6.0;

        // Gravity
        if (!onGround) {
            vy += 650 * dt;
        }

        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();
        double dist = Math.hypot(dx, dy);

        if (attackWindup > 0) {
            attackWindup -= dt;
            vx *= 0.5;
            if (attackWindup <= 0) {
                // Slash attack
                SoundManager.play(SoundManager.Sound.SLASH);
                particles.add(Particle.createSlash(sprites, getCenterX() + (facingRight ? 16 : -16),
                        getCenterY(), facingRight ? 0 : Math.PI));
            }
            return;
        }

        // Agro if within 130px
        if (dist < 130 && Math.abs(dy) < 48) {
            facingRight = dx > 0;
            if (dist < 26) {
                // Trigger windup
                attackWindup = 0.35;
            } else {
                vx = (facingRight ? 1 : -1) * 75.0;
            }
        } else {
            // Patrol
            patrolTimer -= dt;
            if (patrolTimer <= 0) {
                patrolDir = -patrolDir;
                patrolTimer = 1.8 + Math.random() * 1.5;
            }
            facingRight = patrolDir > 0;
            vx = patrolDir * 40.0;
        }
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.sprite("skeleton");
        int f = ((int) animTimer) % frames.length;
        drawSpriteWithFlash(g, frames[f], rx - 5, ry - 4, 24, 24, !facingRight);
    }
}
