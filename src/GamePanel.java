import input.InputHandler;
import rendering.Renderer;
import utilities.Constants;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

/**
 * GamePanel renders the back buffer and forwards key and mouse events (aim, click, dash, wheel)
 * mapped to internal resolution coordinates.
 */
public final class GamePanel extends JPanel {
    private final Renderer renderer;
    private final InputHandler input;

    public GamePanel(Renderer renderer, InputHandler input) {
        this.renderer = renderer;
        this.input = input;

        setPreferredSize(new Dimension(Constants.VIEW_W * Constants.DEFAULT_SCALE, Constants.VIEW_H * Constants.DEFAULT_SCALE));
        setBackground(Color.BLACK);
        setOpaque(true);
        setDoubleBuffered(false);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Hide default system cursor; custom pixel crosshair is rendered in-game
        BufferedImage blankCursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankCursor, new Point(0, 0), "blank");
        setCursor(customCursor);

        addKeyListener(input.keyListener);
        addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { input.releaseAll(); }
        });

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMouse(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateMouse(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                updateMouse(e);
                input.handleMousePress(e.getButton());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateMouse(e);
                input.handleMouseRelease(e.getButton());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                input.handleMouseWheel(e.getWheelRotation());
            }

            private void updateMouse(MouseEvent e) {
                Point p = Renderer.screenToGame(e.getX(), e.getY(), getWidth(), getHeight());
                input.updateMousePos(p.x, p.y);
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    @Override
    protected void paintComponent(Graphics g) {
        renderer.paint((Graphics2D) g, getWidth(), getHeight());
    }
}
