package input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Thread-safe input handler for both keyboard and mouse.
 * Captures mouse aiming (internal game coordinates), primary attack, secondary dash,
 * platform drop, weapon cycling, and menu interaction.
 */
public final class InputHandler extends MouseAdapter {
    public enum Action {
        LEFT, RIGHT, UP, DOWN, JUMP, ATTACK, DASH, INTERACT,
        SWITCH_WEAPON, SLOT_1, SLOT_2, SLOT_3, PAUSE, RESTART
    }

    private static final int N = Action.values().length;
    private final Object lock = new Object();

    // Raw input from EDT
    private final boolean[] rawDown = new boolean[N];
    private final boolean[] rawPressed = new boolean[N];
    private int rawMouseX;
    private int rawMouseY;
    private boolean rawMouseLeftDown;
    private boolean rawMouseLeftPressed;
    private boolean rawMouseRightDown;
    private boolean rawMouseRightPressed;
    private int rawWheelDelta;

    // Polled state for game thread
    private final boolean[] down = new boolean[N];
    private final boolean[] pressed = new boolean[N];
    private int mouseX;
    private int mouseY;
    private boolean mouseLeftDown;
    private boolean mouseLeftPressed;
    private boolean mouseRightDown;
    private boolean mouseRightPressed;
    private int wheelDelta;

    public final KeyAdapter keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            Action a = mapKey(e.getKeyCode());
            if (a == null) return;
            synchronized (lock) {
                if (!rawDown[a.ordinal()]) rawPressed[a.ordinal()] = true;
                rawDown[a.ordinal()] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Action a = mapKey(e.getKeyCode());
            if (a == null) return;
            synchronized (lock) {
                rawDown[a.ordinal()] = false;
            }
        }
    };

    private static Action mapKey(int code) {
        switch (code) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  return Action.LEFT;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: return Action.RIGHT;
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    return Action.UP;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  return Action.DOWN;
            case KeyEvent.VK_SPACE:                     return Action.JUMP;
            case KeyEvent.VK_SHIFT: case KeyEvent.VK_K: return Action.DASH;
            case KeyEvent.VK_J:                         return Action.ATTACK;
            case KeyEvent.VK_E: case KeyEvent.VK_F:     return Action.INTERACT;
            case KeyEvent.VK_Q:                         return Action.SWITCH_WEAPON;
            case KeyEvent.VK_1:                         return Action.SLOT_1;
            case KeyEvent.VK_2:                         return Action.SLOT_2;
            case KeyEvent.VK_3:                         return Action.SLOT_3;
            case KeyEvent.VK_ESCAPE:                    return Action.PAUSE;
            case KeyEvent.VK_R: case KeyEvent.VK_ENTER: return Action.RESTART;
            default:                                    return null;
        }
    }

    public void updateMousePos(int gx, int gy) {
        synchronized (lock) {
            rawMouseX = gx;
            rawMouseY = gy;
        }
    }

    public void handleMousePress(int button) {
        synchronized (lock) {
            if (button == MouseEvent.BUTTON1) {
                if (!rawMouseLeftDown) rawMouseLeftPressed = true;
                rawMouseLeftDown = true;
            } else if (button == MouseEvent.BUTTON3) {
                if (!rawMouseRightDown) rawMouseRightPressed = true;
                rawMouseRightDown = true;
            }
        }
    }

    public void handleMouseRelease(int button) {
        synchronized (lock) {
            if (button == MouseEvent.BUTTON1) rawMouseLeftDown = false;
            else if (button == MouseEvent.BUTTON3) rawMouseRightDown = false;
        }
    }

    public void handleMouseWheel(int rotation) {
        synchronized (lock) {
            rawWheelDelta += rotation;
        }
    }

    public void releaseAll() {
        synchronized (lock) {
            for (int i = 0; i < N; i++) {
                rawDown[i] = false;
                rawPressed[i] = false;
            }
            rawMouseLeftDown = false;
            rawMouseLeftPressed = false;
            rawMouseRightDown = false;
            rawMouseRightPressed = false;
            rawWheelDelta = 0;
        }
    }

    /** Call once per tick on the game logic thread. */
    public void poll() {
        synchronized (lock) {
            for (int i = 0; i < N; i++) {
                down[i] = rawDown[i];
                pressed[i] = rawPressed[i];
                rawPressed[i] = false;
            }
            mouseX = rawMouseX;
            mouseY = rawMouseY;

            mouseLeftDown = rawMouseLeftDown;
            mouseLeftPressed = rawMouseLeftPressed;
            rawMouseLeftPressed = false;

            mouseRightDown = rawMouseRightDown;
            mouseRightPressed = rawMouseRightPressed;
            rawMouseRightPressed = false;

            wheelDelta = rawWheelDelta;
            rawWheelDelta = 0;
        }
    }

    public boolean isDown(Action a) { return down[a.ordinal()]; }
    public boolean wasPressed(Action a) { return pressed[a.ordinal()]; }

    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    public boolean isLeftDown() { return mouseLeftDown || down[Action.ATTACK.ordinal()]; }
    public boolean wasLeftPressed() { return mouseLeftPressed || pressed[Action.ATTACK.ordinal()]; }

    public boolean isRightDown() { return mouseRightDown || down[Action.DASH.ordinal()]; }
    public boolean wasRightPressed() { return mouseRightPressed || pressed[Action.DASH.ordinal()]; }

    public int getWheelDelta() { return wheelDelta; }
}
