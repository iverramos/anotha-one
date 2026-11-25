import input.InputHandler;
import rendering.Renderer;
import rendering.SpriteManager;
import utilities.Constants;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

/** Headless check: renders a frame to PNG (3x nearest-neighbor) without opening a window. */
public final class FrameDump {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        double camX = args.length > 1 ? Double.parseDouble(args[1]) : 0;
        Renderer r = new Renderer();
        Game game = new Game(new InputHandler(), new SpriteManager(Path.of(Constants.ASSET_DIR)), r);
        game.setCameraX(camX);
        for (int i = 0; i < 20; i++) game.update(Constants.DT);
        game.render(r.begin());
        r.end();
        BufferedImage src = r.snapshot();
        int s = 1;
        BufferedImage out = new BufferedImage(src.getWidth() * s, src.getHeight() * s, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
        g.dispose();
        ImageIO.write(out, "png", new File(args.length > 0 ? args[0] : "frame.png"));
        System.out.println("wrote frame to " + (args.length > 0 ? args[0] : "frame.png"));
    }
}
