package combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Weapons in Dungreed style: Melee swords/halberds, Ranged bows/revolvers/crossbows, and Magic staves/wands.
 * Each weapon has distinct stats, price, and rarity.
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
    private final int price;
    private final String description;

    private double cooldownTimer;

    public Weapon(int id, String name, Type type, Rarity rarity, int damage, double cooldown,
                  double range, double projSpeed, int price, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.baseDamage = damage;
        this.cooldownTime = cooldown;
        this.range = range;
        this.projSpeed = projSpeed;
        this.price = price;
        this.description = description;
        this.cooldownTimer = 0;
    }

    public static Weapon createShortsword() {
        return new Weapon(0, "Iron Shortsword", Type.MELEE, Rarity.COMMON, 18, 0.28, 32, 0, 0, "A swift, well-balanced blade.");
    }

    public static Weapon createGreatsword() {
        return new Weapon(1, "Titan Greatsword", Type.MELEE, Rarity.LEGENDARY, 46, 0.60, 44, 0, 130, "Massive cleaver that unleashes sweeping shockwaves.");
    }

    public static Weapon createBow() {
        return new Weapon(2, "Royal Bow", Type.RANGED, Rarity.COMMON, 22, 0.38, 0, 350, 0, "Fires piercing arrows across the chamber.");
    }

    public static Weapon createPistol() {
        return new Weapon(3, "Flintlock Revolver", Type.RANGED, Rarity.RARE, 18, 0.20, 0, 490, 55, "Rapid-fire firearm with deadly accuracy.");
    }

    public static Weapon createFireStaff() {
        return new Weapon(4, "Staff of Embers", Type.MAGIC, Rarity.LEGENDARY, 36, 0.46, 0, 260, 90, "Conjures explosive fireballs that blast foes.");
    }

    public static Weapon createShadowDaggers() {
        return new Weapon(5, "Shadow Daggers", Type.MELEE, Rarity.RARE, 15, 0.15, 26, 0, 70, "Twin obsidian blades with blinding attack speed.");
    }

    public static Weapon createArbalest() {
        return new Weapon(6, "Heavy Crossbow", Type.RANGED, Rarity.RARE, 40, 0.54, 0, 520, 85, "High-tension crossbow firing armor-piercing bolts.");
    }

    public static Weapon createFrostWand() {
        return new Weapon(7, "Glacial Wand", Type.MAGIC, Rarity.RARE, 24, 0.32, 0, 310, 100, "Hurls chilling glacial shards that freeze foes.");
    }

    public static Weapon createThunderHalberd() {
        return new Weapon(8, "Thunder Halberd", Type.MELEE, Rarity.LEGENDARY, 54, 0.72, 50, 0, 145, "Ancient polearm humming with lethal lightning arcs.");
    }

    /** Returns all available weapons catalog in the game. */
    public static List<Weapon> getAllWeapons() {
        List<Weapon> list = new ArrayList<>();
        list.add(createShortsword());
        list.add(createBow());
        list.add(createPistol());
        list.add(createShadowDaggers());
        list.add(createArbalest());
        list.add(createFireStaff());
        list.add(createFrostWand());
        list.add(createGreatsword());
        list.add(createThunderHalberd());
        return list;
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
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public double getCooldownProgress() { return Math.max(0, cooldownTimer / cooldownTime); }
}
