package ui;

import audio.SoundManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main Menu / Title screen for Gloamroot.
 * Features animated title, floating embers, options for Play, Settings, Credits, and Exit,
 * supporting both keyboard and mouse interaction.
 */
public final class TitleScreen {
    public enum Action {
        NONE, PLAY, SETTINGS, CREDITS, EXIT
    }

    private static final String[] OPTIONS = {
            "PLAY / START",
            "SETTINGS",
            "CREDITS",
            "EXIT"
    };

    private static final Font FONT_LOGO = new Font(Font.SANS_SERIF, Font.BOLD, 28);
    private static final Font FONT_SUB = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    private static final Font FONT_OPTION = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font FONT_TIP = new Font(Font.SANS_SERIF, Font.PLAIN, 8);

    private int selectedIndex = 0;
    private double animTime = 0;

    // Background floating embers
    private static class Ember {
        double x, y, vy, vx;
        double life, maxLife;
        Color color;
    }

    private final List<Ember> embers = new ArrayList<>();
    private final Random rng = new Random();

    public TitleScreen() {
        for (int i = 0; i < 30; i++) {
            spawnEmber(true);
        }
    }

    private void spawnEmber(boolean randomY) {
        Ember e = new Ember();
        e.x = rng.nextDouble() * Constants.VIEW_W;
        e.y = randomY ? rng.nextDouble() * Constants.VIEW_H : Constants.VIEW_H + 5;
        e.vy = -15 - rng.nextDouble() * 25;
        e.vx = (rng.nextDouble() - 0.5) * 15;
        e.life = 0;
        e.maxLife = 2.5 + rng.nextDouble() * 3.0;
        float r = 0.9f + rng.nextFloat() * 0.1f;
        float g = 0.5f + rng.nextFloat() * 0.3f;
        float b = 0.1f + rng.nextFloat() * 0.2f;
        e.color = new Color(r, g, b, 0.7f);
        embers.add(e);
    }

    public void update(double dt) {
        animTime += dt;
        for (int i = embers.size() - 1; i >= 0; i--) {
            Ember e = embers.get(i);
            e.life += dt;
            e.x += e.vx * dt;
            e.y += e.vy * dt;
            if (e.life >= e.maxLife || e.y < -10) {
                embers.remove(i);
                spawnEmber(false);
            }
        }
    }

    public Action handleInput(boolean upPressed, boolean downPressed, boolean selectPressed,
                              int mouseX, int mouseY, boolean mouseClicked) {
        if (upPressed) {
            selectedIndex = (selectedIndex - 1 + OPTIONS.length) % OPTIONS.length;
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else if (downPressed) {
            selectedIndex = (selectedIndex + 1) % OPTIONS.length;
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        }

        // Mouse hover detection
        int menuY = 135;
        int btnW = 160;
        int btnH = 22;
        int btnX = (Constants.VIEW_W - btnW) / 2;

        for (int i = 0; i < OPTIONS.length; i++) {
            int y = menuY + i * 26;
            Rectangle rect = new Rectangle(btnX, y, btnW, btnH);
            if (rect.contains(mouseX, mouseY)) {
                if (selectedIndex != i) {
                    selectedIndex = i;
                    SoundManager.play(SoundManager.Sound.UI_CLICK);
                }
                if (mouseClicked) {
                    return getActionForIndex(i);
                }
            }
        }

        if (selectPressed) {
            return getActionForIndex(selectedIndex);
        }

        return Action.NONE;
    }

    private Action getActionForIndex(int idx) {
        SoundManager.play(SoundManager.Sound.UI_CLICK);
        switch (idx) {
            case 0: return Action.PLAY;
            case 1: return Action.SETTINGS;
            case 2: return Action.CREDITS;
            case 3: return Action.EXIT;
            default: return Action.NONE;
        }
    }

    public void render(Graphics2D g) {
        // Dark dungeon atmosphere background
        g.setColor(new Color(0x0e1017));
        g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);

        // Draw animated embers
        for (Ember e : embers) {
            float alpha = (float) Math.max(0, Math.sin((e.life / e.maxLife) * Math.PI));
            g.setColor(new Color(e.color.getRed() / 255f, e.color.getGreen() / 255f, e.color.getBlue() / 255f, alpha * 0.75f));
            int size = (int) (2 + (e.life / e.maxLife) * 2);
            g.fillRect((int) e.x, (int) e.y, size, size);
        }

        // Title glow & text
        String title = Constants.TITLE.toUpperCase();
        g.setFont(FONT_LOGO);
        int tw = g.getFontMetrics().stringWidth(title);
        int tx = (Constants.VIEW_W - tw) / 2;
        int ty = 68;

        // Shadow & glow
        g.setColor(new Color(0x3a0d14));
        g.drawString(title, tx + 2, ty + 2);
        g.setColor(new Color(0x8f121e));
        g.drawString(title, tx + 1, ty + 1);

        // Pulsing main title color
        int goldOsc = (int) (20 * Math.sin(animTime * 3));
        g.setColor(new Color(0xfabc2a));
        g.drawString(title, tx, ty);

        // Subtitle banner
        String sub = "DUNGREED-INSPIRED ACTION ROGUELITE";
        g.setFont(FONT_SUB);
        int sw = g.getFontMetrics().stringWidth(sub);
        int sx = (Constants.VIEW_W - sw) / 2;
        g.setColor(new Color(0x38d7f2));
        g.drawString(sub, sx, ty + 18);

        // Menu buttons
        int menuY = 135;
        int btnW = 160;
        int btnH = 22;
        int btnX = (Constants.VIEW_W - btnW) / 2;

        g.setFont(FONT_OPTION);
        for (int i = 0; i < OPTIONS.length; i++) {
            int y = menuY + i * 26;
            boolean selected = (i == selectedIndex);

            // Button box
            if (selected) {
                g.setColor(new Color(0x282333));
                g.fillRect(btnX, y, btnW, btnH);
                g.setColor(new Color(0xfabc2a));
                g.drawRect(btnX, y, btnW, btnH);
                g.drawRect(btnX + 1, y + 1, btnW - 2, btnH - 2);

                // Animated selection arrows
                int arrowPulse = (int) (3 * Math.sin(animTime * 6));
                g.drawString(">", btnX + 8 + arrowPulse, y + 16);
                g.drawString("<", btnX + btnW - 16 - arrowPulse, y + 16);
            } else {
                g.setColor(new Color(0x191622));
                g.fillRect(btnX, y, btnW, btnH);
                g.setColor(new Color(0x434a63));
                g.drawRect(btnX, y, btnW, btnH);
            }

            // Option text
            String opt = OPTIONS[i];
            int ow = g.getFontMetrics().stringWidth(opt);
            int ox = (Constants.VIEW_W - ow) / 2;
            g.setColor(selected ? Color.WHITE : new Color(0x9faec7));
            g.drawString(opt, ox, y + 16);
        }

        // Footer navigation tip
        g.setFont(FONT_TIP);
        String tip = "[W/S or UP/DOWN] Navigate   |   [ENTER / CLICK] Select";
        int tipW = g.getFontMetrics().stringWidth(tip);
        g.setColor(new Color(0x656f8f));
        g.drawString(tip, (Constants.VIEW_W - tipW) / 2, Constants.VIEW_H - 12);
    }
}
