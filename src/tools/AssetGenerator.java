package tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Procedural pixel-art generator crafted in the vibrant, chunky 16-bit arcade style
 * of Dungreed (Steam app 753420).
 * Generates all sprites, tilesets, characters, weapons, enemies, boss, effects, and HUD icons
 * with authentic black/dark outlines, rich multi-tone shading, and dynamic animation frames.
 */
public final class AssetGenerator {
    private AssetGenerator() {}

    // ---- Universal Palette ----
    public static final int TRANSPARENT = 0x00000000;
    public static final int OUTLINE     = 0xFF14121a; // Crisp dark outline
    public static final int PURE_WHITE  = 0xFFfbfbfb;

    // Stone & Dungeon palette
    public static final int STONE_DARK  = 0xFF2d3142;
    public static final int STONE_MID   = 0xFF434a63;
    public static final int STONE_LIGHT = 0xFF656f8f;
    public static final int STONE_HI    = 0xFF8e9ab8;
    public static final int MORTAR      = 0xFF1b1d28;

    // Wood & Earth palette
    public static final int WOOD_DARK   = 0xFF3a2312;
    public static final int WOOD_MID    = 0xFF684123;
    public static final int WOOD_LIGHT  = 0xFF96633b;
    public static final int WOOD_HI     = 0xFFc08855;

    // Hero / Armor palette
    public static final int STEEL_DARK  = 0xFF495466;
    public static final int STEEL_MID   = 0xFF738299;
    public static final int STEEL_LIGHT = 0xFFa8b6cc;
    public static final int STEEL_HI    = 0xFFdce4f0;

    public static final int CAPE_DARK   = 0xFF153363;
    public static final int CAPE_MID    = 0xFF20529c;
    public static final int CAPE_LIGHT  = 0xFF3a77d4;
    public static final int CAPE_HI     = 0xFF6ba2f5;

    public static final int GOLD_DARK   = 0xFF7a5311;
    public static final int GOLD_MID    = 0xFFc48a1a;
    public static final int GOLD_LIGHT  = 0xFFfabc2a;
    public static final int GOLD_HI     = 0xFFffea78;

    public static final int LEATHER_DARK= 0xFF452416;
    public static final int LEATHER_MID = 0xFF754024;
    public static final int LEATHER_LIGHT=0xFFa6623d;

    // Organic & Monster palette
    public static final int SLIME_DARK  = 0xFF1e5e26;
    public static final int SLIME_MID   = 0xFF35a342;
    public static final int SLIME_LIGHT = 0xFF62db58;
    public static final int SLIME_HI    = 0xFFa8ff85;

    public static final int BAT_DARK    = 0xFF2c1c38;
    public static final int BAT_MID     = 0xFF4e2f63;
    public static final int BAT_LIGHT   = 0xFF7d4e9c;
    public static final int BAT_WING    = 0xFF9c3d4a;

    public static final int BONE_DARK   = 0xFF69665c;
    public static final int BONE_MID    = 0xFF9e9a8e;
    public static final int BONE_LIGHT  = 0xFFd6d2c4;
    public static final int BONE_HI     = 0xFFf5f3eb;

    // Magic & FX palette
    public static final int FIRE_DARK   = 0xFFc42a14;
    public static final int FIRE_MID    = 0xFFe8601c;
    public static final int FIRE_LIGHT  = 0xFFf7a828;
    public static final int FIRE_HI     = 0xFFfaea70;

    public static final int CYAN_DARK   = 0xFF0e5a6e;
    public static final int CYAN_MID    = 0xFF1897b8;
    public static final int CYAN_LIGHT  = 0xFF38d7f2;
    public static final int CYAN_HI     = 0xFF96f2ff;

    public static final int PURPLE_DARK = 0xFF3f1259;
    public static final int PURPLE_MID  = 0xFF6d2299;
    public static final int PURPLE_LIGHT= 0xFFad44f0;
    public static final int PURPLE_HI   = 0xFFdf87ff;

    public static final int RED_HEART   = 0xFFe62e3d;
    public static final int RED_DARK    = 0xFF8f121e;

    public static final String[] DIRS = {
            "sprites/player", "sprites/enemies", "sprites/bosses", "sprites/npcs", "sprites/weapons", "sprites/items",
            "tilesets/village", "tilesets/forest", "tilesets/dungeon", "tilesets/castle", "tilesets/underground", "tilesets/ruins",
            "buildings/medieval", "buildings/shops", "buildings/houses", "buildings/interiors",
            "backgrounds/forest", "backgrounds/dungeon", "backgrounds/town",
            "effects/particles", "effects/magic", "effects/combat",
            "config"
    };

    public static void generateAll(Path root) throws IOException {
        for (String d : DIRS) Files.createDirectories(root.resolve(d));

        write(buildTileset(), root.resolve("tilesets/dungeon/legacy_tileset.png"));
        write(buildTorch(), root.resolve("sprites/items/torch.png"));
        write(buildChest(), root.resolve("sprites/items/chest.png"));
        write(buildFoodCart(), root.resolve("sprites/items/food_cart.png"));

        write(buildPlayer(), root.resolve("sprites/player/player.png"));
        write(buildWeapons(), root.resolve("sprites/weapons/weapons.png"));

        write(buildSlash(), root.resolve("effects/combat/slash.png"));
        write(buildProjectiles(), root.resolve("effects/magic/projectiles.png"));
        write(buildParticles(), root.resolve("effects/particles/particles.png"));

        write(buildSlime(), root.resolve("sprites/enemies/slime.png"));
        write(buildBat(), root.resolve("sprites/enemies/bat.png"));
        write(buildSkeleton(), root.resolve("sprites/enemies/skeleton.png"));
        write(buildMage(), root.resolve("sprites/enemies/mage.png"));
        write(buildBoss(), root.resolve("sprites/bosses/belial.png"));

        write(buildHUD(), root.resolve("sprites/items/hud.png"));
        write(buildCrosshair(), root.resolve("sprites/items/crosshair.png"));

        WorldAssetGenerator.generateWorldAssets(root);
    }

    private static void write(BufferedImage img, Path p) throws IOException {
        ImageIO.write(img, "png", p.toFile());
    }

    // =========================================================================
    // 1. TILESET (10 tiles x 16x16 = 160x16)
    // 0: Wall Brick, 1: Stone Floor, 2: Cobble, 3: Wooden Platform (one-way),
    // 4: Spikes, 5: Dark BG Wall, 6: Arch/Pillar, 7: Portal Arch, 8: Mossy Stone, 9: Boss Gate
    // =========================================================================
    static BufferedImage buildTileset() {
        BufferedImage im = new BufferedImage(160, 16, BufferedImage.TYPE_INT_ARGB);

        // Tile 0: Solid Dungeon Brick
        drawDungeonBrick(im, 0, false);

        // Tile 1: Stone Floor / Top ledge (smooth carved paving with top bevel)
        drawStoneFloor(im, 16);

        // Tile 2: Cobble / Foundation stone
        drawCobble(im, 32);

        // Tile 3: Wooden One-Way Platform (planks with iron brackets)
        drawWoodPlatform(im, 48);

        // Tile 4: Floor Spikes
        drawFloorSpikes(im, 64);

        // Tile 5: Dark Background Wall (recessed stone bricks)
        drawBackgroundWall(im, 80);

        // Tile 6: Background Pillar
        drawBackgroundPillar(im, 96);

        // Tile 7: Dungeon Portal / Door Arch
        drawPortalTile(im, 112);

        // Tile 8: Mossy Stone Wall
        drawDungeonBrick(im, 128, true);

        // Tile 9: Boss Gate / Spiked Arch
        drawBossGate(im, 144);

        return im;
    }

    private static void drawDungeonBrick(BufferedImage im, int ox, boolean mossy) {
        for (int y = 0; y < 16; y++) {
            boolean lower = y >= 8;
            int ry = y % 8;
            for (int x = 0; x < 16; x++) {
                int vx = lower ? ((x == 3 || x == 11) ? 1 : 0) : ((x == 7 || x == 15) ? 1 : 0);
                if (ry == 7 || vx == 1) {
                    im.setRGB(ox + x, y, MORTAR);
                } else if (ry == 0) {
                    im.setRGB(ox + x, y, STONE_HI);
                } else if (ry == 6) {
                    im.setRGB(ox + x, y, STONE_DARK);
                } else {
                    int c = ((x + y * 3) % 5 == 0) ? STONE_LIGHT : STONE_MID;
                    if (mossy && (y > 10 || (x > 4 && x < 9 && y < 5))) {
                        c = ((x + y) % 2 == 0) ? SLIME_DARK : SLIME_MID;
                    }
                    im.setRGB(ox + x, y, c);
                }
            }
        }
    }

    private static void drawStoneFloor(BufferedImage im, int ox) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (y == 0) im.setRGB(ox + x, y, OUTLINE);
                else if (y == 1) im.setRGB(ox + x, y, STONE_HI);
                else if (y == 2) im.setRGB(ox + x, y, STONE_LIGHT);
                else if (y == 15) im.setRGB(ox + x, y, MORTAR);
                else {
                    boolean seam = (x == 5 || x == 12);
                    if (seam) im.setRGB(ox + x, y, STONE_DARK);
                    else im.setRGB(ox + x, y, ((x + y) % 4 == 0) ? STONE_LIGHT : STONE_MID);
                }
            }
        }
        // Small gold/moss flecks on top
        im.setRGB(ox + 3, 2, SLIME_LIGHT);
        im.setRGB(ox + 4, 2, SLIME_MID);
        im.setRGB(ox + 10, 2, SLIME_LIGHT);
    }

    private static void drawCobble(BufferedImage im, int ox) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                im.setRGB(ox + x, y, MORTAR);
            }
        }
        int[][] stones = {{1, 1, 6, 6}, {8, 1, 7, 5}, {1, 8, 7, 7}, {9, 7, 6, 8}};
        for (int[] s : stones) {
            int sx = s[0], sy = s[1], sw = s[2], sh = s[3];
            for (int dy = 0; dy < sh; dy++) {
                for (int dx = 0; dx < sw; dx++) {
                    int c = STONE_MID;
                    if (dx == 0 || dy == 0) c = STONE_HI;
                    else if (dx == sw - 1 || dy == sh - 1) c = STONE_DARK;
                    im.setRGB(ox + sx + dx, sy + dy, c);
                }
            }
        }
    }

    private static void drawWoodPlatform(BufferedImage im, int ox) {
        // Semi-solid drop-through wooden beam
        for (int x = 0; x < 16; x++) {
            im.setRGB(ox + x, 0, OUTLINE);
            im.setRGB(ox + x, 1, WOOD_HI);
            im.setRGB(ox + x, 2, WOOD_LIGHT);
            im.setRGB(ox + x, 3, WOOD_MID);
            im.setRGB(ox + x, 4, WOOD_DARK);
            im.setRGB(ox + x, 5, OUTLINE);
        }
        // Iron brackets on ends and center
        int[] brackets = {1, 7, 13};
        for (int bx : brackets) {
            im.setRGB(ox + bx, 1, STEEL_HI);
            im.setRGB(ox + bx + 1, 1, STEEL_LIGHT);
            im.setRGB(ox + bx, 2, STEEL_LIGHT);
            im.setRGB(ox + bx + 1, 2, STEEL_DARK);
            im.setRGB(ox + bx, 3, STEEL_DARK);
            im.setRGB(ox + bx + 1, 3, STEEL_DARK);
            im.setRGB(ox + bx, 4, OUTLINE);
            im.setRGB(ox + bx + 1, 4, OUTLINE);
        }
    }

    private static void drawFloorSpikes(BufferedImage im, int ox) {
        // Base plate
        for (int x = 0; x < 16; x++) {
            im.setRGB(ox + x, 14, STEEL_DARK);
            im.setRGB(ox + x, 15, OUTLINE);
        }
        // 4 sharp triangular spikes
        int[] spikeX = {1, 5, 9, 13};
        for (int sx : spikeX) {
            im.setRGB(ox + sx + 1, 6, STEEL_HI);
            im.setRGB(ox + sx + 1, 7, STEEL_LIGHT);
            im.setRGB(ox + sx, 8, STEEL_HI);
            im.setRGB(ox + sx + 1, 8, STEEL_LIGHT);
            im.setRGB(ox + sx + 2, 8, STEEL_DARK);
            im.setRGB(ox + sx, 9, STEEL_LIGHT);
            im.setRGB(ox + sx + 1, 9, STEEL_MID);
            im.setRGB(ox + sx + 2, 9, STEEL_DARK);
            for (int y = 10; y <= 13; y++) {
                im.setRGB(ox + sx, y, STEEL_MID);
                im.setRGB(ox + sx + 1, y, STEEL_MID);
                im.setRGB(ox + sx + 2, y, STEEL_DARK);
            }
            // Blood tip on some spikes
            if (sx == 5 || sx == 13) {
                im.setRGB(ox + sx + 1, 6, RED_HEART);
                im.setRGB(ox + sx + 1, 7, RED_DARK);
            }
        }
    }

    private static void drawBackgroundWall(BufferedImage im, int ox) {
        for (int y = 0; y < 16; y++) {
            boolean lower = y >= 8;
            int ry = y % 8;
            for (int x = 0; x < 16; x++) {
                int vx = lower ? ((x == 3 || x == 11) ? 1 : 0) : ((x == 7 || x == 15) ? 1 : 0);
                if (ry == 7 || vx == 1) im.setRGB(ox + x, y, 0xFF0e1017);
                else if (ry == 0) im.setRGB(ox + x, y, 0xFF242736);
                else im.setRGB(ox + x, y, 0xFF181b24);
            }
        }
    }

    private static void drawBackgroundPillar(BufferedImage im, int ox) {
        drawBackgroundWall(im, ox);
        for (int y = 0; y < 16; y++) {
            for (int x = 3; x <= 12; x++) {
                if (x == 3 || x == 12) im.setRGB(ox + x, y, 0xFF0c0d12);
                else if (x == 4) im.setRGB(ox + x, y, 0xFF353b4d);
                else if (x == 11) im.setRGB(ox + x, y, 0xFF1e212b);
                else im.setRGB(ox + x, y, 0xFF282d3b);
            }
        }
    }

    private static void drawPortalTile(BufferedImage im, int ox) {
        // Glowing magic portal doorway
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (x < 2 || x > 13 || y < 2) {
                    im.setRGB(ox + x, y, STONE_DARK);
                } else {
                    int dist = Math.abs(x - 8) + Math.abs(y - 8);
                    int c = CYAN_DARK;
                    if (dist <= 2) c = PURE_WHITE;
                    else if (dist <= 4) c = CYAN_HI;
                    else if (dist <= 6) c = CYAN_LIGHT;
                    else if (dist <= 8) c = CYAN_MID;
                    im.setRGB(ox + x, y, c);
                }
            }
        }
    }

    private static void drawBossGate(BufferedImage im, int ox) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (x < 3 || x > 12 || y < 3) im.setRGB(ox + x, y, OUTLINE);
                else {
                    boolean bar = (x == 5 || x == 7 || x == 9 || x == 11);
                    im.setRGB(ox + x, y, bar ? STEEL_DARK : 0xFF220810);
                    if (y == 8 && (x >= 4 && x <= 11)) im.setRGB(ox + x, y, RED_DARK);
                }
            }
        }
    }

    // =========================================================================
    // 2. WALL TORCH (64x16, 4 frames of 16x16)
    // =========================================================================
    static BufferedImage buildTorch() {
        BufferedImage im = new BufferedImage(64, 16, BufferedImage.TYPE_INT_ARGB);
        int[] flameHeights = {7, 8, 7, 6};
        int[] sway = {0, 1, 0, -1};

        for (int f = 0; f < 4; f++) {
            int ox = f * 16;
            // Sconce bracket
            im.setRGB(ox + 7, 14, OUTLINE);
            im.setRGB(ox + 8, 14, OUTLINE);
            im.setRGB(ox + 7, 13, STEEL_DARK);
            im.setRGB(ox + 8, 13, STEEL_MID);
            im.setRGB(ox + 6, 12, STEEL_DARK);
            im.setRGB(ox + 7, 12, STEEL_LIGHT);
            im.setRGB(ox + 8, 12, STEEL_MID);
            im.setRGB(ox + 9, 12, STEEL_DARK);

            // Metal bowl
            for (int x = 5; x <= 10; x++) im.setRGB(ox + x, 11, OUTLINE);
            for (int x = 6; x <= 9; x++) im.setRGB(ox + x, 10, STEEL_MID);
            im.setRGB(ox + 6, 10, STEEL_LIGHT);

            // Animated flame
            int h = flameHeights[f];
            int sw = sway[f];
            for (int y = 0; y < h; y++) {
                int fy = 9 - y;
                int rad = (h - y) / 2;
                if (rad < 1 && y < h - 1) rad = 1;
                int cx = 7 + (sw * y / h);
                for (int dx = -rad; dx <= rad; dx++) {
                    int c = FIRE_DARK;
                    if (Math.abs(dx) < rad) c = FIRE_MID;
                    if (y < h - 2 && Math.abs(dx) <= rad - 1) c = FIRE_LIGHT;
                    if (y < h / 2 && dx == 0) c = FIRE_HI;
                    im.setRGB(ox + cx + dx, fy, c);
                }
            }
        }
        return im;
    }

    // =========================================================================
    // 3. TREASURE CHEST (72x20, 3 frames of 24x20)
    // Frame 0: Closed, Frame 1: Opening, Frame 2: Open with sparkling loot
    // =========================================================================
    static BufferedImage buildChest() {
        BufferedImage im = new BufferedImage(72, 20, BufferedImage.TYPE_INT_ARGB);

        // Frame 0: Closed
        drawChestBase(im, 0);
        // Lid closed
        drawRect(im, 3, 5, 18, 7, OUTLINE);
        fillRect(im, 4, 6, 16, 5, WOOD_MID);
        // Gold bands & lock
        fillRect(im, 4, 6, 3, 5, GOLD_MID);
        fillRect(im, 17, 6, 3, 5, GOLD_MID);
        fillRect(im, 10, 8, 4, 4, GOLD_HI);
        im.setRGB(11, 10, OUTLINE);
        im.setRGB(12, 10, OUTLINE);

        // Frame 1: Opening
        drawChestBase(im, 24);
        // Angled open lid
        drawRect(im, 24 + 3, 2, 18, 5, OUTLINE);
        fillRect(im, 24 + 4, 3, 16, 3, WOOD_MID);
        fillRect(im, 24 + 4, 3, 3, 3, GOLD_MID);
        fillRect(im, 24 + 17, 3, 3, 3, GOLD_MID);

        // Frame 2: Open with treasure shine
        drawChestBase(im, 48);
        // Open lid shifted up
        drawRect(im, 48 + 3, 1, 18, 4, OUTLINE);
        fillRect(im, 48 + 4, 2, 16, 2, WOOD_MID);
        // Glowing gold inside
        fillRect(im, 48 + 5, 8, 14, 4, GOLD_HI);
        im.setRGB(48 + 8, 9, CYAN_LIGHT); // gem
        im.setRGB(48 + 14, 9, RED_HEART);  // ruby
        im.setRGB(48 + 11, 7, PURE_WHITE); // glint sparkle
        im.setRGB(48 + 12, 6, PURE_WHITE);
        im.setRGB(48 + 10, 6, PURE_WHITE);
        im.setRGB(48 + 11, 5, PURE_WHITE);

        return im;
    }

    private static void drawChestBase(BufferedImage im, int ox) {
        // Bottom container
        drawRect(im, ox + 3, 11, 18, 8, OUTLINE);
        fillRect(im, ox + 4, 12, 16, 6, WOOD_DARK);
        fillRect(im, ox + 4, 12, 3, 6, GOLD_MID);
        fillRect(im, ox + 17, 12, 3, 6, GOLD_MID);
        // Corner highlights
        im.setRGB(ox + 4, 12, GOLD_HI);
        im.setRGB(ox + 17, 12, GOLD_HI);
    }

    // =========================================================================
    // 4. FOOD CART / HAN'S DINER (48x36)
    // Red & white striped awning, wooden stall, lanterns, friendly chef!
    // =========================================================================
    static BufferedImage buildFoodCart() {
        BufferedImage im = new BufferedImage(48, 36, BufferedImage.TYPE_INT_ARGB);

        // Striped Awning (top 10 px)
        for (int x = 2; x <= 45; x++) {
            im.setRGB(x, 2, OUTLINE);
            boolean red = ((x / 4) % 2 == 0);
            int c = red ? RED_HEART : PURE_WHITE;
            for (int y = 3; y <= 9; y++) im.setRGB(x, y, c);
            im.setRGB(x, 10, red ? RED_DARK : STONE_HI);
            im.setRGB(x, 11, OUTLINE);
        }

        // Support pillars
        fillRect(im, 4, 12, 2, 14, WOOD_DARK);
        fillRect(im, 42, 12, 2, 14, WOOD_DARK);

        // Counter desk
        fillRect(im, 2, 24, 44, 11, WOOD_MID);
        drawRect(im, 2, 24, 44, 11, OUTLINE);
        fillRect(im, 3, 25, 42, 2, WOOD_HI); // counter top highlight

        // Hanging sign: "HAN'S"
        fillRect(im, 16, 12, 16, 6, WOOD_LIGHT);
        drawRect(im, 16, 12, 16, 6, OUTLINE);
        // Letters "FOOD" in gold
        im.setRGB(18, 14, GOLD_HI);
        im.setRGB(19, 14, GOLD_HI);
        im.setRGB(21, 14, GOLD_HI);
        im.setRGB(23, 14, GOLD_HI);
        im.setRGB(25, 14, GOLD_HI);

        // Chef NPC behind counter
        // Chef Hat
        fillRect(im, 21, 13, 6, 4, PURE_WHITE);
        drawRect(im, 21, 13, 6, 4, OUTLINE);
        // Face
        fillRect(im, 22, 17, 4, 3, LEATHER_LIGHT);
        im.setRGB(23, 18, OUTLINE); // eye
        im.setRGB(25, 18, OUTLINE); // eye
        im.setRGB(24, 19, RED_DARK); // smile
        // White apron
        fillRect(im, 20, 20, 8, 4, PURE_WHITE);
        im.setRGB(23, 21, RED_HEART); // necktie

        // Food on counter (Steaming Ramen bowl & Meat roast)
        // Bowl
        fillRect(im, 8, 22, 6, 3, CYAN_MID);
        fillRect(im, 9, 21, 4, 1, FIRE_LIGHT); // soup
        im.setRGB(10, 19, PURE_WHITE); // steam
        im.setRGB(11, 18, PURE_WHITE);

        // Meat on bone
        fillRect(im, 34, 22, 5, 3, LEATHER_DARK);
        im.setRGB(33, 23, BONE_HI); // bone tip
        im.setRGB(39, 23, BONE_HI);

        return im;
    }

    // =========================================================================
    // 5. PLAYER HERO (240x24, 10 frames of 24x24)
    // 0..5: Walk, 6: Idle, 7: Jump, 8: Fall, 9: Dash
    // Dungreed hero: Silver helmet, royal blue cape & tunic, leather boots
    // =========================================================================
    static BufferedImage buildPlayer() {
        BufferedImage im = new BufferedImage(240, 24, BufferedImage.TYPE_INT_ARGB);

        int[] legOffsets = {0, 1, 2, 0, -1, -2}; // walk cycle
        int[] bobOffsets = {0, -1, 0, 0, -1, 0};

        // Frames 0..5: Walk
        for (int f = 0; f < 6; f++) {
            drawHeroFrame(im, f * 24, bobOffsets[f], legOffsets[f], false, false);
        }

        // Frame 6: Idle
        drawHeroFrame(im, 6 * 24, 0, 0, false, false);

        // Frame 7: Jump (legs tucked)
        drawHeroFrame(im, 7 * 24, -2, 0, true, false);

        // Frame 8: Fall (arms/cape up)
        drawHeroFrame(im, 8 * 24, 1, 0, false, true);

        // Frame 9: Dash (horizontal blur pose)
        drawHeroDash(im, 9 * 24);

        return im;
    }

    private static void drawHeroFrame(BufferedImage im, int ox, int bob, int leg, boolean jump, boolean fall) {
        int cy = 4 + bob;

        // Cape (behind hero)
        int capeSpread = jump ? 2 : fall ? 4 : 1;
        for (int y = cy + 6; y <= 20; y++) {
            int cx0 = ox + 6 - (y - (cy + 6)) * capeSpread / 8;
            int cx1 = ox + 10;
            for (int x = cx0; x <= cx1; x++) {
                int c = (x == cx0) ? OUTLINE : (x == cx1) ? CAPE_DARK : CAPE_MID;
                im.setRGB(x, y, c);
            }
        }

        // Helmet & Head (8x8)
        drawRect(im, ox + 8, cy, 8, 8, OUTLINE);
        fillRect(im, ox + 9, cy + 1, 6, 6, STEEL_MID);
        fillRect(im, ox + 9, cy + 1, 6, 2, STEEL_LIGHT); // top crown shine
        // Visor slit with glowing blue eye
        fillRect(im, ox + 11, cy + 4, 4, 2, OUTLINE);
        im.setRGB(ox + 12, cy + 4, CYAN_HI);
        im.setRGB(ox + 13, cy + 4, CYAN_LIGHT);
        // Helmet plume / wing crest
        im.setRGB(ox + 8, cy - 1, GOLD_HI);
        im.setRGB(ox + 9, cy - 1, GOLD_MID);
        im.setRGB(ox + 7, cy, GOLD_MID);

        // Torso / Blue Tunic & Gold Brooch
        drawRect(im, ox + 8, cy + 8, 8, 6, OUTLINE);
        fillRect(im, ox + 9, cy + 9, 6, 4, CAPE_LIGHT);
        im.setRGB(ox + 11, cy + 9, GOLD_HI); // brooch
        im.setRGB(ox + 12, cy + 9, GOLD_MID);

        // Belt
        for (int x = ox + 8; x <= ox + 15; x++) im.setRGB(x, cy + 13, LEATHER_DARK);
        im.setRGB(ox + 11, cy + 13, GOLD_HI); // buckle

        // Legs & Boots
        int leftLegY = cy + 14;
        int rightLegY = cy + 14;
        if (jump) {
            // Tucked
            fillRect(im, ox + 8, leftLegY, 3, 3, LEATHER_MID);
            fillRect(im, ox + 13, rightLegY, 3, 3, LEATHER_MID);
        } else {
            // Walking strides
            int lOff = leg;
            int rOff = -leg;
            fillRect(im, ox + 8 + lOff, leftLegY, 3, 5, LEATHER_MID);
            fillRect(im, ox + 13 + rOff, rightLegY, 3, 5, LEATHER_DARK);
            // Boot toes
            im.setRGB(ox + 7 + lOff, leftLegY + 4, OUTLINE);
            im.setRGB(ox + 12 + rOff, rightLegY + 4, OUTLINE);
        }
    }

    private static void drawHeroDash(BufferedImage im, int ox) {
        // Streamlined horizontal dash posture
        // Blue ghost after-trail
        for (int y = 5; y <= 18; y++) {
            im.setRGB(ox + 2, y, CYAN_DARK);
            im.setRGB(ox + 4, y, CYAN_MID);
        }
        // Leaning body
        drawRect(im, ox + 10, 6, 9, 7, OUTLINE);
        fillRect(im, ox + 11, 7, 7, 5, STEEL_LIGHT);
        fillRect(im, ox + 14, 9, 4, 2, OUTLINE); // eye
        im.setRGB(ox + 15, 9, CYAN_HI);

        // Extended cape trailing behind
        for (int x = ox + 3; x <= ox + 10; x++) {
            im.setRGB(x, 10, CAPE_MID);
            im.setRGB(x, 11, CAPE_LIGHT);
            im.setRGB(x, 12, CAPE_DARK);
        }
        // Forward body & outstretched legs
        fillRect(im, ox + 8, 13, 8, 4, CAPE_LIGHT);
        fillRect(im, ox + 6, 16, 9, 3, LEATHER_MID);
        im.setRGB(ox + 17, 17, STEEL_HI);
    }

    // =========================================================================
    // 6. WEAPONS (216x24, 9 weapons of 24x24)
    // 0: Iron Shortsword, 1: Titan Greatsword, 2: Royal Bow,
    // 3: Flintlock Revolver, 4: Staff of Embers, 5: Shadow Daggers,
    // 6: Heavy Crossbow, 7: Glacial Wand, 8: Thunder Halberd
    // =========================================================================
    static BufferedImage buildWeapons() {
        BufferedImage im = new BufferedImage(216, 24, BufferedImage.TYPE_INT_ARGB);

        // Weapon 0: Iron Shortsword (diagonal 45 deg)
        int ox0 = 0;
        // Grip & Pommel
        im.setRGB(ox0 + 5, 18, GOLD_HI);
        im.setRGB(ox0 + 6, 17, LEATHER_DARK);
        im.setRGB(ox0 + 7, 16, LEATHER_DARK);
        // Crossguard
        for (int d = -2; d <= 2; d++) {
            im.setRGB(ox0 + 8 - d, 15 - d, GOLD_MID);
            im.setRGB(ox0 + 8 - d + 1, 15 - d, GOLD_HI);
        }
        // Blade
        for (int i = 0; i < 9; i++) {
            int bx = ox0 + 9 + i, by = 14 - i;
            im.setRGB(bx, by, STEEL_LIGHT);
            im.setRGB(bx + 1, by, STEEL_HI);
            im.setRGB(bx, by + 1, STEEL_MID);
        }
        im.setRGB(ox0 + 19, 4, PURE_WHITE); // tip sparkle

        // Weapon 1: Titan Greatsword (massive broad notched blade)
        int ox1 = 24;
        im.setRGB(ox1 + 3, 20, PURPLE_LIGHT); // pommel gem
        im.setRGB(ox1 + 4, 19, LEATHER_DARK);
        im.setRGB(ox1 + 5, 18, LEATHER_DARK);
        // Heavy guard
        fillRect(im, ox1 + 5, 15, 4, 4, STEEL_DARK);
        fillRect(im, ox1 + 6, 16, 2, 2, GOLD_HI);
        // Broad Blade
        for (int i = 0; i < 11; i++) {
            int bx = ox1 + 7 + i, by = 15 - i;
            im.setRGB(bx - 1, by, STEEL_DARK);
            im.setRGB(bx, by, STEEL_MID);
            im.setRGB(bx + 1, by, STEEL_LIGHT);
            im.setRGB(bx + 2, by, STEEL_HI);
            im.setRGB(bx + 1, by + 1, STEEL_DARK);
            // Red glowing fuller groove
            if (i >= 2 && i <= 8) im.setRGB(bx, by, RED_HEART);
        }
        im.setRGB(ox1 + 19, 3, STEEL_HI);
        im.setRGB(ox1 + 20, 3, PURE_WHITE);

        // Weapon 2: Royal Recurve Bow
        int ox2 = 48;
        // Curved wooden limbs
        int[] bowX = {10, 9, 8, 7, 7, 8, 9, 10, 12, 14, 15};
        int[] bowY = {4,  5, 7, 9, 12, 14, 16, 18, 19, 19, 18};
        for (int k = 0; k < bowX.length; k++) {
            im.setRGB(ox2 + bowX[k], bowY[k], WOOD_LIGHT);
            im.setRGB(ox2 + bowX[k] + 1, bowY[k], WOOD_HI);
        }
        // Bowstring
        for (int y = 4; y <= 18; y++) im.setRGB(ox2 + 15, y, 0xFFe0ded5);
        // Arrow knocked
        for (int x = 6; x <= 18; x++) im.setRGB(ox2 + x, 11, WOOD_DARK);
        im.setRGB(ox2 + 5, 11, STEEL_HI); // arrowhead
        im.setRGB(ox2 + 18, 10, CYAN_LIGHT); // fletching
        im.setRGB(ox2 + 18, 12, CYAN_LIGHT);

        // Weapon 3: Flintlock Revolver
        int ox3 = 72;
        // Walnut handle
        fillRect(im, ox3 + 5, 13, 3, 6, WOOD_MID);
        im.setRGB(ox3 + 5, 18, GOLD_HI);
        // Cylinder
        fillRect(im, ox3 + 8, 11, 4, 5, STEEL_MID);
        im.setRGB(ox3 + 9, 12, STEEL_DARK);
        im.setRGB(ox3 + 10, 14, STEEL_DARK);
        // Long steel barrel
        fillRect(im, ox3 + 12, 11, 8, 3, STEEL_LIGHT);
        fillRect(im, ox3 + 12, 11, 8, 1, STEEL_HI);
        im.setRGB(ox3 + 20, 10, GOLD_MID); // front sight
        // Trigger guard
        im.setRGB(ox3 + 8, 16, STEEL_HI);
        im.setRGB(ox3 + 9, 16, STEEL_HI);

        // Weapon 4: Staff of Embers
        int ox4 = 96;
        // Gnarled wood shaft
        for (int i = 0; i < 14; i++) {
            im.setRGB(ox4 + 4 + i, 19 - i, WOOD_DARK);
            im.setRGB(ox4 + 5 + i, 19 - i, WOOD_MID);
        }
        // Prongs holding fiery crystal
        fillRect(im, ox4 + 16, 4, 5, 5, FIRE_LIGHT);
        fillRect(im, ox4 + 17, 5, 3, 3, FIRE_HI);
        im.setRGB(ox4 + 18, 6, PURE_WHITE);
        im.setRGB(ox4 + 15, 6, GOLD_MID);
        im.setRGB(ox4 + 21, 6, GOLD_MID);
        im.setRGB(ox4 + 18, 3, GOLD_HI);

        // Weapon 5: Shadow Daggers (crossed dual obsidian blades with violet runes)
        int ox5 = 120;
        // Dagger 1 (main)
        for (int i = 0; i < 8; i++) {
            im.setRGB(ox5 + 6 + i, 16 - i, PURPLE_DARK);
            im.setRGB(ox5 + 7 + i, 16 - i, PURPLE_LIGHT);
            im.setRGB(ox5 + 7 + i, 15 - i, PURPLE_HI);
        }
        im.setRGB(ox5 + 15, 7, PURE_WHITE);
        // Dagger 2 (crossing)
        for (int i = 0; i < 6; i++) {
            im.setRGB(ox5 + 7 + i, 9 + i, PURPLE_DARK);
            im.setRGB(ox5 + 8 + i, 9 + i, PURPLE_MID);
        }
        im.setRGB(ox5 + 5, 18, GOLD_HI);
        im.setRGB(ox5 + 14, 16, GOLD_HI);

        // Weapon 6: Heavy Crossbow
        int ox6 = 144;
        // Wooden stock
        fillRect(im, ox6 + 4, 12, 14, 3, WOOD_MID);
        fillRect(im, ox6 + 4, 13, 14, 1, WOOD_DARK);
        // Metal prod / bow crossbar
        for (int y = 5; y <= 20; y++) {
            im.setRGB(ox6 + 14, y, STEEL_MID);
            im.setRGB(ox6 + 15, y, STEEL_LIGHT);
        }
        im.setRGB(ox6 + 13, 5, STEEL_HI);
        im.setRGB(ox6 + 13, 20, STEEL_HI);
        // Heavy bolt loaded
        for (int x = 7; x <= 17; x++) im.setRGB(ox6 + x, 11, STEEL_HI);
        im.setRGB(ox6 + 18, 11, GOLD_HI);

        // Weapon 7: Glacial Wand
        int ox7 = 168;
        // Crystal handle
        for (int i = 0; i < 12; i++) {
            im.setRGB(ox7 + 5 + i, 18 - i, CYAN_DARK);
            im.setRGB(ox7 + 6 + i, 18 - i, CYAN_MID);
        }
        // Jagged Ice Crystal Head
        fillRect(im, ox7 + 15, 3, 6, 6, CYAN_LIGHT);
        fillRect(im, ox7 + 16, 4, 4, 4, CYAN_HI);
        im.setRGB(ox7 + 18, 5, PURE_WHITE);
        im.setRGB(ox7 + 14, 2, CYAN_HI);
        im.setRGB(ox7 + 21, 8, CYAN_HI);

        // Weapon 8: Thunder Halberd
        int ox8 = 192;
        // Long dark shaft
        for (int i = 0; i < 16; i++) {
            im.setRGB(ox8 + 3 + i, 20 - i, STEEL_DARK);
            im.setRGB(ox8 + 4 + i, 20 - i, STEEL_MID);
        }
        // Gold socket & crossbar
        fillRect(im, ox8 + 14, 6, 4, 3, GOLD_MID);
        im.setRGB(ox8 + 15, 5, GOLD_HI);
        // Halberd axe blade + spearhead
        for (int i = 0; i < 5; i++) {
            setPixel(im, ox8 + 18 + i, 4 - i, STEEL_HI);
            setPixel(im, ox8 + 15 + i, 3, GOLD_HI);
            setPixel(im, ox8 + 14, 3 - i, STEEL_LIGHT); // y goes 3..−1; setPixel guards negative y
        }
        im.setRGB(ox8 + 17, 2, PURE_WHITE); // spear tip
        im.setRGB(ox8 + 13, 2, CYAN_HI); // electric spark

        return im;
    }

    // =========================================================================
    // 7. SLASH ANIMATION (128x32, 4 frames of 32x32)
    // Dynamic crescent energy blade slash
    // =========================================================================
    static BufferedImage buildSlash() {
        BufferedImage im = new BufferedImage(128, 32, BufferedImage.TYPE_INT_ARGB);

        // Frame 0: Quick initiation spark
        int ox0 = 0;
        im.setRGB(ox0 + 16, 16, PURE_WHITE);
        im.setRGB(ox0 + 15, 16, CYAN_HI);
        im.setRGB(ox0 + 17, 16, CYAN_HI);
        im.setRGB(ox0 + 16, 15, CYAN_LIGHT);
        im.setRGB(ox0 + 16, 17, CYAN_LIGHT);

        // Frame 1: Full brilliant crescent slash
        int ox1 = 32;
        drawCrescent(im, ox1, 14, CYAN_LIGHT, CYAN_HI, PURE_WHITE);

        // Frame 2: Widened arc breaking
        int ox2 = 64;
        drawCrescent(im, ox2, 16, CYAN_MID, CYAN_LIGHT, CYAN_HI);
        // Spark particles
        im.setRGB(ox2 + 28, 8, PURE_WHITE);
        im.setRGB(ox2 + 29, 24, PURE_WHITE);
        im.setRGB(ox2 + 24, 4, CYAN_HI);

        // Frame 3: Dissipating sparkles
        int ox3 = 96;
        im.setRGB(ox3 + 24, 10, CYAN_LIGHT);
        im.setRGB(ox3 + 27, 16, PURE_WHITE);
        im.setRGB(ox3 + 23, 22, CYAN_LIGHT);

        return im;
    }

    private static void drawCrescent(BufferedImage im, int ox, int radius, int cEdge, int cMid, int cCore) {
        int cx = ox + 14, cy = 16;
        for (int deg = -65; deg <= 65; deg += 4) {
            double rad = Math.toRadians(deg);
            int rx = cx + (int) (Math.cos(rad) * radius);
            int ry = cy + (int) (Math.sin(rad) * radius);
            if (rx >= ox && rx < ox + 32 && ry >= 0 && ry < 32) {
                im.setRGB(rx, ry, cCore);
                if (rx + 1 < ox + 32) im.setRGB(rx + 1, ry, cMid);
                if (rx - 1 >= ox) im.setRGB(rx - 1, ry, cMid);
                if (ry + 1 < 32) im.setRGB(rx, ry + 1, cEdge);
            }
        }
    }

    // =========================================================================
    // 8. PROJECTILES (64x16, 4 frames of 16x16)
    // 0: Arrow, 1: Bullet, 2: Fireball, 3: Boss Dark Magic Orb
    // =========================================================================
    static BufferedImage buildProjectiles() {
        BufferedImage im = new BufferedImage(64, 16, BufferedImage.TYPE_INT_ARGB);

        // 0: Arrow
        int ox0 = 0;
        for (int x = 2; x <= 13; x++) im.setRGB(ox0 + x, 8, WOOD_DARK);
        // Head
        im.setRGB(ox0 + 13, 7, STEEL_LIGHT);
        im.setRGB(ox0 + 14, 8, STEEL_HI);
        im.setRGB(ox0 + 13, 9, STEEL_LIGHT);
        // Fletching
        im.setRGB(ox0 + 2, 7, CYAN_LIGHT);
        im.setRGB(ox0 + 3, 7, CYAN_LIGHT);
        im.setRGB(ox0 + 2, 9, CYAN_LIGHT);
        im.setRGB(ox0 + 3, 9, CYAN_LIGHT);

        // 1: Bullet
        int ox1 = 16;
        fillRect(im, ox1 + 4, 7, 8, 3, GOLD_MID);
        fillRect(im, ox1 + 5, 7, 7, 2, GOLD_LIGHT);
        fillRect(im, ox1 + 8, 7, 3, 2, GOLD_HI);
        im.setRGB(ox1 + 12, 8, PURE_WHITE);
        im.setRGB(ox1 + 2, 8, FIRE_MID); // spark trail

        // 2: Fireball
        int ox2 = 32;
        fillCircle(im, ox2 + 8, 8, 5, FIRE_DARK);
        fillCircle(im, ox2 + 9, 8, 3, FIRE_MID);
        fillCircle(im, ox2 + 10, 8, 2, FIRE_HI);
        im.setRGB(ox2 + 10, 8, PURE_WHITE);
        im.setRGB(ox2 + 2, 7, FIRE_DARK);
        im.setRGB(ox2 + 3, 9, FIRE_MID);

        // 3: Boss Dark Magic Orb
        int ox3 = 48;
        fillCircle(im, ox3 + 8, 8, 6, OUTLINE);
        fillCircle(im, ox3 + 8, 8, 5, PURPLE_DARK);
        fillCircle(im, ox3 + 8, 8, 3, PURPLE_MID);
        fillCircle(im, ox3 + 8, 8, 2, PURPLE_LIGHT);
        im.setRGB(ox3 + 8, 8, PURPLE_HI);

        return im;
    }

    // =========================================================================
    // 9. PARTICLES (64x8, 8 sprites of 8x8)
    // 0: Dust, 1: Blood, 2: Slime, 3: Coin Sparkle,
    // 4: Ghost trail, 5: Fire spark, 6: Wood chip, 7: Shockwave
    // =========================================================================
    static BufferedImage buildParticles() {
        BufferedImage im = new BufferedImage(64, 8, BufferedImage.TYPE_INT_ARGB);

        // 0: Dust
        fillCircle(im, 4, 4, 3, STONE_LIGHT);
        im.setRGB(4, 4, STONE_HI);

        // 1: Blood
        im.setRGB(8 + 3, 3, RED_HEART);
        im.setRGB(8 + 4, 4, RED_DARK);
        im.setRGB(8 + 3, 4, RED_HEART);

        // 2: Slime
        fillCircle(im, 16 + 4, 4, 2, SLIME_LIGHT);
        im.setRGB(16 + 4, 4, SLIME_HI);

        // 3: Coin sparkle
        im.setRGB(24 + 4, 2, GOLD_HI);
        im.setRGB(24 + 4, 6, GOLD_HI);
        im.setRGB(24 + 2, 4, GOLD_HI);
        im.setRGB(24 + 6, 4, GOLD_HI);
        im.setRGB(24 + 4, 4, PURE_WHITE);

        // 4: Dash ghost particle
        im.setRGB(32 + 3, 3, CYAN_HI);
        im.setRGB(32 + 4, 4, CYAN_LIGHT);
        im.setRGB(32 + 3, 4, CYAN_MID);

        // 5: Fire spark
        im.setRGB(40 + 4, 3, FIRE_HI);
        im.setRGB(40 + 4, 4, FIRE_LIGHT);
        im.setRGB(40 + 3, 4, FIRE_MID);

        // 6: Wood splinter
        im.setRGB(48 + 2, 2, WOOD_HI);
        im.setRGB(48 + 3, 3, WOOD_LIGHT);
        im.setRGB(48 + 4, 4, WOOD_DARK);

        // 7: Shockwave
        for (int y = 2; y <= 6; y++) {
            im.setRGB(56 + 4, y, PURE_WHITE);
            im.setRGB(56 + 3, y, STONE_HI);
            im.setRGB(56 + 5, y, STONE_LIGHT);
        }

        return im;
    }

    // =========================================================================
    // 10. SLIME ENEMY (80x20, 4 frames of 20x20)
    // Cute chunky green blob with angry eyes, squashing & bouncing
    // =========================================================================
    static BufferedImage buildSlime() {
        BufferedImage im = new BufferedImage(80, 20, BufferedImage.TYPE_INT_ARGB);

        // Frame 0: Idle round blob
        int ox0 = 0;
        drawSlimeShape(im, ox0, 3, 13, 14, 6);
        drawSlimeEyes(im, ox0 + 6, 12);

        // Frame 1: Squash (preparing to leap)
        int ox1 = 20;
        drawSlimeShape(im, ox1, 2, 15, 16, 4);
        drawSlimeEyes(im, ox1 + 6, 14);

        // Frame 2: Leap Stretch (airborne apex)
        int ox2 = 40;
        drawSlimeShape(im, ox2, 5, 7, 10, 11);
        drawSlimeEyes(im, ox2 + 6, 8);

        // Frame 3: Landing squish
        int ox3 = 60;
        drawSlimeShape(im, ox3, 3, 12, 14, 7);
        drawSlimeEyes(im, ox3 + 6, 11);

        return im;
    }

    private static void drawSlimeShape(BufferedImage im, int ox, int rx, int ry, int rw, int rh) {
        drawRect(im, ox + rx, ry, rw, rh, OUTLINE);
        fillRect(im, ox + rx + 1, ry + 1, rw - 2, rh - 2, SLIME_MID);
        fillRect(im, ox + rx + 2, ry + 1, rw - 4, 2, SLIME_LIGHT); // top sheen
        im.setRGB(ox + rx + 3, ry + 2, SLIME_HI); // specular highlight
    }

    private static void drawSlimeEyes(BufferedImage im, int ex, int ey) {
        // Angry slanted eyes
        im.setRGB(ex, ey, OUTLINE);
        im.setRGB(ex + 1, ey + 1, OUTLINE);
        im.setRGB(ex + 5, ey, OUTLINE);
        im.setRGB(ex + 4, ey + 1, OUTLINE);
        im.setRGB(ex, ey + 1, PURE_WHITE);
        im.setRGB(ex + 5, ey + 1, PURE_WHITE);
    }

    // =========================================================================
    // 11. CAVERN BAT (96x20, 4 frames of 24x20)
    // Dark violet body, flapping crimson wings, glowing yellow eyes
    // =========================================================================
    static BufferedImage buildBat() {
        BufferedImage im = new BufferedImage(96, 20, BufferedImage.TYPE_INT_ARGB);

        int[][] wingHeights = {
                {4, 5, 7, 9},   // wings up
                {7, 8, 9, 10},  // wings middle
                {10, 11, 12, 13},// wings down
                {8, 8, 9, 10}   // recovering
        };

        for (int f = 0; f < 4; f++) {
            int ox = f * 24;
            int[] wh = wingHeights[f];

            // Left Wing
            for (int i = 0; i < 4; i++) {
                int wx = ox + 3 + i * 2;
                int wy = wh[i];
                for (int y = wy; y <= wy + 4; y++) im.setRGB(wx, y, (y == wy) ? OUTLINE : BAT_WING);
            }
            // Right Wing
            for (int i = 0; i < 4; i++) {
                int wx = ox + 19 - i * 2;
                int wy = wh[i];
                for (int y = wy; y <= wy + 4; y++) im.setRGB(wx, y, (y == wy) ? OUTLINE : BAT_WING);
            }

            // Bat Body (Head & Fangs)
            drawRect(im, ox + 9, 7, 6, 7, OUTLINE);
            fillRect(im, ox + 10, 8, 4, 5, BAT_DARK);
            // Ears
            im.setRGB(ox + 9, 6, BAT_MID);
            im.setRGB(ox + 14, 6, BAT_MID);
            // Glowing yellow eyes
            im.setRGB(ox + 10, 9, GOLD_HI);
            im.setRGB(ox + 13, 9, GOLD_HI);
            // Fangs
            im.setRGB(ox + 11, 12, PURE_WHITE);
            im.setRGB(ox + 12, 12, PURE_WHITE);
        }
        return im;
    }

    // =========================================================================
    // 12. SKELETON WARRIOR (96x24, 4 frames of 24x24)
    // Skull with dark sockets, bone ribs, ragged loincloth, iron sword
    // =========================================================================
    static BufferedImage buildSkeleton() {
        BufferedImage im = new BufferedImage(96, 24, BufferedImage.TYPE_INT_ARGB);

        int[] legShifts = {0, 1, 0, -1};

        for (int f = 0; f < 4; f++) {
            int ox = f * 24;
            int ls = legShifts[f];

            // Skull
            drawRect(im, ox + 9, 3, 6, 6, OUTLINE);
            fillRect(im, ox + 10, 4, 4, 4, BONE_LIGHT);
            im.setRGB(ox + 11, 4, BONE_HI);
            // Eye sockets & teeth
            im.setRGB(ox + 10, 5, OUTLINE);
            im.setRGB(ox + 13, 5, OUTLINE);
            im.setRGB(ox + 11, 7, OUTLINE);
            im.setRGB(ox + 12, 7, OUTLINE);

            // Ribcage Spine
            for (int y = 9; y <= 13; y++) im.setRGB(ox + 11, y, BONE_MID);
            for (int y = 10; y <= 12; y++) {
                im.setRGB(ox + 9, y, BONE_LIGHT);
                im.setRGB(ox + 13, y, BONE_LIGHT);
            }

            // Ragged red loincloth
            fillRect(im, ox + 9, 14, 6, 3, RED_DARK);
            im.setRGB(ox + 11, 15, RED_HEART);

            // Legs
            int lY = 17, rY = 17;
            fillRect(im, ox + 9 + ls, lY, 2, 5, BONE_MID);
            fillRect(im, ox + 13 - ls, rY, 2, 5, BONE_MID);

            // Sword held in right hand
            for (int y = 6; y <= 16; y++) im.setRGB(ox + 17, y, STEEL_LIGHT);
            im.setRGB(ox + 17, 5, STEEL_HI); // tip
            im.setRGB(ox + 16, 14, GOLD_MID); // guard
            im.setRGB(ox + 18, 14, GOLD_MID);
            im.setRGB(ox + 17, 17, WOOD_DARK); // grip
        }
        return im;
    }

    // =========================================================================
    // 13. SKELETON CULTIST MAGE (96x24, 4 frames of 24x24)
    // Dark violet tattered hood, skull face, hovering staff
    // =========================================================================
    static BufferedImage buildMage() {
        BufferedImage im = new BufferedImage(96, 24, BufferedImage.TYPE_INT_ARGB);
        int[] hoverBob = {0, -1, -2, -1};

        for (int f = 0; f < 4; f++) {
            int ox = f * 24;
            int hb = hoverBob[f];

            // Hood
            drawRect(im, ox + 8, 3 + hb, 8, 8, OUTLINE);
            fillRect(im, ox + 9, 4 + hb, 6, 6, PURPLE_MID);
            fillRect(im, ox + 10, 6 + hb, 4, 3, OUTLINE); // dark face void
            // Glowing purple eyes
            im.setRGB(ox + 11, 7 + hb, PURPLE_HI);
            im.setRGB(ox + 13, 7 + hb, PURPLE_HI);

            // Tattered Robe
            for (int y = 11 + hb; y <= 21; y++) {
                int w = (y - (11 + hb)) * 8 / 10 + 6;
                int rx = ox + 12 - w / 2;
                for (int x = rx; x <= rx + w; x++) {
                    im.setRGB(x, y, (x == rx || x == rx + w || y == 21) ? OUTLINE : PURPLE_DARK);
                }
            }

            // Bone Staff with Dark Magic Flame
            for (int y = 5 + hb; y <= 19; y++) im.setRGB(ox + 5, y, BONE_DARK);
            fillCircle(im, ox + 5, 4 + hb, 3, PURPLE_LIGHT);
            im.setRGB(ox + 5, 4 + hb, PURPLE_HI);
        }
        return im;
    }

    // =========================================================================
    // 14. BOSS: BELIAL - DEMON WARDEN (192x48, 4 frames of 48x48)
    // Colossal demonic boss: giant horned iron helm, glowing red visor,
    // muscular plate armor with glowing red runes, giant dual-bladed cleaver!
    // =========================================================================
    static BufferedImage buildBoss() {
        BufferedImage im = new BufferedImage(192, 48, BufferedImage.TYPE_INT_ARGB);

        int[] breathBob = {0, -1, -2, -1};

        for (int f = 0; f < 4; f++) {
            int ox = f * 48;
            int bob = breathBob[f];
            int cy = 8 + bob;

            // Giant Horns curving upward
            // Left Horn
            im.setRGB(ox + 17, cy - 4, BONE_HI);
            im.setRGB(ox + 16, cy - 3, BONE_LIGHT);
            im.setRGB(ox + 16, cy - 2, BONE_MID);
            im.setRGB(ox + 17, cy - 1, BONE_DARK);
            // Right Horn
            im.setRGB(ox + 30, cy - 4, BONE_HI);
            im.setRGB(ox + 31, cy - 3, BONE_LIGHT);
            im.setRGB(ox + 31, cy - 2, BONE_MID);
            im.setRGB(ox + 30, cy - 1, BONE_DARK);

            // Armored Head & Glowing Visor
            drawRect(im, ox + 18, cy, 12, 12, OUTLINE);
            fillRect(im, ox + 19, cy + 1, 10, 10, STEEL_DARK);
            fillRect(im, ox + 19, cy + 1, 10, 3, STEEL_MID);
            // Menacing Red Visor Slit
            fillRect(im, ox + 20, cy + 5, 8, 2, RED_HEART);
            im.setRGB(ox + 22, cy + 5, PURE_WHITE);
            im.setRGB(ox + 25, cy + 5, PURE_WHITE);

            // Broad Shoulders & Spiked Pauldrons
            fillRect(im, ox + 11, cy + 11, 7, 7, STEEL_MID);
            drawRect(im, ox + 11, cy + 11, 7, 7, OUTLINE);
            im.setRGB(ox + 13, cy + 10, BONE_HI); // spike

            fillRect(im, ox + 30, cy + 11, 7, 7, STEEL_MID);
            drawRect(im, ox + 30, cy + 11, 7, 7, OUTLINE);
            im.setRGB(ox + 34, cy + 10, BONE_HI); // spike

            // Chestplate with glowing demonic sigil
            fillRect(im, ox + 18, cy + 12, 12, 14, STEEL_DARK);
            drawRect(im, ox + 18, cy + 12, 12, 14, OUTLINE);
            // Red glowing rune on chest
            im.setRGB(ox + 23, cy + 16, RED_HEART);
            im.setRGB(ox + 24, cy + 16, RED_HEART);
            im.setRGB(ox + 23, cy + 17, PURE_WHITE);
            im.setRGB(ox + 24, cy + 17, PURE_WHITE);
            im.setRGB(ox + 21, cy + 19, RED_HEART);
            im.setRGB(ox + 26, cy + 19, RED_HEART);

            // Spiked Belt & Tattered Loincloth
            fillRect(im, ox + 17, cy + 26, 14, 4, LEATHER_DARK);
            fillRect(im, ox + 20, cy + 29, 8, 8, RED_DARK);

            // Heavy Plated Greaves / Legs
            fillRect(im, ox + 15, cy + 32, 6, 8, STEEL_MID);
            drawRect(im, ox + 15, cy + 32, 6, 8, OUTLINE);
            fillRect(im, ox + 27, cy + 32, 6, 8, STEEL_DARK);
            drawRect(im, ox + 27, cy + 32, 6, 8, OUTLINE);

            // Giant Executioner's Cleaver (wielded in right hand)
            if (f < 2) {
                // At rest (leaning down)
                fillRect(im, ox + 37, cy + 6, 6, 30, STEEL_MID);
                drawRect(im, ox + 37, cy + 6, 6, 30, OUTLINE);
                fillRect(im, ox + 38, cy + 7, 2, 28, STEEL_HI); // edge
                im.setRGB(ox + 40, cy + 18, RED_HEART); // blood stain
                im.setRGB(ox + 40, cy + 24, RED_HEART);
            } else {
                // Raised high / windup (frames 2 & 3)
                fillRect(im, ox + 32, cy - 6, 14, 8, STEEL_MID);
                drawRect(im, ox + 32, cy - 6, 14, 8, OUTLINE);
                fillRect(im, ox + 33, cy - 5, 12, 2, STEEL_HI);
            }
        }
        return im;
    }

    // =========================================================================
    // 15. HUD & UI ICONS (160x32)
    // Full Heart, Empty Heart, Dash Wings, Gold Coin, Food Dishes, Hotbar Frame
    // =========================================================================
    static BufferedImage buildHUD() {
        BufferedImage im = new BufferedImage(160, 32, BufferedImage.TYPE_INT_ARGB);

        // 1. Full Heart (12x12 at x=0)
        drawHeart(im, 0, RED_HEART, RED_DARK, PURE_WHITE);

        // 2. Empty Heart (12x12 at x=14)
        drawHeart(im, 14, STONE_DARK, MORTAR, STONE_LIGHT);

        // 3. Dash Wing Active (14x14 at x=28)
        drawDashWing(im, 28, CYAN_HI, CYAN_MID);

        // 4. Dash Wing Empty / Recharging (14x14 at x=44)
        drawDashWing(im, 44, STONE_MID, MORTAR);

        // 5. Gold Coin (10x10 at x=60)
        fillCircle(im, 60 + 5, 6, 4, OUTLINE);
        fillCircle(im, 60 + 5, 6, 3, GOLD_MID);
        fillCircle(im, 60 + 5, 6, 2, GOLD_LIGHT);
        im.setRGB(60 + 4, 5, GOLD_HI);
        im.setRGB(60 + 5, 5, PURE_WHITE);

        // 6. Food Dish 1: Prime Rib Roast (x=74)
        drawRect(im, 74 + 1, 2, 12, 9, OUTLINE);
        fillRect(im, 74 + 2, 3, 10, 7, LEATHER_DARK);
        fillRect(im, 74 + 4, 4, 6, 4, RED_DARK);
        im.setRGB(74 + 1, 6, BONE_HI); // bone handle
        im.setRGB(74 + 12, 6, BONE_HI);

        // 7. Food Dish 2: Steaming Dragon Ramen (x=90)
        fillRect(im, 90 + 2, 5, 10, 6, CYAN_MID);
        drawRect(im, 90 + 2, 5, 10, 6, OUTLINE);
        fillRect(im, 90 + 3, 4, 8, 2, FIRE_LIGHT); // noodles
        im.setRGB(90 + 6, 2, PURE_WHITE); // steam
        im.setRGB(90 + 8, 1, PURE_WHITE);

        // 8. Food Dish 3: Hellfire Stew (x=106)
        fillRect(im, 106 + 2, 5, 10, 6, STONE_DARK);
        drawRect(im, 106 + 2, 5, 10, 6, OUTLINE);
        fillRect(im, 106 + 3, 4, 8, 3, FIRE_DARK);
        im.setRGB(106 + 6, 5, FIRE_HI); // boiling bubble
        im.setRGB(106 + 8, 5, FIRE_LIGHT);

        // 9. Food Dish 4: Honey Pancake (x=122)
        fillRect(im, 122 + 2, 5, 10, 5, GOLD_LIGHT);
        drawRect(im, 122 + 2, 5, 10, 5, OUTLINE);
        im.setRGB(122 + 6, 3, GOLD_HI); // melting butter
        im.setRGB(122 + 7, 3, GOLD_HI);

        // 10. Hotbar Selection Frame (22x22 at x=136, y=2)
        drawRect(im, 136, 2, 22, 22, OUTLINE);
        drawRect(im, 137, 3, 20, 20, GOLD_MID);
        im.setRGB(137, 3, GOLD_HI);
        im.setRGB(156, 3, GOLD_HI);
        im.setRGB(137, 22, GOLD_DARK);
        im.setRGB(156, 22, GOLD_DARK);

        return im;
    }

    private static void drawHeart(BufferedImage im, int ox, int cFill, int cDark, int cHi) {
        int[] rows = {
                0b01100110,
                0b11111111,
                0b11111111,
                0b11111111,
                0b01111110,
                0b00111100,
                0b00011000
        };
        for (int r = 0; r < rows.length; r++) {
            int mask = rows[r];
            for (int bit = 0; bit < 8; bit++) {
                if ((mask & (1 << (7 - bit))) != 0) {
                    int x = ox + 2 + bit;
                    int y = 2 + r;
                    int c = (r == 0 || r >= 4) ? cDark : cFill;
                    if (r == 1 && (bit == 1 || bit == 2)) c = cHi;
                    im.setRGB(x, y, c);
                }
            }
        }
    }

    private static void drawDashWing(BufferedImage im, int ox, int cHi, int cMid) {
        // Wing shape pointing diagonal
        im.setRGB(ox + 2, 2, cHi);
        im.setRGB(ox + 3, 3, cHi);
        fillRect(im, ox + 4, 3, 4, 3, cMid);
        fillRect(im, ox + 6, 6, 4, 3, cMid);
        fillRect(im, ox + 8, 9, 3, 2, cHi);
        im.setRGB(ox + 10, 11, cHi);
    }

    // =========================================================================
    // 16. CROSSHAIR (16x16)
    // Precision reticle with corner brackets and center dot
    // =========================================================================
    static BufferedImage buildCrosshair() {
        BufferedImage im = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int cx = 7, cy = 7;

        // Center dot
        im.setRGB(cx, cy, PURE_WHITE);
        im.setRGB(cx + 1, cy, OUTLINE);
        im.setRGB(cx - 1, cy, OUTLINE);
        im.setRGB(cx, cy + 1, OUTLINE);
        im.setRGB(cx, cy - 1, OUTLINE);

        // 4 corner brackets
        int[][] brackets = {
                {cx - 4, cy - 4, 1, 0, 0, 1}, // top-left
                {cx + 4, cy - 4, -1, 0, 0, 1}, // top-right
                {cx - 4, cy + 4, 1, 0, 0, -1}, // bottom-left
                {cx + 4, cy + 4, -1, 0, 0, -1} // bottom-right
        };
        for (int[] b : brackets) {
            int bx = b[0], by = b[1], dx = b[2], dy = b[3];
            im.setRGB(bx, by, RED_HEART);
            im.setRGB(bx + dx * 2, by, RED_HEART);
            im.setRGB(bx, by + dy * 2, RED_HEART);
        }

        return im;
    }

    // =========================================================================
    // Pixel Drawing Utilities
    // =========================================================================
    static void fillRect(BufferedImage im, int x, int y, int w, int h, int color) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int px = x + dx, py = y + dy;
                if (px >= 0 && px < im.getWidth() && py >= 0 && py < im.getHeight()) {
                    im.setRGB(px, py, color);
                }
            }
        }
    }

    static void drawRect(BufferedImage im, int x, int y, int w, int h, int color) {
        for (int dx = 0; dx < w; dx++) {
            setPixel(im, x + dx, y, color);
            setPixel(im, x + dx, y + h - 1, color);
        }
        for (int dy = 0; dy < h; dy++) {
            setPixel(im, x, y + dy, color);
            setPixel(im, x + w - 1, y + dy, color);
        }
    }

    static void fillCircle(BufferedImage im, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    setPixel(im, cx + dx, cy + dy, color);
                }
            }
        }
    }

    static void setPixel(BufferedImage im, int x, int y, int color) {
        if (x >= 0 && x < im.getWidth() && y >= 0 && y < im.getHeight()) {
            im.setRGB(x, y, color);
        }
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : "assets");
        generateAll(root);
        System.out.println("All Dungreed assets generated successfully in " + root.toAbsolutePath());
    }
}
