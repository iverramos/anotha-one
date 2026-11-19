package combat;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Floating bouncy damage/heal number with shadow and outline.
 */
public final class DamageNumber {
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 9);
    private static final Font CRIT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    private double x, y;
    private double vx, vy;
    private final String text;
    private final Color color;
    private final boolean crit;
    private double life;
    private final double maxLife;

    public DamageNumber(double x, double y, String text, Color color, boolean crit) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.crit = crit;
        this.vx = (Math.random() - 0.5) * 40;
        this.vy = -70 - Math.random() * 40;
        this.life = 0;
        this.maxLife = crit ? 0.9 : 0.7;
    }

    public boolean update(double dt) {
        life += dt;
        x += vx * dt;
        y += vy * dt;
        vy += 150 * dt; // gravity arc
        return life < maxLife;
    }

    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        g.setFont(crit ? CRIT_FONT : FONT);
        // Black outline for crisp readability
        g.setColor(Color.BLACK);
        g.drawString(text, rx - 1, ry);
        g.drawString(text, rx + 1, ry);
        g.drawString(text, rx, ry - 1);
        g.drawString(text, rx, ry + 1);

        g.setColor(color);
        g.drawString(text, rx, ry);
    }
}
