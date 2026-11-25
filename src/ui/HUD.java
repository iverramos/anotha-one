package ui;

import combat.Weapon;
import entities.BossEnemy;
import entities.Enemy;
import entities.Player;
import rendering.SpriteManager;
import utilities.Constants;
import world.Dungeon;
import world.Room;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * High-readability Dungreed HUD rendered at 960x540 UI resolution:
 * Hearts, dash charge wing gems, gold counter, animated fullness gauge,
 * weapon loadout frames, dungeon minimap, boss health bar, and crosshair.
 */
public final class HUD {
    private static final Font FONT_LARGE = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font FONT_BODY  = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    private static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.BOLD, 13);

    private final SpriteManager sprites;

    public HUD(SpriteManager sprites) {
        this.sprites = sprites;
    }

    public void render(Graphics2D g, Player player, Dungeon dungeon, int mouseX, int mouseY) {
        BufferedImage hudSheet = sprites.sprite("hud")[0];

        // 1. Health Bar & Hearts (Top-Left)
        int hp = player.getHealth();
        int maxHp = player.getMaxHealth();
        int totalHearts = (maxHp + 19) / 20;
        int fullHearts = hp / 20;
        int partial = hp % 20;

        for (int i = 0; i < totalHearts; i++) {
            int hx = 16 + i * 28;
            int hy = 16;
            if (i < fullHearts || (i == fullHearts && partial > 10)) {
                g.drawImage(hudSheet.getSubimage(0, 0, 12, 12), hx, hy, 24, 24, null);
            } else {
                g.drawImage(hudSheet.getSubimage(14, 0, 12, 12), hx, hy, 24, 24, null);
            }
        }

        // HP numeric text
        g.setFont(FONT_BODY);
        text(g, hp + " / " + maxHp, 24 + totalHearts * 28, 34, Color.WHITE);

        // 2. Dash Charges (Below Hearts)
        int charges = player.getDashCharges();
        for (int i = 0; i < Player.MAX_DASHES; i++) {
            int dx = 16 + i * 32;
            int dy = 48;
            if (i < charges) {
                g.drawImage(hudSheet.getSubimage(28, 0, 14, 14), dx, dy, 28, 28, null);
            } else {
                g.drawImage(hudSheet.getSubimage(44, 0, 14, 14), dx, dy, 28, 28, null);
            }
        }
        g.setFont(FONT_SMALL);
        text(g, "DASH", 24 + Player.MAX_DASHES * 32, 68, new Color(0x38d7f2));

        // 3. Gold Counter & Animated Fullness Gauge
        int gy = 86;
        g.drawImage(hudSheet.getSubimage(60, 0, 10, 10), 16, gy, 20, 20, null);
        g.setFont(FONT_BODY);
        text(g, player.getGold() + " G", 42, gy + 16, new Color(0xfabc2a));

        // Fullness Gauge Bar
        int barX = 128;
        int barY = gy + 1;
        int barW = 136;
        int barH = 18;
        double dispPct = Math.max(0.0, Math.min(1.0, player.getDisplayFullness() / Player.MAX_FULLNESS));
        double actualPct = Math.max(0.0, Math.min(1.0, (double) player.getFullness() / Player.MAX_FULLNESS));
        boolean draining = player.getFullnessDrainFlashTimer() > 0;

        g.setColor(new Color(0x1b1d28));
        g.fillRect(barX, barY, barW, barH);

        // Trailing delta bar when draining
        if (dispPct > actualPct) {
            int trailW = (int) Math.round((barW - 2) * dispPct);
            g.setColor(new Color(0xfff0a8));
            g.fillRect(barX + 1, barY + 1, trailW, barH - 2);
        }

        int fillW = (int) Math.round((barW - 2) * actualPct);
        g.setColor(draining ? new Color(0xffb838) : new Color(0xe87b22));
        g.fillRect(barX + 1, barY + 1, fillW, barH - 2);

        g.setColor(draining ? new Color(0xffea78) : new Color(0x656f8f));
        g.drawRect(barX, barY, barW, barH);

        g.setFont(FONT_SMALL);
        String fullLabel = "FULL " + player.getFullness() + "/" + Player.MAX_FULLNESS;
        int flw = g.getFontMetrics().stringWidth(fullLabel);
        text(g, fullLabel, barX + (barW - flw) / 2, barY + 14,
                draining ? new Color(0xffea78) : Color.WHITE);

        // 4. Weapons Hotbar (Bottom-Left)
        List<Weapon> weapons = player.getWeapons();
        int activeIdx = player.getActiveWeaponIndex();
        BufferedImage[] wSprites = sprites.sprite("weapons");

        int slotX = 16;
        int slotY = Constants.UI_H - 66;
        for (int i = 0; i < 3; i++) {
            int bx = slotX + i * 54;
            boolean active = (i == activeIdx);

            g.drawImage(hudSheet.getSubimage(136, 2, 22, 22), bx, slotY, 44, 44, null);

            if (i < weapons.size()) {
                Weapon w = weapons.get(i);
                int wIdx = w.getId() < wSprites.length ? w.getId() : 0;
                g.drawImage(wSprites[wIdx], bx - 2, slotY - 2, 48, 48, null);

                double cd = w.getCooldownProgress();
                if (cd > 0) {
                    g.setColor(new Color(0, 0, 0, 145));
                    int fillH = (int) Math.round(40 * cd);
                    g.fillRect(bx + 2, slotY + 42 - fillH, 40, fillH);
                }
            }

            if (active) {
                g.setColor(new Color(0xfabc2a));
                g.drawRect(bx - 2, slotY - 2, 47, 47);
                g.drawRect(bx - 1, slotY - 1, 45, 45);
            }
        }

        // Active weapon name & stats
        Weapon activeW = player.getActiveWeapon();
        if (activeW != null) {
            g.setFont(FONT_BODY);
            text(g, activeW.getName(), 190, Constants.UI_H - 44, activeW.getRarity().color);
            g.setFont(FONT_SMALL);
            text(g, "ATK " + activeW.getBaseDamage() + "  |  " + activeW.getDescription(),
                    190, Constants.UI_H - 24, new Color(0xdce4f0));
        }

        // 5. Dungeon Minimap (Top-Right)
        renderMinimap(g, dungeon);

        // 6. Boss Health Bar (Top-Center in Boss Room)
        Room currentRoom = dungeon.getCurrentRoom();
        if (currentRoom.getType() == Room.Type.BOSS || currentRoom.getType() == Room.Type.TIER2_BOSS) {
            for (Enemy e : currentRoom.getEnemies()) {
                if (!e.isDead()) {
                    if (e instanceof BossEnemy) {
                        renderBossHealthBar(g, BossEnemy.BOSS_NAME, e.getHealth(), e.getMaxHealth());
                        break;
                    } else if (e instanceof entities.Tier2BossEnemy) {
                        renderBossHealthBar(g, entities.Tier2BossEnemy.BOSS_NAME, e.getHealth(), e.getMaxHealth());
                        break;
                    }
                }
            }
        }

        // 7. Aim Crosshair
        BufferedImage crosshair = sprites.sprite("crosshair")[0];
        g.drawImage(crosshair, mouseX - 16, mouseY - 16, 32, 32, null);
    }

    private void renderMinimap(Graphics2D g, Dungeon dungeon) {
        int mmW = 148, mmH = 44;
        int mmX = Constants.UI_W - mmW - 14;
        int mmY = 12;

        g.setColor(new Color(15, 12, 22, 200));
        g.fillRect(mmX, mmY, mmW, mmH);
        g.setColor(new Color(0x434a63));
        g.drawRect(mmX, mmY, mmW, mmH);

        int count = dungeon.getRoomCount();
        int curIdx = dungeon.getCurrentRoomIndex();
        int rw = 16, rh = 16;

        for (int i = 0; i < count; i++) {
            int rx = mmX + 10 + i * 22;
            int ry = mmY + 14;
            if (!dungeon.isVisited(i)) {
                g.setColor(new Color(0x282333));
                g.fillRect(rx, ry, rw, rh);
                continue;
            }

            Room r = dungeon.getRoom(i);
            Color rc;
            if (i == curIdx) {
                rc = new Color(0x38d7f2);
            } else if (r.getType() == Room.Type.BOSS || r.getType() == Room.Type.TIER2_BOSS) {
                rc = new Color(0xe62e3d);
            } else if (r.getType() == Room.Type.TREASURE) {
                rc = new Color(0xfabc2a);
            } else if (r.getType() == Room.Type.FOOD_SHOP) {
                rc = new Color(0xfaea70);
            } else {
                rc = new Color(0x738299);
            }

            g.setColor(rc);
            g.fillRect(rx, ry, rw, rh);
            g.setColor(Color.BLACK);
            g.drawRect(rx, ry, rw, rh);
        }
    }

    private void renderBossHealthBar(Graphics2D g, String bossName, int hp, int maxHp) {
        int barW = 440, barH = 22;
        int barX = (Constants.UI_W - barW) / 2;
        int barY = 32;

        g.setColor(new Color(20, 14, 26, 225));
        g.fillRect(barX - 4, barY - 24, barW + 8, barH + 30);
        g.setColor(new Color(0x8f121e));
        g.drawRect(barX - 4, barY - 24, barW + 8, barH + 30);

        g.setFont(FONT_BODY);
        String label = "☠ " + bossName + " ☠";
        int lw = g.getFontMetrics().stringWidth(label);
        text(g, label, barX + (barW - lw) / 2, barY - 6, new Color(0xfabc2a));

        double pct = Math.max(0, (double) hp / maxHp);
        int fillW = (int) Math.round(barW * pct);
        g.setColor(new Color(0x2d3142));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(new Color(0xe62e3d));
        g.fillRect(barX, barY, fillW, barH);

        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);

        g.setFont(FONT_SMALL);
        String hpText = hp + " / " + maxHp;
        int hw = g.getFontMetrics().stringWidth(hpText);
        text(g, hpText, barX + (barW - hw) / 2, barY + 16, Color.WHITE);
    }

    private static void text(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(Color.BLACK);
        g.drawString(s, x + 2, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
    }
}
