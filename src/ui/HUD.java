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
 * Authentic Dungreed HUD:
 * Hearts, dash charge wing gems, gold counter, fullness gauge,
 * weapon loadout frames, dungeon minimap, boss health bar, and pixel crosshair.
 */
public final class HUD {
    private static final Font FONT_LARGE = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    private static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.BOLD, 8);
    private static final Font FONT_TINY  = new Font(Font.SANS_SERIF, Font.PLAIN, 7);

    private final SpriteManager sprites;

    public HUD(SpriteManager sprites) {
        this.sprites = sprites;
    }

    public void render(Graphics2D g, Player player, Dungeon dungeon, int mouseX, int mouseY) {
        BufferedImage hudSheet = sprites.get("ui/hud.png");

        // 1. Health Bar & Hearts (Top-Left)
        int hp = player.getHealth();
        int maxHp = player.getMaxHealth();
        int totalHearts = (maxHp + 19) / 20; // 20 hp per heart
        int fullHearts = hp / 20;
        int partial = hp % 20;

        for (int i = 0; i < totalHearts; i++) {
            int hx = 8 + i * 14;
            int hy = 8;
            if (i < fullHearts || (i == fullHearts && partial > 10)) {
                // Full heart (12x12 at x=0)
                g.drawImage(hudSheet.getSubimage(0, 0, 12, 12), hx, hy, null);
            } else {
                // Empty heart (12x12 at x=14)
                g.drawImage(hudSheet.getSubimage(14, 0, 12, 12), hx, hy, null);
            }
        }

        // HP numeric text
        g.setFont(FONT_SMALL);
        text(g, hp + " / " + maxHp, 12 + totalHearts * 14, 18, Color.WHITE);

        // 2. Dash Charges (Below Hearts)
        int charges = player.getDashCharges();
        for (int i = 0; i < Player.MAX_DASHES; i++) {
            int dx = 8 + i * 16;
            int dy = 24;
            if (i < charges) {
                // Cyan active wing (14x14 at x=28)
                g.drawImage(hudSheet.getSubimage(28, 0, 14, 14), dx, dy, null);
            } else {
                // Dim recharge wing (14x14 at x=44)
                g.drawImage(hudSheet.getSubimage(44, 0, 14, 14), dx, dy, null);
            }
        }
        text(g, "DASH", 12 + Player.MAX_DASHES * 16, 35, new Color(0x38d7f2));

        // 3. Gold Counter & Fullness
        int gy = 44;
        // Coin icon (10x10 at x=60)
        g.drawImage(hudSheet.getSubimage(60, 0, 10, 10), 8, gy, null);
        text(g, player.getGold() + " G", 22, gy + 9, new Color(0xfabc2a));

        // Fullness
        text(g, "Full: " + player.getFullness() + "/" + Player.MAX_FULLNESS, 65, gy + 9, new Color(0xa8b6cc));

        // 4. Weapons Hotbar (Bottom-Left)
        List<Weapon> weapons = player.getWeapons();
        int activeIdx = player.getActiveWeaponIndex();
        BufferedImage[] wSprites = sprites.slice("characters/weapons.png", 24, 24);

        int slotX = 8;
        int slotY = Constants.VIEW_H - 32;
        for (int i = 0; i < 3; i++) {
            int bx = slotX + i * 28;
            boolean active = (i == activeIdx);

            // Hotbar frame (22x22 at x=136)
            g.drawImage(hudSheet.getSubimage(136, 2, 22, 22), bx, slotY, null);

            if (i < weapons.size()) {
                Weapon w = weapons.get(i);
                if (w.getId() < wSprites.length) {
                    g.drawImage(wSprites[w.getId()], bx - 1, slotY - 1, 24, 24, null);
                }
                // Cooldown overlay
                double cd = w.getCooldownProgress();
                if (cd > 0) {
                    g.setColor(new Color(0, 0, 0, 140));
                    int fillH = (int) Math.round(20 * cd);
                    g.fillRect(bx + 1, slotY + 21 - fillH, 20, fillH);
                }
            }

            if (active) {
                g.setColor(new Color(0xfabc2a));
                g.drawRect(bx - 1, slotY - 1, 23, 23);
            }
        }

        // Active weapon name & stats
        Weapon activeW = player.getActiveWeapon();
        if (activeW != null) {
            g.setFont(FONT_SMALL);
            text(g, activeW.getName(), 96, Constants.VIEW_H - 22, activeW.getRarity().color);
            g.setFont(FONT_TINY);
            text(g, "ATK " + activeW.getBaseDamage() + "  |  " + activeW.getDescription(),
                    96, Constants.VIEW_H - 12, new Color(0xdce4f0));
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
        BufferedImage crosshair = sprites.get("ui/crosshair.png");
        g.drawImage(crosshair, mouseX - 8, mouseY - 8, null);
    }

    private void renderMinimap(Graphics2D g, Dungeon dungeon) {
        int mmW = 68, mmH = 22;
        int mmX = Constants.VIEW_W - mmW - 6;
        int mmY = 6;

        // Backdrop
        g.setColor(new Color(15, 12, 22, 190));
        g.fillRect(mmX, mmY, mmW, mmH);
        g.setColor(new Color(0x434a63));
        g.drawRect(mmX, mmY, mmW, mmH);

        int count = dungeon.getRoomCount();
        int curIdx = dungeon.getCurrentRoomIndex();
        int rw = 8, rh = 8;

        for (int i = 0; i < count; i++) {
            int rx = mmX + 4 + i * 10;
            int ry = mmY + 7;
            if (!dungeon.isVisited(i)) {
                g.setColor(new Color(0x282333));
                g.fillRect(rx, ry, rw, rh);
                continue;
            }

            Room r = dungeon.getRoom(i);
            Color rc;
            if (i == curIdx) {
                rc = new Color(0x38d7f2); // Current: bright cyan
            } else if (r.getType() == Room.Type.BOSS || r.getType() == Room.Type.TIER2_BOSS) {
                rc = new Color(0xe62e3d); // Boss: crimson
            } else if (r.getType() == Room.Type.TREASURE) {
                rc = new Color(0xfabc2a); // Treasure: gold
            } else if (r.getType() == Room.Type.FOOD_SHOP) {
                rc = new Color(0xfaea70); // Diner: yellow
            } else {
                rc = new Color(0x738299); // Combat: slate
            }

            g.setColor(rc);
            g.fillRect(rx, ry, rw, rh);
            g.setColor(Color.BLACK);
            g.drawRect(rx, ry, rw, rh);
        }
    }

    private void renderBossHealthBar(Graphics2D g, String bossName, int hp, int maxHp) {
        int barW = 220, barH = 12;
        int barX = (Constants.VIEW_W - barW) / 2;
        int barY = 14;

        // Frame
        g.setColor(new Color(20, 14, 26, 220));
        g.fillRect(barX - 2, barY - 12, barW + 4, barH + 16);
        g.setColor(new Color(0x8f121e));
        g.drawRect(barX - 2, barY - 12, barW + 4, barH + 16);

        // Boss Title
        g.setFont(FONT_SMALL);
        text(g, "☠ " + bossName + " ☠", barX + 35, barY - 3, new Color(0xfabc2a));

        // Red Fill
        double pct = Math.max(0, (double) hp / maxHp);
        int fillW = (int) Math.round(barW * pct);
        g.setColor(new Color(0x2d3142));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(new Color(0xe62e3d));
        g.fillRect(barX, barY, fillW, barH);

        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);

        // HP numeric
        g.setFont(FONT_TINY);
        text(g, hp + " / " + maxHp, barX + barW / 2 - 20, barY + 9, Color.WHITE);
    }

    private static void text(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(Color.BLACK);
        g.drawString(s, x + 1, y + 1);
        g.setColor(c);
        g.drawString(s, x, y);
    }
}
