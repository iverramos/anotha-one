package entities;

import audio.SoundManager;
import combat.DamageNumber;
import combat.Weapon;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hero character implementing Dungreed's signature mechanics:
 * 360-degree mouse aiming, double dash with i-frames and ghost trails,
 * ground and airborne double-jump, platform drop-down, primary/secondary weapon loadout,
 * and food fullness / stat scaling.
 */
public final class Player extends Entity {
    public static final int MAX_FULLNESS = 100;

    private final SpriteManager sprites;

    // Movement tuning
    private static final double MOVE_SPEED = 145.0;
    private static final double JUMP_FORCE = -275.0;
    private static final double GRAVITY = 750.0;
    private static final double MAX_FALL = 360.0;

    // Dash (Dodge)
    public static final int MAX_DASHES = 2;
    private int dashCharges = MAX_DASHES;
    private double dashRechargeTimer;
    private static final double DASH_RECHARGE_TIME = 1.0;
    private boolean dashing;
    private double dashTimer;
    private static final double DASH_DURATION = 0.16;
    private static final double DASH_SPEED = 430.0;
    private double dashVx, dashVy;
    private double dashGhostTimer;

    // Jump mechanics (Ground jump + 1 Air Double-Jump)
    private static final int MAX_JUMPS = 2;
    private int jumpsRemaining = MAX_JUMPS;
    private double coyoteTimer;
    private double jumpBufferTimer;
    private boolean droppingThroughPlatform;

    // Weapons & Inventory
    private final List<Weapon> weapons = new ArrayList<>();
    private final Set<Integer> ownedWeaponIds = new HashSet<>();
    private int activeWeaponIndex = 0;
    private double aimAngle;
    private boolean facingRight = true;

    // Stats & Food
    private int gold = 0;
    private int fullness = 0;
    private int bonusAttack = 0;
    private double bonusSpeed = 0;
    private double bonusCrit = 0.05; // 5% base crit
    private double iFrameTimer = 0;

    // Animation
    private double animTimer;
    private int walkFrame;

    public Player(SpriteManager sprites, double x, double y) {
        super(x, y, 14, 18, 100);
        this.sprites = sprites;

        // Starting weapons: Iron Shortsword + Royal Bow
        weapons.add(Weapon.createShortsword());
        weapons.add(Weapon.createBow());
        ownedWeaponIds.add(0);
        ownedWeaponIds.add(2);
    }

    public void update(double dt, boolean left, boolean right, boolean jumpDown, boolean jumpPressed,
                       boolean downPressed, boolean dashPressed, int mouseAimX, int mouseAimY,
                       List<Projectile> projectiles, List<Particle> particles, List<Enemy> enemies,
                       List<DamageNumber> damageNumbers) {
        // Aim direction
        double centerX = getCenterX();
        double centerY = getCenterY();
        aimAngle = Math.atan2(mouseAimY - centerY, mouseAimX - centerX);
        facingRight = mouseAimX >= centerX;

        // Timers
        if (hitFlashTimer > 0) hitFlashTimer -= dt;
        if (iFrameTimer > 0) iFrameTimer -= dt;
        for (Weapon w : weapons) w.update(dt);

        // Dash Recharge
        if (dashCharges < MAX_DASHES) {
            dashRechargeTimer += dt;
            if (dashRechargeTimer >= DASH_RECHARGE_TIME) {
                dashCharges++;
                dashRechargeTimer = 0;
            }
        }

        // Platform drop-down
        droppingThroughPlatform = downPressed;

        // Trigger Dash (Dodge)
        if (dashPressed && dashCharges > 0 && !dashing) {
            dashCharges--;
            dashing = true;
            dashTimer = DASH_DURATION;
            dashGhostTimer = 0;
            SoundManager.play(SoundManager.Sound.DASH);

            double moveX = (right ? 1 : 0) - (left ? 1 : 0);
            double dAngle;
            if (moveX != 0) {
                dAngle = Math.atan2(0, moveX);
            } else {
                dAngle = aimAngle;
            }
            dashVx = Math.cos(dAngle) * DASH_SPEED;
            dashVy = Math.sin(dAngle) * (DASH_SPEED * 0.4); // horizontal bias
        }

        if (dashing) {
            dashTimer -= dt;
            vx = dashVx;
            vy = dashVy;
            dashGhostTimer += dt;
            if (dashGhostTimer >= 0.035) {
                dashGhostTimer = 0;
                BufferedImage heroFrame = getHeroSprite(9);
                particles.add(Particle.createDashGhost(sprites, x - 5, y - 3, heroFrame));
            }

            // Dash Attack: damage enemies in path
            Rectangle dashBox = getBounds();
            for (Enemy e : enemies) {
                if (!e.isDead() && e.getBounds().intersects(dashBox) && e.getHitFlashTimer() <= 0) {
                    int dmg = 22 + bonusAttack;
                    e.takeDamage(dmg);
                    e.applyKnockback(dashVx * 0.5, -60);
                    SoundManager.play(SoundManager.Sound.HIT);
                    particles.add(Particle.createImpact(sprites, e.getCenterX(), e.getCenterY(), Particle.Type.BLOOD));
                    damageNumbers.add(new DamageNumber(e.getCenterX(), e.getY() - 4, String.valueOf(dmg), java.awt.Color.YELLOW, false));
                }
            }

            if (dashTimer <= 0) {
                dashing = false;
                vx *= 0.5;
                vy *= 0.5;
            }
        } else {
            // Standard horizontal movement with high-responsiveness air control
            double speed = (MOVE_SPEED * (1.0 + bonusSpeed));
            int dir = (right ? 1 : 0) - (left ? 1 : 0);
            vx = dir * speed;

            // Ground check & reset jumps
            if (onGround) {
                coyoteTimer = 0.10;
                jumpsRemaining = MAX_JUMPS;
            } else {
                coyoteTimer -= dt;
            }

            if (jumpPressed) {
                jumpBufferTimer = 0.12;
            } else {
                jumpBufferTimer -= dt;
            }

            // 1. Ground / Coyote Jump
            if (jumpBufferTimer > 0 && coyoteTimer > 0) {
                vy = JUMP_FORCE;
                jumpBufferTimer = 0;
                coyoteTimer = 0;
                onGround = false;
                jumpsRemaining = MAX_JUMPS - 1;
                SoundManager.play(SoundManager.Sound.JUMP);
                particles.add(Particle.createDust(sprites, x + width / 2.0, y + height));
            }
            // 2. Air Double-Jump
            else if (jumpPressed && jumpsRemaining > 0 && coyoteTimer <= 0) {
                jumpsRemaining--;
                vy = JUMP_FORCE * 0.95;
                onGround = false;
                SoundManager.play(SoundManager.Sound.DOUBLE_JUMP);

                // Double jump visual cue: particle puff at feet
                particles.add(Particle.createDust(sprites, x + width / 2.0, y + height));
                for (int p = 0; p < 4; p++) {
                    particles.add(Particle.createImpact(sprites, x + width / 2.0, y + height, Particle.Type.SPARK));
                }
            }

            // Variable jump height
            if (!jumpDown && vy < -100) {
                vy *= 0.65;
            }

            // Gravity
            vy = Math.min(MAX_FALL, vy + GRAVITY * dt);

            // Animation frame
            if (dir != 0 && onGround) {
                animTimer += dt * 10;
                walkFrame = ((int) animTimer) % 6;
            } else {
                animTimer = 0;
                walkFrame = 6; // idle
            }
        }
    }

    public void tryAttack(List<Projectile> projectiles, List<Particle> particles, List<Enemy> enemies, List<DamageNumber> damageNumbers) {
        if (dashing || weapons.isEmpty()) return;
        Weapon w = weapons.get(activeWeaponIndex);
        if (!w.canAttack()) return;

        w.triggerCooldown();
        double pCenterX = getCenterX();
        double pCenterY = getCenterY();

        boolean isCrit = Math.random() < bonusCrit;
        double critMult = isCrit ? 1.75 : 1.0;
        int totalDamage = (int) Math.round((w.getBaseDamage() + bonusAttack) * critMult);

        if (w.getType() == Weapon.Type.MELEE) {
            SoundManager.play(SoundManager.Sound.SLASH);
            // Spawn slash arc at sword position
            double slashDist = w.getRange() * 0.7;
            double slashX = pCenterX + Math.cos(aimAngle) * slashDist;
            double slashY = pCenterY + Math.sin(aimAngle) * slashDist;
            particles.add(Particle.createSlash(sprites, slashX, slashY, aimAngle));

            if (w.getId() == 8) { // Thunder Halberd sparks
                for (int k = 0; k < 3; k++) {
                    particles.add(Particle.createImpact(sprites, slashX, slashY, Particle.Type.SPARK));
                }
            }

            // Melee hit detection cone
            for (Enemy e : enemies) {
                if (e.isDead()) continue;
                double dx = e.getCenterX() - pCenterX;
                double dy = e.getCenterY() - pCenterY;
                double dist = Math.hypot(dx, dy);

                if (dist <= w.getRange() + e.getWidth() / 2.0) {
                    double targetAngle = Math.atan2(dy, dx);
                    double angleDiff = Math.abs(normalizeAngle(targetAngle - aimAngle));
                    if (angleDiff < Math.toRadians(65)) {
                        e.takeDamage(totalDamage);
                        double kx = Math.cos(aimAngle) * 160;
                        double ky = Math.sin(aimAngle) * 120 - 40;
                        e.applyKnockback(kx, ky);
                        SoundManager.play(SoundManager.Sound.HIT);
                        particles.add(Particle.createImpact(sprites, e.getCenterX(), e.getCenterY(), Particle.Type.BLOOD));
                        damageNumbers.add(new DamageNumber(e.getCenterX(), e.getY() - 4, totalDamage + (isCrit ? "!" : ""),
                                isCrit ? new java.awt.Color(0xfabc2a) : java.awt.Color.YELLOW, isCrit));
                    }
                }
            }
        } else if (w.getType() == Weapon.Type.RANGED) {
            if (w.getId() == 2) {
                // Royal Bow
                SoundManager.play(SoundManager.Sound.BOW_SHOOT);
                projectiles.add(new Projectile(sprites, pCenterX, pCenterY, aimAngle, w.getProjSpeed(),
                        Projectile.Type.ARROW, Projectile.Owner.PLAYER, totalDamage));
            } else if (w.getId() == 6) {
                // Heavy Crossbow
                SoundManager.play(SoundManager.Sound.BOW_SHOOT);
                projectiles.add(new Projectile(sprites, pCenterX, pCenterY, aimAngle, w.getProjSpeed(),
                        Projectile.Type.ARROW, Projectile.Owner.PLAYER, totalDamage));
            } else {
                // Flintlock Revolver
                SoundManager.play(SoundManager.Sound.GUN_SHOOT);
                projectiles.add(new Projectile(sprites, pCenterX, pCenterY, aimAngle, w.getProjSpeed(),
                        Projectile.Type.BULLET, Projectile.Owner.PLAYER, totalDamage));
            }
        } else if (w.getType() == Weapon.Type.MAGIC) {
            SoundManager.play(SoundManager.Sound.MAGIC_CAST);
            if (w.getId() == 7) {
                // Glacial Wand
                projectiles.add(new Projectile(sprites, pCenterX, pCenterY, aimAngle, w.getProjSpeed(),
                        Projectile.Type.DARK_ORB, Projectile.Owner.PLAYER, totalDamage));
                particles.add(Particle.createImpact(sprites, pCenterX, pCenterY, Particle.Type.SPARK));
            } else {
                // Staff of Embers
                projectiles.add(new Projectile(sprites, pCenterX, pCenterY, aimAngle, w.getProjSpeed(),
                        Projectile.Type.FIREBALL, Projectile.Owner.PLAYER, totalDamage));
            }
        }
    }

    private static double normalizeAngle(double a) {
        while (a > Math.PI) a -= Math.PI * 2;
        while (a < -Math.PI) a += Math.PI * 2;
        return a;
    }

    @Override
    public void update(double dt) {}

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        // Visual blinking / flashing during invulnerability frames (0.85s window)
        Composite oldComp = null;
        if (iFrameTimer > 0) {
            if (((int) (iFrameTimer * 24)) % 2 == 0) {
                oldComp = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            }
        }

        int frameIdx;
        if (dashing) {
            frameIdx = 9; // dash frame
        } else if (!onGround) {
            frameIdx = (vy < 0) ? 7 : 8; // jump or fall
        } else {
            frameIdx = walkFrame;
        }

        BufferedImage heroImg = getHeroSprite(frameIdx);

        // Render Hero
        int drawX = rx - 5;
        int drawY = ry - 3;
        if (!facingRight) {
            g.drawImage(heroImg, drawX + 24, drawY, -24, 24, null);
        } else {
            g.drawImage(heroImg, drawX, drawY, 24, 24, null);
        }

        // Render Held Weapon rotated towards aimAngle
        if (!weapons.isEmpty() && !dashing) {
            Weapon w = weapons.get(activeWeaponIndex);
            BufferedImage[] wImgs = sprites.slice("characters/weapons.png", 24, 24);
            int wImgIdx = w.getId() < wImgs.length ? w.getId() : 0;
            BufferedImage wImg = wImgs[wImgIdx];
            AffineTransform oldTransform = g.getTransform();
            int weaponPivotX = rx + width / 2;
            int weaponPivotY = ry + height / 2;

            g.translate(weaponPivotX, weaponPivotY);
            g.rotate(aimAngle);
            if (!facingRight) {
                g.scale(1, -1);
            }
            g.drawImage(wImg, 0, -12, null);
            g.setTransform(oldTransform);
        }

        if (oldComp != null) {
            g.setComposite(oldComp);
        }
    }

    private BufferedImage getHeroSprite(int frame) {
        BufferedImage[] frames = sprites.slice("characters/player.png", 24, 24);
        return frames[Math.min(frames.length - 1, Math.max(0, frame))];
    }

    @Override
    public void takeDamage(int amount) {
        if (dashing || iFrameTimer > 0 || dead) return;
        super.takeDamage(amount);
        iFrameTimer = 0.85; // 0.85s invulnerability window
        SoundManager.play(SoundManager.Sound.PLAYER_HURT);
    }

    public boolean isInvulnerable() {
        return dashing || iFrameTimer > 0;
    }

    public void switchWeapon(int delta) {
        if (weapons.isEmpty()) return;
        activeWeaponIndex = (activeWeaponIndex + delta + weapons.size()) % weapons.size();
    }

    public void setWeaponSlot(int slot) {
        if (slot >= 0 && slot < weapons.size()) {
            activeWeaponIndex = slot;
        }
    }

    public void addWeapon(Weapon w) {
        unlockWeapon(w.getId());
        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getId() == w.getId()) {
                weapons.set(i, w);
                activeWeaponIndex = i;
                return;
            }
        }
        if (weapons.size() < 3) {
            weapons.add(w);
            activeWeaponIndex = weapons.size() - 1;
        } else {
            weapons.set(activeWeaponIndex, w);
        }
    }

    public void equipWeapon(Weapon w) {
        unlockWeapon(w.getId());
        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getId() == w.getId()) {
                activeWeaponIndex = i;
                return;
            }
        }
        if (weapons.size() < 3) {
            weapons.add(w);
            activeWeaponIndex = weapons.size() - 1;
        } else {
            weapons.set(activeWeaponIndex, w);
        }
    }

    public boolean ownsWeapon(int weaponId) {
        return ownedWeaponIds.contains(weaponId);
    }

    public void unlockWeapon(int weaponId) {
        ownedWeaponIds.add(weaponId);
    }

    public Set<Integer> getOwnedWeaponIds() {
        return ownedWeaponIds;
    }

    // Stat Buff Helpers
    public void addGold(int g) { this.gold += g; }
    public void spendGold(int g) { this.gold = Math.max(0, this.gold - g); }
    public int getGold() { return gold; }

    public void addFullness(int f) { this.fullness = Math.min(MAX_FULLNESS, this.fullness + f); }
    public int getFullness() { return fullness; }

    public void increaseMaxHp(int bonus) {
        this.maxHealth += bonus;
        this.health += bonus;
    }
    public void addBonusAttack(int ba) { this.bonusAttack += ba; }
    public void addBonusSpeed(double bs) { this.bonusSpeed += bs; }
    public void addBonusCrit(double bc) { this.bonusCrit += bc; }

    public int getDashCharges() { return dashCharges; }
    public double getDashRechargeProgress() { return Math.min(1.0, dashRechargeTimer / DASH_RECHARGE_TIME); }
    public int getJumpsRemaining() { return jumpsRemaining; }
    public boolean isDroppingThroughPlatform() { return droppingThroughPlatform; }
    public List<Weapon> getWeapons() { return weapons; }
    public int getActiveWeaponIndex() { return activeWeaponIndex; }
    public Weapon getActiveWeapon() { return weapons.isEmpty() ? null : weapons.get(activeWeaponIndex); }
    public double getAimAngle() { return aimAngle; }
    public boolean isDashing() { return dashing; }
}
