package ui;

import audio.SoundManager;
import combat.Weapon;
import entities.Player;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Interactive Weapon Shop & Armory overlay.
 * Players can browse, purchase, and equip weapons with full stat comparisons,
 * clear pricing, and locked / owned / equipped visual badges.
 */
public final class WeaponShop {
    private static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font FONT_TITLE  = new Font(Font.SANS_SERIF, Font.BOLD, 9);
    private static final Font FONT_DESC   = new Font(Font.SANS_SERIF, Font.PLAIN, 7);
    private static final Font FONT_BADGE  = new Font(Font.SANS_SERIF, Font.BOLD, 8);
    private static final Font FONT_INFO   = new Font(Font.SANS_SERIF, Font.BOLD, 9);

    private boolean open;
    private final SpriteManager sprites;
    private final List<Weapon> catalog;
    private int selectedIndex = 0;

    public WeaponShop(SpriteManager sprites) {
        this.sprites = sprites;
        this.catalog = Weapon.getAllWeapons();
    }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public void toggle() { this.open = !this.open; }

    public void handleInput(boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed,
                            boolean enterPressed, boolean escPressed,
                            int mouseX, int mouseY, boolean mouseClicked, Player player) {
        if (!open) return;

        if (escPressed) {
            setOpen(false);
            SoundManager.play(SoundManager.Sound.UI_CLICK);
            return;
        }

        // Keyboard grid navigation (2 columns)
        if (leftPressed || rightPressed) {
            selectedIndex = (selectedIndex % 2 == 0) ? Math.min(catalog.size() - 1, selectedIndex + 1) : Math.max(0, selectedIndex - 1);
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        }
        if (upPressed) {
            selectedIndex = (selectedIndex - 2 + catalog.size()) % catalog.size();
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        }
        if (downPressed) {
            selectedIndex = (selectedIndex + 2) % catalog.size();
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        }

        if (enterPressed && selectedIndex >= 0 && selectedIndex < catalog.size()) {
            interactWithWeapon(catalog.get(selectedIndex), player);
        }

        // Mouse click detection on 2-column cards
        int menuW = 440, menuH = 240;
        int menuX = (Constants.VIEW_W - menuW) / 2;
        int menuY = (Constants.VIEW_H - menuH) / 2;
        int cardW = 206, cardH = 34;

        for (int i = 0; i < catalog.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = menuX + 10 + col * (cardW + 8);
            int cy = menuY + 36 + row * (cardH + 5);

            Rectangle cardRect = new Rectangle(cx, cy, cardW, cardH);
            if (cardRect.contains(mouseX, mouseY)) {
                if (selectedIndex != i) {
                    selectedIndex = i;
                }
                if (mouseClicked) {
                    interactWithWeapon(catalog.get(i), player);
                    return;
                }
            }
        }
    }

    private void interactWithWeapon(Weapon w, Player player) {
        Weapon active = player.getActiveWeapon();
        boolean isEquipped = (active != null && active.getId() == w.getId());
        boolean isOwned = player.ownsWeapon(w.getId());

        if (isEquipped) {
            // Already equipped
            return;
        }

        if (isOwned) {
            // Equip owned weapon
            player.equipWeapon(w);
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        } else {
            // Buy locked weapon
            if (player.getGold() >= w.getPrice()) {
                player.spendGold(w.getPrice());
                player.unlockWeapon(w.getId());
                player.equipWeapon(w);
                SoundManager.play(SoundManager.Sound.PURCHASE);
            }
        }
    }

    public void render(Graphics2D g, Player player) {
        if (!open) return;

        // Semi-transparent backdrop
        g.setColor(new Color(12, 10, 18, 220));
        g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);

        int menuW = 440, menuH = 240;
        int menuX = (Constants.VIEW_W - menuW) / 2;
        int menuY = (Constants.VIEW_H - menuH) / 2;

        // Main Board
        g.setColor(new Color(0x282333));
        g.fillRect(menuX, menuY, menuW, menuH);
        g.setColor(new Color(0xfabc2a));
        g.drawRect(menuX, menuY, menuW, menuH);
        g.drawRect(menuX + 2, menuY + 2, menuW - 4, menuH - 4);

        // Header Title
        g.setFont(FONT_HEADER);
        g.setColor(new Color(0xfabc2a));
        g.drawString("ROYAL ARMORY & WEAPON SHOP", menuX + 16, menuY + 20);

        // Player Gold Counter
        g.setFont(FONT_INFO);
        g.setColor(Color.WHITE);
        String goldStr = "Gold: " + player.getGold() + " G";
        int gw = g.getFontMetrics().stringWidth(goldStr);
        g.drawString(goldStr, menuX + menuW - gw - 20, menuY + 20);

        // Divider
        g.setColor(new Color(0x434a63));
        g.drawLine(menuX + 10, menuY + 28, menuX + menuW - 10, menuY + 28);

        // Render Weapon Cards in 2 columns
        BufferedImage[] wImgs = sprites.slice("characters/weapons.png", 24, 24);
        Weapon activeWeapon = player.getActiveWeapon();
        int cardW = 206, cardH = 34;

        for (int i = 0; i < catalog.size(); i++) {
            Weapon w = catalog.get(i);
            int col = i % 2;
            int row = i / 2;
            int cx = menuX + 10 + col * (cardW + 8);
            int cy = menuY + 36 + row * (cardH + 5);

            boolean isEquipped = (activeWeapon != null && activeWeapon.getId() == w.getId());
            boolean isOwned = player.ownsWeapon(w.getId());
            boolean canAfford = player.getGold() >= w.getPrice();
            boolean isSelected = (i == selectedIndex);

            // Card background
            if (isEquipped) {
                g.setColor(new Color(0x263747));
            } else if (isSelected) {
                g.setColor(new Color(0x383347));
            } else {
                g.setColor(new Color(0x191622));
            }
            g.fillRect(cx, cy, cardW, cardH);

            // Border
            if (isEquipped) {
                g.setColor(new Color(0x38d7f2));
            } else if (isSelected) {
                g.setColor(new Color(0xfabc2a));
            } else {
                g.setColor(new Color(0x434a63));
            }
            g.drawRect(cx, cy, cardW, cardH);

            // Weapon icon
            int imgIdx = w.getId() < wImgs.length ? w.getId() : 0;
            g.drawImage(wImgs[imgIdx], cx + 4, cy + 5, null);

            // Weapon Name & Rarity
            g.setFont(FONT_TITLE);
            g.setColor(w.getRarity().color);
            g.drawString(w.getName(), cx + 32, cy + 12);

            // Type & ATK & Speed stats
            g.setFont(FONT_DESC);
            g.setColor(new Color(0xa8b6cc));
            String stats = "ATK " + w.getBaseDamage() + "  |  " + w.getType().name();
            g.drawString(stats, cx + 32, cy + 22);

            // Description
            g.setColor(new Color(0x738299));
            String desc = w.getDescription();
            if (desc.length() > 32) desc = desc.substring(0, 30) + "..";
            g.drawString(desc, cx + 32, cy + 30);

            // Status Badge (Right side of card)
            int badgeW = 54, badgeH = 16;
            int bx = cx + cardW - badgeW - 6;
            int by = cy + (cardH - badgeH) / 2;

            if (isEquipped) {
                g.setColor(new Color(0x0e5a6e));
                g.fillRect(bx, by, badgeW, badgeH);
                g.setColor(new Color(0x38d7f2));
                g.drawRect(bx, by, badgeW, badgeH);
                g.setFont(FONT_BADGE);
                g.setColor(new Color(0x96f2ff));
                centerText(g, "EQUIPPED", bx, by, badgeW, badgeH);
            } else if (isOwned) {
                g.setColor(new Color(0x1e5e26));
                g.fillRect(bx, by, badgeW, badgeH);
                g.setColor(new Color(0x62db58));
                g.drawRect(bx, by, badgeW, badgeH);
                g.setFont(FONT_BADGE);
                g.setColor(new Color(0xa8ff85));
                centerText(g, "EQUIP", bx, by, badgeW, badgeH);
            } else {
                // Locked / Buy
                g.setColor(canAfford ? new Color(0x523910) : new Color(0x282333));
                g.fillRect(bx, by, badgeW, badgeH);
                g.setColor(canAfford ? new Color(0xfabc2a) : new Color(0x555060));
                g.drawRect(bx, by, badgeW, badgeH);
                g.setFont(FONT_BADGE);
                g.setColor(canAfford ? new Color(0xffea78) : Color.GRAY);
                centerText(g, w.getPrice() + " G", bx, by, badgeW, badgeH);
            }
        }

        // Footer instructions
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
        g.setColor(new Color(0xa8b6cc));
        g.drawString("Click or [ENTER] to Buy / Equip   |   [ESC] or [B] to Close", menuX + 80, menuY + menuH - 6);
    }

    private static void centerText(Graphics2D g, String text, int x, int y, int w, int h) {
        int tw = g.getFontMetrics().stringWidth(text);
        int th = g.getFontMetrics().getAscent();
        g.drawString(text, x + (w - tw) / 2, y + (h + th) / 2 - 2);
    }
}
