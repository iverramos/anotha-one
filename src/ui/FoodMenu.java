package ui;

import audio.SoundManager;
import entities.Player;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Interactive food menu overlay for Han's Dungeon Diner (960x540 UI space).
 */
public final class FoodMenu {
    public static class Dish {
        public final int id;
        public final String name;
        public final int cost;
        public final int fullness;
        public final int bonusMaxHp;
        public final int healHp;
        public final int bonusAtk;
        public final double bonusSpeed;
        public final double bonusCrit;
        public final String desc;
        public final int iconIdx;

        public Dish(int id, String name, int cost, int fullness, int bonusMaxHp, int healHp, int bonusAtk, double bonusSpeed, double bonusCrit, String desc, int iconIdx) {
            this.id = id;
            this.name = name;
            this.cost = cost;
            this.fullness = fullness;
            this.bonusMaxHp = bonusMaxHp;
            this.healHp = healHp;
            this.bonusAtk = bonusAtk;
            this.bonusSpeed = bonusSpeed;
            this.bonusCrit = bonusCrit;
            this.desc = desc;
            this.iconIdx = iconIdx;
        }
    }

    private static final Dish[] DISHES = {
            new Dish(0, "Prime Rib Roast", 25, 25, 20, 30, 0, 0, 0, "+20 Max HP, Heals 30 HP", 0),
            new Dish(1, "Dragon Ramen",    20, 20, 0, 20, 3, 0.10, 0, "+3 ATK, +10% Speed, Heals 20 HP", 1),
            new Dish(2, "Hellfire Stew",   30, 30, 0, 25, 5, 0, 0.15, "+5 ATK, +15% Crit, Heals 25 HP", 2),
            new Dish(3, "Honey Pancake",   15, 15, 0, 40, 0, 0, 0, "+40 HP Recovery", 3)
    };

    private static final Font FONT_HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    private static final Font FONT_STATS  = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONT_ITEM   = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONT_DESC   = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font FONT_TIP    = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    private boolean open;
    private final SpriteManager sprites;

    public FoodMenu(SpriteManager sprites) {
        this.sprites = sprites;
    }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public void toggle() { this.open = !this.open; }

    public void tryBuy(int dishIndex, Player player) {
        if (dishIndex < 0 || dishIndex >= DISHES.length) return;
        Dish d = DISHES[dishIndex];

        if (player.getGold() >= d.cost && player.getFullness() + d.fullness <= Player.MAX_FULLNESS) {
            player.spendGold(d.cost);
            player.addFullness(d.fullness);
            if (d.bonusMaxHp > 0) player.increaseMaxHp(d.bonusMaxHp);
            if (d.healHp > 0) player.heal(d.healHp);
            if (d.bonusAtk > 0) player.addBonusAttack(d.bonusAtk);
            if (d.bonusSpeed > 0) player.addBonusSpeed(d.bonusSpeed);
            if (d.bonusCrit > 0) player.addBonusCrit(d.bonusCrit);
            SoundManager.play(SoundManager.Sound.FOOD_EAT);
        }
    }

    public void handleMouseClick(int mouseX, int mouseY, Player player) {
        if (!open) return;
        int menuW = 580, menuH = 380;
        int menuX = (Constants.UI_W - menuW) / 2;
        int menuY = (Constants.UI_H - menuH) / 2;

        for (int i = 0; i < DISHES.length; i++) {
            int itemY = menuY + 94 + i * 60;
            Rectangle rect = new Rectangle(menuX + 24, itemY, menuW - 48, 52);
            if (rect.contains(mouseX, mouseY)) {
                tryBuy(i, player);
                return;
            }
        }
    }

    public void render(Graphics2D g, Player player) {
        if (!open) return;

        g.setColor(new Color(15, 12, 22, 210));
        g.fillRect(0, 0, Constants.UI_W, Constants.UI_H);

        int menuW = 580, menuH = 380;
        int menuX = (Constants.UI_W - menuW) / 2;
        int menuY = (Constants.UI_H - menuH) / 2;

        g.setColor(new Color(0x282333));
        g.fillRect(menuX, menuY, menuW, menuH);
        g.setColor(new Color(0xfabc2a));
        g.drawRect(menuX, menuY, menuW, menuH);
        g.drawRect(menuX + 3, menuY + 3, menuW - 6, menuH - 6);

        g.setFont(FONT_HEADER);
        String header = "HAN'S DUNGEON DINER";
        int hw = g.getFontMetrics().stringWidth(header);
        g.setColor(new Color(0xfabc2a));
        g.drawString(header, menuX + (menuW - hw) / 2, menuY + 38);

        g.setFont(FONT_STATS);
        g.setColor(Color.WHITE);
        g.drawString("Gold: " + player.getGold() + " G", menuX + 32, menuY + 72);
        String fullStr = "Fullness: " + player.getFullness() + " / " + Player.MAX_FULLNESS;
        int fw = g.getFontMetrics().stringWidth(fullStr);
        g.drawString(fullStr, menuX + menuW - fw - 32, menuY + 72);

        BufferedImage hud = sprites.sprite("hud")[0];
        int[] iconOffsets = {74, 90, 106, 122};

        for (int i = 0; i < DISHES.length; i++) {
            Dish d = DISHES[i];
            int itemY = menuY + 94 + i * 60;
            boolean canAfford = player.getGold() >= d.cost && (player.getFullness() + d.fullness <= Player.MAX_FULLNESS);

            g.setColor(canAfford ? new Color(0x383347) : new Color(0x221e2c));
            g.fillRect(menuX + 24, itemY, menuW - 48, 52);
            g.setColor(canAfford ? new Color(0x65607a) : new Color(0x332c3f));
            g.drawRect(menuX + 24, itemY, menuW - 48, 52);

            BufferedImage icon = hud.getSubimage(iconOffsets[d.iconIdx], 2, 14, 14);
            g.drawImage(icon, menuX + 34, itemY + 12, 28, 28, null);

            g.setFont(FONT_ITEM);
            g.setColor(canAfford ? Color.WHITE : Color.GRAY);
            g.drawString("[" + (i + 1) + "] " + d.name, menuX + 74, itemY + 23);

            String priceStr = d.cost + " G (" + d.fullness + " Full)";
            int pw = g.getFontMetrics().stringWidth(priceStr);
            g.setColor(canAfford ? new Color(0xfabc2a) : Color.DARK_GRAY);
            g.drawString(priceStr, menuX + menuW - 38 - pw, itemY + 23);

            g.setFont(FONT_DESC);
            g.setColor(canAfford ? new Color(0x38d7f2) : Color.DARK_GRAY);
            g.drawString(d.desc, menuX + 74, itemY + 42);
        }

        g.setFont(FONT_TIP);
        String tip = "Press [1-4] or Click to eat   |   [E] or [ESC] to Close";
        int tw = g.getFontMetrics().stringWidth(tip);
        g.setColor(new Color(0xa8b6cc));
        g.drawString(tip, menuX + (menuW - tw) / 2, menuY + menuH - 14);
    }
}
