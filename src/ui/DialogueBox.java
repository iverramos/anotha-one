package ui;

import audio.SoundManager;
import utilities.Constants;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive RPG Dialogue Box rendered at 960x540 UI resolution.
 * Supports multi-line conversations between NPCs and the Hero, typewriter reveal,
 * interactive branching choices, and clean closing without leaking input.
 */
public final class DialogueBox {
    public static final class Choice {
        public final String label;
        public final int nextIndex; // -1 ends dialogue
        public final Runnable action;

        public Choice(String label, int nextIndex, Runnable action) {
            this.label = label;
            this.nextIndex = nextIndex;
            this.action = action;
        }
    }

    public static final class Line {
        public final String speaker;
        public final Color speakerColor;
        public final boolean isHero;
        public final String text;
        public final List<Choice> choices;

        public Line(String speaker, Color speakerColor, boolean isHero, String text) {
            this(speaker, speakerColor, isHero, text, null);
        }

        public Line(String speaker, Color speakerColor, boolean isHero, String text, List<Choice> choices) {
            this.speaker = speaker;
            this.speakerColor = speakerColor;
            this.isHero = isHero;
            this.text = text;
            this.choices = choices;
        }
    }

    private static final Font FONT_SPEAKER = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FONT_TEXT    = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private static final Font FONT_CHOICE  = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font FONT_PROMPT  = new Font(Font.SANS_SERIF, Font.BOLD, 13);

    private boolean open;
    private List<Line> lines = new ArrayList<>();
    private int currentIndex = 0;
    private double revealedChars = 0;
    private int selectedChoice = 0;
    private double blinkTimer = 0;
    private Runnable onComplete;

    public boolean isOpen() { return open; }

    public void start(List<Line> conversation, Runnable onComplete) {
        if (conversation == null || conversation.isEmpty()) return;
        this.lines = conversation;
        this.currentIndex = 0;
        this.revealedChars = 0;
        this.selectedChoice = 0;
        this.blinkTimer = 0;
        this.onComplete = onComplete;
        this.open = true;
        SoundManager.play(SoundManager.Sound.UI_CLICK);
    }

    public void close() {
        if (!open) return;
        open = false;
        SoundManager.play(SoundManager.Sound.UI_CLICK);
        if (onComplete != null) {
            Runnable cb = onComplete;
            onComplete = null;
            cb.run();
        }
    }

    public void update(double dt) {
        if (!open) return;
        blinkTimer += dt;
        Line line = lines.get(currentIndex);
        if (revealedChars < line.text.length()) {
            revealedChars = Math.min(line.text.length(), revealedChars + 55.0 * dt);
        }
    }

    public void handleInput(boolean upPressed, boolean downPressed, boolean advancePressed,
                            boolean escPressed, int slotPressed,
                            int mouseX, int mouseY, boolean mouseClicked) {
        if (!open) return;

        if (escPressed) {
            close();
            return;
        }

        Line line = lines.get(currentIndex);
        boolean fullyRevealed = revealedChars >= line.text.length();
        boolean hasChoices = (line.choices != null && !line.choices.isEmpty());

        if (hasChoices && fullyRevealed) {
            if (upPressed) {
                selectedChoice = (selectedChoice - 1 + line.choices.size()) % line.choices.size();
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            } else if (downPressed) {
                selectedChoice = (selectedChoice + 1) % line.choices.size();
                SoundManager.play(SoundManager.Sound.UI_CLICK);
            }

            if (slotPressed >= 0 && slotPressed < line.choices.size()) {
                triggerChoice(line.choices.get(slotPressed));
                return;
            }

            // Mouse hover/click on choice buttons
            int boxW = 820, boxH = 148;
            int boxX = (Constants.UI_W - boxW) / 2;
            int boxY = Constants.UI_H - boxH - 16;
            int choiceX = boxX + 460;
            int choiceW = 336;
            int choiceH = 26;
            int startY = boxY + boxH - 12 - line.choices.size() * (choiceH + 4);

            for (int i = 0; i < line.choices.size(); i++) {
                int cy = startY + i * (choiceH + 4);
                Rectangle rect = new Rectangle(choiceX, cy, choiceW, choiceH);
                if (rect.contains(mouseX, mouseY)) {
                    selectedChoice = i;
                    if (mouseClicked) {
                        triggerChoice(line.choices.get(i));
                        return;
                    }
                }
            }
        }

        if (advancePressed || mouseClicked) {
            if (!fullyRevealed) {
                revealedChars = line.text.length();
            } else if (hasChoices) {
                triggerChoice(line.choices.get(selectedChoice));
            } else {
                advanceLine();
            }
        }
    }

    private void triggerChoice(Choice c) {
        SoundManager.play(SoundManager.Sound.UI_CLICK);
        if (c.action != null) {
            c.action.run();
        }
        if (c.nextIndex < 0 || c.nextIndex >= lines.size()) {
            close();
        } else {
            currentIndex = c.nextIndex;
            revealedChars = 0;
            selectedChoice = 0;
        }
    }

    private void advanceLine() {
        SoundManager.play(SoundManager.Sound.UI_CLICK);
        if (currentIndex + 1 < lines.size()) {
            currentIndex++;
            revealedChars = 0;
            selectedChoice = 0;
        } else {
            close();
        }
    }

    public void render(Graphics2D g) {
        if (!open || lines.isEmpty()) return;

        Line line = lines.get(currentIndex);
        int boxW = 820, boxH = 148;
        int boxX = (Constants.UI_W - boxW) / 2;
        int boxY = Constants.UI_H - boxH - 16;

        // Dialogue panel backdrop
        g.setColor(new Color(18, 15, 26, 238));
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(new Color(0xfabc2a));
        g.drawRect(boxX, boxY, boxW, boxH);
        g.drawRect(boxX + 3, boxY + 3, boxW - 6, boxH - 6);

        // Speaker Nameplate Badge
        g.setFont(FONT_SPEAKER);
        String speakerTag = line.isHero ? ("YOU (" + line.speaker + ")") : line.speaker;
        int tagW = g.getFontMetrics().stringWidth(speakerTag) + 24;
        int tagX = line.isHero ? (boxX + boxW - tagW - 18) : (boxX + 18);
        int tagY = boxY - 14;

        g.setColor(new Color(0x282333));
        g.fillRect(tagX, tagY, tagW, 28);
        g.setColor(line.speakerColor);
        g.drawRect(tagX, tagY, tagW, 28);
        g.drawString(speakerTag, tagX + 12, tagY + 20);

        // Body text with typewriter + word wrap
        boolean hasChoices = (line.choices != null && !line.choices.isEmpty());
        int textMaxW = hasChoices ? 420 : (boxW - 48);
        int visibleLen = (int) Math.floor(revealedChars);
        String shown = line.text.substring(0, Math.min(line.text.length(), visibleLen));

        g.setFont(FONT_TEXT);
        g.setColor(new Color(0xedf2fa));
        drawWrappedText(g, shown, boxX + 24, boxY + 42, textMaxW, 22);

        // Render choices if revealed
        boolean fullyRevealed = visibleLen >= line.text.length();
        if (hasChoices && fullyRevealed) {
            int choiceX = boxX + 460;
            int choiceW = 336;
            int choiceH = 26;
            int startY = boxY + boxH - 12 - line.choices.size() * (choiceH + 4);

            g.setFont(FONT_CHOICE);
            for (int i = 0; i < line.choices.size(); i++) {
                int cy = startY + i * (choiceH + 4);
                boolean sel = (i == selectedChoice);
                g.setColor(sel ? new Color(0x383347) : new Color(0x1f1b29));
                g.fillRect(choiceX, cy, choiceW, choiceH);
                g.setColor(sel ? new Color(0xfabc2a) : new Color(0x555c78));
                g.drawRect(choiceX, cy, choiceW, choiceH);

                g.setColor(sel ? new Color(0xffea78) : new Color(0xc4d0e3));
                g.drawString("[" + (i + 1) + "] " + line.choices.get(i).label, choiceX + 10, cy + 18);
            }
        }

        // Advance prompt
        if (!hasChoices && (((int) (blinkTimer * 3)) % 2 == 0 || !fullyRevealed)) {
            g.setFont(FONT_PROMPT);
            String prompt = fullyRevealed ? "[SPACE / E] Continue ▼" : "[SPACE] Skip";
            int pw = g.getFontMetrics().stringWidth(prompt);
            g.setColor(new Color(0xfabc2a));
            g.drawString(prompt, boxX + boxW - pw - 20, boxY + boxH - 14);
        }
    }

    private static void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
                g.drawString(line.toString(), x, curY);
                curY += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            g.drawString(line.toString(), x, curY);
        }
    }
}
