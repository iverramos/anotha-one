package tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static tools.AssetGenerator.*;

/**
 * Generates high-craft 16-bit pixel-art assets for:
 * - 6 Biome Tilesets (256x32 each: autotile surfaces, corners, variations, hazards, props)
 * - Modular Medieval Architecture & Interior Props (inspired by reference town & dungeon art)
 * - Dual-layer Seamless Parallax Backgrounds (Town, Forest, Dungeon)
 * - NPCs (Gareth, Lyra, Vorn), Training Dummy, and Malakor Tier-2 Boss
 */
public final class WorldAssetGenerator {
    private WorldAssetGenerator() {}

    // Additional Biome Palettes
    private static final int PLASTER_HI   = 0xFFf0e8d8;
    private static final int PLASTER_MID  = 0xFFd6cbb8;
    private static final int PLASTER_DARK = 0xFFa89b87;

    private static final int SLATE_HI     = 0xFF5d6882;
    private static final int SLATE_MID    = 0xFF3e465c;
    private static final int SLATE_DARK   = 0xFF282d3d;

    private static final int GRASS_HI     = 0xFF8ae04a;
    private static final int GRASS_MID    = 0xFF4cb83b;
    private static final int GRASS_DARK   = 0xFF2a7a29;

    private static final int DIRT_HI      = 0xFF9c653b;
    private static final int DIRT_MID     = 0xFF704224;
    private static final int DIRT_DARK    = 0xFF4a2815;

    private static final int VOID_DARK    = 0xFF1a102b;
    private static final int VOID_MID     = 0xFF311f52;
    private static final int VOID_LIGHT   = 0xFF66389c;

    private static final int SAND_HI      = 0xFFc2b59b;
    private static final int SAND_MID     = 0xFF8f836c;
    private static final int SAND_DARK    = 0xFF5e5545;

    public static void generateWorldAssets(Path root) throws IOException {
        // 6 Biome Tilesets (256x32 = 16x2 grid of 16x16 tiles)
        write(buildBiomeTileset(BiomeStyle.VILLAGE),     root.resolve("tilesets/village/tileset.png"));
        write(buildBiomeTileset(BiomeStyle.FOREST),      root.resolve("tilesets/forest/tileset.png"));
        write(buildBiomeTileset(BiomeStyle.DUNGEON),     root.resolve("tilesets/dungeon/tileset.png"));
        write(buildBiomeTileset(BiomeStyle.CASTLE),      root.resolve("tilesets/castle/tileset.png"));
        write(buildBiomeTileset(BiomeStyle.UNDERGROUND), root.resolve("tilesets/underground/tileset.png"));
        write(buildBiomeTileset(BiomeStyle.RUINS),       root.resolve("tilesets/ruins/tileset.png"));

        // Modular Buildings & Interior Furniture
        write(buildArchitectureSheet(), root.resolve("buildings/medieval/architecture.png"));
        write(buildInteriorProps(),     root.resolve("buildings/interiors/props.png"));

        // Seamless Dual-Layer Parallax Backgrounds (480x540: top=far, bottom=near)
        write(buildTownParallax(),    root.resolve("backgrounds/town/parallax.png"));
        write(buildForestParallax(),  root.resolve("backgrounds/forest/parallax.png"));
        write(buildDungeonParallax(), root.resolve("backgrounds/dungeon/parallax.png"));

        // Characters, NPCs, Training Dummy & Tier 2 Boss
        write(buildNPCs(),          root.resolve("sprites/npcs/npcs.png"));
        write(buildTrainingDummy(), root.resolve("sprites/enemies/dummy.png"));
        write(buildMalakorBoss(),   root.resolve("sprites/bosses/malakor.png"));
    }

    private static void write(BufferedImage img, Path p) throws IOException {
        ImageIO.write(img, "png", p.toFile());
    }

    private enum BiomeStyle {
        VILLAGE, FOREST, DUNGEON, CASTLE, UNDERGROUND, RUINS
    }

    // =========================================================================
    // 1. BIOME TILESETS (256x32, standardized 32-slot layout)
    // =========================================================================
    private static BufferedImage buildBiomeTileset(BiomeStyle style) {
        BufferedImage im = new BufferedImage(256, 32, BufferedImage.TYPE_INT_ARGB);

        int topHi, topMid, topDark, wallHi, wallMid, wallDark, mortar, bgWall, bgDark, accent;
        switch (style) {
            case VILLAGE:
                topHi = STONE_HI; topMid = STONE_LIGHT; topDark = STONE_MID;
                wallHi = STONE_LIGHT; wallMid = STONE_MID; wallDark = STONE_DARK; mortar = MORTAR;
                bgWall = 0xFF3a2e28; bgDark = 0xFF261d19; accent = GRASS_MID;
                break;
            case FOREST:
                topHi = GRASS_HI; topMid = GRASS_MID; topDark = GRASS_DARK;
                wallHi = DIRT_HI; wallMid = DIRT_MID; wallDark = DIRT_DARK; mortar = 0xFF2b160c;
                bgWall = 0xFF1b3022; bgDark = 0xFF101f15; accent = GOLD_HI;
                break;
            case CASTLE:
                topHi = STEEL_HI; topMid = STEEL_LIGHT; topDark = STEEL_MID;
                wallHi = SLATE_HI; wallMid = SLATE_MID; wallDark = SLATE_DARK; mortar = 0xFF151824;
                bgWall = 0xFF22273b; bgDark = 0xFF161926; accent = GOLD_LIGHT;
                break;
            case UNDERGROUND:
                topHi = CYAN_LIGHT; topMid = CYAN_MID; topDark = CYAN_DARK;
                wallHi = 0xFF34475c; wallMid = 0xFF233040; wallDark = 0xFF151d29; mortar = 0xFF0b0f17;
                bgWall = 0xFF121824; bgDark = 0xFF0a0d14; accent = PURPLE_LIGHT;
                break;
            case RUINS:
                topHi = SAND_HI; topMid = SAND_MID; topDark = SAND_DARK;
                wallHi = SAND_MID; wallMid = SAND_DARK; wallDark = 0xFF3e382e; mortar = 0xFF1c1914;
                bgWall = 0xFF2a2521; bgDark = 0xFF1a1714; accent = SLIME_MID;
                break;
            case DUNGEON:
            default:
                topHi = STONE_HI; topMid = STONE_MID; topDark = STONE_DARK;
                wallHi = STONE_LIGHT; wallMid = STONE_MID; wallDark = STONE_DARK; mortar = MORTAR;
                bgWall = 0xFF181b24; bgDark = 0xFF0e1017; accent = SLIME_LIGHT;
                break;
        }

        // Tile 0: Wall Brick (Var A)
        drawBrickTile(im, 0, 0, wallHi, wallMid, wallDark, mortar, false, accent, 0);
        // Tile 14: Wall Brick (Var B - cracked/weathered)
        drawBrickTile(im, 14 * 16, 0, wallHi, wallMid, wallDark, mortar, false, accent, 1);

        // Tile 1, 10, 11: Top Surface Floor Variations (0=clean, 1=speckled/tufts, 2=cracked/pebbles)
        drawSurfaceFloorTile(im, 1 * 16, 0, topHi, topMid, topDark, wallMid, wallDark, mortar, accent, 0, false, false);
        drawSurfaceFloorTile(im, 10 * 16, 0, topHi, topMid, topDark, wallMid, wallDark, mortar, accent, 1, false, false);
        drawSurfaceFloorTile(im, 11 * 16, 0, topHi, topMid, topDark, wallMid, wallDark, mortar, accent, 2, false, false);

        // Tile 12, 13: Exposed Left & Right Corner Surface Floors
        drawSurfaceFloorTile(im, 12 * 16, 0, topHi, topMid, topDark, wallMid, wallDark, mortar, accent, 0, true, false);
        drawSurfaceFloorTile(im, 13 * 16, 0, topHi, topMid, topDark, wallMid, wallDark, mortar, accent, 0, false, true);

        // Tile 2 & 15: Cobble / Earth Subsurface (Var A & B)
        drawSubsurfaceTile(im, 2 * 16, 0, wallHi, wallMid, wallDark, mortar, 0);
        drawSubsurfaceTile(im, 15 * 16, 0, wallHi, wallMid, wallDark, mortar, 1);

        // Tile 3: One-Way Wooden Platform
        drawBiomePlatform(im, 3 * 16, 0, style);

        // Tile 4: Hazard Spikes / Thorns
        drawBiomeSpikes(im, 4 * 16, 0, style, accent);

        // Tile 5: Background Wall
        drawBgTile(im, 5 * 16, 0, bgWall, bgDark);

        // Tile 6: Background Pillar
        drawBgPillarTile(im, 6 * 16, 0, bgWall, bgDark, wallHi, wallMid, wallDark);

        // Tile 7: Portal Archway
        drawBiomePortal(im, 7 * 16, 0, wallDark, style == BiomeStyle.UNDERGROUND ? PURPLE_LIGHT : CYAN_LIGHT,
                style == BiomeStyle.UNDERGROUND ? PURPLE_HI : CYAN_HI);

        // Tile 8: Mossy / Crystal Overgrown Brick
        drawBrickTile(im, 8 * 16, 0, wallHi, wallMid, wallDark, mortar, true, accent, 2);

        // Tile 9: Boss Gate
        drawBossGateTile(im, 9 * 16, 0);

        // Row 1 (16..23): Biome Decorations (drawn on top of terrain)
        drawBiomeDecorations(im, style, accent);

        return im;
    }

    private static void drawBrickTile(BufferedImage im, int ox, int oy, int hi, int mid, int dark, int mortar,
                                      boolean overgrown, int accent, int var) {
        for (int y = 0; y < 16; y++) {
            boolean lower = y >= 8;
            int ry = y % 8;
            for (int x = 0; x < 16; x++) {
                int vx = lower ? ((x == 3 || x == 11) ? 1 : 0) : ((x == 7 || x == 15) ? 1 : 0);
                int c;
                if (ry == 7 || vx == 1) c = mortar;
                else if (ry == 0) c = hi;
                else if (ry == 6) c = dark;
                else {
                    c = (((x * 7 + y * 13 + var * 5) % 9) == 0) ? hi : mid;
                    if (var == 1 && (x == 5 && y >= 2 && y <= 5 || x == 10 && y >= 10 && y <= 13)) c = mortar;
                }
                if (overgrown && (y > 10 || (x > 3 && x < 10 && y < 4))) {
                    if ((x + y) % 3 != 0) c = accent;
                }
                im.setRGB(ox + x, oy + y, c);
            }
        }
    }

    private static void drawSurfaceFloorTile(BufferedImage im, int ox, int oy,
                                             int topHi, int topMid, int topDark,
                                             int subMid, int subDark, int mortar, int accent,
                                             int var, boolean leftCorner, boolean rightCorner) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int c;
                if (y == 0) {
                    c = OUTLINE;
                } else if (y == 1) {
                    c = topHi;
                } else if (y == 2) {
                    c = topMid;
                } else if (y == 3) {
                    c = ((x + var) % 3 == 0) ? topMid : topDark;
                } else if (y == 4) {
                    c = ((x * 3 + y + var) % 4 == 0) ? topDark : subMid;
                } else if (y == 15) {
                    c = mortar;
                } else {
                    boolean seam = (x == 5 || x == 12) && (y > 5);
                    c = seam ? subDark : (((x + y + var) % 5 == 0) ? topDark : subMid);
                }
                if (leftCorner && x == 0) c = OUTLINE;
                if (leftCorner && x == 1 && y > 0) c = topHi;
                if (rightCorner && x == 15) c = OUTLINE;
                if (rightCorner && x == 14 && y > 0) c = topDark;

                im.setRGB(ox + x, oy + y, c);
            }
        }
        if (var == 1) {
            im.setRGB(ox + 3, 2, accent);
            im.setRGB(ox + 4, 1, accent);
            im.setRGB(ox + 11, 2, accent);
        } else if (var == 2) {
            im.setRGB(ox + 7, 2, topHi);
            im.setRGB(ox + 8, 3, OUTLINE);
            im.setRGB(ox + 2, 10, subDark);
        }
    }

    private static void drawSubsurfaceTile(BufferedImage im, int ox, int oy, int hi, int mid, int dark, int mortar, int var) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                im.setRGB(ox + x, oy + y, mortar);
            }
        }
        int[][] stones = (var == 0)
                ? new int[][]{{1, 1, 6, 6}, {8, 1, 7, 5}, {1, 8, 7, 7}, {9, 7, 6, 8}}
                : new int[][]{{1, 1, 8, 5}, {10, 1, 5, 7}, {1, 7, 6, 8}, {8, 9, 7, 6}};
        for (int[] s : stones) {
            for (int dy = 0; dy < s[3]; dy++) {
                for (int dx = 0; dx < s[2]; dx++) {
                    int c = mid;
                    if (dx == 0 || dy == 0) c = hi;
                    else if (dx == s[2] - 1 || dy == s[3] - 1) c = dark;
                    im.setRGB(ox + s[0] + dx, oy + s[1] + dy, c);
                }
            }
        }
    }

    private static void drawBiomePlatform(BufferedImage im, int ox, int oy, BiomeStyle style) {
        int hi = (style == BiomeStyle.CASTLE) ? STEEL_HI : WOOD_HI;
        int mid = (style == BiomeStyle.CASTLE) ? STEEL_MID : WOOD_MID;
        int dark = (style == BiomeStyle.CASTLE) ? STEEL_DARK : WOOD_DARK;
        for (int x = 0; x < 16; x++) {
            im.setRGB(ox + x, oy, OUTLINE);
            im.setRGB(ox + x, oy + 1, hi);
            im.setRGB(ox + x, oy + 2, (style == BiomeStyle.CASTLE) ? GOLD_LIGHT : WOOD_LIGHT);
            im.setRGB(ox + x, oy + 3, mid);
            im.setRGB(ox + x, oy + 4, dark);
            im.setRGB(ox + x, oy + 5, OUTLINE);
        }
        for (int bx : new int[]{1, 7, 13}) {
            im.setRGB(ox + bx, oy + 1, STEEL_HI);
            im.setRGB(ox + bx + 1, oy + 1, STEEL_LIGHT);
            im.setRGB(ox + bx, oy + 2, STEEL_LIGHT);
            im.setRGB(ox + bx + 1, oy + 2, STEEL_DARK);
            im.setRGB(ox + bx, oy + 3, STEEL_DARK);
            im.setRGB(ox + bx + 1, oy + 3, STEEL_DARK);
        }
    }

    private static void drawBiomeSpikes(BufferedImage im, int ox, int oy, BiomeStyle style, int accent) {
        for (int x = 0; x < 16; x++) {
            im.setRGB(ox + x, oy + 14, STEEL_DARK);
            im.setRGB(ox + x, oy + 15, OUTLINE);
        }
        int tipColor = (style == BiomeStyle.UNDERGROUND) ? CYAN_HI : (style == BiomeStyle.FOREST) ? GRASS_HI : RED_HEART;
        int bladeHi = (style == BiomeStyle.UNDERGROUND) ? CYAN_LIGHT : STEEL_HI;
        int bladeMid = (style == BiomeStyle.UNDERGROUND) ? CYAN_MID : STEEL_MID;
        for (int sx : new int[]{1, 5, 9, 13}) {
            im.setRGB(ox + sx + 1, oy + 6, bladeHi);
            im.setRGB(ox + sx + 1, oy + 7, bladeMid);
            im.setRGB(ox + sx, oy + 8, bladeHi);
            im.setRGB(ox + sx + 1, oy + 8, bladeMid);
            im.setRGB(ox + sx + 2, oy + 8, STEEL_DARK);
            for (int y = 9; y <= 13; y++) {
                im.setRGB(ox + sx, oy + y, bladeMid);
                im.setRGB(ox + sx + 1, oy + y, bladeMid);
                im.setRGB(ox + sx + 2, oy + y, STEEL_DARK);
            }
            if (sx == 5 || sx == 13) im.setRGB(ox + sx + 1, oy + 6, tipColor);
        }
    }

    private static void drawBgTile(BufferedImage im, int ox, int oy, int bgWall, int bgDark) {
        int bgHi = adjustBrightness(bgWall, 1.25);
        for (int y = 0; y < 16; y++) {
            boolean lower = y >= 8;
            int ry = y % 8;
            for (int x = 0; x < 16; x++) {
                int vx = lower ? ((x == 3 || x == 11) ? 1 : 0) : ((x == 7 || x == 15) ? 1 : 0);
                if (ry == 7 || vx == 1) im.setRGB(ox + x, oy + y, bgDark);
                else if (ry == 0) im.setRGB(ox + x, oy + y, bgHi);
                else im.setRGB(ox + x, oy + y, bgWall);
            }
        }
    }

    private static void drawBgPillarTile(BufferedImage im, int ox, int oy, int bgWall, int bgDark, int hi, int mid, int dark) {
        drawBgTile(im, ox, oy, bgWall, bgDark);
        for (int y = 0; y < 16; y++) {
            for (int x = 3; x <= 12; x++) {
                int c;
                if (x == 3 || x == 12) c = OUTLINE;
                else if (x == 4 || x == 5) c = adjustBrightness(mid, 0.85);
                else if (x == 11) c = adjustBrightness(dark, 0.75);
                else c = adjustBrightness(dark, 0.9);
                im.setRGB(ox + x, oy + y, c);
            }
        }
    }

    private static void drawBiomePortal(BufferedImage im, int ox, int oy, int frameColor, int glowMid, int glowHi) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (x < 2 || x > 13 || y < 2) {
                    im.setRGB(ox + x, oy + y, frameColor);
                } else {
                    int dist = Math.abs(x - 8) + Math.abs(y - 8);
                    int c = adjustBrightness(glowMid, 0.5);
                    if (dist <= 2) c = PURE_WHITE;
                    else if (dist <= 4) c = glowHi;
                    else if (dist <= 7) c = glowMid;
                    im.setRGB(ox + x, oy + y, c);
                }
            }
        }
    }

    private static void drawBossGateTile(BufferedImage im, int ox, int oy) {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (x < 3 || x > 12 || y < 3) im.setRGB(ox + x, oy + y, OUTLINE);
                else {
                    boolean bar = (x == 5 || x == 7 || x == 9 || x == 11);
                    im.setRGB(ox + x, oy + y, bar ? STEEL_DARK : 0xFF220810);
                    if (y == 8 && (x >= 4 && x <= 11)) im.setRGB(ox + x, oy + y, RED_DARK);
                }
            }
        }
    }

    private static void drawBiomeDecorations(BufferedImage im, BiomeStyle style, int accent) {
        int oy = 16;
        // 16: Bush / Moss / Rubble cluster
        int ox16 = 0;
        int bushCol = (style == BiomeStyle.VILLAGE || style == BiomeStyle.FOREST || style == BiomeStyle.RUINS)
                ? GRASS_MID : STONE_MID;
        int bushHi = (style == BiomeStyle.VILLAGE || style == BiomeStyle.FOREST || style == BiomeStyle.RUINS)
                ? GRASS_HI : STONE_HI;
        fillCircle(im, ox16 + 5, oy + 12, 4, OUTLINE);
        fillCircle(im, ox16 + 10, oy + 12, 4, OUTLINE);
        fillCircle(im, ox16 + 7, oy + 9, 4, OUTLINE);
        fillCircle(im, ox16 + 5, oy + 12, 3, bushCol);
        fillCircle(im, ox16 + 10, oy + 12, 3, bushCol);
        fillCircle(im, ox16 + 7, oy + 9, 3, bushHi);

        // 17: Crate prop
        int ox17 = 16;
        drawRect(im, ox17 + 2, oy + 3, 12, 13, OUTLINE);
        fillRect(im, ox17 + 3, oy + 4, 10, 11, WOOD_MID);
        fillRect(im, ox17 + 3, oy + 4, 10, 2, WOOD_HI);
        for (int i = 0; i < 8; i++) im.setRGB(ox17 + 4 + i, oy + 5 + i, WOOD_DARK);

        // 18 & 19: Hanging Chain / Vine
        for (int y = 0; y < 16; y++) {
            int c = (style == BiomeStyle.FOREST || style == BiomeStyle.RUINS)
                    ? ((y % 2 == 0) ? GRASS_MID : GRASS_DARK)
                    : ((y % 3 == 0) ? STEEL_HI : STEEL_DARK);
            im.setRGB(2 * 16 + 7, oy + y, c);
            im.setRGB(2 * 16 + 8, oy + y, OUTLINE);
            if (y < 10) {
                im.setRGB(3 * 16 + 7, oy + y, c);
                im.setRGB(3 * 16 + 8, oy + y, OUTLINE);
            }
        }

        // 20 & 21: Glowing Crystal / Mushroom / Flower cluster
        int ox20 = 4 * 16;
        int ox21 = 5 * 16;
        int glowA = (style == BiomeStyle.UNDERGROUND) ? CYAN_HI : (style == BiomeStyle.FOREST) ? GOLD_HI : FIRE_LIGHT;
        int glowB = (style == BiomeStyle.UNDERGROUND) ? PURPLE_LIGHT : (style == BiomeStyle.FOREST) ? CYAN_HI : RED_HEART;
        // Cluster A
        fillRect(im, ox20 + 6, oy + 8, 4, 8, accent);
        fillRect(im, ox20 + 7, oy + 6, 2, 10, glowA);
        im.setRGB(ox20 + 7, oy + 5, PURE_WHITE);
        // Cluster B
        fillRect(im, ox21 + 4, oy + 10, 3, 6, glowB);
        fillRect(im, ox21 + 9, oy + 8, 3, 8, glowA);
        im.setRGB(ox21 + 10, oy + 7, PURE_WHITE);

        // 22: Signpost / Monument
        int ox22 = 6 * 16;
        fillRect(im, ox22 + 7, oy + 8, 2, 8, WOOD_DARK);
        drawRect(im, ox22 + 2, oy + 3, 12, 7, OUTLINE);
        fillRect(im, ox22 + 3, oy + 4, 10, 5, WOOD_LIGHT);
        fillRect(im, ox22 + 5, oy + 6, 6, 1, GOLD_HI);
    }

    // =========================================================================
    // 2. MODULAR MEDIEVAL ARCHITECTURE SHEET (128x64)
    // =========================================================================
    private static BufferedImage buildArchitectureSheet() {
        BufferedImage im = new BufferedImage(128, 64, BufferedImage.TYPE_INT_ARGB);

        // (0,0) stone_wall
        drawBrickTile(im, 0, 0, STONE_HI, STONE_MID, STONE_DARK, MORTAR, false, 0, 0);

        // (16,0) timber_wall (plaster + oak cross framing like Ref 1!)
        fillRect(im, 16, 0, 16, 16, PLASTER_MID);
        fillRect(im, 16, 0, 16, 2, WOOD_MID);
        fillRect(im, 16, 14, 16, 2, WOOD_DARK);
        fillRect(im, 16, 0, 2, 16, WOOD_MID);
        fillRect(im, 30, 0, 2, 16, WOOD_DARK);
        for (int i = 0; i < 16; i++) {
            im.setRGB(16 + i, i, WOOD_DARK);
            if (i + 1 < 16) im.setRGB(16 + i + 1, i, WOOD_MID);
        }

        // (32,0) interior_wall (warm timber paneling)
        fillRect(im, 32, 0, 16, 16, 0xFF332118);
        for (int x = 32; x < 48; x += 4) fillRect(im, x, 0, 1, 16, 0xFF21140e);
        fillRect(im, 32, 11, 16, 5, 0xFF472d1e);

        // (48,0), (64,0), (80,0) roof_slate_left, mid, right
        for (int i = 0; i < 3; i++) {
            int rx = 48 + i * 16;
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    if (i == 0 && x + y < 10) continue;
                    if (i == 2 && (15 - x) + y < 10) continue;
                    int c = (y % 4 == 0) ? SLATE_DARK : ((x + y) % 5 == 0 ? SLATE_HI : SLATE_MID);
                    if (y == 15) c = OUTLINE;
                    if (y == 14) c = SLATE_HI;
                    im.setRGB(rx + x, y, c);
                }
            }
        }

        // (96,0) roof_peak
        for (int y = 0; y < 16; y++) {
            int half = y / 2 + 1;
            for (int x = 8 - half; x <= 8 + half; x++) {
                im.setRGB(96 + x, y, (x == 8 - half || x == 8 + half) ? WOOD_DARK : SLATE_MID);
            }
        }

        // (112,0) dormer_window
        fillRect(im, 112, 2, 16, 14, SLATE_MID);
        drawRect(im, 115, 4, 10, 12, OUTLINE);
        fillRect(im, 116, 5, 8, 10, WOOD_MID);
        fillRect(im, 117, 6, 6, 7, FIRE_HI); // warm lit window
        fillRect(im, 119, 6, 2, 7, WOOD_DARK); // mullion

        // (0,16) shop_window
        fillRect(im, 0, 16, 16, 16, PLASTER_MID);
        drawRect(im, 2, 18, 12, 12, OUTLINE);
        fillRect(im, 3, 19, 10, 10, 0xFFffdc7a);
        fillRect(im, 7, 19, 2, 10, WOOD_DARK);
        fillRect(im, 3, 23, 10, 2, WOOD_DARK);
        fillRect(im, 1, 29, 14, 2, STONE_HI);

        // (16,16) wooden_door (16x32)
        fillRect(im, 16, 16, 16, 32, STONE_MID);
        drawRect(im, 18, 20, 12, 28, OUTLINE);
        fillRect(im, 19, 21, 10, 26, WOOD_MID);
        for (int x = 21; x <= 27; x += 3) fillRect(im, x, 21, 1, 26, WOOD_DARK);
        fillRect(im, 26, 34, 2, 2, GOLD_HI); // handle
        fillRect(im, 19, 25, 3, 2, STEEL_LIGHT); // hinge

        // (32,16) balcony (walkable deck + railing)
        fillRect(im, 32, 28, 16, 4, WOOD_MID);
        fillRect(im, 32, 28, 16, 1, WOOD_HI);
        fillRect(im, 32, 31, 16, 1, OUTLINE);
        fillRect(im, 32, 19, 16, 2, WOOD_LIGHT);
        for (int x = 33; x < 48; x += 4) fillRect(im, x, 21, 2, 7, WOOD_DARK);

        // (48,16) stairs
        for (int step = 0; step < 4; step++) {
            fillRect(im, 48 + step * 4, 16 + (3 - step) * 4, 4, 3, WOOD_LIGHT);
            fillRect(im, 48 + step * 4, 16 + (3 - step) * 4 + 3, 4, 1, OUTLINE);
        }

        // (64,16) pillar
        fillRect(im, 67, 16, 10, 16, STONE_MID);
        fillRect(im, 66, 16, 1, 16, OUTLINE);
        fillRect(im, 77, 16, 1, 16, OUTLINE);
        fillRect(im, 68, 16, 2, 16, STONE_HI);

        // (80,16) chimney
        drawRect(im, 83, 18, 10, 14, OUTLINE);
        fillRect(im, 84, 19, 8, 12, RED_DARK);
        fillRect(im, 82, 17, 12, 3, STONE_DARK);
        im.setRGB(86, 14, STONE_HI); // smoke wisp
        im.setRGB(88, 12, PURE_WHITE);

        // (96,16) shop_sign
        fillRect(im, 96, 18, 12, 2, STEEL_DARK);
        drawRect(im, 98, 20, 10, 9, OUTLINE);
        fillRect(im, 99, 21, 8, 7, RED_HEART);
        fillRect(im, 101, 23, 4, 3, GOLD_HI);

        // (112,16) blacksmith_sign
        fillRect(im, 112, 18, 12, 2, STEEL_DARK);
        drawRect(im, 114, 20, 10, 9, OUTLINE);
        fillRect(im, 115, 21, 8, 7, STEEL_MID);
        fillRect(im, 117, 23, 4, 3, FIRE_LIGHT);

        // (32,32) street_lamp (16x32)
        fillRect(im, 39, 44, 2, 20, STEEL_DARK);
        fillRect(im, 37, 61, 6, 3, OUTLINE);
        drawRect(im, 36, 36, 8, 8, OUTLINE);
        fillRect(im, 37, 37, 6, 6, FIRE_LIGHT);
        fillRect(im, 38, 38, 4, 4, FIRE_HI);
        fillRect(im, 35, 34, 10, 2, STEEL_DARK);

        // (48,32) banner_red (16x32) & (64,32) banner_blue (16x32)
        drawBanner(im, 48, 32, RED_HEART, RED_DARK, GOLD_HI);
        drawBanner(im, 64, 32, CAPE_LIGHT, CAPE_DARK, GOLD_HI);

        // (80,32) wood_platform
        drawBiomePlatform(im, 80, 32, BiomeStyle.VILLAGE);

        return im;
    }

    private static void drawBanner(BufferedImage im, int ox, int oy, int mainC, int darkC, int trimC) {
        fillRect(im, ox + 2, oy + 2, 12, 2, WOOD_MID);
        for (int y = 4; y < 28; y++) {
            int inset = (y > 22) ? (y - 22) : 0;
            for (int x = 3 + inset; x <= 12 - inset; x++) {
                int c = (x == 3 + inset || x == 12 - inset) ? OUTLINE : (x == 4 || x == 11) ? trimC : mainC;
                if (y > 20 && c == mainC) c = darkC;
                im.setRGB(ox + x, oy + y, c);
            }
        }
        fillRect(im, ox + 6, oy + 11, 4, 4, trimC);
    }

    // =========================================================================
    // 3. INTERIOR PROPS SHEET (128x64)
    // =========================================================================
    private static BufferedImage buildInteriorProps() {
        BufferedImage im = new BufferedImage(128, 64, BufferedImage.TYPE_INT_ARGB);

        // (0,0) bookshelf (16x32)
        drawRect(im, 1, 2, 14, 30, OUTLINE);
        fillRect(im, 2, 3, 12, 28, WOOD_DARK);
        for (int shelf = 0; shelf < 3; shelf++) {
            int sy = 4 + shelf * 9;
            fillRect(im, 2, sy + 6, 12, 2, WOOD_LIGHT);
            int[] bookColors = {RED_HEART, CAPE_LIGHT, GOLD_MID, SLIME_MID, PURPLE_LIGHT};
            for (int b = 0; b < 5; b++) {
                fillRect(im, 3 + b * 2, sy + 1, 2, 5, bookColors[(b + shelf) % bookColors.length]);
            }
        }

        // (16,16) table (32x16)
        drawRect(im, 17, 21, 30, 4, OUTLINE);
        fillRect(im, 18, 22, 28, 2, WOOD_LIGHT);
        fillRect(im, 20, 25, 3, 7, WOOD_DARK);
        fillRect(im, 41, 25, 3, 7, WOOD_DARK);
        // Candle & mug on table
        fillRect(im, 24, 18, 3, 3, GOLD_MID);
        im.setRGB(25, 17, FIRE_HI);
        fillRect(im, 34, 18, 4, 3, STEEL_LIGHT);

        // (48,16) anvil (16x16)
        fillRect(im, 51, 26, 10, 6, WOOD_MID); // stump
        fillRect(im, 50, 21, 12, 5, STEEL_DARK);
        fillRect(im, 49, 20, 14, 3, STEEL_MID);
        fillRect(im, 52, 20, 8, 1, STEEL_HI);

        // (64,16) barrel (16x16)
        drawRect(im, 67, 19, 10, 13, OUTLINE);
        fillRect(im, 68, 20, 8, 11, WOOD_MID);
        fillRect(im, 68, 22, 8, 2, STEEL_DARK);
        fillRect(im, 68, 27, 8, 2, STEEL_DARK);

        // (80,0) candle_stand (16x32)
        fillRect(im, 87, 10, 2, 22, STEEL_DARK);
        fillRect(im, 84, 30, 8, 2, OUTLINE);
        fillRect(im, 83, 10, 10, 2, GOLD_MID);
        for (int cx : new int[]{84, 87, 91}) {
            fillRect(im, cx, 7, 2, 3, PURE_WHITE);
            im.setRGB(cx, 5, FIRE_HI);
            im.setRGB(cx, 6, FIRE_LIGHT);
        }

        return im;
    }

    // =========================================================================
    // 4. SEAMLESS DUAL-LAYER PARALLAX BACKGROUNDS (480x540)
    // Top (0..269): Far Layer   |   Bottom (270..539): Near Layer (transparent sky)
    // =========================================================================
    private static BufferedImage buildTownParallax() {
        BufferedImage im = new BufferedImage(480, 540, BufferedImage.TYPE_INT_ARGB);

        // Far Sky Gradient (0..269)
        for (int y = 0; y < 270; y++) {
            float t = y / 270f;
            int r = (int) (72 + t * 65);
            int g = (int) (136 + t * 68);
            int b = (int) (208 + t * 35);
            int c = 0xFF000000 | (r << 16) | (g << 8) | b;
            for (int x = 0; x < 480; x++) im.setRGB(x, y, c);
        }
        // Pixel Clouds
        for (int cx : new int[]{60, 190, 340, 440}) {
            fillCircle(im, cx, 55, 16, PURE_WHITE);
            fillCircle(im, cx + 16, 52, 20, PURE_WHITE);
            fillCircle(im, cx + 34, 57, 14, 0xFFdce8f5);
        }
        // Far Blue Mountains
        for (int x = 0; x < 480; x++) {
            double h = 155 + Math.sin(x * 0.018) * 30 + Math.cos(x * 0.042) * 14;
            for (int y = (int) h; y < 270; y++) {
                im.setRGB(x, y, (y < h + 4) ? 0xFF7094b8 : 0xFF527296);
            }
        }

        // Near Layer (270..539): Rolling green forest hills & distant rooftops
        for (int x = 0; x < 480; x++) {
            double h = 420 + Math.sin(x * 0.025 + 1.0) * 18 + Math.cos(x * 0.07) * 8;
            for (int y = (int) h; y < 540; y++) {
                im.setRGB(x, y, (y < h + 4) ? GRASS_MID : GRASS_DARK);
            }
        }
        return im;
    }

    private static BufferedImage buildForestParallax() {
        BufferedImage im = new BufferedImage(480, 540, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 270; y++) {
            float t = y / 270f;
            int r = (int) (28 + t * 30);
            int g = (int) (68 + t * 55);
            int b = (int) (74 + t * 40);
            int c = 0xFF000000 | (r << 16) | (g << 8) | b;
            for (int x = 0; x < 480; x++) im.setRGB(x, y, c);
        }
        // Misty Forest Silhouettes (Far)
        for (int tx = 10; tx < 480; tx += 36) {
            fillRect(im, tx, 100, 12, 170, 0xFF1d4234);
            fillCircle(im, tx + 6, 98, 22, 0xFF23523d);
        }
        // Near Ancient Trees (Bottom half, transparent sky)
        for (int tx = 20; tx < 480; tx += 80) {
            fillRect(im, tx, 300, 18, 240, 0xFF142b1c);
            fillRect(im, tx + 2, 300, 4, 240, 0xFF21422c);
            fillCircle(im, tx + 9, 310, 28, 0xFF1b3d26);
        }
        return im;
    }

    private static BufferedImage buildDungeonParallax() {
        BufferedImage im = new BufferedImage(480, 540, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 270; y++) {
            for (int x = 0; x < 480; x++) im.setRGB(x, y, 0xFF0b0d14);
        }
        // Distant arched colonnades
        for (int px = 0; px < 480; px += 80) {
            fillRect(im, px, 60, 16, 210, 0xFF141824);
            fillRect(im, px + 16, 60, 64, 14, 0xFF141824);
        }
        // Near silhouettes & pillars (270..539)
        for (int px = 32; px < 480; px += 120) {
            fillRect(im, px, 290, 22, 250, 0xFF10131d);
            fillRect(im, px + 4, 290, 4, 250, 0xFF191e2e);
        }
        return im;
    }

    // =========================================================================
    // 5. NPCs (72x24: 0=Instructor Gareth, 1=Scout Lyra, 2=Forger Vorn)
    // =========================================================================
    private static BufferedImage buildNPCs() {
        BufferedImage im = new BufferedImage(72, 24, BufferedImage.TYPE_INT_ARGB);

        // NPC 0: Instructor Gareth (Veteran Knight with white beard, gold trim & red scarf)
        drawNpcBase(im, 0, STEEL_MID, STEEL_HI, RED_HEART, GOLD_HI, PURE_WHITE);

        // NPC 1: Scout Lyra (Hooded Emerald Ranger)
        drawNpcBase(im, 24, SLIME_DARK, SLIME_MID, CYAN_LIGHT, LEATHER_MID, GOLD_LIGHT);

        // NPC 2: Master Forger Vorn (Stout Blacksmith with apron & amber goggles)
        drawNpcBase(im, 48, LEATHER_DARK, LEATHER_MID, FIRE_LIGHT, STEEL_HI, FIRE_HI);

        return im;
    }

    private static void drawNpcBase(BufferedImage im, int ox, int bodyDark, int bodyMain, int scarfCol, int trimCol, int hairCol) {
        // Head
        drawRect(im, ox + 8, 4, 8, 8, OUTLINE);
        fillRect(im, ox + 9, 5, 6, 6, LEATHER_LIGHT);
        fillRect(im, ox + 9, 5, 6, 2, hairCol);
        im.setRGB(ox + 11, 8, OUTLINE);
        im.setRGB(ox + 14, 8, OUTLINE);
        // Scarf / Collar
        fillRect(im, ox + 8, 11, 8, 2, scarfCol);
        // Body
        drawRect(im, ox + 7, 12, 10, 7, OUTLINE);
        fillRect(im, ox + 8, 13, 8, 5, bodyMain);
        fillRect(im, ox + 11, 13, 2, 5, trimCol);
        // Boots (bottom pixel at y=22, flush with hitbox!)
        fillRect(im, ox + 8, 19, 3, 4, bodyDark);
        fillRect(im, ox + 13, 19, 3, 4, bodyDark);
    }

    // =========================================================================
    // 6. TRAINING DUMMY (48x24: 0=Idle, 1=Hit Recoil)
    // =========================================================================
    private static BufferedImage buildTrainingDummy() {
        BufferedImage im = new BufferedImage(48, 24, BufferedImage.TYPE_INT_ARGB);
        drawDummyFrame(im, 0, 0);
        drawDummyFrame(im, 24, 2);
        return im;
    }

    private static void drawDummyFrame(BufferedImage im, int ox, int tilt) {
        // Base & Pole
        fillRect(im, ox + 8, 21, 8, 2, WOOD_DARK);
        fillRect(im, ox + 11, 14, 2, 8, WOOD_MID);
        // Crossbar arms
        fillRect(im, ox + 5 + tilt, 10, 14, 2, WOOD_LIGHT);
        // Straw Head
        drawRect(im, ox + 9 + tilt, 3, 6, 6, OUTLINE);
        fillRect(im, ox + 10 + tilt, 4, 4, 4, GOLD_HI);
        // Torso Sack + Painted Red Target
        drawRect(im, ox + 8 + tilt, 9, 8, 9, OUTLINE);
        fillRect(im, ox + 9 + tilt, 10, 6, 7, GOLD_MID);
        fillRect(im, ox + 10 + tilt, 12, 4, 3, RED_HEART);
        im.setRGB(ox + 12 + tilt, 13, PURE_WHITE);
    }

    // =========================================================================
    // 7. TIER 2 BOSS: MALAKOR - VOID ARCHON (192x48, 4 frames of 48x48)
    // =========================================================================
    private static BufferedImage buildMalakorBoss() {
        BufferedImage im = new BufferedImage(192, 48, BufferedImage.TYPE_INT_ARGB);
        for (int f = 0; f < 4; f++) {
            int ox = f * 48;
            int bob = (f == 1) ? -1 : (f == 2) ? -2 : 0;
            // Void Wings
            for (int y = 12 + bob; y < 34 + bob; y++) {
                int span = 12 + (y - 12) / 3;
                im.setRGB(ox + 24 - span, y, PURPLE_LIGHT);
                im.setRGB(ox + 24 + span, y, PURPLE_LIGHT);
                im.setRGB(ox + 24 - span + 1, y, VOID_LIGHT);
                im.setRGB(ox + 24 + span - 1, y, VOID_LIGHT);
            }
            // Armored Torso
            drawRect(im, ox + 14, 16 + bob, 20, 20, OUTLINE);
            fillRect(im, ox + 15, 17 + bob, 18, 18, VOID_MID);
            fillRect(im, ox + 20, 22 + bob, 8, 8, CYAN_MID);
            fillRect(im, ox + 22, 24 + bob, 4, 4, (f == 3) ? PURE_WHITE : CYAN_HI);
            // Crown & Helmet
            drawRect(im, ox + 17, 6 + bob, 14, 10, OUTLINE);
            fillRect(im, ox + 18, 7 + bob, 12, 8, VOID_DARK);
            fillRect(im, ox + 16, 4 + bob, 3, 4, PURPLE_HI);
            fillRect(im, ox + 29, 4 + bob, 3, 4, PURPLE_HI);
            // Glowing Void Eyes
            fillRect(im, ox + 20, 11 + bob, 3, 2, CYAN_HI);
            fillRect(im, ox + 26, 11 + bob, 3, 2, CYAN_HI);
            // Floating Robe Skirt
            fillRect(im, ox + 15, 36 + bob, 18, 8, VOID_DARK);
            fillRect(im, ox + 16, 36 + bob, 16, 2, PURPLE_MID);
        }
        return im;
    }

    private static int adjustBrightness(int argb, double factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(255, Math.max(0, (int) (((argb >> 16) & 0xFF) * factor)));
        int g = Math.min(255, Math.max(0, (int) (((argb >> 8) & 0xFF) * factor)));
        int b = Math.min(255, Math.max(0, (int) ((argb & 0xFF) * factor)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
