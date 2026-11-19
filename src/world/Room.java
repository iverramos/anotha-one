package world;

import combat.Weapon;
import entities.Chest;
import entities.Enemy;
import entities.FoodCart;
import entities.Pickup;
import rendering.Animation;
import rendering.Renderer;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A dungeon chamber with terrain, platforms, enemies, torches, doors, and special features
 * (Chests, Han's Diner food cart, or Boss arena).
 */
public final class Room {
    public enum Type {
        START, COMBAT_SLIMES, COMBAT_SKELETONS, TREASURE, FOOD_SHOP, BOSS
    }

    private final int id;
    private final Type type;
    private final String title;
    private final TileMap map;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Animation> torches = new ArrayList<>();
    private final List<int[]> torchCoords = new ArrayList<>();
    private Chest chest;
    private FoodCart foodCart;

    private boolean cleared;
    private double clearBannerTimer;
    private boolean leftDoorOpen;
    private boolean rightDoorOpen;

    public Room(int id, Type type, String title, SpriteManager sprites, Renderer renderer, int cols, int rows) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.map = new TileMap(sprites, cols, rows);
        buildRoomLayout(sprites, renderer);
    }

    private void buildRoomLayout(SpriteManager sprites, Renderer renderer) {
        int cols = map.getCols();
        int rows = map.getRows();

        // 1. Background walls and pillars
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c == 6 || c == cols - 7 || c == cols / 2) {
                    map.setBackground(c, r, TileMap.BG_PILLAR);
                } else {
                    map.setBackground(c, r, TileMap.BG_WALL);
                }
            }
        }

        // 2. Outer boundary walls (solid)
        for (int c = 0; c < cols; c++) {
            map.setForeground(c, 0, TileMap.WALL);
            map.setForeground(c, 1, TileMap.WALL);
            // Floor
            map.setForeground(c, rows - 2, TileMap.FLOOR);
            map.setForeground(c, rows - 1, TileMap.COBBLE);
        }
        for (int r = 0; r < rows; r++) {
            map.setForeground(0, r, TileMap.WALL);
            map.setForeground(1, r, TileMap.WALL);
            map.setForeground(cols - 1, r, TileMap.WALL);
            map.setForeground(cols - 2, r, TileMap.WALL);
        }

        // 3. Doors (Left and Right)
        // Left Door at col 1, rows 12..14
        if (id > 0) {
            map.setForeground(1, rows - 3, TileMap.PORTAL);
            map.setForeground(1, rows - 4, TileMap.PORTAL);
        }
        // Right Door at col cols-2, rows 12..14
        if (type != Type.BOSS) {
            map.setForeground(cols - 2, rows - 3, TileMap.PORTAL);
            map.setForeground(cols - 2, rows - 4, TileMap.PORTAL);
        }

        // 4. Room-specific architecture & features
        if (type == Type.START) {
            cleared = true;
            leftDoorOpen = true;
            rightDoorOpen = true;

            // Wooden training ledge
            for (int c = 8; c <= 14; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);
            for (int c = 22; c <= 28; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);

            addTorch(sprites, 6, 8);
            addTorch(sprites, cols - 7, 8);
        } else if (type == Type.COMBAT_SLIMES) {
            // Multi-tier platforms
            for (int c = 6; c <= 12; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);
            for (int c = 16; c <= 22; c++) map.setForeground(c, 8, TileMap.WOOD_PLATFORM);
            for (int c = 26; c <= 32; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);

            addTorch(sprites, 10, 6);
            addTorch(sprites, 28, 6);

            // Slimes and bats
            enemies.add(new entities.SlimeEnemy(sprites, 120, 180));
            enemies.add(new entities.SlimeEnemy(sprites, 260, 180));
            enemies.add(new entities.SlimeEnemy(sprites, 420, 180));
            enemies.add(new entities.BatEnemy(sprites, 180, 80));
            enemies.add(new entities.BatEnemy(sprites, 340, 80));
        } else if (type == Type.COMBAT_SKELETONS) {
            // High stone ledges and spikes
            for (int c = 6; c <= 13; c++) map.setForeground(c, 9, TileMap.FLOOR);
            for (int c = cols - 14; c <= cols - 7; c++) map.setForeground(c, 9, TileMap.FLOOR);
            for (int c = 16; c <= 22; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);

            // Spikes in center pit
            for (int c = 17; c <= 21; c++) map.setForeground(c, rows - 3, TileMap.SPIKES);

            addTorch(sprites, 8, 5);
            addTorch(sprites, cols - 9, 5);

            enemies.add(new entities.SkeletonEnemy(sprites, 140, 100));
            enemies.add(new entities.SkeletonEnemy(sprites, 380, 100));
            enemies.add(new entities.MageEnemy(sprites, 280, 120));
            enemies.add(new entities.SlimeEnemy(sprites, 220, 180));
        } else if (type == Type.TREASURE) {
            cleared = true;
            leftDoorOpen = true;
            rightDoorOpen = true;

            // Vault pedestal
            for (int c = 14; c <= 24; c++) map.setForeground(c, 11, TileMap.FLOOR);
            addTorch(sprites, 12, 7);
            addTorch(sprites, 26, 7);

            // Golden Chest containing high-tier gear!
            Weapon secretWeapon = (Math.random() < 0.5) ? Weapon.createGreatsword() : Weapon.createFireStaff();
            chest = new Chest(sprites, 18 * Constants.TILE, 9 * Constants.TILE, secretWeapon);
        } else if (type == Type.FOOD_SHOP) {
            cleared = true;
            leftDoorOpen = true;
            rightDoorOpen = true;

            addTorch(sprites, 8, 8);
            addTorch(sprites, cols - 9, 8);

            // Han's Food Stall in center
            foodCart = new FoodCart(sprites, 16 * Constants.TILE, (rows - 4) * Constants.TILE - 4);
        } else if (type == Type.BOSS) {
            // Large boss chamber
            for (int c = 5; c <= 11; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);
            for (int c = cols - 12; c <= cols - 6; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);

            addTorch(sprites, 6, 6);
            addTorch(sprites, cols - 7, 6);
            addTorch(sprites, cols / 2, 5);

            enemies.add(new entities.BossEnemy(sprites, renderer, 22 * Constants.TILE, 8 * Constants.TILE));
        }
    }

    private void addTorch(SpriteManager sprites, int col, int row) {
        Animation torch = new Animation(sprites.slice("environment/torch.png", 16, 16), 0.12, true);
        torch.update(Math.random());
        torches.add(torch);
        torchCoords.add(new int[]{col, row});
    }

    public void update(double dt) {
        for (Animation a : torches) a.update(dt);
        if (chest != null) chest.update(dt);

        // Check room clearance
        if (!cleared) {
            boolean allDead = true;
            for (Enemy e : enemies) {
                if (!e.isDead()) {
                    allDead = false;
                    break;
                }
            }
            if (allDead) {
                cleared = true;
                leftDoorOpen = true;
                rightDoorOpen = true;
                clearBannerTimer = 2.5;
                audio.SoundManager.play(audio.SoundManager.Sound.ROOM_CLEAR);
            }
        } else if (clearBannerTimer > 0) {
            clearBannerTimer -= dt;
        }
    }

    public void render(Graphics2D g, int camX, int camY, double playerX, double playerY) {
        map.renderBackground(g, camX, camY);

        // Render Torches
        int t = Constants.TILE;
        for (int i = 0; i < torches.size(); i++) {
            int[] pos = torchCoords.get(i);
            g.drawImage(torches.get(i).frame(), pos[0] * t - camX, pos[1] * t - camY, null);
        }

        // Render Food Stall
        if (foodCart != null) {
            foodCart.render(g, camX, camY, playerX, playerY);
        }

        // Render Chest
        if (chest != null) {
            chest.render(g, camX, camY, playerX, playerY);
        }

        map.renderForeground(g, camX, camY);

        // Room cleared banner
        if (clearBannerTimer > 0) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String banner = "★ ROOM CLEARED! ★";
            int bx = (Constants.VIEW_W - g.getFontMetrics().stringWidth(banner)) / 2;
            int by = 60;
            g.setColor(Color.BLACK);
            g.drawString(banner, bx + 1, by + 1);
            g.setColor(new Color(0xfabc2a));
            g.drawString(banner, bx, by);
        }
    }

    public Rectangle getLeftDoorBounds() {
        int t = Constants.TILE;
        int rows = map.getRows();
        return new Rectangle(t, (rows - 4) * t, t, t * 2);
    }

    public Rectangle getRightDoorBounds() {
        int t = Constants.TILE;
        int cols = map.getCols();
        int rows = map.getRows();
        return new Rectangle((cols - 2) * t, (rows - 4) * t, t, t * 2);
    }

    public int getId() { return id; }
    public Type getType() { return type; }
    public String getTitle() { return title; }
    public TileMap getMap() { return map; }
    public List<Enemy> getEnemies() { return enemies; }
    public Chest getChest() { return chest; }
    public FoodCart getFoodCart() { return foodCart; }
    public boolean isCleared() { return cleared; }
    public boolean isLeftDoorOpen() { return leftDoorOpen; }
    public boolean isRightDoorOpen() { return rightDoorOpen; }
}
