package world;

import combat.Weapon;
import config.GameConfig;
import entities.Chest;
import entities.Enemy;
import entities.FoodCart;
import entities.NPC;
import entities.Tier2BossEnemy;
import entities.TrainingDummy;
import rendering.Animation;
import rendering.Renderer;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A dungeon/world chamber supporting JSON biomes, dual-layer parallax backgrounds,
 * autotiled terrain, modular buildings, dynamic lighting, ambient particles,
 * NPCs, TrainingDummy, chests, shops, and fullness progression triggers.
 */
public final class Room {
    public enum Type {
        START, COMBAT_SLIMES, COMBAT_SKELETONS, TREASURE, FOOD_SHOP, BOSS,
        COMBAT_CATACOMBS, CHASM_PLATFORMING, TIER2_BOSS
    }

    private static final class AmbientDot {
        double x, y, vx, vy, phase;
        int size;
    }

    private final int id;
    private final Type type;
    private final String title;
    private final int tier;
    private final String biomeId;
    private final SpriteManager sprites;
    private final TileMap map;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();
    private final List<NPC> npcs = new ArrayList<>();
    private final List<Animation> torches = new ArrayList<>();
    private final List<int[]> torchCoords = new ArrayList<>();
    private final List<AmbientDot> ambientDots = new ArrayList<>();

    private Chest chest;
    private FoodCart foodCart;
    private TrainingDummy trainingDummy;

    private boolean cleared;
    private double clearBannerTimer;
    private boolean leftDoorOpen;
    private boolean rightDoorOpen;

    private boolean fullnessTicked;
    private boolean pendingClearFullness;

    public Room(int id, Type type, String title, SpriteManager sprites, Renderer renderer, int cols, int rows, int tier, String biomeId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.tier = tier;
        this.biomeId = biomeId;
        this.sprites = sprites;
        this.map = new TileMap(sprites, cols, rows);

        GameConfig.BiomeDef biome = sprites.getConfig().getBiome(biomeId);
        if (biome != null) {
            this.map.setTileset(biome.tilesetId);
        }

        buildRoomLayout(sprites, renderer);
        initAmbientParticles();
    }

    private boolean isOutdoorBiome() {
        return "medieval_village".equals(biomeId) || "enchanted_forest".equals(biomeId) || "ruined_kingdom".equals(biomeId);
    }

    private void buildRoomLayout(SpriteManager sprites, Renderer renderer) {
        int cols = map.getCols();
        int rows = map.getRows();
        boolean outdoor = isOutdoorBiome();

        // 1. Background walls / pillars (leave upper sky open in outdoor biomes so parallax & buildings shine!)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (outdoor) {
                    if (r >= rows - 2) {
                        map.setBackground(c, r, TileMap.BG_WALL);
                    }
                } else {
                    if (c == 6 || c == cols - 7 || c == cols / 2) {
                        map.setBackground(c, r, TileMap.BG_PILLAR);
                    } else {
                        map.setBackground(c, r, TileMap.BG_WALL);
                    }
                }
            }
        }

        // 2. Outer boundary walls & floor
        for (int c = 0; c < cols; c++) {
            if (!outdoor) {
                map.setForeground(c, 0, TileMap.WALL);
                map.setForeground(c, 1, TileMap.WALL);
            }
            map.setForeground(c, rows - 2, TileMap.FLOOR);
            map.setForeground(c, rows - 1, TileMap.COBBLE);
        }
        for (int r = 0; r < rows; r++) {
            map.setForeground(0, r, TileMap.WALL);
            map.setForeground(1, r, TileMap.WALL);
            map.setForeground(cols - 1, r, TileMap.WALL);
            map.setForeground(cols - 2, r, TileMap.WALL);
        }

        // 3. Doors (Left & Right Portals)
        if (id > 0) {
            map.setForeground(1, rows - 3, TileMap.PORTAL);
            map.setForeground(1, rows - 4, TileMap.PORTAL);
        }
        if (type != Type.BOSS && type != Type.TIER2_BOSS) {
            map.setForeground(cols - 2, rows - 3, TileMap.PORTAL);
            map.setForeground(cols - 2, rows - 4, TileMap.PORTAL);
        }

        int floorPixelY = (rows - 2) * Constants.TILE;

        // 4. Room-specific layout, modular buildings, decorations, NPCs & enemies
        if (type == Type.START) {
            cleared = true;
            leftDoorOpen = false;
            rightDoorOpen = (tier != 1); // Tier 1 Room 0 opens once tutorial finishes!
            fullnessTicked = true;       // Never drain fullness in starting room

            if (tier == 1) {
                // Modular Tavern / Training Outpost + Gareth + Training Dummy
                addBuilding("tavern", 4, rows - 2);
                npcs.add(NPC.createGareth(sprites, 14 * Constants.TILE, floorPixelY - 18));
                trainingDummy = new TrainingDummy(sprites, 24 * Constants.TILE, floorPixelY - 18);

                // Elevated wooden training platform for jumping practice
                for (int c = 17; c <= 22; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);
                map.setDecoration(3, rows - 3, TileMap.DECOR_BUSH);
                map.setDecoration(29, rows - 3, TileMap.DECOR_SIGN);
                map.setDecoration(31, rows - 3, TileMap.DECOR_BUSH);
                addTorch(sprites, 32, 11);
            } else {
                // Tier 2 Start: Ruined Keep + Scout Lyra
                addBuilding("ruined_keep", 5, rows - 2);
                npcs.add(NPC.createLyra(sprites, 14 * Constants.TILE, floorPixelY - 18));
                for (int c = 20; c <= 26; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);
                map.setDecoration(3, rows - 3, TileMap.DECOR_CRYSTAL_A);
                map.setDecoration(29, rows - 3, TileMap.DECOR_BUSH);
                addTorch(sprites, 18, 10);
                addTorch(sprites, cols - 7, 10);
            }
        } else if (type == Type.COMBAT_SLIMES) {
            // Enchanted Forest Courtyard with Cottage & floating platforms
            addBuilding("residential_house", 26, rows - 2);
            for (int c = 6; c <= 12; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);
            for (int c = 15; c <= 22; c++) map.setForeground(c, 8, TileMap.WOOD_PLATFORM);

            map.setDecoration(4, rows - 3, TileMap.DECOR_BUSH);
            map.setDecoration(14, rows - 3, TileMap.DECOR_CRYSTAL_A);
            map.setDecoration(23, rows - 3, TileMap.DECOR_BUSH);
            addTorch(sprites, 10, 8);
            addTorch(sprites, 24, 8);

            enemies.add(new entities.SlimeEnemy(sprites, 130, 180));
            enemies.add(new entities.SlimeEnemy(sprites, 270, 180));
            enemies.add(new entities.SlimeEnemy(sprites, 430, 180));
            enemies.add(new entities.BatEnemy(sprites, 190, 80));
            enemies.add(new entities.BatEnemy(sprites, 350, 80));
        } else if (type == Type.COMBAT_SKELETONS) {
            // Underground Dungeon Guardpost
            for (int c = 6; c <= 13; c++) map.setForeground(c, 9, TileMap.FLOOR);
            for (int c = cols - 14; c <= cols - 7; c++) map.setForeground(c, 9, TileMap.FLOOR);
            for (int c = 16; c <= 22; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);
            for (int c = 17; c <= 21; c++) map.setForeground(c, rows - 3, TileMap.SPIKES);

            for (int c = 8; c <= 28; c += 10) {
                map.setDecoration(c, 2, TileMap.DECOR_CHAIN_TOP);
                map.setDecoration(c, 3, TileMap.DECOR_CHAIN_END);
            }
            map.setDecoration(9, 8, TileMap.DECOR_CRATE);
            addTorch(sprites, 8, 5);
            addTorch(sprites, cols - 9, 5);

            enemies.add(new entities.SkeletonEnemy(sprites, 140, 100));
            enemies.add(new entities.SkeletonEnemy(sprites, 380, 100));
            enemies.add(new entities.MageEnemy(sprites, 280, 120));
            enemies.add(new entities.SlimeEnemy(sprites, 220, 180));
        } else if (type == Type.COMBAT_CATACOMBS) {
            // Dark Caverns Crypt
            for (int c = 5; c <= 12; c++) map.setForeground(c, 10, TileMap.FLOOR);
            for (int c = cols - 13; c <= cols - 6; c++) map.setForeground(c, 10, TileMap.FLOOR);
            for (int c = 15; c <= 21; c++) map.setForeground(c, 7, TileMap.WOOD_PLATFORM);

            map.setDecoration(7, rows - 3, TileMap.DECOR_CRYSTAL_A);
            map.setDecoration(cols - 8, rows - 3, TileMap.DECOR_CRYSTAL_B);
            map.setDecoration(18, 2, TileMap.DECOR_CHAIN_TOP);
            map.setDecoration(18, 3, TileMap.DECOR_CHAIN_END);
            addTorch(sprites, 8, 6);
            addTorch(sprites, cols - 9, 6);
            addTorch(sprites, cols / 2, 4);

            enemies.add(new entities.SkeletonEnemy(sprites, 120, 100));
            enemies.add(new entities.SkeletonEnemy(sprites, 400, 100));
            enemies.add(new entities.MageEnemy(sprites, 260, 80));
            enemies.add(new entities.BatEnemy(sprites, 180, 60));
            enemies.add(new entities.BatEnemy(sprites, 340, 60));
        } else if (type == Type.CHASM_PLATFORMING) {
            // Dark Caverns Spiked Abyss requiring double-jump & air dash
            for (int c = 10; c <= cols - 11; c++) {
                map.setForeground(c, rows - 3, TileMap.SPIKES);
            }
            for (int c = 10; c <= 13; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);
            for (int c = 17; c <= 21; c++) map.setForeground(c, 8, TileMap.WOOD_PLATFORM);
            for (int c = 25; c <= 28; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);

            map.setDecoration(7, rows - 3, TileMap.DECOR_CRYSTAL_B);
            map.setDecoration(cols - 8, rows - 3, TileMap.DECOR_CRYSTAL_A);
            addTorch(sprites, 6, 7);
            addTorch(sprites, cols - 7, 7);

            enemies.add(new entities.BatEnemy(sprites, 200, 70));
            enemies.add(new entities.BatEnemy(sprites, 360, 70));
            enemies.add(new entities.MageEnemy(sprites, 290, 80));
        } else if (type == Type.TREASURE) {
            cleared = true;
            leftDoorOpen = true;
            rightDoorOpen = true;

            for (int c = 14; c <= 24; c++) map.setForeground(c, 11, TileMap.FLOOR);
            map.setDecoration(15, 10, TileMap.DECOR_CRATE);
            map.setDecoration(23, 10, TileMap.DECOR_CRYSTAL_A);
            addTorch(sprites, 12, 7);
            addTorch(sprites, 26, 7);

            Weapon secretWeapon = (tier == 2)
                    ? ((Math.random() < 0.5) ? Weapon.createThunderHalberd() : Weapon.createFrostWand())
                    : ((Math.random() < 0.5) ? Weapon.createGreatsword() : Weapon.createFireStaff());
            chest = new Chest(sprites, 18 * Constants.TILE, 9 * Constants.TILE, secretWeapon);
        } else if (type == Type.FOOD_SHOP) {
            cleared = true;
            leftDoorOpen = true;
            rightDoorOpen = true;
            fullnessTicked = true; // Entering shop should not consume fullness before buying food

            // Town Bazaar featuring Medieval Shop + Blacksmith + Chef Han + Forger Vorn
            addBuilding("medieval_shop", 5, rows - 2);
            addBuilding("blacksmith", 23, rows - 2);
            foodCart = new FoodCart(sprites, 14 * Constants.TILE, floorPixelY - 36);
            npcs.add(NPC.createVorn(sprites, 26 * Constants.TILE, floorPixelY - 18));

            map.setDecoration(3, rows - 3, TileMap.DECOR_BUSH);
            map.setDecoration(31, rows - 3, TileMap.DECOR_CRATE);
            addTorch(sprites, 12, 9);
            addTorch(sprites, 21, 9);
        } else if (type == Type.BOSS) {
            for (int c = 5; c <= 11; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);
            for (int c = cols - 12; c <= cols - 6; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);

            map.setDecoration(4, rows - 3, TileMap.DECOR_CRATE);
            map.setDecoration(cols - 5, rows - 3, TileMap.DECOR_CRATE);
            addTorch(sprites, 6, 6);
            addTorch(sprites, cols - 7, 6);
            addTorch(sprites, cols / 2, 5);

            enemies.add(new entities.BossEnemy(sprites, renderer, 22 * Constants.TILE, 8 * Constants.TILE));
        } else if (type == Type.TIER2_BOSS) {
            for (int c = 6; c <= 12; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);
            for (int c = cols - 13; c <= cols - 7; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);
            for (int c = cols / 2 - 4; c <= cols / 2 + 4; c++) map.setForeground(c, 7, TileMap.WOOD_PLATFORM);

            map.setDecoration(4, rows - 3, TileMap.DECOR_CRYSTAL_B);
            map.setDecoration(cols - 5, rows - 3, TileMap.DECOR_CRYSTAL_A);
            addTorch(sprites, 6, 6);
            addTorch(sprites, cols - 7, 6);
            addTorch(sprites, cols / 2, 4);

            enemies.add(new Tier2BossEnemy(sprites, renderer, 23 * Constants.TILE, 7 * Constants.TILE));
        }
    }

    private void addBuilding(String templateId, int baseCol, int floorRow) {
        GameConfig.BuildingTemplate tpl = sprites.getConfig().getBuildingTemplate(templateId);
        if (tpl == null) return;
        Building b = new Building(tpl, baseCol, floorRow);
        b.registerPlatforms(map, sprites.getConfig());
        buildings.add(b);
    }

    private void addTorch(SpriteManager sprites, int col, int row) {
        Animation torch = new Animation(sprites.sprite("torch"), 0.12, true);
        torch.update(Math.random());
        torches.add(torch);
        torchCoords.add(new int[]{col, row});
    }

    private void initAmbientParticles() {
        Random r = new Random(id * 97L + tier * 31L);
        int w = map.getPixelWidth();
        int h = map.getPixelHeight();
        for (int i = 0; i < 20; i++) {
            AmbientDot d = new AmbientDot();
            d.x = r.nextDouble() * w;
            d.y = r.nextDouble() * (h - 32);
            d.vx = (r.nextDouble() - 0.5) * 14;
            d.vy = -4 - r.nextDouble() * 10;
            d.phase = r.nextDouble() * Math.PI * 2;
            d.size = r.nextBoolean() ? 2 : 1;
            ambientDots.add(d);
        }
    }

    public void unlockTutorialDoor() {
        if (type == Type.START && !rightDoorOpen) {
            rightDoorOpen = true;
        }
    }

    /** Consumes the one-time fullness drain when clearing a combat/boss room. */
    public boolean pollClearFullnessTrigger() {
        if (pendingClearFullness && !fullnessTicked) {
            pendingClearFullness = false;
            fullnessTicked = true;
            return true;
        }
        return false;
    }

    /** Consumes the one-time fullness drain when entering a non-combat, non-shop room (e.g. Treasure). */
    public boolean consumeFirstEntryFullnessTrigger() {
        if (cleared && !fullnessTicked) {
            fullnessTicked = true;
            return true;
        }
        return false;
    }

    public void update(double dt) {
        for (Animation a : torches) a.update(dt);
        if (chest != null) chest.update(dt);
        if (trainingDummy != null) trainingDummy.update(dt);

        // Update ambient floating biome particles
        int maxW = map.getPixelWidth();
        int maxH = map.getPixelHeight() - 32;
        for (AmbientDot d : ambientDots) {
            d.phase += dt * 3.0;
            d.x += (d.vx + Math.sin(d.phase) * 8.0) * dt;
            d.y += d.vy * dt;
            if (d.y < 8) d.y = maxH;
            if (d.x < 16) d.x = maxW - 24;
            if (d.x > maxW - 16) d.x = 24;
        }

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
                if (!fullnessTicked) {
                    pendingClearFullness = true;
                }
                audio.SoundManager.play(audio.SoundManager.Sound.ROOM_CLEAR);
            }
        } else if (clearBannerTimer > 0) {
            clearBannerTimer -= dt;
        }
    }

    public void render(Graphics2D g, int camX, int camY, double playerX, double playerY) {
        GameConfig.BiomeDef biome = sprites.getConfig().getBiome(biomeId);

        // 1. Sky + Seamless Dual-Layer Parallax Background
        renderParallax(g, biome, camX);

        // 2. Room Background Tiles & Pillars
        map.renderBackground(g, camX, camY);

        // 3. Modular Buildings (behind characters, balconies act as walkable platforms)
        for (Building b : buildings) {
            b.render(g, sprites, camX, camY);
        }

        // 4. Torches
        int t = Constants.TILE;
        for (int i = 0; i < torches.size(); i++) {
            int[] pos = torchCoords.get(i);
            g.drawImage(torches.get(i).frame(), pos[0] * t - camX, pos[1] * t - camY, null);
        }

        // 5. Food Stall, Chest, Training Dummy & NPCs
        if (foodCart != null) foodCart.render(g, camX, camY, playerX, playerY);
        if (chest != null) chest.render(g, camX, camY, playerX, playerY);
        if (trainingDummy != null) trainingDummy.render(g, camX, camY);
        for (NPC npc : npcs) {
            npc.update(Constants.DT, playerX);
            npc.render(g, camX, camY);
        }

        // 6. Foreground Tiles (with Autotiling) & Environmental Decorations
        map.renderForeground(g, camX, camY);
        map.renderDecorations(g, camX, camY);

        // 7. Ambient Lighting Halos & Atmospheric Particles
        renderAtmosphere(g, biome, camX, camY);
    }

    private void renderParallax(Graphics2D g, GameConfig.BiomeDef biome, int camX) {
        Color sky = (biome != null) ? biome.skyColor : new Color(0x0e1017);
        g.setColor(sky);
        g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);

        if (biome == null) return;
        BufferedImage sheet = sprites.get(biome.backgroundFile);
        if (sheet.getWidth() < Constants.VIEW_W || sheet.getHeight() < Constants.VIEW_H * 2) return;

        BufferedImage farLayer = sprites.subimage(biome.backgroundFile, 0, 0, Constants.VIEW_W, Constants.VIEW_H);
        BufferedImage nearLayer = sprites.subimage(biome.backgroundFile, 0, Constants.VIEW_H, Constants.VIEW_W, Constants.VIEW_H);

        int farOff = -((int) Math.round(camX * biome.parallaxFar) % Constants.VIEW_W);
        if (farOff > 0) farOff -= Constants.VIEW_W;
        g.drawImage(farLayer, farOff, 0, null);
        g.drawImage(farLayer, farOff + Constants.VIEW_W, 0, null);

        int nearOff = -((int) Math.round(camX * biome.parallaxNear) % Constants.VIEW_W);
        if (nearOff > 0) nearOff -= Constants.VIEW_W;
        g.drawImage(nearLayer, nearOff, 0, null);
        g.drawImage(nearLayer, nearOff + Constants.VIEW_W, 0, null);
    }

    private void renderAtmosphere(Graphics2D g, GameConfig.BiomeDef biome, int camX, int camY) {
        // Subtle ambient overlay
        if (biome != null && biome.ambientAlpha > 0) {
            g.setColor(new Color(biome.ambientColor.getRed(), biome.ambientColor.getGreen(),
                    biome.ambientColor.getBlue(), biome.ambientAlpha));
            g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);
        }

        // Soft glowing point-lights around torches & building lamps/windows
        Color lightCol = (biome != null) ? biome.torchColor : new Color(0xffa63b);
        Color outerGlow = new Color(lightCol.getRed(), lightCol.getGreen(), lightCol.getBlue(), 24);
        Color innerGlow = new Color(lightCol.getRed(), lightCol.getGreen(), lightCol.getBlue(), 38);

        int t = Constants.TILE;
        for (int[] pos : torchCoords) {
            int lx = pos[0] * t + 8 - camX;
            int ly = pos[1] * t + 6 - camY;
            g.setColor(outerGlow);
            g.fillOval(lx - 26, ly - 26, 52, 52);
            g.setColor(innerGlow);
            g.fillOval(lx - 13, ly - 13, 26, 26);
        }
        for (Building b : buildings) {
            for (Point pt : b.getLightSources(sprites.getConfig())) {
                int lx = pt.x - camX;
                int ly = pt.y - camY;
                g.setColor(outerGlow);
                g.fillOval(lx - 20, ly - 20, 40, 40);
            }
        }

        // Ambient floating dust / fireflies / spores / leaves
        Color dotCol = new Color(lightCol.getRed(), lightCol.getGreen(), lightCol.getBlue(), 150);
        g.setColor(dotCol);
        for (AmbientDot d : ambientDots) {
            int dx = (int) Math.round(d.x - camX);
            int dy = (int) Math.round(d.y - camY);
            if (dx >= 0 && dx < Constants.VIEW_W && dy >= 0 && dy < Constants.VIEW_H) {
                g.fillRect(dx, dy, d.size, d.size);
            }
        }
    }

    public boolean isClearBannerActive() { return clearBannerTimer > 0; }

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
    public int getTier() { return tier; }
    public String getBiomeId() { return biomeId; }
    public TileMap getMap() { return map; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<NPC> getNpcs() { return npcs; }
    public List<Building> getBuildings() { return buildings; }
    public TrainingDummy getTrainingDummy() { return trainingDummy; }
    public Chest getChest() { return chest; }
    public FoodCart getFoodCart() { return foodCart; }
    public boolean isCleared() { return cleared; }
    public boolean isLeftDoorOpen() { return leftDoorOpen; }
    public boolean isRightDoorOpen() { return rightDoorOpen; }
}
