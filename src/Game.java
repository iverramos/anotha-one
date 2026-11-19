import audio.SoundManager;
import combat.DamageNumber;
import combat.Weapon;
import entities.BossEnemy;
import entities.Chest;
import entities.Enemy;
import entities.FoodCart;
import entities.Particle;
import entities.Pickup;
import entities.Player;
import entities.Projectile;
import input.InputHandler;
import input.InputHandler.Action;
import rendering.Renderer;
import rendering.SpriteManager;
import ui.FoodMenu;
import ui.HUD;
import utilities.Constants;
import world.Dungeon;
import world.Room;
import world.TileMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Top-level Game State orchestrating the Dungreed-style dungeon crawler.
 * Manages player, dungeon rooms, 360-aim combat, enemies, boss encounters,
 * food buffs, floating damage numbers, and camera lerp.
 */
public final class Game {
    private static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONT_BODY  = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    private static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 8);

    private final InputHandler input;
    private final SpriteManager sprites;
    private final Renderer renderer;
    private final HUD hud;
    private final FoodMenu foodMenu;

    private Dungeon dungeon;
    private Player player;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private final List<DamageNumber> damageNumbers = new ArrayList<>();

    private double camX, camY;
    private boolean paused;
    private boolean victory;
    private volatile int fps;

    public Game(InputHandler input, SpriteManager sprites) {
        this.input = input;
        this.sprites = sprites;
        this.renderer = new Renderer();
        this.hud = new HUD(sprites);
        this.foodMenu = new FoodMenu(sprites);
        initGame();
    }

    public Game(InputHandler input, SpriteManager sprites, Renderer renderer) {
        this.input = input;
        this.sprites = sprites;
        this.renderer = renderer;
        this.hud = new HUD(sprites);
        this.foodMenu = new FoodMenu(sprites);
        initGame();
    }

    private void initGame() {
        dungeon = new Dungeon(sprites, renderer);
        player = new Player(sprites, 4 * Constants.TILE, 12 * Constants.TILE);
        projectiles.clear();
        particles.clear();
        pickups.clear();
        damageNumbers.clear();
        camX = 0;
        camY = 0;
        paused = false;
        victory = false;
    }

    public void setFps(int fps) { this.fps = fps; }
    public void setCameraX(double x) { this.camX = x; }

    public void update(double dt) {
        input.poll();

        // Restart on R / Enter when dead or victorious
        if ((player.isDead() || victory) && input.wasPressed(Action.RESTART)) {
            initGame();
            return;
        }

        // Food Menu interaction
        Room currentRoom = dungeon.getCurrentRoom();
        if (currentRoom.getFoodCart() != null && currentRoom.getFoodCart().isPlayerNearby(player.getCenterX(), player.getCenterY())) {
            if (input.wasPressed(Action.INTERACT)) {
                foodMenu.toggle();
            }
        } else if (foodMenu.isOpen()) {
            foodMenu.setOpen(false);
        }

        if (foodMenu.isOpen()) {
            if (input.wasPressed(Action.PAUSE)) {
                foodMenu.setOpen(false);
                return;
            }
            if (input.wasPressed(Action.SLOT_1)) foodMenu.tryBuy(0, player);
            if (input.wasPressed(Action.SLOT_2)) foodMenu.tryBuy(1, player);
            if (input.wasPressed(Action.SLOT_3)) foodMenu.tryBuy(2, player);
            if (input.wasLeftPressed()) foodMenu.handleMouseClick(input.getMouseX(), input.getMouseY(), player);
            return;
        }

        // Pause
        if (input.wasPressed(Action.PAUSE)) {
            paused = !paused;
        }
        if (paused || player.isDead() || victory) return;

        renderer.updateShake(dt);

        // Weapon hotbar switching (Q, 1..3, wheel)
        if (input.wasPressed(Action.SWITCH_WEAPON)) player.switchWeapon(1);
        if (input.wasPressed(Action.SLOT_1)) player.setWeaponSlot(0);
        if (input.wasPressed(Action.SLOT_2)) player.setWeaponSlot(1);
        if (input.wasPressed(Action.SLOT_3)) player.setWeaponSlot(2);
        if (input.getWheelDelta() != 0) player.switchWeapon(input.getWheelDelta());

        // Interact with Chests
        if (input.wasPressed(Action.INTERACT)) {
            Chest chest = currentRoom.getChest();
            if (chest != null) {
                List<Pickup> loot = chest.tryOpen(player.getCenterX(), player.getCenterY());
                pickups.addAll(loot);
            }
        }

        // Update Room
        currentRoom.update(dt);
        TileMap map = currentRoom.getMap();

        // Update Player
        int mouseWorldX = (int) (input.getMouseX() + camX);
        int mouseWorldY = (int) (input.getMouseY() + camY);

        player.update(dt,
                input.isDown(Action.LEFT),
                input.isDown(Action.RIGHT),
                input.isDown(Action.JUMP) || input.isDown(Action.UP),
                input.wasPressed(Action.JUMP) || input.wasPressed(Action.UP),
                input.isDown(Action.DOWN),
                input.wasRightPressed(),
                mouseWorldX, mouseWorldY,
                projectiles, particles, currentRoom.getEnemies(), damageNumbers);

        map.resolveCollisions(player, dt, player.isDroppingThroughPlatform());

        // Hazard spike check for player
        if (map.checkHazard(player.getBounds())) {
            player.takeDamage(15);
            player.setVelocity(player.getVx() * 0.5, -220);
            damageNumbers.add(new DamageNumber(player.getCenterX(), player.getY() - 4, "-15", Color.RED, false));
        }

        // Player attack
        if (input.isLeftDown()) {
            player.tryAttack(projectiles, particles, currentRoom.getEnemies(), damageNumbers);
        }

        // Update Enemies
        Iterator<Enemy> enemyIter = currentRoom.getEnemies().iterator();
        while (enemyIter.hasNext()) {
            Enemy e = enemyIter.next();
            if (e.isDead()) {
                pickups.addAll(e.spawnDeathLoot());
                enemyIter.remove();
                if (e instanceof BossEnemy) {
                    victory = true;
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

            // Check collision with solid tiles
            int col = (int) (p.getX() / Constants.TILE);
            int row = (int) (p.getY() / Constants.TILE);
            if (TileMap.isSolid(map.getForeground(col, row))) {
                p.deactivate();
                projIter.remove();
                particles.add(Particle.createImpact(sprites, p.getX(), p.getY(), Particle.Type.SPARK));
                continue;
            }

            Rectangle pBox = p.getBounds();

            // Player projectile vs Enemies
            if (p.getOwner() == Projectile.Owner.PLAYER) {
                for (Enemy e : currentRoom.getEnemies()) {
                    if (!e.isDead() && e.getBounds().intersects(pBox)) {
                        e.takeDamage(p.getDamage());
                        double kx = p.getVx() * 0.3;
                        double ky = p.getVy() * 0.2 - 20;
                        e.applyKnockback(kx, ky);
                        SoundManager.play(SoundManager.Sound.HIT);
                        particles.add(Particle.createImpact(sprites, p.getX(), p.getY(),
                                (e instanceof entities.SlimeEnemy) ? Particle.Type.SLIME : Particle.Type.BLOOD));
                        damageNumbers.add(new DamageNumber(e.getCenterX(), e.getY() - 4,
                                String.valueOf(p.getDamage()), Color.YELLOW, false));
                        p.deactivate();
                        projIter.remove();
                        break;
                    }
                }
            } else {
                // Enemy projectile vs Player
                if (!player.isDashing() && player.getBounds().intersects(pBox)) {
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

            // Tile collision for pickups
            int pCol = (int) (pick.getX() / Constants.TILE);
            int pRow = (int) ((pick.getY() + 6) / Constants.TILE);
            if (TileMap.isSolid(map.getForeground(pCol, pRow)) || TileMap.isOneWayPlatform(map.getForeground(pCol, pRow))) {
                pick.setOnGround(true);
            }

            // Collection check
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

        // Update Particles
        particles.removeIf(pt -> !pt.update(dt));

        // Update Damage Numbers
        damageNumbers.removeIf(dn -> !dn.update(dt));

        // Room Transitions via Doors
        if (currentRoom.isLeftDoorOpen() && player.getBounds().intersects(currentRoom.getLeftDoorBounds())) {
            int prevIdx = dungeon.getCurrentRoomIndex() - 1;
            if (prevIdx >= 0) {
                dungeon.moveToRoom(prevIdx);
                projectiles.clear();
                particles.clear();
                pickups.clear();
                Room nextRoom = dungeon.getCurrentRoom();
                player.setPosition((nextRoom.getMap().getCols() - 4) * Constants.TILE, (nextRoom.getMap().getRows() - 4) * Constants.TILE);
            }
        } else if (currentRoom.isRightDoorOpen() && player.getBounds().intersects(currentRoom.getRightDoorBounds())) {
            int nextIdx = dungeon.getCurrentRoomIndex() + 1;
            if (nextIdx < dungeon.getRoomCount()) {
                dungeon.moveToRoom(nextIdx);
                projectiles.clear();
                particles.clear();
                pickups.clear();
                Room nextRoom = dungeon.getCurrentRoom();
                player.setPosition(3 * Constants.TILE, (nextRoom.getMap().getRows() - 4) * Constants.TILE);
            }
        }

        // Smooth Camera Lerp
        double targetCamX = player.getCenterX() - Constants.VIEW_W / 2.0 + Math.cos(player.getAimAngle()) * 30.0;
        double targetCamY = player.getCenterY() - Constants.VIEW_H / 2.0 + Math.sin(player.getAimAngle()) * 20.0;
        double maxCamX = map.getPixelWidth() - Constants.VIEW_W;
        double maxCamY = map.getPixelHeight() - Constants.VIEW_H;

        targetCamX = Math.max(0, Math.min(maxCamX, targetCamX));
        targetCamY = Math.max(0, Math.min(maxCamY, targetCamY));

        camX += (targetCamX - camX) * 10.0 * dt;
        camY += (targetCamY - camY) * 10.0 * dt;
    }

    public void render(Graphics2D g) {
        // Deep background
        g.setColor(new Color(0x0e1017));
        g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);

        int renderCamX = (int) Math.round(camX) + renderer.getShakeOffsetX();
        int renderCamY = (int) Math.round(camY) + renderer.getShakeOffsetY();

        Room room = dungeon.getCurrentRoom();

        // 1. Room background & tiles
        room.render(g, renderCamX, renderCamY, player.getCenterX(), player.getCenterY());

        // 2. Pickups
        for (Pickup pick : pickups) pick.render(g, renderCamX, renderCamY);

        // 3. Enemies
        for (Enemy e : room.getEnemies()) e.render(g, renderCamX, renderCamY);

        // 4. Player
        player.render(g, renderCamX, renderCamY);

        // 5. Projectiles
        for (Projectile p : projectiles) p.render(g, renderCamX, renderCamY);

        // 6. Particles
        for (Particle pt : particles) pt.render(g, renderCamX, renderCamY);

        // 7. Floating Damage Numbers
        for (DamageNumber dn : damageNumbers) dn.render(g, renderCamX, renderCamY);

        // 8. HUD & Crosshair
        hud.render(g, player, dungeon, input.getMouseX(), input.getMouseY());

        // 9. Food Menu
        foodMenu.render(g, player);

        // 10. Room Title Banner (Top Left)
        g.setFont(FONT_SMALL);
        text(g, room.getTitle() + "  |  FPS " + fps, 8, Constants.VIEW_H - 4, new Color(0x656f8f));

        // Overlays: Paused / Game Over / Victory
        if (paused) {
            drawOverlay(g, "PAUSED", "Press ESC to resume", new Color(0xfabc2a));
        } else if (player.isDead()) {
            drawOverlay(g, "YOU DIED", "Press [R] to re-enter the dungeon", new Color(0xe62e3d));
        } else if (victory) {
            drawOverlay(g, "VICTORY ACHIEVED!", "The Demon Warden has fallen! Press [R] to run again", new Color(0xfabc2a));
        }
    }

    private void drawOverlay(Graphics2D g, String title, String sub, Color titleColor) {
        g.setColor(new Color(15, 12, 22, 190));
        g.fillRect(0, 0, Constants.VIEW_W, Constants.VIEW_H);

        g.setFont(FONT_TITLE);
        int tw = g.getFontMetrics().stringWidth(title);
        text(g, title, (Constants.VIEW_W - tw) / 2, Constants.VIEW_H / 2 - 10, titleColor);

        g.setFont(FONT_BODY);
        int sw = g.getFontMetrics().stringWidth(sub);
        text(g, sub, (Constants.VIEW_W - sw) / 2, Constants.VIEW_H / 2 + 14, Color.WHITE);
    }

    private static void text(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(Color.BLACK);
        g.drawString(s, x + 1, y + 1);
        g.setColor(c);
        g.drawString(s, x, y);
    }
}
