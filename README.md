# Gloamroot - Dungreed-Inspired Action Roguelite

A 2D side-scrolling action platformer rogue-lite written in pure Java (Swing/Java2D) inspired directly by **[Dungreed](https://store.steampowered.com/app/753420/Dungreed/)**.
Features handcrafted procedural 16-bit arcade pixel art, 360-degree mouse aiming, double dash mobility with invulnerability frames and ghost trails, multi-room dungeon exploration, enemy AI, treasure vaults, Han's Diner food buff system, floating damage numbers, procedural sound synthesis, and the colossal floor boss **Belial - Demon Warden**.

---

## Quick Start
- **Windows**: Run `run.bat` (or compile with `javac` and run `java -cp out Main`).
- **Linux / macOS**: `./run.sh`
- Missing or deleted `assets/` are automatically generated on first launch, or can be regenerated anytime with:
  ```bash
  java -cp out tools.AssetGenerator
  ```

---

## Controls
| Action | Key / Mouse | Description |
|---|---|---|
| **Move Left / Right** | `A` / `D` or `←` / `→` | Platformer movement |
| **Jump** | `W` / `Space` / `↑` | Variable height jump with coyote time & buffer |
| **Drop Down Platform** | `S` + `Jump` | Drops down through wooden one-way platforms |
| **Aim** | **Mouse Cursor** | 360° aiming reticle |
| **Attack / Fire** | **Left Mouse Click** or `J` | Swings melee blade with slash arc or fires projectiles |
| **Dash** | **Right Mouse Click** or `Shift` / `K` | 2-charge dash with invulnerability & ghost afterimages |
| **Switch Weapon** | `Q` or `1` / `2` / `3` or `Scroll Wheel` | Cycle active primary & secondary weapons |
| **Interact** | `E` or `F` | Open treasure chests & interact with Han's Diner |
| **Fullscreen** | `F11` | Toggle borderless fullscreen |
| **Pause** | `ESC` | Pause game |
| **Restart** | `R` / `Enter` | Restart run after Game Over or Victory |

---

## Dungreed-Inspired Features

### 1. 360° Mouse Aiming & Arsenal
- Weapons rotate dynamically towards your mouse crosshair.
- **Iron Shortsword**: Fast, responsive melee sweeping slash.
- **Titan Greatsword**: Heavy broad blade with high damage and wide cleave.
- **Royal Bow**: Fires piercing arrows that fly across the room.
- **Flintlock Revolver**: High-velocity ballistic firearm.
- **Staff of Embers**: Casts explosive fireballs that blast enemy groups.

### 2. Double Dash Mobility
- 2 rechargeable dash charges with cyan wing HUD indicators.
- Grants complete invulnerability (i-frames) during the dash.
- Leaves trailing cyan ghost afterimages.
- **Dash Attack**: Dashing through enemies deals direct contact damage and knocks them back!

### 3. The Iconic Food System (Han's Dungeon Diner)
- Room 4 houses Han's Diner stall with a friendly chef NPC!
- Press `E` near the food cart to open the interactive menu.
- Spend collected gold on hearty meals:
  - **Prime Rib Roast** (25 G): +20 Max HP, heals 30 HP, +25 Fullness.
  - **Dragon Ramen** (20 G): +3 Base Attack, +10% Movement Speed, heals 20 HP, +20 Fullness.
  - **Hellfire Stew** (30 G): +5 Base Attack, +15% Crit Chance, heals 25 HP, +30 Fullness.
  - **Honey Pancake** (15 G): +40 HP Recovery, +15 Fullness.
- Stomach fullness gauge balances stat buffs across the run.

### 4. Procedural Dungeon & Minimap
- **Room 0 - Prison Entrance**: Safe starting hall with starter gear.
- **Room 1 - Slime Cavern**: Multi-tiered ledges with hopping green slimes and cavern bats.
- **Room 2 - Guardpost Ruins**: Skeleton warriors, cultist mages, and deadly floor spikes.
- **Room 3 - Secret Vault**: Pedestal vault with a Golden Chest that bursts with weapons and gold.
- **Room 4 - Han's Diner**: Food vendor tavern stall.
- **Room 5 - The Warden's Lair**: Boss arena.
- **Minimap**: Real-time HUD minimap showing visited rooms, active position, treasure, shop, and boss room.

### 5. Boss Battle: BELIAL - Demon Warden
- Full boss health bar at the top of the screen (400 HP).
- Multiple attack patterns:
  - **Executioner's Cleave**: High-speed dash strike.
  - **Leaping Ground Slam**: Leaps into the air and slams down, creating screen shake and twin floor shockwaves!
  - **Hellfire Bullet Burst**: 12-way radial fireball ring.
  - **Phase 2 Enrage**: Attacks faster when HP falls below 50%.
- Defeat shower: 100+ gold coins, health elixirs, legendary weapon drops, and the Victory fanfare!

### 6. Procedural Pixel Art & Sound FX
- Handcrafted Dungreed aesthetic in `tools.AssetGenerator`: bold dark outlines, vibrant 16-bit palettes, highlights, and animations.
- Pure Java procedural sound synthesis in `audio.SoundManager`: zero external audio dependencies for slashes, gunshots, bows, hits, coin chimes, dash whooshes, chest fanfare, and explosions!
