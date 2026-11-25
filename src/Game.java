import audio.SoundManager;
import combat.DamageNumber;
import config.GameConfig;
import entities.BossEnemy;
import entities.Chest;
import entities.Enemy;
import entities.FoodCart;
import entities.NPC;
import entities.Particle;
import entities.Pickup;
import entities.Player;
import entities.Projectile;
import entities.Tier2BossEnemy;
import entities.TrainingDummy;
import input.InputHandler;
import input.InputHandler.Action;
import rendering.Renderer;
import rendering.SpriteManager;
import ui.CreditsScreen;
import ui.DialogueBox;
import ui.FoodMenu;
import ui.HUD;
import ui.SettingsMenu;
import ui.TitleScreen;
import ui.TutorialManager;
import ui.WeaponShop;
import utilities.Constants;
import world.Dungeon;
import world.Room;
import world.TileMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Top-level Game State orchestrating the Dead Cells / Dungreed-inspired action platformer.
 * Coordinates Title/Settings/Credits, 960x540 crisp UI + 2x pixel-art world rendering,
 * NPC DialogueBox, Starting Tutorial with TrainingDummy, Fullness progression,
 * JSON-driven biomes & modular buildings, and Tier 1 & 2 dungeon encounters.
 */
public final class Game {
    public enum GameState {
        TITLE, SETTINGS, CREDITS, PLAYING
    }

    private static final Font FONT_TITLE      = new Font(Font.SANS_SERIF, Font.BOLD, 32);
    private static final Font FONT_BODY       = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font FONT_SMALL      = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font FONT_PROMPT     = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    private static final Font FONT_BANNER     = new Font(Font.SANS_SERIF, Font.BOLD, 24);

    private final InputHandler input;
    private final SpriteManager sprites;
    private final Renderer renderer;
    private final HUD hud;
    private final FoodMenu foodMenu;
    private final WeaponShop weaponShop;
    private final TitleScreen titleScreen;
    private final SettingsMenu settingsMenu;
    private final CreditsScreen creditsScreen;
    private final DialogueBox dialogueBox;
    private final TutorialManager tutorialManager;

    private GameState state = GameState.TITLE;
    private GameState returnState = GameState.TITLE;

    private Dungeon dungeon;
    private Player player;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private final List<DamageNumber> damageNumbers = new ArrayList<>();

    private double camX, camY;
    private boolean paused;
    private boolean victory;
    private boolean tier1Clear;
    private volatile int fps;

    public Game(InputHandler input, SpriteManager sprites) {
        this(input, sprites, new Renderer());
    }

    public Game(InputHandler input, SpriteManager sprites, Renderer renderer) {
        this.input = input;
        this.sprites = sprites;
        this.renderer = renderer;
        this.hud = new HUD(sprites);
        this.foodMenu = new FoodMenu(sprites);
        this.weaponShop = new WeaponShop(sprites);
        this.titleScreen = new TitleScreen();
        this.settingsMenu = new SettingsMenu();
        this.creditsScreen = new CreditsScreen();
        this.dialogueBox = new DialogueBox();
        this.tutorialManager = new TutorialManager();

        SoundManager.init();
        initGame();
        this.state = GameState.TITLE;
    }

    private void initGame() {
        dungeon = new Dungeon(sprites, renderer, 1);
        player = new Player(sprites, 4 * Constants.TILE, 13 * Constants.TILE);
        projectiles.clear();
        particles.clear();
        pickups.clear();
        damageNumbers.clear();
        dialogueBox.close();
        foodMenu.setOpen(false);
        weaponShop.setOpen(false);
        tutorialManager.reset();
        camX = 0;
        camY = 0;
        paused = false;
        victory = false;
        tier1Clear = false;
    }

    private void descendToTier2() {
        dungeon = new Dungeon(sprites, renderer, 2);
        projectiles.clear();
        particles.clear();
        pickups.clear();
        damageNumbers.clear();
        tier1Clear = false;
        Room nextRoom = dungeon.getCurrentRoom();
        player.setPosition(4 * Constants.TILE, (nextRoom.getMap().getRows() - 4) * Constants.TILE);
        SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
    }

    public void setFps(int fps) { this.fps = fps; }
    public void setCameraX(double x) { this.camX = x; }

    public void update(double dt) {
        input.poll();

        // -------------------------------------------------------------
        // Menu State Machine
        // -------------------------------------------------------------
        if (state == GameState.TITLE) {
            titleScreen.update(dt);
            TitleScreen.Action act = titleScreen.handleInput(
                    input.wasPressed(Action.UP) || input.wasPressed(Action.SLOT_1),
                    input.wasPressed(Action.DOWN) || input.wasPressed(Action.SLOT_2),
                    input.wasPressed(Action.JUMP) || input.wasPressed(Action.RESTART),
                    input.getMouseX(), input.getMouseY(), input.wasLeftPressed()
            );

            if (act == TitleScreen.Action.PLAY) {
                initGame();
                state = GameState.PLAYING;
            } else if (act == TitleScreen.Action.SETTINGS) {
                returnState = GameState.TITLE;
                state = GameState.SETTINGS;
            } else if (act == TitleScreen.Action.CREDITS) {
                state = GameState.CREDITS;
            } else if (act == TitleScreen.Action.EXIT) {
                System.exit(0);
            }
            return;
        }

        if (state == GameState.SETTINGS) {
            settingsMenu.update();
            boolean close = settingsMenu.handleInput(
                    input.wasPressed(Action.UP),
                    input.wasPressed(Action.DOWN),
                    input.wasPressed(Action.LEFT),
                    input.wasPressed(Action.RIGHT),
                    input.wasPressed(Action.JUMP) || input.wasPressed(Action.RESTART),
                    input.wasPressed(Action.PAUSE),
                    input.getMouseX(), input.getMouseY(),
                    input.isLeftDown(), input.wasLeftPressed(), input.wasLeftPressed()
            );
            if (close) {
                state = returnState;
            }
            return;
        }

        if (state == GameState.CREDITS) {
            creditsScreen.update(dt);
            boolean close = creditsScreen.handleInput(
                    input.wasPressed(Action.JUMP) || input.wasPressed(Action.RESTART),
                    input.wasPressed(Action.PAUSE),
                    input.getMouseX(), input.getMouseY(),
                    input.wasLeftPressed()
            );
            if (close) {
                state = GameState.TITLE;
            }
            return;
        }

        // -------------------------------------------------------------
        // Playing State
        // -------------------------------------------------------------
        if ((player.isDead() || victory) && input.wasPressed(Action.RESTART)) {
            initGame();
            state = GameState.TITLE;
            return;
        }

        if (tier1Clear) {
            if (input.wasPressed(Action.INTERACT) || input.wasPressed(Action.RESTART)) {
                descendToTier2();
                return;
            }
        }

        // Active Dialogue Box
        if (dialogueBox.isOpen()) {
            dialogueBox.update(dt);
            int slot = -1;
            if (input.wasPressed(Action.SLOT_1)) slot = 0;
            else if (input.wasPressed(Action.SLOT_2)) slot = 1;
            else if (input.wasPressed(Action.SLOT_3)) slot = 2;
            dialogueBox.handleInput(
                    input.wasPressed(Action.UP),
                    input.wasPressed(Action.DOWN),
                    input.wasPressed(Action.JUMP) || input.wasPressed(Action.INTERACT) || input.wasPressed(Action.RESTART),
                    input.wasPressed(Action.PAUSE),
                    slot,
                    input.getMouseX(), input.getMouseY(), input.wasLeftPressed()
            );
            return;
        }

        // Weapon Shop interaction
        if (weaponShop.isOpen()) {
            weaponShop.handleInput(
                    input.wasPressed(Action.UP),
                    input.wasPressed(Action.DOWN),
                    input.wasPressed(Action.LEFT),
                    input.wasPressed(Action.RIGHT),
                    input.wasPressed(Action.INTERACT) || input.wasPressed(Action.RESTART),
                    input.wasPressed(Action.PAUSE) || input.wasPressed(Action.SHOP),
                    input.getMouseX(), input.getMouseY(), input.wasLeftPressed(),
                    player
            );
            return;
        }

        Room currentRoom = dungeon.getCurrentRoom();

        // Food Menu interaction
        if (foodMenu.isOpen()) {
            if (input.wasPressed(Action.PAUSE) || input.wasPressed(Action.INTERACT)) {
                foodMenu.setOpen(false);
                return;
            }
            if (input.wasPressed(Action.SLOT_1)) foodMenu.tryBuy(0, player);
            if (input.wasPressed(Action.SLOT_2)) foodMenu.tryBuy(1, player);
            if (input.wasPressed(Action.SLOT_3)) foodMenu.tryBuy(2, player);
            if (input.wasLeftPressed()) foodMenu.handleMouseClick(input.getMouseX(), input.getMouseY(), player);
            return;
        }

        // Quick-open Weapon Shop via [B]
        if (input.wasPressed(Action.SHOP)) {
            weaponShop.toggle();
            return;
        }

        // Pause & in-game Settings
        if (input.wasPressed(Action.PAUSE)) {
            paused = !paused;
            SoundManager.play(SoundManager.Sound.UI_CLICK);
        }
        if (paused) {
            if (input.wasPressed(Action.SHOP)) {
                returnState = GameState.PLAYING;
                state = GameState.SETTINGS;
            }
            return;
        }
        if (player.isDead() || victory) return;

        renderer.updateShake(dt);

        // Weapon hotbar switching (Q, 1..3, wheel)
        if (input.wasPressed(Action.SWITCH_WEAPON)) player.switchWeapon(1);
        if (input.wasPressed(Action.SLOT_1)) player.setWeaponSlot(0);
        if (input.wasPressed(Action.SLOT_2)) player.setWeaponSlot(1);
        if (input.wasPressed(Action.SLOT_3)) player.setWeaponSlot(2);
        if (input.getWheelDelta() != 0) player.switchWeapon(input.getWheelDelta());

        // Interactions ([E] / [F]): NPCs -> FoodCart -> Chest
        if (input.wasPressed(Action.INTERACT)) {
            boolean handled = false;
            for (NPC npc : currentRoom.getNpcs()) {
                if (npc.isPlayerNearby(player.getCenterX(), player.getCenterY())) {
                    Runnable onTalked = (npc.getRole() == NPC.Role.INSTRUCTOR_GARETH)
                            ? tutorialManager::notifyNpcTalked
                            : null;
                    npc.startDialogue(dialogueBox, onTalked, () -> weaponShop.setOpen(true));
                    handled = true;
                    break;
                }
            }
            if (!handled && currentRoom.getFoodCart() != null
                    && currentRoom.getFoodCart().isPlayerNearby(player.getCenterX(), player.getCenterY())) {
                NPC.startHanDinerDialogue(dialogueBox, () -> foodMenu.setOpen(true));
                handled = true;
            }
            if (!handled && currentRoom.getChest() != null) {
                List<Pickup> loot = currentRoom.getChest().tryOpen(player.getCenterX(), player.getCenterY());
                pickups.addAll(loot);
            }
        }

        // Update Room
        currentRoom.update(dt);
        TileMap map = currentRoom.getMap();

        // Check one-time fullness reduction when a room is cleared
        if (currentRoom.pollClearFullnessTrigger()) {
            applyRoomFullnessDrain(15);
        }

        // Update Player
        int mouseWorldX = input.getWorldMouseX() + (int) Math.round(camX);
        int mouseWorldY = input.getWorldMouseY() + (int) Math.round(camY);

        boolean leftDown = input.isDown(Action.LEFT);
        boolean rightDown = input.isDown(Action.RIGHT);
        boolean jumpPressed = input.wasPressed(Action.JUMP) || input.wasPressed(Action.UP);

        player.update(dt,
                leftDown,
                rightDown,
                input.isDown(Action.JUMP) || input.isDown(Action.UP),
                jumpPressed,
                input.isDown(Action.DOWN),
                input.wasRightPressed(),
                mouseWorldX, mouseWorldY,
                projectiles, particles, currentRoom.getEnemies(), damageNumbers);

        map.resolveCollisions(player, dt, player.isDroppingThroughPlatform());

        // Tutorial Progression (Tier 1 Room 0)
        if (dungeon.getTier() == 1 && currentRoom.getType() == Room.Type.START) {
            tutorialManager.update(dt, player, leftDown, rightDown, jumpPressed);
            if (tutorialManager.isCompleted()) {
                currentRoom.unlockTutorialDoor();
            }
        }

        // Dash vs Training Dummy
        TrainingDummy dummy = currentRoom.getTrainingDummy();
        if (dummy != null && player.isDashing() && dummy.canTakeHit()
                && player.getBounds().intersects(dummy.getBounds())) {
            dummy.onHit(22, player.getCenterX() < dummy.getCenterX(), particles, damageNumbers);
            tutorialManager.notifyDummyHit();
        }

        // Hazard spike check for player (respects i-frames)
        if (map.checkHazard(player.getBounds())) {
            if (!player.isInvulnerable()) {
                player.takeDamage(15);
                player.setVelocity(player.getVx() * 0.5, -220);
                damageNumbers.add(new DamageNumber(player.getCenterX(), player.getY() - 4, "-15", Color.RED, false));
            }
        }

        // Player attack (supports Enemies and Training Dummy)
        if (input.isLeftDown()) {
            player.tryAttack(projectiles, particles, currentRoom.getEnemies(), damageNumbers, dummy, tutorialManager);
        }

        // Update Enemies
        Iterator<Enemy> enemyIter = currentRoom.getEnemies().iterator();
        while (enemyIter.hasNext()) {
            Enemy e = enemyIter.next();
            if (e.isDead()) {
                pickups.addAll(e.spawnDeathLoot());
                enemyIter.remove();
                if (e instanceof BossEnemy) {
                    tier1Clear = true;
                    SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
                } else if (e instanceof Tier2BossEnemy) {
                    victory = true;
                    SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
                }
                continue;
            }

            e.update(dt);
            e.updateAI(dt, player, projectiles, particles);
            map.resolveCollisions(e, dt, false);
            e.checkContactPlayer(player, damageNumbers);
        }

        // Update Projectiles
        Iterator<Projectile> projIter = projectiles.iterator();
        while (projIter.hasNext()) {
            Projectile p = projIter.next();
            p.update(dt);
            if (!p.isActive()) {
                projIter.remove();
                continue;
            }

            int col = (int) (p.getX() / Constants.TILE);
            int row = (int) (p.getY() / Constants.TILE);
            if (TileMap.isSolid(map.getForeground(col, row))) {
                p.deactivate();
                projIter.remove();
                particles.add(Particle.createImpact(sprites, p.getX(), p.getY(), Particle.Type.SPARK));
                continue;
            }

            Rectangle pBox = p.getBounds();

            if (p.getOwner() == Projectile.Owner.PLAYER) {
                boolean hitSomething = false;
                for (Enemy e : currentRoom.getEnemies()) {
                    if (!e.isDead() && e.getBounds().intersects(pBox)) {
                        e.takeDamage(p.getDamage());
                        e.applyKnockback(p.getVx() * 0.3, p.getVy() * 0.2 - 20);
                        SoundManager.play(SoundManager.Sound.HIT);
                        particles.add(Particle.createImpact(sprites, p.getX(), p.getY(),
                                (e instanceof entities.SlimeEnemy) ? Particle.Type.SLIME : Particle.Type.BLOOD));
                        damageNumbers.add(new DamageNumber(e.getCenterX(), e.getY() - 4,
                                String.valueOf(p.getDamage()), Color.YELLOW, false));
                        p.deactivate();
                        projIter.remove();
                        hitSomething = true;
                        break;
                    }
                }
                if (!hitSomething && dummy != null && dummy.getBounds().intersects(pBox)) {
                    dummy.onHit(p.getDamage(), p.getVx() > 0, particles, damageNumbers);
                    tutorialManager.notifyDummyHit();
                    p.deactivate();
                    projIter.remove();
                }
            } else {
                if (!player.isInvulnerable() && player.getBounds().intersects(pBox)) {
                    player.takeDamage(p.getDamage());
                    damageNumbers.add(new DamageNumber(player.getCenterX(), player.getY() - 4,
                            "-" + p.getDamage(), Color.RED, false));
                    p.deactivate();
                    projIter.remove();
                }
            }
        }

        // Update Pickups
        Iterator<Pickup> pickIter = pickups.iterator();
        while (pickIter.hasNext()) {
            Pickup pick = pickIter.next();
            pick.update(dt, player.getCenterX(), player.getCenterY());

            int pCol = (int) (pick.getX() / Constants.TILE);
            int pRow = (int) ((pick.getY() + 6) / Constants.TILE);
            if (TileMap.isSolid(map.getForeground(pCol, pRow))
                    || TileMap.isOneWayPlatform(map.getForeground(pCol, pRow))
                    || map.isPlatformOverlay(pCol, pRow)) {
                pick.setOnGround(true);
            }

            if (pick.getBounds().intersects(player.getBounds())) {
                if (pick.getType() == Pickup.Type.COIN) {
                    player.addGold(pick.getValue());
                    SoundManager.play(SoundManager.Sound.COIN);
                    particles.add(Particle.createImpact(sprites, pick.getX(), pick.getY(), Particle.Type.SPARK));
                    pick.collect();
                    pickIter.remove();
                } else if (pick.getType() == Pickup.Type.HEART) {
                    player.heal(pick.getValue());
                    SoundManager.play(SoundManager.Sound.COIN);
                    damageNumbers.add(new DamageNumber(player.getCenterX(), player.getY() - 4,
                            "+" + pick.getValue() + " HP", new Color(0x62db58), false));
                    pick.collect();
                    pickIter.remove();
                } else if (pick.getType() == Pickup.Type.WEAPON && pick.getWeapon() != null) {
                    player.addWeapon(pick.getWeapon());
                    SoundManager.play(SoundManager.Sound.CHEST_OPEN);
                    damageNumbers.add(new DamageNumber(player.getCenterX(), player.getY() - 4,
                            pick.getWeapon().getName(), pick.getWeapon().getRarity().color, true));
                    pick.collect();
                    pickIter.remove();
                }
            }
        }

        particles.removeIf(pt -> !pt.update(dt));
        damageNumbers.removeIf(dn -> !dn.update(dt));

        // Room Transitions via Doors
        if (currentRoom.isLeftDoorOpen() && player.getBounds().intersects(currentRoom.getLeftDoorBounds())) {
            int prevIdx = dungeon.getCurrentRoomIndex() - 1;
            if (prevIdx >= 0) {
                enterRoom(prevIdx, true);
            }
        } else if (currentRoom.isRightDoorOpen() && player.getBounds().intersects(currentRoom.getRightDoorBounds())) {
            if (dungeon.getTier() == 1 && currentRoom.getType() == Room.Type.BOSS && tier1Clear) {
                descendToTier2();
                return;
            }
            int nextIdx = dungeon.getCurrentRoomIndex() + 1;
            if (nextIdx < dungeon.getRoomCount()) {
                enterRoom(nextIdx, false);
            }
        }

        // Smooth Camera Lerp with deadzone snap
        double targetCamX = player.getCenterX() - Constants.VIEW_W / 2.0 + Math.cos(player.getAimAngle()) * 24.0;
        double targetCamY = player.getCenterY() - Constants.VIEW_H / 2.0 + Math.sin(player.getAimAngle()) * 14.0;
        double maxCamX = map.getPixelWidth() - Constants.VIEW_W;
        double maxCamY = map.getPixelHeight() - Constants.VIEW_H;

        targetCamX = Math.max(0, Math.min(maxCamX, targetCamX));
        targetCamY = Math.max(0, Math.min(maxCamY, targetCamY));

        camX += (targetCamX - camX) * 10.0 * dt;
        camY += (targetCamY - camY) * 10.0 * dt;
        if (Math.abs(targetCamX - camX) < 0.05) camX = targetCamX;
        if (Math.abs(targetCamY - camY) < 0.05) camY = targetCamY;
    }

    private void enterRoom(int index, boolean fromRight) {
        dungeon.moveToRoom(index);
        projectiles.clear();
        particles.clear();
        pickups.clear();
        Room nextRoom = dungeon.getCurrentRoom();
        double spawnX = fromRight
                ? (nextRoom.getMap().getCols() - 4) * Constants.TILE
                : 3 * Constants.TILE;
        double spawnY = (nextRoom.getMap().getRows() - 4) * Constants.TILE;
        player.setPosition(spawnX, spawnY);

        if (nextRoom.consumeFirstEntryFullnessTrigger()) {
            applyRoomFullnessDrain(15);
        }
    }

    private void applyRoomFullnessDrain(int amount) {
        int dropped = player.consumeFullness(amount);
        if (dropped > 0) {
            damageNumbers.add(new DamageNumber(player.getCenterX(), player.getY() - 10,
                    "-" + dropped + " Full", new Color(0xffb838), false));
        }
    }

    public void render(Graphics2D g) {
        g.setColor(new Color(0x0e1017));
        g.fillRect(0, 0, Constants.UI_W, Constants.UI_H);

        if (state == GameState.TITLE) {
            titleScreen.render(g);
            return;
        }
        if (state == GameState.SETTINGS) {
            settingsMenu.render(g);
            return;
        }
        if (state == GameState.CREDITS) {
            creditsScreen.render(g, input.getMouseX(), input.getMouseY());
            return;
        }

        int renderCamX = (int) Math.round(camX) + renderer.getShakeOffsetX();
        int renderCamY = (int) Math.round(camY) + renderer.getShakeOffsetY();
        Room room = dungeon.getCurrentRoom();

        // -------------------------------------------------------------
        // Pass 1: World Layer (scaled 2x with crisp nearest-neighbor)
        // -------------------------------------------------------------
        AffineTransform oldTx = g.getTransform();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.scale(Constants.UI_SCALE, Constants.UI_SCALE);

        room.render(g, renderCamX, renderCamY, player.getCenterX(), player.getCenterY());
        for (Pickup pick : pickups) pick.render(g, renderCamX, renderCamY);
        for (Enemy e : room.getEnemies()) e.render(g, renderCamX, renderCamY);
        player.render(g, renderCamX, renderCamY);
        for (Projectile p : projectiles) p.render(g, renderCamX, renderCamY);
        for (Particle pt : particles) pt.render(g, renderCamX, renderCamY);

        g.setTransform(oldTx);

        // -------------------------------------------------------------
        // Pass 2: High-Resolution UI & Text Layer (960x540)
        // -------------------------------------------------------------
        for (DamageNumber dn : damageNumbers) dn.render(g, renderCamX, renderCamY);

        renderWorldInteractPrompts(g, room, renderCamX, renderCamY);

        if (room.isClearBannerActive()) {
            g.setFont(FONT_BANNER);
            String banner = "★ ROOM CLEARED! ★";
            int bx = (Constants.UI_W - g.getFontMetrics().stringWidth(banner)) / 2;
            text(g, banner, bx, 128, new Color(0xfabc2a));
        }

        hud.render(g, player, dungeon, input.getMouseX(), input.getMouseY());

        if (dungeon.getTier() == 1 && room.getType() == Room.Type.START && !dialogueBox.isOpen()) {
            tutorialManager.render(g, true);
        }

        foodMenu.render(g, player);
        weaponShop.render(g, player);
        dialogueBox.render(g);

        // Bottom Location, Biome & FPS Bar
        GameConfig.BiomeDef biome = sprites.getConfig().getBiome(room.getBiomeId());
        String biomeName = (biome != null) ? biome.name : room.getBiomeId();
        g.setFont(FONT_SMALL);
        text(g, room.getTitle() + "  [" + biomeName + "]  |  FPS " + fps, 16, Constants.UI_H - 10, new Color(0x8a96ba));

        if (room.getType() == Room.Type.FOOD_SHOP && !foodMenu.isOpen() && !weaponShop.isOpen() && !dialogueBox.isOpen()) {
            String shopTip = "[E] Talk / Diner / Forge   |   [B] Quick Armory";
            int sw = g.getFontMetrics().stringWidth(shopTip);
            text(g, shopTip, Constants.UI_W - sw - 16, Constants.UI_H - 10, new Color(0xfabc2a));
        }

        if (paused) {
            drawOverlay(g, "PAUSED", "Press ESC to resume   |   [B] Audio Settings", new Color(0xfabc2a));
        } else if (player.isDead()) {
            drawOverlay(g, "YOU DIED", "Press [R] to return to Title", new Color(0xe62e3d));
        } else if (tier1Clear) {
            drawOverlay(g, "★ TIER 1 CLEARED! ★", "Belial has fallen! Press [E] or [ENTER] to descend to B2F", new Color(0x38d7f2));
        } else if (victory) {
            drawOverlay(g, "VICTORY ACHIEVED!", "The Void Archon has fallen! Press [R] for Title", new Color(0xfabc2a));
        }
    }

    private void renderWorldInteractPrompts(Graphics2D g, Room room, int camX, int camY) {
        if (dialogueBox.isOpen() || foodMenu.isOpen() || weaponShop.isOpen()) return;
        g.setFont(FONT_PROMPT);

        for (NPC npc : room.getNpcs()) {
            if (npc.isPlayerNearby(player.getCenterX(), player.getCenterY())) {
                int ux = (int) Math.round((npc.getCenterX() - camX) * Constants.UI_SCALE);
                int uy = (int) Math.round((npc.getY() - camY - 12) * Constants.UI_SCALE);
                String p = "[E] TALK - " + npc.getName();
                int pw = g.getFontMetrics().stringWidth(p);
                text(g, p, ux - pw / 2, uy, new Color(0xfabc2a));
            }
        }

        FoodCart cart = room.getFoodCart();
        if (cart != null && cart.isPlayerNearby(player.getCenterX(), player.getCenterY())) {
            int ux = (int) Math.round((cart.getX() + 24 - camX) * Constants.UI_SCALE);
            int uy = (int) Math.round((cart.getY() - camY - 8) * Constants.UI_SCALE);
            String p = "[E] CHEF HAN'S DINER";
            int pw = g.getFontMetrics().stringWidth(p);
            text(g, p, ux - pw / 2, uy, new Color(0xfabc2a));
        }

        Chest chest = room.getChest();
        if (chest != null && chest.isPlayerNearby(player.getCenterX(), player.getCenterY())) {
            int ux = (int) Math.round((chest.getX() + 12 - camX) * Constants.UI_SCALE);
            int uy = (int) Math.round((chest.getY() - camY - 8) * Constants.UI_SCALE);
            String p = "[E] OPEN CHEST";
            int pw = g.getFontMetrics().stringWidth(p);
            text(g, p, ux - pw / 2, uy, new Color(0xfabc2a));
        }

        // Locked portal hint during tutorial
        if (dungeon.getTier() == 1 && room.getType() == Room.Type.START && !room.isRightDoorOpen()) {
            Rectangle door = room.getRightDoorBounds();
            if (Math.abs(player.getCenterX() - door.getCenterX()) < 54) {
                int ux = (int) Math.round((door.getCenterX() - camX) * Constants.UI_SCALE);
                int uy = (int) Math.round((door.y - camY - 10) * Constants.UI_SCALE);
                String p = "Complete Tutorial to Enter";
                int pw = g.getFontMetrics().stringWidth(p);
                text(g, p, ux - pw / 2, uy, new Color(0xff7852));
            }
        }
    }

    private void drawOverlay(Graphics2D g, String title, String sub, Color titleColor) {
        g.setColor(new Color(15, 12, 22, 195));
        g.fillRect(0, 0, Constants.UI_W, Constants.UI_H);

        g.setFont(FONT_TITLE);
        int tw = g.getFontMetrics().stringWidth(title);
        text(g, title, (Constants.UI_W - tw) / 2, Constants.UI_H / 2 - 18, titleColor);

        g.setFont(FONT_BODY);
        int sw = g.getFontMetrics().stringWidth(sub);
        text(g, sub, (Constants.UI_W - sw) / 2, Constants.UI_H / 2 + 24, Color.WHITE);
    }

    private static void text(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(Color.BLACK);
        g.drawString(s, x + 2, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
    }
}
