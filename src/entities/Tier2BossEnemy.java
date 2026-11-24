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
 * Tier 2 Dungeon Boss: "MALAKOR - Void Archon of B2F"
 * Intense multi-pattern boss encounter testing double jumping and dash evasion:
 * 1. Abyssal Cleave Rush (telegraphed charge with massive void slash)
 * 2. Leaping Abyssal Slam (dual ground shockwaves + 6 upward dark eruptive bursts)
 * 3. 5-Way Fan Spread Dark Orbs (aimed tracking burst)
 * 4. Void Nova Spiral (20-orb expanding radial ring)
 * 5. Phase 2 Enrage & Teleport Ambush
 */
public final class Tier2BossEnemy extends Enemy {
    public static final String BOSS_NAME = "MALAKOR - VOID ARCHON";

    private double stateTimer;
    private int attackPattern = 0; // 0: Stalk, 1: Void Cleave, 2: Leap Slam, 3: Fan Spread, 4: Void Nova
    private boolean phase2Triggered;
    private final Renderer renderer;
    private int currentAnimFrame = 0;
    private double burstTimer = 0;
    private int burstCount = 0;

    public Tier2BossEnemy(SpriteManager sprites, Renderer renderer, double x, double y) {
        super(sprites, x, y, 40, 46, 650, 26, 150);
        this.renderer = renderer;
        this.stateTimer = 2.0;
    }

    @Override
    public void updateAI(double dt, Player player, List<Projectile> projectiles, List<Particle> particles) {
        animTimer += dt * 4.5;
        stateTimer -= dt;

        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();
        facingRight = dx > 0;

        // Gravity
        if (!onGround) {
            vy += 700 * dt;
        }

        // Phase 2 Enrage Trigger at 50% HP
        if (!phase2Triggered && health <= maxHealth / 2) {
            phase2Triggered = true;
            SoundManager.play(SoundManager.Sound.BOSS_ROAR);
            renderer.addShake(0.6, 7);
            for (int i = 0; i < 12; i++) {
                particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
            }
        }

        double patternCooldown = phase2Triggered ? 1.1 : 1.8;

        if (attackPattern == 0) {
            // Idle & Stalking
            currentAnimFrame = ((int) animTimer) % 2;
            double speed = phase2Triggered ? 75.0 : 50.0;
            vx = (facingRight ? 1 : -1) * speed;

            if (stateTimer <= 0) {
                // Select attack pattern (1 to 4)
                attackPattern = (int) (Math.random() * 4) + 1;

                if (phase2Triggered && Math.random() < 0.35) {
                    // Teleport ambush behind player
                    particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
                    double targetX = player.getCenterX() + (player.getVx() >= 0 ? -60 : 60);
                    setPosition(Math.max(60, Math.min(600, targetX)), y);
                    particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.FIRE));
                    SoundManager.play(SoundManager.Sound.DASH);
                }

                if (attackPattern == 1) {
                    // Windup for Cleave
                    stateTimer = 1.0;
                    currentAnimFrame = 2;
                    SoundManager.play(SoundManager.Sound.SLASH);
                    // Telegraph spark particles
                    particles.add(Particle.createImpact(sprites, getCenterX(), getCenterY(), Particle.Type.SPARK));
                } else if (attackPattern == 2) {
                    // Leap into air
                    stateTimer = 1.6;
                    vy = -320;
                    vx = (facingRight ? 1 : -1) * 110;
                    onGround = false;
                    currentAnimFrame = 2;
                    SoundManager.play(SoundManager.Sound.JUMP);
                } else if (attackPattern == 3) {
                    // 5-Way Fan Spread
                    stateTimer = 1.2;
                    currentAnimFrame = 3;
                    burstCount = 0;
                    burstTimer = 0.15;
                    SoundManager.play(SoundManager.Sound.MAGIC_CAST);
                } else if (attackPattern == 4) {
                    // Void Nova Spiral
                    stateTimer = 1.4;
                    currentAnimFrame = 3;
                    SoundManager.play(SoundManager.Sound.BOSS_ROAR);
                }
            }
        } else if (attackPattern == 1) {
            // Abyssal Cleave Rush
            currentAnimFrame = 2;
            vx = (facingRight ? 1 : -1) * 190.0;
            if (stateTimer <= 0.35 && stateTimer > 0.1) {
                currentAnimFrame = 3;
                particles.add(Particle.createSlash(sprites, getCenterX() + (facingRight ? 32 : -32),
                        getCenterY(), facingRight ? 0 : Math.PI));
            }
            if (stateTimer <= 0) {
                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        } else if (attackPattern == 2) {
            // Leaping Abyssal Slam
            if (onGround && vy >= 0) {
                currentAnimFrame = 3;
                renderer.addShake(0.5, 6);
                SoundManager.play(SoundManager.Sound.EXPLOSION);

                // Quad shockwaves traveling left and right
                particles.add(Particle.createShockwave(sprites, getCenterX() + 18, y + height - 4, 210));
                particles.add(Particle.createShockwave(sprites, getCenterX() - 18, y + height - 4, -210));

                // 6 upward erupting dark projectiles
                for (int i = 0; i < 6; i++) {
                    double angle = -Math.PI / 6 - (Math.PI * 2 / 3) * (i / 5.0);
                    projectiles.add(new Projectile(sprites, getCenterX(), y + height - 8, angle, 160.0,
                            Projectile.Type.DARK_ORB, Projectile.Owner.ENEMY, 16));
                }

                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        } else if (attackPattern == 3) {
            // 5-Way Fan Spread Dark Orbs
            vx = 0;
            currentAnimFrame = 3;
            burstTimer -= dt;
            if (burstTimer <= 0 && burstCount < (phase2Triggered ? 2 : 1)) {
                burstTimer = 0.25;
                burstCount++;
                double baseAngle = Math.atan2(dy, dx);
                int count = 5;
                double spread = Math.toRadians(40);
                for (int i = 0; i < count; i++) {
                    double angle = baseAngle - spread / 2 + (spread / (count - 1)) * i;
                    projectiles.add(new Projectile(sprites, getCenterX(), getCenterY(), angle, 140.0,
                            Projectile.Type.DARK_ORB, Projectile.Owner.ENEMY, 15));
                }
                SoundManager.play(SoundManager.Sound.MAGIC_CAST);
            }
            if (stateTimer <= 0) {
                attackPattern = 0;
                stateTimer = patternCooldown;
            }
        } else if (attackPattern == 4) {
            // Void Nova Spiral Ring
            vx = 0;
            currentAnimFrame = 3;
            if (stateTimer <= 0.8 && stateTimer > 0.6) {
                int bullets = phase2Triggered ? 20 : 16;
                for (int i = 0; i < bullets; i++) {
                    double angle = (Math.PI * 2 / bullets) * i;
                    projectiles.add(new Projectile(sprites, getCenterX(), getCenterY(), angle, 115.0,
                            Projectile.Type.DARK_ORB, Projectile.Owner.ENEMY, 16));
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
        renderer.addShake(0.9, 10);

        // Generous shower of gold coins
        for (int i = 0; i < 32; i++) {
            loot.add(Pickup.createCoin(sprites, x + (i % 8) * 5, y + 10, 5));
        }
        // Healing hearts
        loot.add(Pickup.createHeart(sprites, x + 8, y, 40));
        loot.add(Pickup.createHeart(sprites, x + 24, y, 40));

        // Guaranteed Legendary Weapon drop
        Weapon legendary = (Math.random() < 0.5) ? Weapon.createThunderHalberd() : Weapon.createGreatsword();
        loot.add(Pickup.createWeapon(sprites, x + 18, y - 8, legendary));

        return loot;
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] frames = sprites.slice("enemies/boss.png", 48, 48);
        int f = Math.min(frames.length - 1, Math.max(0, currentAnimFrame));
        drawSpriteWithFlash(g, frames[f], rx - 4, ry - 4, 48, 48, !facingRight);
    }
}
