# Developer Guide: How to Add Tiers & Levels to Gloamroot

This guide explains how Gloamroot's multi-tier dungeon progression is designed and provides a step-by-step tutorial on how to add a **3rd Tier / Level** (e.g., *B3F - The Infernal Core*) and beyond.

---

## 1. Core Architecture Overview

Gloamroot organizes world progression into **Tiers** (floors). Each tier is managed by a `Dungeon` instance containing interconnected `Room` instances:

```
[Title / Main Menu] -> [B1F: The Upper Prison] -> Boss (Belial)
                             | (Descent)
                       [B2F: Sunken Catacombs] -> Boss (Malakor)
                             | (Descent)
                       [B3F: Future Tier 3]    -> Boss (Tier 3 Boss)
```

### Key Files & Responsibilities:

| File | Purpose |
|---|---|
| `src/world/Dungeon.java` | Manages floor tiers, room lists, room progression index, and visited rooms. |
| `src/world/Room.java` | Defines room types (`Room.Type`), terrain geometry, platforms, torches, enemies, chests, and shops. |
| `src/world/TileMap.java` | Manages 16×16 tile grids, swept-axis collision resolution, one-way platforms, hazards (spikes), and rendering layers. |
| `src/entities/BossEnemy.java` | Tier 1 Boss (*Belial - Demon Warden*), featuring multi-bullet rings, cleave charges, and slam shockwaves. |
| `src/entities/Tier2BossEnemy.java` | Tier 2 Boss (*Malakor - Void Archon*), featuring 5-way spread dark orbs, leaping shockwaves, void nova spirals, and teleport strikes. |
| `src/ui/HUD.java` | Minimap, hearts, dash wings, boss health bars, and weapon hotbars. |
| `src/Game.java` | Game state machine (`TITLE`, `SETTINGS`, `CREDITS`, `PLAYING`), combat updates, collision dispatch, and tier progression hooks. |
| `src/combat/Weapon.java` | Weapon catalog with damage, attack cooldown, range, projectile velocity, price, and rarity. |
| `src/ui/WeaponShop.java` | Interactive armory shop overlay for purchasing and equipping weapons. |

---

## 2. Step-by-Step Guide: Adding Tier 3 ("B3F - The Infernal Core")

Follow these 6 steps to add Tier 3 to the game:

### Step 1: Add New Room Types in `Room.java`

Open `src/world/Room.java` and locate the `Room.Type` enum. Add your Tier 3 room types:

```java
public enum Type {
    START, COMBAT_SLIMES, COMBAT_SKELETONS, TREASURE, FOOD_SHOP, BOSS,
    COMBAT_CATACOMBS, CHASM_PLATFORMING, TIER2_BOSS,
    // --- Tier 3 Rooms ---
    COMBAT_MAGMA, LAVA_PLATFORMING, TIER3_BOSS
}
```

---

### Step 2: Implement Room Layouts in `Room.java`

In `Room.java`, customize `buildRoomLayout(SpriteManager sprites, Renderer renderer)`:

1. **Set Visual Theme for Tier 3:**
   ```java
   int bgMain = (tier == 3) ? TileMap.COBBLE : (tier == 2) ? TileMap.MOSSY_WALL : TileMap.BG_WALL;
   int bgPillar = (tier == 3) ? TileMap.WALL : (tier == 2) ? TileMap.COBBLE : TileMap.BG_PILLAR;
   ```

2. **Add Layout Cases for Tier 3:**
   ```java
   } else if (type == Type.COMBAT_MAGMA) {
       // High stone platforms
       for (int c = 6; c <= 14; c++) map.setForeground(c, 10, TileMap.FLOOR);
       for (int c = cols - 15; c <= cols - 7; c++) map.setForeground(c, 10, TileMap.FLOOR);
       for (int c = 16; c <= 20; c++) map.setForeground(c, 7, TileMap.WOOD_PLATFORM);

       // Floor hazard spikes / lava
       for (int c = 15; c <= 21; c++) map.setForeground(c, rows - 3, TileMap.SPIKES);

       addTorch(sprites, 8, 6);
       addTorch(sprites, cols - 9, 6);

       enemies.add(new entities.SkeletonEnemy(sprites, 140, 100));
       enemies.add(new entities.MageEnemy(sprites, 260, 80));
       enemies.add(new entities.BatEnemy(sprites, 340, 80));
   } else if (type == Type.LAVA_PLATFORMING) {
       // Challenging platforming gap requiring double jump and air dash
       for (int c = 8; c <= cols - 9; c++) map.setForeground(c, rows - 3, TileMap.SPIKES);
       for (int c = 10; c <= 13; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);
       for (int c = 17; c <= 21; c++) map.setForeground(c, 8, TileMap.WOOD_PLATFORM);
       for (int c = 25; c <= 28; c++) map.setForeground(c, 11, TileMap.WOOD_PLATFORM);

       enemies.add(new entities.MageEnemy(sprites, 280, 80));
       enemies.add(new entities.BatEnemy(sprites, 200, 60));
   } else if (type == Type.TIER3_BOSS) {
       // Boss arena
       for (int c = 6; c <= 12; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);
       for (int c = cols - 13; c <= cols - 7; c++) map.setForeground(c, 10, TileMap.WOOD_PLATFORM);
       for (int c = cols / 2 - 4; c <= cols / 2 + 4; c++) map.setForeground(c, 7, TileMap.WOOD_PLATFORM);

       addTorch(sprites, 6, 6);
       addTorch(sprites, cols - 7, 6);
       addTorch(sprites, cols / 2, 4);

       enemies.add(new entities.Tier3BossEnemy(sprites, renderer, 23 * Constants.TILE, 7 * Constants.TILE));
   }
   ```

---

### Step 3: Configure Tier 3 Floor in `Dungeon.java`

Open `src/world/Dungeon.java`. In the constructor `public Dungeon(SpriteManager sprites, Renderer renderer, int tier)`, add a branch for `tier == 3`:

```java
if (tier == 3) {
    floorTitle = "B3F - The Infernal Core";
    rooms.add(new Room(0, Room.Type.START, "B3F - Core Descent", sprites, renderer, cols, rows, 3));
    rooms.add(new Room(1, Room.Type.COMBAT_MAGMA, "B3F - Magma Chambers", sprites, renderer, cols, rows, 3));
    rooms.add(new Room(2, Room.Type.LAVA_PLATFORMING, "B3F - The Molten Chasm", sprites, renderer, 42, rows, 3));
    rooms.add(new Room(3, Room.Type.TREASURE, "B3F - Infernal Vault", sprites, renderer, cols, rows, 3));
    rooms.add(new Room(4, Room.Type.FOOD_SHOP, "B3F - Magma Bazaar & Armory", sprites, renderer, cols, rows, 3));
    rooms.add(new Room(5, Room.Type.TIER3_BOSS, "B3F - Throne of the Fire Lord", sprites, renderer, 48, rows, 3));
}
```

---

### Step 4: Create the Tier 3 Boss Class

Create `src/entities/Tier3BossEnemy.java` extending `Enemy`:

```java
package entities;

import audio.SoundManager;
import combat.Weapon;
import rendering.Renderer;
import rendering.SpriteManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class Tier3BossEnemy extends Enemy {
    public static final String BOSS_NAME = "VULCANUS - INFERNAL LORD";

    private final Renderer renderer;
    private double stateTimer = 2.0;
    private int attackPattern = 0;
    private boolean phase2Triggered;
    private int currentAnimFrame = 0;

    public Tier3BossEnemy(SpriteManager sprites, Renderer renderer, double x, double y) {
        super(sprites, x, y, 42, 48, 850, 30, 200); // 850 HP, 30 contact dmg, 200 Gold
        this.renderer = renderer;
    }

    @Override
    public void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles) {
        // Implement attack patterns:
        // 1. Magma Wave (6-way spread fireballs)
        // 2. Meteor Slam (shockwaves + falling projectiles)
        // 3. Firestorm Nova (24-bullet spiral)
    }

    @Override
    public List<Pickup> spawnDeathLoot() {
        List<Pickup> loot = new ArrayList<>();
        // Generous shower of gold and guaranteed legendary weapon
        for (int i = 0; i < 40; i++) loot.add(Pickup.createCoin(sprites, x + (i % 8) * 5, y + 10, 5));
        loot.add(Pickup.createHeart(sprites, x + 8, y, 50));
        loot.add(Pickup.createWeapon(sprites, x + 18, y - 8, Weapon.createThunderHalberd()));
        return loot;
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);
        BufferedImage[] frames = sprites.slice("enemies/boss.png", 48, 48);
        drawSpriteWithFlash(g, frames[currentAnimFrame % frames.length], rx - 4, ry - 4, 48, 48, !facingRight);
    }
}
```

---

### Step 5: Update `HUD.java` for the Boss Bar & Minimap

Open `src/ui/HUD.java`:

1. In `render()`, add the `Tier3BossEnemy` check:
   ```java
   if (e instanceof entities.Tier3BossEnemy) {
       renderBossHealthBar(g, entities.Tier3BossEnemy.BOSS_NAME, e.getHealth(), e.getMaxHealth());
       break;
   }
   ```
2. In `renderMinimap()`, ensure `Type.TIER3_BOSS` is highlighted crimson:
   ```java
   } else if (r.getType() == Room.Type.BOSS || r.getType() == Room.Type.TIER2_BOSS || r.getType() == Room.Type.TIER3_BOSS) {
       rc = new Color(0xe62e3d); // Crimson
   }
   ```

---

### Step 6: Connect Progression Hooks in `Game.java`

Open `src/Game.java`:

1. Add a descent helper for Tier 3:
   ```java
   private boolean tier2Clear;

   private void descendToTier3() {
       dungeon = new Dungeon(sprites, renderer, 3);
       projectiles.clear();
       particles.clear();
       pickups.clear();
       damageNumbers.clear();
       tier2Clear = false;
       Room nextRoom = dungeon.getCurrentRoom();
       player.setPosition(4 * Constants.TILE, (nextRoom.getMap().getRows() - 4) * Constants.TILE);
       SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
   }
   ```

2. When enemies die in `Game.update()`:
   ```java
   if (e instanceof BossEnemy) {
       tier1Clear = true;
       SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
   } else if (e instanceof Tier2BossEnemy) {
       tier2Clear = true; // Opens descent to Tier 3!
       SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
   } else if (e instanceof Tier3BossEnemy) {
       victory = true; // True game victory!
       SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
   }
   ```

3. In door transitions or input checks, connect `descendToTier3()`:
   ```java
   if (tier2Clear && (input.wasPressed(Action.INTERACT) || input.wasPressed(Action.RESTART))) {
       descendToTier3();
       return;
   }
   ```

4. Display the descent prompt banner in `Game.render()`:
   ```java
   if (tier2Clear) {
       drawOverlay(g, "★ TIER 2 CLEARED! ★", "Malakor has fallen! Press [E] or [ENTER] to descend to B3F Core", new Color(0xfaea70));
   }
   ```

---

## 3. Balancing & Design Checklist

When tuning new tiers:
* **Platforming Gaps:** Standard jump spans up to 4-5 tiles horizontally. Gaps of 6-8 tiles require double jumping or a dash. Gaps of 9+ tiles require jumping, air-dashing, and double jumping.
* **Invulnerability Frames (I-Frames):** Player i-frames are set to `0.85s` (`Player.takeDamage()`). Enemy contact damage and projectile collisions both check `!player.isInvulnerable()`.
* **Weapon Loadout:** Give the player access to the Armory Shop (`[B]` key in Shop rooms) to prepare for harder bosses.
* **Sound Effects:** Use `SoundManager.play(SoundManager.Sound.<NAME>)` for distinct Bfxr retro audio cues.
