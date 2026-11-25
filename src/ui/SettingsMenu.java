package ui;

import audio.AudioSettings;
import audio.SoundManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Settings menu rendered at 960x540 providing independent SFX and BGM sliders and mute toggles,
 * with persistence across game runs via audio_settings.properties.
 */
public final class SettingsMenu {
    private static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    private static final Font FONT_LABEL  = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font FONT_VALUE  = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    private static final Font FONT_TIP    = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    private int selectedRow = 0; // 0: SFX Vol, 1: SFX Mute, 2: BGM Vol, 3: BGM Mute, 4: Back
    private boolean draggingSfxSlider = false;
    private boolean draggingBgmSlider = false;

    public void update() {}

    public boolean handleInput(boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed,
                               boolean enterPressed, boolean escPressed,
                               int mouseX, int mouseY, boolean mousePressed, boolean mouseReleased, boolean mouseClicked) {
        if (escPressed) {
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true;
        }

        if (upPressed) {
            selectedRow = (selectedRow - 1 + 5) % 5;
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else if (downPressed) {
            selectedRow = (selectedRow + 1) % 5;
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        }

        if (selectedRow == 0) {
            if (leftPressed) {
                AudioSettings.setSfxVolume(AudioSettings.getSfxVolume() - 0.05);
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            } else if (rightPressed) {
                AudioSettings.setSfxVolume(AudioSettings.getSfxVolume() + 0.05);
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            }
        } else if (selectedRow == 1 && enterPressed) {
            AudioSettings.toggleSfxMute();
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else if (selectedRow == 2) {
            if (leftPressed) {
                AudioSettings.setBgmVolume(AudioSettings.getBgmVolume() - 0.05);
            } else if (rightPressed) {
                AudioSettings.setBgmVolume(AudioSettings.getBgmVolume() + 0.05);
            }
        } else if (selectedRow == 3 && enterPressed) {
            AudioSettings.toggleBgmMute();
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else if (selectedRow == 4 && enterPressed) {
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return true;
        }

        int w = 640, h = 420;
        int x = (Constants.UI_W - w) / 2;
        int y = (Constants.UI_H - h) / 2;

        int sfxSliderX = x + 220, sfxSliderY = y + 114, sliderW = 240, sliderH = 22;
        int sfxMuteX   = x + 485, sfxMuteY   = y + 108, btnW = 110, btnH = 34;

        int bgmSliderX = x + 220, bgmSliderY = y + 210;
        int bgmMuteX   = x + 485, bgmMuteY   = y + 204;

        int backBtnX   = x + (w - 200) / 2, backBtnY = y + 318, backBtnW = 200, backBtnH = 44;

        Rectangle sfxSliderRect = new Rectangle(sfxSliderX, sfxSliderY - 8, sliderW, sliderH + 16);
        Rectangle sfxMuteRect   = new Rectangle(sfxMuteX, sfxMuteY, btnW, btnH);
        Rectangle bgmSliderRect = new Rectangle(bgmSliderX, bgmSliderY - 8, sliderW, sliderH + 16);
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
        g.setColor(new Color(10, 8, 16, 220));
        g.fillRect(0, 0, Constants.UI_W, Constants.UI_H);

        int w = 640, h = 420;
        int x = (Constants.UI_W - w) / 2;
        int y = (Constants.UI_H - h) / 2;

        g.setColor(new Color(0x282333));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(0xfabc2a));
        g.drawRect(x, y, w, h);
        g.drawRect(x + 3, y + 3, w - 6, h - 6);

        g.setFont(FONT_HEADER);
        String title = "AUDIO SETTINGS";
        int tw = g.getFontMetrics().stringWidth(title);
        g.setColor(Color.BLACK);
        g.drawString(title, x + (w - tw) / 2 + 2, y + 48);
        g.setColor(new Color(0xfabc2a));
        g.drawString(title, x + (w - tw) / 2, y + 46);

        g.setColor(new Color(0x434a63));
        g.drawLine(x + 32, y + 66, x + w - 32, y + 66);

        renderAudioSection(g, "SFX Volume:", AudioSettings.getSfxVolume(), AudioSettings.isSfxMuted(),
                x + 36, y + 114, x + 220, y + 114, x + 485, y + 108, selectedRow == 0, selectedRow == 1);

        renderAudioSection(g, "BGM Volume:", AudioSettings.getBgmVolume(), AudioSettings.isBgmMuted(),
                x + 36, y + 210, x + 220, y + 210, x + 485, y + 204, selectedRow == 2, selectedRow == 3);

        int backBtnX = x + (w - 200) / 2, backBtnY = y + 318, backBtnW = 200, backBtnH = 44;
        boolean backSelected = (selectedRow == 4);
        g.setColor(backSelected ? new Color(0x383347) : new Color(0x191622));
        g.fillRect(backBtnX, backBtnY, backBtnW, backBtnH);
        g.setColor(backSelected ? new Color(0xfabc2a) : new Color(0x65607a));
        g.drawRect(backBtnX, backBtnY, backBtnW, backBtnH);

        g.setFont(FONT_LABEL);
        String backText = "BACK [ESC]";
        int btw = g.getFontMetrics().stringWidth(backText);
        g.setColor(backSelected ? Color.WHITE : new Color(0xa8b6cc));
        g.drawString(backText, backBtnX + (backBtnW - btw) / 2, backBtnY + 29);

        g.setFont(FONT_TIP);
        String tip = "Use Arrow Keys or Click/Drag to Adjust   |   Settings Saved Automatically";
        int tipW = g.getFontMetrics().stringWidth(tip);
        g.setColor(new Color(0x8894b8));
        g.drawString(tip, x + (w - tipW) / 2, y + h - 18);
    }

    private void renderAudioSection(Graphics2D g, String label, double volume, boolean muted,
                                    int lx, int ly, int sx, int sy, int mx, int my,
                                    boolean sliderSelected, boolean muteSelected) {
        g.setFont(FONT_LABEL);
        g.setColor(sliderSelected ? new Color(0xfabc2a) : Color.WHITE);
        g.drawString(label, lx, ly + 17);

        int trackW = 240, trackH = 20;
        g.setColor(new Color(0x1b1d28));
        g.fillRect(sx, sy, trackW, trackH);
        g.setColor(sliderSelected ? new Color(0xfabc2a) : new Color(0x434a63));
        g.drawRect(sx, sy, trackW, trackH);

        int fillW = (int) Math.round(trackW * volume);
        Color fillColor = muted ? new Color(0x69665c) : (sliderSelected ? new Color(0x38d7f2) : new Color(0x20529c));
        g.setColor(fillColor);
        g.fillRect(sx + 2, sy + 2, Math.max(0, fillW - 4), trackH - 4);

        int thumbX = sx + fillW - 6;
        g.setColor(muted ? Color.GRAY : Color.WHITE);
        g.fillRect(thumbX, sy - 4, 12, trackH + 8);
        g.setColor(Color.BLACK);
        g.drawRect(thumbX, sy - 4, 12, trackH + 8);

        g.setFont(FONT_VALUE);
        String pct = ((int) Math.round(volume * 100)) + "%";
        g.setColor(muted ? Color.GRAY : Color.WHITE);
        g.drawString(pct, sx + trackW - 48, sy - 8);

        int btnW = 110, btnH = 34;
        g.setColor(muted ? new Color(0x452416) : (muteSelected ? new Color(0x2a382c) : new Color(0x1e2420)));
        g.fillRect(mx, my, btnW, btnH);
        g.setColor(muteSelected ? new Color(0xfabc2a) : (muted ? new Color(0xe62e3d) : new Color(0x35a342)));
        g.drawRect(mx, my, btnW, btnH);

        g.setFont(FONT_VALUE);
        String muteText = muted ? "MUTED" : "ACTIVE";
        int mtw = g.getFontMetrics().stringWidth(muteText);
        g.setColor(muted ? new Color(0xf57f87) : new Color(0x85f59c));
        g.drawString(muteText, mx + (btnW - mtw) / 2, my + 23);
    }
}
