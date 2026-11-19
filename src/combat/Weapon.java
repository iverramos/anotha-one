package combat;

import java.awt.Color;

/**
 * Weapons in Dungreed style: Melee swords, Ranged bows & guns, and Magic staves.
 */
public final class Weapon {
    public enum Type { MELEE, RANGED, MAGIC }
    public enum Rarity {
        COMMON(new Color(0xb0b8c4)),
        RARE(new Color(0x38d7f2)),
        LEGENDARY(new Color(0xfabc2a));

        public final Color color;
        Rarity(Color color) { this.color = color; }
    }

    private final int id;
    private final String name;
    private final Type type;
    private final Rarity rarity;
    private final int baseDamage;
    private final double cooldownTime;
    private final double range;
    private final double projSpeed;
    private final String description;

    private double cooldownTimer;

    public Weapon(int id, String name, Type type, Rarity rarity, int damage, double cooldown, double range, double projSpeed, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.baseDamage = damage;
        this.cooldownTime = cooldown;
        this.range = range;
        this.projSpeed = projSpeed;
        this.description = description;
        this.cooldownTimer = 0;
    }

    public static Weapon createShortsword() {
        return new Weapon(0, "Iron Shortsword", Type.MELEE, Rarity.COMMON, 18, 0.28, 30, 0, "A swift, well-balanced blade.");
    }

    public static Weapon createGreatsword() {
        return new Weapon(1, "Titan Greatsword", Type.MELEE, Rarity.LEGENDARY, 44, 0.62, 42, 0, "Massive cleaver that unleashes sweeping shockwaves.");
    }

    public static Weapon createBow() {
        return new Weapon(2, "Royal Bow", Type.RANGED, Rarity.COMMON, 22, 0.38, 0, 340, "Fires piercing arrows across the chamber.");
    }

    public static Weapon createPistol() {
        return new Weapon(3, "Flintlock Revolver", Type.RANGED, Rarity.RARE, 16, 0.20, 0, 480, "Rapid-fire firearm with deadly accuracy.");
    }

    public static Weapon createFireStaff() {
        return new Weapon(4, "Staff of Embers", Type.MAGIC, Rarity.LEGENDARY, 34, 0.48, 0, 240, "Conjures explosive fireballs that blast foes.");
    }

    public void update(double dt) {
        if (cooldownTimer > 0) cooldownTimer -= dt;
    }

    public boolean canAttack() {
        return cooldownTimer <= 0;
    }

    public void triggerCooldown() {
        cooldownTimer = cooldownTime;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public int getBaseDamage() { return baseDamage; }
    public double getCooldownTime() { return cooldownTime; }
    public double getRange() { return range; }
    public double getProjSpeed() { return projSpeed; }
    public String getDescription() { return description; }
    public double getCooldownProgress() { return Math.max(0, cooldownTimer / cooldownTime); }
}
