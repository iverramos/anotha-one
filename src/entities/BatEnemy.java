package entities;

import rendering.SpriteManager;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Cavern Bat swooping in sinusoidal waves towards the player.
 */
public final class BatEnemy extends Enemy {
    private double flightTimer;

    public BatEnemy(SpriteManager sprites, double x, double y) {
        super(sprites, x, y, 16, 12, 22, 8, 8);
        this.flightTimer = Math.random() * Math.PI * 2;
    }

    @Override
    public void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles) {
        flightTimer += dt * 8.0;
        animTimer += dt * 10.0;

        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();
        double dist = Math.hypot(dx, dy);

        facingRight = dx > 0;
        if (dist > 12) {
            double speed = 65.0;
            vx = (dx / dist) * speed;
            vy = (dy / dist) * speed + Math.sin(flightTimer) * 40.0;
        } else {
            vx *= 0.5;
            vy = Math.sin(flightTimer) * 30.0;
        }

        x += vx * dt;
        y += vy * dt;
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.sprite("bat");
        int f = ((int) animTimer) % frames.length;
        drawSpriteWithFlash(g, frames[f], rx - 4, ry - 4, 24, 20, !facingRight);
    }
}
