package entities;

import rendering.SpriteManager;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Visual effects, slash animations, dash ghosts, and juicy impact particles.
 */
public final class Particle {
    public enum Type {
        SLASH, DASH_GHOST, BLOOD, SLIME, SPARK, DUST, SHOCKWAVE, FIRE
    }

    private double x, y;
    private double vx, vy;
    private final Type type;
    private double life;
    private final double maxLife;
    private final double angle;
    private final BufferedImage customImage;
    private final SpriteManager sprites;

    public Particle(SpriteManager sprites, double x, double y, double vx, double vy, double life, Type type, double angle, BufferedImage customImage) {
        this.sprites = sprites;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = 0;
        this.maxLife = life;
        this.type = type;
        this.angle = angle;
        this.customImage = customImage;
    }

    public static Particle createSlash(SpriteManager sm, double x, double y, double angle) {
        return new Particle(sm, x, y, 0, 0, 0.16, Type.SLASH, angle, null);
    }

    public static Particle createDashGhost(SpriteManager sm, double x, double y, BufferedImage heroFrame) {
        return new Particle(sm, x, y, 0, 0, 0.18, Type.DASH_GHOST, 0, heroFrame);
    }

    public static Particle createImpact(SpriteManager sm, double x, double y, Type type) {
        double vx = (Math.random() - 0.5) * 80;
        double vy = -40 - Math.random() * 50;
        return new Particle(sm, x, y, vx, vy, 0.35, type, 0, null);
    }

    public static Particle createDust(SpriteManager sm, double x, double y) {
        double vx = (Math.random() - 0.5) * 30;
        double vy = -15 - Math.random() * 20;
        return new Particle(sm, x, y, vx, vy, 0.30, Type.DUST, 0, null);
    }

    public static Particle createShockwave(SpriteManager sm, double x, double y, double vx) {
        return new Particle(sm, x, y, vx, 0, 0.40, Type.SHOCKWAVE, 0, null);
    }

    public boolean update(double dt) {
        life += dt;
        x += vx * dt;
        y += vy * dt;
        if (type == Type.BLOOD || type == Type.SLIME || type == Type.SPARK) {
            vy += 220 * dt; // gravity
        }
        return life < maxLife;
    }

    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);
        double progress = life / maxLife;

        if (type == Type.SLASH) {
            BufferedImage[] slashFrames = sprites.sprite("slash");
            int frameIdx = Math.min(slashFrames.length - 1, (int) (progress * slashFrames.length));
            BufferedImage img = slashFrames[frameIdx];

            AffineTransform old = g.getTransform();
            g.translate(rx, ry);
            g.rotate(angle);
            g.drawImage(img, -16, -16, null);
            g.setTransform(old);
            return;
        }

        if (type == Type.DASH_GHOST && customImage != null) {
            Composite oldComp = g.getComposite();
            float alpha = (float) Math.max(0, 1.0 - progress);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.6f));
            g.drawImage(customImage, rx, ry, null);
            g.setComposite(oldComp);
            return;
        }

        // Particle sprite index from particles sheet (8x8)
        BufferedImage[] partFrames = sprites.sprite("particles");
        int idx = 0;
        switch (type) {
            case DUST: idx = 0; break;
            case BLOOD: idx = 1; break;
            case SLIME: idx = 2; break;
            case SPARK: idx = 3; break;
            case FIRE: idx = 5; break;
            case SHOCKWAVE: idx = 7; break;
            default: idx = 0; break;
        }
        if (idx < partFrames.length) {
            g.drawImage(partFrames[idx], rx - 4, ry - 4, null);
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Type getType() { return type; }
}
