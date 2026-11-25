package ui;

import audio.SoundManager;
import entities.Player;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Interactive step-by-step tutorial in Tier 1's starting area.
 * Teaches Movement, NPC Interaction, and Combat against the Training Dummy
 * one objective at a time, then unlocks the dungeon portal.
 */
public final class TutorialManager {
    public enum Step {
        MOVEMENT,
        INTERACT,
        ATTACK,
        COMPLETE
    }

    private static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONT_BODY  = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    private static final Font FONT_PROG  = new Font(Font.SANS_SERIF, Font.BOLD, 14);

    private Step step = Step.MOVEMENT;
    private boolean movedLeft;
    private boolean movedRight;
    private boolean jumped;
    private boolean talkedToNpc;
    private int dummyHits;
    private double completeBannerTimer;

    public void reset() {
        step = Step.MOVEMENT;
        movedLeft = false;
        movedRight = false;
        jumped = false;
        talkedToNpc = false;
        dummyHits = 0;
        completeBannerTimer = 0;
    }

    public void update(double dt, Player player, boolean leftDown, boolean rightDown, boolean jumpPressed) {
        if (step == Step.MOVEMENT) {
            if (leftDown && player.getVx() < -10) movedLeft = true;
            if (rightDown && player.getVx() > 10) movedRight = true;
            if (jumpPressed || !player.isOnGround()) jumped = true;

            if (movedLeft && movedRight && jumped) {
                step = Step.INTERACT;
                SoundManager.play(SoundManager.Sound.COIN);
            }
        } else if (step == Step.INTERACT) {
            if (talkedToNpc) {
                step = Step.ATTACK;
                SoundManager.play(SoundManager.Sound.COIN);
            }
        } else if (step == Step.ATTACK) {
            if (dummyHits >= 3) {
                step = Step.COMPLETE;
                completeBannerTimer = 6.0;
                SoundManager.play(SoundManager.Sound.ROOM_CLEAR);
            }
        } else if (step == Step.COMPLETE) {
            if (completeBannerTimer > 0) {
                completeBannerTimer = Math.max(0, completeBannerTimer - dt);
            }
        }
    }

    public void notifyNpcTalked() {
        talkedToNpc = true;
    }

    public void notifyDummyHit() {
        if (step == Step.ATTACK) {
            dummyHits++;
        }
    }

    public boolean isCompleted() {
        return step == Step.COMPLETE;
    }

    public Step getStep() { return step; }

    public void render(Graphics2D g, boolean inStartRoom) {
        if (!inStartRoom) return;
        if (step == Step.COMPLETE && completeBannerTimer <= 0) return;

        int boxW = 540;
        int boxH = 74;
        int boxX = (Constants.UI_W - boxW) / 2;
        int boxY = 16;

        g.setColor(new Color(18, 15, 28, 222));
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(step == Step.COMPLETE ? new Color(0x62db58) : new Color(0xfabc2a));
        g.drawRect(boxX, boxY, boxW, boxH);
        g.drawRect(boxX + 2, boxY + 2, boxW - 4, boxH - 4);

        String title;
        String desc;
        String progress;
        Color accent;

        switch (step) {
            case MOVEMENT:
                title = "TUTORIAL 1/3 — MOVEMENT & JUMPING";
                desc = "Move with [A] / [D] (or Arrows) and Jump with [SPACE] / [W].";
                progress = (movedLeft ? "[✓] Left  " : "[ ] Left  ")
                         + (movedRight ? "[✓] Right  " : "[ ] Right  ")
                         + (jumped ? "[✓] Jump" : "[ ] Jump");
                accent = new Color(0x38d7f2);
                break;
            case INTERACT:
                title = "TUTORIAL 2/3 — NPC INTERACTION";
                desc = "Walk to Instructor Gareth and press [E] (or [F]) to talk.";
                progress = talkedToNpc ? "[✓] Spoken to Gareth" : "[ ] Press [E] near Instructor Gareth";
                accent = new Color(0xfabc2a);
                break;
            case ATTACK:
                title = "TUTORIAL 3/3 — COMBAT PRACTICE";
                desc = "Aim with the Mouse and strike the Training Dummy ([Left Click] / [J]).";
                progress = "Dummy Hits: " + Math.min(3, dummyHits) + " / 3   (Press [Q] to switch weapons)";
                accent = new Color(0xff7852);
                break;
            default:
                title = "★ TUTORIAL COMPLETE! ★";
                desc = "You're ready for battle! Enter the glowing Eastern Portal to begin.";
                progress = "Tip: Clear rooms to digest Fullness, and visit shops to upgrade!";
                accent = new Color(0x62db58);
                break;
        }

        g.setFont(FONT_TITLE);
        g.setColor(accent);
        g.drawString(title, boxX + 18, boxY + 23);

        g.setFont(FONT_BODY);
        g.setColor(Color.WHITE);
        g.drawString(desc, boxX + 18, boxY + 45);

        g.setFont(FONT_PROG);
        g.setColor(new Color(0xdce4f0));
        g.drawString(progress, boxX + 18, boxY + 64);
    }
}
