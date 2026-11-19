import input.InputHandler;
import rendering.Renderer;
import rendering.SpriteManager;
import utilities.Constants;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.nio.file.Path;

public final class Main {
    private static JFrame frame;
    private static GamePanel panel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShow);
    }

    private static void createAndShow() {
        InputHandler input = new InputHandler();
        SpriteManager sprites = new SpriteManager(Path.of(Constants.ASSET_DIR));
        Renderer renderer = new Renderer();
        Game game = new Game(input, sprites, renderer);
        panel = new GamePanel(renderer, input);

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F11"), "fullscreen");
        panel.getActionMap().put("fullscreen", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { toggleFullscreen(); }
        });

        frame = new JFrame(Constants.TITLE + " - Dungreed-Inspired Action Roguelite");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();

        new GameLoop(game, renderer, panel::repaint).start();
    }

    private static void toggleFullscreen() {
        GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        boolean goingFull = dev.getFullScreenWindow() != frame;
        if (goingFull) {
            frame.dispose();
            frame.setUndecorated(true);
            dev.setFullScreenWindow(frame);
        } else {
            dev.setFullScreenWindow(null);
            frame.dispose();
            frame.setUndecorated(false);
            frame.setVisible(true);
        }
        panel.requestFocusInWindow();
    }
}
