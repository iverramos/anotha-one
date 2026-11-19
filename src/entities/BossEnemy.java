package entities;

import audio.SoundManager;
import combat.DamageNumber;
import combat.Weapon;
import rendering.Renderer;
import rendering.SpriteManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Dungeon Floor Boss: "BELIAL - Demon Warden of B1F"
 * Multi-pattern boss battle featuring:
 * 1. Executioner's Cleave rush
 * 2. Leaping Ground Slam with dual floor shockwaves & screen shake
 * 3. 12-way Hellfire bullet ring
 * 4. Phase 2 enrage & minion summon
 */
public final class BossEnemy extends Enemy {
    public static final String BOSS_NAME = "BELIAL - DEMON WARDEN";

    private double stateTimer;
    private int attackPattern = 0; // 0: Idle/Approach, 1: Cleave Windup, 2: Leap Slam, 3: Hellfire Burst
    private boolean phase2Triggered;
    private final Renderer renderer;
    private int currentAnimFrame = 0;

    public BossEnemy(SpriteManager sprites, Renderer renderer, double x, double y) {
        super(sprites, x, y, 36, 42, 400, 22, 100);
        this.renderer = renderer;
        this.stateTimer = 2.0;
    }

    @Override
    public void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles) {
        animTimer += dt * 4.0;
        stateTimer -= dt;

        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();
        facingRight = dx > 0;

        // Gravity
        if (!onGround) {
            vy += 680 * dt;
        }

        // Phase 2 Enrage Trigger
        if (!phase2Triggered && health <= maxHealth / 2) {
            phase2Triggered = true;
            SoundManager.play(SoundManager.Sound.BOSS_ROAR);
            renderer.addShake(0.5, 6);
            for (int i = 0; i < 8; i++) {
                particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
            }
        }

        double patternCooldown = phase2Triggered ? 1.4 : 2.2;

        if (attackPattern == 0) {
            // Idle & Stalking
            currentAnimFrame = ((int) animTimer) % 2;
            double speed = phase2Triggered ? 60.0 : 40.0;
            vx = (facingRight ? 1 : -1) * speed;

            if (stateTimer <= 0) {
                // Select next attack pattern
                attackPattern = (int) (Math.random() * 3) + 1;
                stateTimer = (attackPattern == 2) ? 1.6 : (attackPattern == 1) ? 1.1 : 1.3;
                if (attackPattern == 1) {
                    currentAnimFrame = 2; // Windup
                    SoundManager.play(SoundManager.Sound.SLASH);
                } else if (attackPattern == 2) {
                    // Leap into air
                    vy = -280;
                    vx = (facingRight ? 1 : -1) * 90;
                    onGround = false;
                    currentAnimFrame = 2;
                } else if (attackPattern == 3) {
                    // Hellfire Ring
                    currentAnimFrame = 3;
                    SoundManager.play(SoundManager.Sound.BOSS_ROAR);
                }
            }
        } else if (attackPattern == 1) {
            // Cleave Rush
            currentAnimFrame = 2;
            vx = (facingRight ? 1 : -1) * 160.0;
            if (stateTimer <= 0.3 && stateTimer > 0.1) {
                currentAnimFrame = 3;
                // Giant slash
                particles.add(Particle.createSlash(sprites, getCenterX() + (facingRight ? 28 : -28),
                        getCenterY(), facingRight ? 0 : Math.PI));
            }
            if (stateTimer <= 0) {
                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        } else if (attackPattern == 2) {
            // Leaping Ground Slam
            if (onGround && vy >= 0) {
                currentAnimFrame = 3;
                renderer.addShake(0.4, 5);
                SoundManager.play(SoundManager.Sound.EXPLOSION);

                // Twin shockwaves traveling left and right
                particles.add(Particle.createShockwave(sprites, getCenterX() + 16, y + height - 4, 180));
                particles.add(Particle.createShockwave(sprites, getCenterX() - 16, y + height - 4, -180));

                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        } else if (attackPattern == 3) {
            // Hellfire radial bullet burst
            currentAnimFrame = 3;
            vx = 0;
            if (stateTimer <= 0.6 && stateTimer > 0.4) {
                int bullets = phase2Triggered ? 14 : 10;
                for (int i = 0; i < bullets; i++) {
                    double angle = (Math.PI * 2 / bullets) * i;
                    projectiles.add(new Projectile(sprites, getCenterX(), getCenterY(), angle, 120.0,
                            Projectile.Type.FIREBALL, Projectile.Owner.ENEMY, 16));
                }
            }
            if (stateTimer <= 0) {
                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        }
    }

    @Override
    public List<Pickup> spawnDeathLoot() {
        List<Pickup> loot = new ArrayList<>();
        SoundManager.play(SoundManager.Sound.BOSS_ROAR);
        SoundManager.play(SoundManager.Sound.EXPLOSION);
        renderer.addShake(0.8, 8);

        // Shower of gold coins
        for (int i = 0; i < 24; i++) {
            loot.add(Pickup.createCoin(sprites, x + (i % 6) * 6, y + 10, 5));
        }
        // Healing hearts
        loot.add(Pickup.createHeart(sprites, x + 10, y, 40));
        loot.add(Pickup.createHeart(sprites, x + 24, y, 40));

        // Guaranteed Legendary Weapon drop
        Weapon legendary = (Math.random() < 0.5) ? Weapon.createGreatsword() : Weapon.createFireStaff();
        loot.add(Pickup.createWeapon(sprites, x + 18, y - 8, legendary));

        return loot;
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.slice("enemies/boss.png", 48, 48);
        int f = Math.min(frames.length - 1, Math.max(0, currentAnimFrame));
        drawSpriteWithFlash(g, frames[f], rx - 6, ry - 6, 48, 48, !facingRight);
    }
}
