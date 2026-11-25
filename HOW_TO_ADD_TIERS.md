# Developer Guide: Adding Tiers, Biomes, Sprites & Modular Buildings

This guide documents Gloamroot / Dungeons Descent's JSON-driven world architecture and explains how to add a **3rd Tier / Level** (e.g., *B3F - The Infernal Core*), new **Biomes**, new **Sprites**, and new **Modular Buildings**.

---

## 1. Core Architecture & JSON Configuration System

All sprites, tilesets, modular buildings, and biome themes are driven by external JSON files in `assets/config/` parsed by `src/config/JsonParser.java` and `src/config/GameConfig.java`:

| File | Purpose |
|---|---|
| `assets/config/sprite_config.json` | Maps sprite IDs (`hero`, `npcs`, `training_dummy`, `weapons`, enemies, bosses) to files, frame dimensions, counts, and animation speeds. |
| `assets/config/tileset_config.json` | Registers tileset PNGs (`village`, `forest`, `dungeon`, `castle`, `underground`, `ruins`), tile dimensions, collision, and tile variations. |
| `assets/config/building_config.json` | Defines modular architectural pieces (roofs, windows, doors, walls, balconies, lamps, banners, furniture) and assembled building templates (`tavern`, `medieval_shop`, `blacksmith`, `residential_house`, `ruined_keep`). |
| `assets/config/biome_config.json` | Defines biomes (`medieval_village`, `enchanted_forest`, `underground_dungeon`, `ancient_castle`, `dark_caverns`, `ruined_kingdom`), their tileset, parallax scroll rates, sky/ambient lighting colors, and ambient particles. |
| `src/rendering/SpriteManager.java` | Caches isolated `BufferedImage` slices (preventing texture bleeding) with missing-texture checkerboard fallbacks. |
| `src/world/Building.java` | Instantiates JSON building templates in rooms, draws layered components, and registers walkable one-way platforms (balconies/rooftops/stairs). |
| `src/world/TileMap.java` | Handles neighbor-aware autotiling, deterministic variations, environmental decorations, and swept-axis collision resolution. |
| `src/world/Room.java` | Constructs rooms with parallax skies, biome tilesets, modular buildings, lighting halos, NPCs, TrainingDummy, chests, shops, and one-time fullness drain triggers. |
| `src/world/Dungeon.java` | Defines floor tiers (`tier = 1`, `tier = 2`, etc.) and room sequences. |
| `src/Game.java` | Orchestrates the state machine (`TITLE`, `SETTINGS`, `CREDITS`, `PLAYING`), `DialogueBox`, `TutorialManager`, 2x pixel-art world rendering + 960x540 crisp UI pass, and tier transitions. |

---

## 2. Step-by-Step Guide: Adding Tier 3 ("B3F - The Infernal Core")

### Step 1: Add a Biome (Optional) in `assets/config/biome_config.json` & `tileset_config.json`
Add a new tileset entry in `tileset_config.json` (using the standardized 16×2 tile grid) and register a new biome (e.g. `"infernal_core"`) in `biome_config.json`.

### Step 2: Add Room Types in `src/world/Room.java`
Add your Tier 3 room types to `Room.Type`:
```java
public enum Type {
    START, COMBAT_SLIMES, COMBAT_SKELETONS, TREASURE, FOOD_SHOP, BOSS,
    COMBAT_CATACOMBS, CHASM_PLATFORMING, TIER2_BOSS,
    COMBAT_MAGMA, LAVA_PLATFORMING, TIER3_BOSS
}
```
Then add `else if (type == Type.COMBAT_MAGMA)` / `LAVA_PLATFORMING` / `TIER3_BOSS` branches in `Room.buildRoomLayout(...)`. You can place modular buildings with `addBuilding("ruined_keep", col, rows - 2)` and NPCs with `npcs.add(...)`.

### Step 3: Add Tier 3 Floor Layout in `src/world/Dungeon.java`
In `Dungeon(SpriteManager sprites, Renderer renderer, int tier)`, add an `else if (tier == 3)` branch assigning each `Room` its `Room.Type` and `biomeId`:
```java
if (tier == 3) {
    floorTitle = "B3F - The Infernal Core";
    rooms.add(new Room(0, Room.Type.START, "B3F - Core Descent", sprites, renderer, cols, rows, 3, "ruined_kingdom"));
    rooms.add(new Room(1, Room.Type.COMBAT_MAGMA, "B3F - Magma Chambers", sprites, renderer, cols, rows, 3, "underground_dungeon"));
    rooms.add(new Room(2, Room.Type.LAVA_PLATFORMING, "B3F - The Molten Chasm", sprites, renderer, 42, rows, 3, "dark_caverns"));
    rooms.add(new Room(3, Room.Type.TREASURE, "B3F - Infernal Vault", sprites, renderer, cols, rows, 3, "ancient_castle"));
    rooms.add(new Room(4, Room.Type.FOOD_SHOP, "B3F - Core Outpost Bazaar", sprites, renderer, cols, rows, 3, "medieval_village"));
    rooms.add(new Room(5, Room.Type.TIER3_BOSS, "B3F - Throne of Vulcanus", sprites, renderer, 48, rows, 3, "underground_dungeon"));
}
```

### Step 4: Create `src/entities/Tier3BossEnemy.java`
Extend `Enemy`, load its frames from `sprites.sprite("boss_vulcanus")` (registered in `sprite_config.json`), implement multi-phase AI in `updateAI(...)`, and override `spawnDeathLoot()`.

### Step 5: Hook Boss Health Bar & Descent in `HUD.java` and `Game.java`
- In `src/ui/HUD.java`: handle `Room.Type.TIER3_BOSS` and `e instanceof Tier3BossEnemy` in `renderBossHealthBar(...)` and `renderMinimap(...)`.
- In `src/Game.java`: set `tier2Clear = true;` when `Tier2BossEnemy` dies, add `descendToTier3()`, and set `victory = true;` when `Tier3BossEnemy` dies.
