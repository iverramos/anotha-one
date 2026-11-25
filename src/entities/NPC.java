package entities;

import rendering.SpriteManager;
import ui.DialogueBox;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Non-Player Character (NPC) standing in towns, camps, and bazaars.
 * Initiates interactive DialogueBox conversations with Hero responses and choices.
 */
public final class NPC {
    public enum Role {
        INSTRUCTOR_GARETH,
        SCOUT_LYRA,
        FORGER_VORN
    }

    private final SpriteManager sprites;
    private final int spriteIndex;
    private final String name;
    private final Role role;
    private final double x;
    private final double y;
    private final int width = 14;
    private final int height = 18;
    private double bobTimer;
    private boolean facingRight = true;

    public NPC(SpriteManager sprites, int spriteIndex, String name, Role role, double x, double y) {
        this.sprites = sprites;
        this.spriteIndex = spriteIndex;
        this.name = name;
        this.role = role;
        this.x = x;
        this.y = y;
    }

    public static NPC createGareth(SpriteManager sprites, double x, double y) {
        return new NPC(sprites, 0, "Instructor Gareth", Role.INSTRUCTOR_GARETH, x, y);
    }

    public static NPC createLyra(SpriteManager sprites, double x, double y) {
        return new NPC(sprites, 1, "Scout Lyra", Role.SCOUT_LYRA, x, y);
    }

    public static NPC createVorn(SpriteManager sprites, double x, double y) {
        return new NPC(sprites, 2, "Master Forger Vorn", Role.FORGER_VORN, x, y);
    }

    public void update(double dt, double playerCenterX) {
        bobTimer += dt * 3.0;
        facingRight = playerCenterX >= getCenterX();
    }

    public boolean isPlayerNearby(double playerX, double playerY) {
        return Math.hypot(playerX - getCenterX(), playerY - getCenterY()) < 42.0;
    }

    public void render(Graphics2D g, int camX, int camY) {
        int rx = (int) Math.round(x - camX);
        int ry = (int) Math.round(y - camY);

        BufferedImage[] npcFrames = sprites.sprite("npcs");
        int idx = Math.min(npcFrames.length - 1, Math.max(0, spriteIndex));
        BufferedImage img = npcFrames[idx];

        // Feet sit at sprite row 22 -> align with height 18 via ry - 5
        int drawX = rx - 5;
        int drawY = ry - 5;
        if (!facingRight) {
            g.drawImage(img, drawX + 24, drawY, -24, 24, null);
        } else {
            g.drawImage(img, drawX, drawY, 24, 24, null);
        }
    }

    public void startDialogue(DialogueBox box, Runnable onTalkedAction, Runnable openShopAction) {
        List<DialogueBox.Line> lines = new ArrayList<>();
        Color heroColor = new Color(0x38d7f2);

        if (role == Role.INSTRUCTOR_GARETH) {
            Color npcColor = new Color(0xfabc2a);
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Hail, rookie! Glad to see another adventurer brave enough to take on the Frontier Dungeon."));
            lines.add(new DialogueBox.Line("Hero", heroColor, true,
                    "I'm ready to descend. Any tips before I face whatever lurks past that portal?"));
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Stay light on your feet! Press [SPACE] twice for a Double-Jump, and [Right Click] or [Shift] to Dash cleanly through projectiles."));

            List<DialogueBox.Choice> choices = new ArrayList<>();
            choices.add(new DialogueBox.Choice("Got it! Time to test my weapon.", -1, null));
            choices.add(new DialogueBox.Choice("How do Fullness & Shops work?", 4, null));
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Go ahead and strike that Training Dummy to warm up. Anything else you'd like to ask?", choices));
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Chef Han's Diner boosts your stats and heals you, filling your Fullness gauge. Clearing rooms digests Fullness so you can feast again!"));
            box.start(lines, onTalkedAction);
        } else if (role == Role.FORGER_VORN) {
            Color npcColor = new Color(0xff9e38);
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Clang and cinder! Need sharper steel or arcane firepower before the next chamber?"));
            lines.add(new DialogueBox.Line("Hero", heroColor, true,
                    "Show me what you've forged, Master Vorn."));

            List<DialogueBox.Choice> choices = new ArrayList<>();
            choices.add(new DialogueBox.Choice("Open Weapon Armory", -1, openShopAction));
            choices.add(new DialogueBox.Choice("Ask about Weapon Types", 3, null));
            choices.add(new DialogueBox.Choice("Maybe later.", -1, null));
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Every blade, bow, and wand here is battle-tested. What'll it be?", choices));

            List<DialogueBox.Choice> afterTip = new ArrayList<>();
            afterTip.add(new DialogueBox.Choice("Browse Weapons Now", -1, openShopAction));
            afterTip.add(new DialogueBox.Choice("Thanks, Vorn!", -1, null));
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Swords cleave close foes, bows & revolvers snipe from afar, and staves conjure magic! Equip up to 3 in your hotbar.", afterTip));
            box.start(lines, null);
        } else if (role == Role.SCOUT_LYRA) {
            Color npcColor = new Color(0x62db58);
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "So you defeated Belial... Welcome to the Ruined Kingdom and the Sunken Catacombs beneath."));
            lines.add(new DialogueBox.Line("Hero", heroColor, true,
                    "The air feels heavy down here. What kind of threats await in B2F?"));
            lines.add(new DialogueBox.Line(name, npcColor, false,
                    "Deep spiked chasms and Malakor the Void Archon! Use your Double-Jump and Air Dash to cross the abyss and dodge his void spirals."));
            box.start(lines, null);
        }
    }

    public static void startHanDinerDialogue(DialogueBox box, Runnable openFoodMenu) {
        List<DialogueBox.Line> lines = new ArrayList<>();
        Color hanColor = new Color(0xfabc2a);
        Color heroColor = new Color(0x38d7f2);

        lines.add(new DialogueBox.Line("Chef Han", hanColor, false,
                "Step right up! Nothing fights off dungeon gloom like a sizzling roast or dragon ramen!"));
        lines.add(new DialogueBox.Line("Hero", heroColor, true,
                "Smells amazing, Han. What's cooking today?"));

        List<DialogueBox.Choice> choices = new ArrayList<>();
        choices.add(new DialogueBox.Choice("Browse Diner Menu", -1, openFoodMenu));
        choices.add(new DialogueBox.Choice("Just passing through.", -1, null));
        lines.add(new DialogueBox.Line("Chef Han", hanColor, false,
                "Eat up to raise Max HP, ATK, Speed, and Crit — just watch your Fullness gauge!", choices));
        box.start(lines, null);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }
    public String getName() { return name; }
    public Role getRole() { return role; }
}
