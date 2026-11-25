package ui;

import audio.SoundManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Clean Credits screen rendered at 960x540 displaying team info and game attributions.
 */
public final class CreditsScreen {
    private static final Font FONT_TITLE  = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    private static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONT_BODY   = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    private static final Font FONT_BTN    = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    public void update(double dt) {}

    public boolean handleInput(boolean selectPressed, boolean escPressed,
                               int mouseX, int mouseY, boolean mouseClicked) {
        if (escPressed || selectPressed) {
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true;
        }

        int btnW = 220, btnH = 42;
        int btnX = (Constants.UI_W - btnW) / 2;
        int btnY = Constants.UI_H - 74;
        Rectangle btn = new Rectangle(btnX, btnY, btnW, btnH);

        if (mouseClicked && btn.contains(mouseX, mouseY)) {
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true;
        }

        return false;
    }

    public void render(Graphics2D g, int mouseX, int mouseY) {
        g.setColor(new Color(0x0e1017));
        g.fillRect(0, 0, Constants.UI_W, Constants.UI_H);

        int cardW = 680, cardH = 425;
        int cardX = (Constants.UI_W - cardW) / 2;
        int cardY = (Constants.UI_H - cardH) / 2 - 12;

        g.setColor(new Color(0x191622));
        g.fillRect(cardX, cardY, cardW, cardH);
        g.setColor(new Color(0xfabc2a));
        g.drawRect(cardX, cardY, cardW, cardH);
        g.drawRect(cardX + 3, cardY + 3, cardW - 6, cardH - 6);

        g.setFont(FONT_TITLE);
        String title = "CREDITS & ATTRIBUTIONS";
        int tw = g.getFontMetrics().stringWidth(title);
        g.setColor(new Color(0xfabc2a));
        g.drawString(title, cardX + (cardW - tw) / 2, cardY + 42);

        g.setColor(new Color(0x434a63));
        g.drawLine(cardX + 36, cardY + 58, cardX + cardW - 36, cardY + 58);

        String[][] credits = {
                {"GAME DESIGN & ARCHITECTURE", Constants.TITLE + " Dev Team - Dungreed-Inspired Action Roguelite"},
                {"COMBAT & PHYSICS SYSTEMS", "360 Aiming, Double Jump, I-Frames, Dash & Drop-Downs"},
                {"AUDIO OVERHAUL & SYNTHESIS", "Bfxr-Style Retro Procedural Chiptune Synthesizer"},
                {"TIER 2 DUNGEON & BOSS ENCOUNTERS", "Sunken Catacombs, Malakor Void Archon & Weapon Armory"},
                {"SPECIAL THANKS & INSPIRATION", "Team Horay (Dungreed) & Classic 2D Action Platformers"}
        };

        int entryY = cardY + 90;
        for (String[] entry : credits) {
            g.setFont(FONT_HEADER);
            g.setColor(new Color(0x38d7f2));
            g.drawString(entry[0], cardX + 36, entryY);

            g.setFont(FONT_BODY);
            g.setColor(new Color(0xdce4f0));
            g.drawString(entry[1], cardX + 36, entryY + 20);

            entryY += 54;
        }

        int btnW = 220, btnH = 42;
        int btnX = (Constants.UI_W - btnW) / 2;
        int btnY = Constants.UI_H - 74;
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
        g.drawString(btnText, btnX + (btnW - btw) / 2, btnY + 27);
    }
}
