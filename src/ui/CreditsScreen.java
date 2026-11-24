package ui;

import audio.SoundManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Clean Credits screen displaying team info and game attributions.
 */
public final class CreditsScreen {
    private static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    private static final Font FONT_BODY   = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    private static final Font FONT_BTN    = new Font(Font.SANS_SERIF, Font.BOLD, 10);

    private double animTime = 0;

    public void update(double dt) {
        animTime += dt;
    }

    public boolean handleInput(boolean selectPressed, boolean escPressed,
                               int mouseX, int mouseY, boolean mouseClicked) {
        if (escPressed || selectPressed) {
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true;
        }

        // Return button rect
        int btnW = 120, btnH = 22;
        int btnX = (Constants.VIEW_W - btnW) / 2;
        int btnY = Constants.VIEW_H - 40;
        Rectangle btn = new Rectangle(btnX, btnY, btnW, btnH);

        if (mouseClicked && btn.contains(mouseX, mouseY)) {
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true;
        }

        return false;
    }

    public void render(Graphics2D g, int mouseX, int mouseY) {
        // Dark background
        g.setColor(new Color(0x0e1017));
        g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);

        int cardW = 340, cardH = 220;
        int cardX = (Constants.VIEW_W - cardW) / 2;
        int cardY = (Constants.VIEW_H - cardH) / 2 - 6;

        // Card frame
        g.setColor(new Color(0x191622));
        g.fillRect(cardX, cardY, cardW, cardH);
        g.setColor(new Color(0xfabc2a));
        g.drawRect(cardX, cardY, cardW, cardH);
        g.drawRect(cardX + 2, cardY + 2, cardW - 4, cardH - 4);

        // Header Title
        g.setFont(FONT_TITLE);
        String title = "CREDITS & ATTRIBUTIONS";
        int tw = g.getFontMetrics().stringWidth(title);
        g.setColor(new Color(0xfabc2a));
        g.drawString(title, cardX + (cardW - tw) / 2, cardY + 24);

        // Divider
        g.setColor(new Color(0x434a63));
        g.drawLine(cardX + 20, cardY + 32, cardX + cardW - 20, cardY + 32);

        // Credits entries
        String[][] credits = {
                {"GAME DESIGN & ARCHITECTURE", "Gloamroot Dev Team - Dungreed-Inspired Action Roguelite"},
                {"COMBAT & PHYSICS SYSTEMS", "360 Aiming, Double Jump, I-Frames, Dash & Drop-Downs"},
                {"AUDIO OVERHAUL & SYNTHESIS", "Bfxr Retro Web Audio Chiptune Synthesizer (Zero Assets)"},
                {"TIER 2 DUNGEON & BOSS ENCOUNTERS", "Sunken Catacombs, Malakor Void Archon & Weapon Armory"},
                {"SPECIAL THANKS & INSPIRATION", "Team Horay (Dungreed) & Classic 2D Action Platformers"}
        };

        int entryY = cardY + 48;
        for (String[] entry : credits) {
            g.setFont(FONT_HEADER);
            g.setColor(new Color(0x38d7f2));
            g.drawString(entry[0], cardX + 20, entryY);

            g.setFont(FONT_BODY);
            g.setColor(new Color(0xdce4f0));
            g.drawString(entry[1], cardX + 20, entryY + 12);

            entryY += 28;
        }

        // Return button
        int btnW = 120, btnH = 22;
        int btnX = (Constants.VIEW_W - btnW) / 2;
        int btnY = Constants.VIEW_H - 40;
        Rectangle btn = new Rectangle(btnX, btnY, btnW, btnH);
        boolean hover = btn.contains(mouseX, mouseY);

        g.setColor(hover ? new Color(0x383347) : new Color(0x282333));
        g.fillRect(btnX, btnY, btnW, btnH);
        g.setColor(hover ? new Color(0xfabc2a) : new Color(0x65607a));
        g.drawRect(btnX, btnY, btnW, btnH);

        g.setFont(FONT_BTN);
        String btnText = "BACK TO TITLE";
        int btw = g.getFontMetrics().stringWidth(btnText);
        g.setColor(hover ? Color.WHITE : new Color(0xa8b6cc));
        g.drawString(btnText, btnX + (btnW - btw) / 2, btnY + 15);
    }
}
