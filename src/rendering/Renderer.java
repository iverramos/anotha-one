package rendering;

import utilities.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Owns the low-resolution back buffer (480x270).
 * Handles double-buffering, integer nearest-neighbor scaling to window,
 * coordinate translation from screen to game space, and screen-shake juice.
 */
public final class Renderer {
    private final BufferedImage[] images = new BufferedImage[2];
    private final Graphics2D[] graphics = new Graphics2D[2];
    private final Object lock = new Object();
    private int front = 0;

    // Screen shake
    private double shakeTimer;
    private double shakeMagnitude;
    private int shakeOffsetX;
    private int shakeOffsetY;
    private final java.util.Random rng = new java.util.Random();

    public Renderer() {
        for (int i = 0; i < 2; i++) {
            images[i] = new BufferedImage(Constants.VIEW_W, Constants.VIEW_H, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = images[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics[i] = g;
        }
    }

    public void addShake(double duration, double magnitude) {
        this.shakeTimer = Math.max(this.shakeTimer, duration);
        this.shakeMagnitude = Math.max(this.shakeMagnitude, magnitude);
    }

    public void updateShake(double dt) {
        if (shakeTimer > 0) {
            shakeTimer -= dt;
            int mag = (int) Math.round(shakeMagnitude * (shakeTimer / 0.3));
            if (mag > 0) {
                shakeOffsetX = rng.nextInt(mag * 2 + 1) - mag;
                shakeOffsetY = rng.nextInt(mag * 2 + 1) - mag;
            } else {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
            shakeMagnitude = 0;
        }
    }

    public int getShakeOffsetX() { return shakeOffsetX; }
    public int getShakeOffsetY() { return shakeOffsetY; }

    /** Graphics context for the back buffer. */
    public Graphics2D begin() { return graphics[1 - front]; }

    /** Publish the back buffer. */
    public void end() {
        synchronized (lock) { front = 1 - front; }
    }

    /** EDT: draw the front buffer scaled and letterboxed into the window. */
    public void paint(Graphics2D target, int w, int h) {
        target.setColor(Color.BLACK);
        target.fillRect(0, 0, w, h);

        double s = Math.min((double) w / Constants.VIEW_W, (double) h / Constants.VIEW_H);
        if (s >= 1) s = Math.floor(s);
        if (s <= 0) s = 1;

        int dw = (int) (Constants.VIEW_W * s);
        int dh = (int) (Constants.VIEW_H * s);
        int dx = (w - dw) / 2;
        int dy = (h - dh) / 2;

        target.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        synchronized (lock) {
            target.drawImage(images[front], dx, dy, dw, dh, null);
        }
    }

    /** Convert screen window pixel coordinates into game's internal 480x270 coordinates. */
    public static Point screenToGame(int sx, int sy, int w, int h) {
        double s = Math.min((double) w / Constants.VIEW_W, (double) h / Constants.VIEW_H);
        if (s >= 1) s = Math.floor(s);
        if (s <= 0) s = 1;

        int dw = (int) (Constants.VIEW_W * s);
        int dh = (int) (Constants.VIEW_H * s);
        int dx = (w - dw) / 2;
        int dy = (h - dh) / 2;

        int gx = (int) ((sx - dx) / s);
        int gy = (int) ((sy - dy) / s);
        return new Point(Math.max(0, Math.min(Constants.VIEW_W - 1, gx)),
                         Math.max(0, Math.min(Constants.VIEW_H - 1, gy)));
    }

    /** Copy of the most recently published frame. */
    public BufferedImage snapshot() {
        BufferedImage copy = new BufferedImage(Constants.VIEW_W, Constants.VIEW_H, BufferedImage.TYPE_INT_RGB);
        synchronized (lock) {
            Graphics2D g = copy.createGraphics();
            g.drawImage(images[front], 0, 0, null);
            g.dispose();
        }
        return copy;
    }
}
