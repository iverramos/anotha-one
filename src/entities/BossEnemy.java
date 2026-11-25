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
 * 1. Executioner's Cleave rush with telegraphed windup
 * 2. Leaping Ground Slam with dual floor shockwaves + upward fireball arc
 * 3. 16-way Hellfire bullet ring
 * 4. Aimed 3-round Hellfire burst
 * 5. Phase 2 enrage & summon
 */
public final class BossEnemy extends Enemy {
    public static final String BOSS_NAME = "BELIAL - DEMON WARDEN";

    private double stateTimer;
    private int attackPattern = 0; // 0: Idle/Approach, 1: Cleave Windup, 2: Leap Slam, 3: Hellfire Ring, 4: Aimed Burst
    private boolean phase2Triggered;
    private final Renderer renderer;
    private int currentAnimFrame = 0;
    private int burstShotsLeft = 0;
    private double burstTimer = 0;

    public BossEnemy(SpriteManager sprites, Renderer renderer, double x, double y) {
        super(sprites, x, y, 36, 42, 420, 22, 100);
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

        // Phase 2 Enrage Trigger at 50% HP
        if (!phase2Triggered && health <= maxHealth / 2) {
            phase2Triggered = true;
            SoundManager.play(SoundManager.Sound.BOSS_ROAR);
            renderer.addShake(0.5, 6);
            for (int i = 0; i < 8; i++) {
                particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
            }
        }

        double patternCooldown = phase2Triggered ? 1.3 : 2.0;

        if (attackPattern == 0) {
            // Idle & Stalking
            currentAnimFrame = ((int) animTimer) % 2;
            double speed = phase2Triggered ? 65.0 : 45.0;
            vx = (facingRight ? 1 : -1) * speed;

            if (stateTimer <= 0) {
                // Select next attack pattern (1 to 4)
                attackPattern = (int) (Math.random() * 4) + 1;

                if (attackPattern == 1) {
                    // Cleave Windup (Telegraphed!)
                    stateTimer = 1.2;
                    currentAnimFrame = 2;
                    SoundManager.play(SoundManager.Sound.SLASH);
                    particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.SPARK));
                } else if (attackPattern == 2) {
                    // Leap into air
                    stateTimer = 1.6;
                    vy = -290;
                    vx = (facingRight ? 1 : -1) * 95;
                    onGround = false;
                    currentAnimFrame = 2;
                    SoundManager.play(SoundManager.Sound.JUMP);
                } else if (attackPattern == 3) {
                    // Hellfire Ring
                    stateTimer = 1.3;
                    currentAnimFrame = 3;
                    SoundManager.play(SoundManager.Sound.BOSS_ROAR);
                } else if (attackPattern == 4) {
                    // Aimed 3-Round Burst
                    stateTimer = 1.2;
                    currentAnimFrame = 3;
                    burstShotsLeft = 3;
                    burstTimer = 0.15;
                    SoundManager.play(SoundManager.Sound.MAGIC_CAST);
                }
            }
        } else if (attackPattern == 1) {
            // Cleave Rush
            currentAnimFrame = 2;
            vx = (facingRight ? 1 : -1) * 170.0;
            if (stateTimer <= 0.35 && stateTimer > 0.1) {
                currentAnimFrame = 3;
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
                particles.add(Particle.createShockwave(sprites, getCenterX() + 16, y + height - 4, 190));
                particles.add(Particle.createShockwave(sprites, getCenterX() - 16, y + height - 4, -190));

                // 3 upward erupting fireballs to punish standing close
                for (int i = 0; i < 3; i++) {
                    double angle = -Math.PI / 4 - (Math.PI / 2) * (i / 2.0);
                    projectiles.add(new Projectile(sprites, getCenterX(), y + height - 8, angle, 140.0,
                            Projectile.Type.FIREBALL, Projectile.Owner.ENEMY, 15));
                }

                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        } else if (attackPattern == 3) {
            // Hellfire radial bullet burst
            currentAnimFrame = 3;
            vx = 0;
            if (stateTimer <= 0.65 && stateTimer > 0.45) {
                int bullets = phase2Triggered ? 16 : 12;
                for (int i = 0; i < bullets; i++) {
                    double angle = (Math.PI * 2 / bullets) * i;
                    projectiles.add(new Projectile(sprites, getCenterX(), getCenterY(), angle, 125.0,
                            Projectile.Type.FIREBALL, Projectile.Owner.ENEMY, 16));
                }
            }
            if (stateTimer <= 0) {
                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        } else if (attackPattern == 4) {
            // Aimed burst
            vx = 0;
            currentAnimFrame = 3;
            burstTimer -= dt;
            if (burstTimer <= 0 && burstShotsLeft > 0) {
                burstTimer = 0.22;
                burstShotsLeft--;
                double angle = Math.atan2(dy, dx);
                projectiles.add(new Projectile(sprites, getCenterX(), getCenterY(), angle, 150.0,
                        Projectile.Type.FIREBALL, Projectile.Owner.ENEMY, 14));
                SoundManager.play(SoundManager.Sound.MAGIC_CAST);
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

        BufferedImage[] frames = sprites.sprite("boss_belial");
        int f = Math.min(frames.length - 1, Math.max(0, currentAnimFrame));
        drawSpriteWithFlash(g, frames[f], rx - 6, ry - 6, 48, 48, !facingRight);
    }
}
