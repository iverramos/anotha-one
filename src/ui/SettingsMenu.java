package ui;

import audio.AudioSettings;
import audio.SoundManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Settings menu providing independent SFX and BGM sliders and mute toggles,
 * with persistence across game runs via audio_settings.properties.
 */
public final class SettingsMenu {
    private static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font FONT_LABEL  = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    private static final Font FONT_VALUE  = new Font(Font.SANS_SERIF, Font.BOLD, 9);
    private static final Font FONT_TIP    = new Font(Font.SANS_SERIF, Font.PLAIN, 8);

    private int selectedRow = 0; // 0: SFX Vol, 1: SFX Mute, 2: BGM Vol, 3: BGM Mute, 4: Back
    private boolean draggingSfxSlider = false;
    private boolean draggingBgmSlider = false;

    public void update() {}

    public boolean handleInput(boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed,
                               boolean enterPressed, boolean escPressed,
                               int mouseX, int mouseY, boolean mousePressed, boolean mouseReleased, boolean mouseClicked) {
        if (escPressed) {
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true; // Request close
        }

        if (upPressed) {
            selectedRow = (selectedRow - 1 + 5) % 5;
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else if (downPressed) {
            selectedRow = (selectedRow + 1) % 5;
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        }

        // Slider adjust via keyboard
        if (selectedRow == 0) { // SFX Vol
            if (leftPressed) {
                AudioSettings.setSfxVolume(AudioSettings.getSfxVolume() - 0.05);
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            } else if (rightPressed) {
                AudioSettings.setSfxVolume(AudioSettings.getSfxVolume() + 0.05);
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            }
        } else if (selectedRow == 1 && enterPressed) { // SFX Mute
            AudioSettings.toggleSfxMute();
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else if (selectedRow == 2) { // BGM Vol
            if (leftPressed) {
                AudioSettings.setBgmVolume(AudioSettings.getBgmVolume() - 0.05);
            } else if (rightPressed) {
                AudioSettings.setBgmVolume(AudioSettings.getBgmVolume() + 0.05);
            }
        } else if (selectedRow == 3 && enterPressed) { // BGM Mute
            AudioSettings.toggleBgmMute();
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else if (selectedRow == 4 && enterPressed) { // Back
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true;
        }

        // Layout bounds for mouse interaction
        int w = 320, h = 210;
        int x = (Constants.VIEW_W - w) / 2;
        int y = (Constants.VIEW_H - h) / 2;

        int sfxSliderX = x + 110, sfxSliderY = y + 54, sliderW = 120, sliderH = 14;
        int sfxMuteX   = x + 240, sfxMuteY   = y + 52, btnW = 54, btnH = 18;

        int bgmSliderX = x + 110, bgmSliderY = y + 104;
        int bgmMuteX   = x + 240, bgmMuteY   = y + 102;

        int backBtnX   = x + (w - 100) / 2, backBtnY = y + 160, backBtnW = 100, backBtnH = 22;

        Rectangle sfxSliderRect = new Rectangle(sfxSliderX, sfxSliderY - 4, sliderW, sliderH + 8);
        Rectangle sfxMuteRect   = new Rectangle(sfxMuteX, sfxMuteY, btnW, btnH);
        Rectangle bgmSliderRect = new Rectangle(bgmSliderX, bgmSliderY - 4, sliderW, sliderH + 8);
        Rectangle bgmMuteRect   = new Rectangle(bgmMuteX, bgmMuteY, btnW, btnH);
        Rectangle backBtnRect   = new Rectangle(backBtnX, backBtnY, backBtnW, backBtnH);

        if (mousePressed) {
            if (sfxSliderRect.contains(mouseX, mouseY)) {
                draggingSfxSlider = true;
                selectedRow = 0;
            } else if (bgmSliderRect.contains(mouseX, mouseY)) {
                draggingBgmSlider = true;
                selectedRow = 2;
            }
        }

        if (mouseReleased) {
            if (draggingSfxSlider) SoundManager.play(SoundManager.Sound.UI_CLICK);
            draggingSfxSlider = false;
            draggingBgmSlider = false;
        }

        if (draggingSfxSlider) {
            double frac = (double) (mouseX - sfxSliderX) / sliderW;
            AudioSettings.setSfxVolume(frac);
        }

        if (draggingBgmSlider) {
            double frac = (double) (mouseX - bgmSliderX) / sliderW;
            AudioSettings.setBgmVolume(frac);
        }

        if (mouseClicked) {
            if (sfxMuteRect.contains(mouseX, mouseY)) {
                AudioSettings.toggleSfxMute();
                selectedRow = 1;
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            } else if (bgmMuteRect.contains(mouseX, mouseY)) {
                AudioSettings.toggleBgmMute();
                selectedRow = 3;
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            } else if (backBtnRect.contains(mouseX, mouseY)) {
                SoundManager.play(SoundManager.Sound.UI_CLICK);
                return true;
            }
        }

        return false;
    }

    public void render(Graphics2D g) {
        // Dimmed backdrop
        g.setColor(new Color(10, 8, 16, 215));
        g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);

        int w = 320, h = 210;
        int x = (Constants.VIEW_W - w) / 2;
        int y = (Constants.VIEW_H - h) / 2;

        // Card frame
        g.setColor(new Color(0x282333));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(0xfabc2a));
        g.drawRect(x, y, w, h);
        g.drawRect(x + 2, y + 2, w - 4, h - 4);

        // Header
        g.setFont(FONT_HEADER);
        String title = "AUDIO SETTINGS";
        int tw = g.getFontMetrics().stringWidth(title);
        g.setColor(Color.BLACK);
        g.drawString(title, x + (w - tw) / 2 + 1, y + 25);
        g.setColor(new Color(0xfabc2a));
        g.drawString(title, x + (w - tw) / 2, y + 24);

        // Divider
        g.setColor(new Color(0x434a63));
        g.drawLine(x + 16, y + 34, x + w - 16, y + 34);

        // 1. Sound Effects (SFX)
        renderAudioSection(g, "SFX Volume:", AudioSettings.getSfxVolume(), AudioSettings.isSfxMuted(),
                x + 16, y + 54, x + 110, y + 54, x + 240, y + 52, selectedRow == 0, selectedRow == 1);

        // 2. Background Music (BGM)
        renderAudioSection(g, "BGM Volume:", AudioSettings.getBgmVolume(), AudioSettings.isBgmMuted(),
                x + 16, y + 104, x + 110, y + 104, x + 240, y + 102, selectedRow == 2, selectedRow == 3);

        // Back Button
        int backBtnX = x + (w - 100) / 2, backBtnY = y + 160, backBtnW = 100, backBtnH = 22;
        boolean backSelected = (selectedRow == 4);
        g.setColor(backSelected ? new Color(0x383347) : new Color(0x191622));
        g.fillRect(backBtnX, backBtnY, backBtnW, backBtnH);
        g.setColor(backSelected ? new Color(0xfabc2a) : new Color(0x65607a));
        g.drawRect(backBtnX, backBtnY, backBtnW, backBtnH);

        g.setFont(FONT_LABEL);
        String backText = "BACK [ESC]";
        int btw = g.getFontMetrics().stringWidth(backText);
        g.setColor(backSelected ? Color.WHITE : new Color(0xa8b6cc));
        g.drawString(backText, backBtnX + (backBtnW - btw) / 2, backBtnY + 15);

        // Footer instruction
        g.setFont(FONT_TIP);
        String tip = "Use Arrow Keys or Click/Drag to Adjust   |   Settings Saved Automatically";
        int tipW = g.getFontMetrics().stringWidth(tip);
        g.setColor(new Color(0x656f8f));
        g.drawString(tip, x + (w - tipW) / 2, y + h - 8);
    }

    private void renderAudioSection(Graphics2D g, String label, double volume, boolean muted,
                                    int lx, int ly, int sx, int sy, int mx, int my,
                                    boolean sliderSelected, boolean muteSelected) {
        // Label
        g.setFont(FONT_LABEL);
        g.setColor(sliderSelected ? new Color(0xfabc2a) : Color.WHITE);
        g.drawString(label, lx, ly + 10);

        // Slider track
        int trackW = 120, trackH = 10;
        g.setColor(new Color(0x1b1d28));
        g.fillRect(sx, sy, trackW, trackH);
        g.setColor(sliderSelected ? new Color(0xfabc2a) : new Color(0x434a63));
        g.drawRect(sx, sy, trackW, trackH);

        // Fill bar
        int fillW = (int) Math.round(trackW * volume);
        Color fillColor = muted ? new Color(0x69665c) : (sliderSelected ? new Color(0x38d7f2) : new Color(0x20529c));
        g.setColor(fillColor);
        g.fillRect(sx + 1, sy + 1, Math.max(0, fillW - 2), trackH - 2);

        // Slider thumb handle
        int thumbX = sx + fillW - 3;
        g.setColor(muted ? Color.GRAY : Color.WHITE);
        g.fillRect(thumbX, sy - 2, 6, trackH + 4);
        g.setColor(Color.BLACK);
        g.drawRect(thumbX, sy - 2, 6, trackH + 4);

        // Percentage text
        g.setFont(FONT_VALUE);
        String pct = ((int) Math.round(volume * 100)) + "%";
        g.setColor(muted ? Color.GRAY : Color.WHITE);
        g.drawString(pct, sx + trackW - 28, sy - 3);

        // Mute button
        int btnW = 54, btnH = 18;
        g.setColor(muted ? new Color(0x452416) : (muteSelected ? new Color(0x2a382c) : new Color(0x1e2420)));
        g.fillRect(mx, my, btnW, btnH);
        g.setColor(muteSelected ? new Color(0xfabc2a) : (muted ? new Color(0xe62e3d) : new Color(0x35a342)));
        g.drawRect(mx, my, btnW, btnH);

        g.setFont(FONT_VALUE);
        String muteText = muted ? "MUTED" : "ACTIVE";
        int mtw = g.getFontMetrics().stringWidth(muteText);
        g.setColor(muted ? new Color(0xf57f87) : new Color(0x85f59c));
        g.drawString(muteText, mx + (btnW - mtw) / 2, my + 13);
    }
}
