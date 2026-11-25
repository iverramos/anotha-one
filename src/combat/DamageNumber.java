package combat;

import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Floating bouncy damage/heal/fullness number rendered in crisp UI space (960x540).
 */
public final class DamageNumber {
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font CRIT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);

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
        vy += 150 * dt;
        return life < maxLife;
    }

    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round((x - camX) * Constants.UI_SCALE);
        int ry = (int) Math.round((y - camY) * Constants.UI_SCALE);

        g.setFont(crit ? CRIT_FONT : FONT);
        g.setColor(Color.BLACK);
        g.drawString(text, rx - 1, ry);
        g.drawString(text, rx + 1, ry);
        g.drawString(text, rx, ry - 1);
        g.drawString(text, rx, ry + 1);

        g.setColor(color);
        g.drawString(text, rx, ry);
    }
}
