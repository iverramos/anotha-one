package tools;

import combat.DamageNumber;
import entities.Player;
import input.InputHandler;
import rendering.Renderer;
import rendering.SpriteManager;
import utilities.Constants;
import world.Dungeon;
import world.Room;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

/**
 * Headless room dump utility: captures high-res screenshots of every dungeon chamber.
 */
public final class RoomDump {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        SpriteManager sprites = new SpriteManager(Path.of(Constants.ASSET_DIR));

        for (int i = 0; i <= 5; i++) {
            Renderer r = new Renderer();
            Dungeon d = new Dungeon(sprites, r);
            d.moveToRoom(i);
            Room room = d.getCurrentRoom();
            Player p = new Player(sprites, 6 * Constants.TILE, 12 * Constants.TILE);

            // Simulate a few ticks for physics and animations
            for (int t = 0; t < 30; t++) {
                room.update(0.016);
                p.update(0.016, false, false, false, false, false, false, 240, 135,
                        new java.util.ArrayList<>(), new java.util.ArrayList<>(), room.getEnemies(), new java.util.ArrayList<>());
                room.getMap().resolveCollisions(p, 0.016, false);
                for (entities.Enemy e : room.getEnemies()) {
                    e.update(0.016);
                    e.updateAI(0.016, p, new java.util.ArrayList<>(), new java.util.ArrayList<>());
                    room.getMap().resolveCollisions(e, 0.016, false);
                }
            }

            Graphics2D g = r.begin();
            // Background
            g.setColor(new Color(0x0e1017));
            g.fillRect(0, 0, Constants.UI_W, Constants.UI_H);

            int camX = Math.max(0, Math.min(room.getMap().getPixelWidth() - Constants.VIEW_W, (int) p.getCenterX() - Constants.VIEW_W / 2));
            int camY = 0;

            java.awt.geom.AffineTransform oldTx = g.getTransform();
            g.scale(Constants.UI_SCALE, Constants.UI_SCALE);
            room.render(g, camX, camY, p.getCenterX(), p.getCenterY());
            for (entities.Enemy e : room.getEnemies()) e.render(g, camX, camY);
            p.render(g, camX, camY);
            g.setTransform(oldTx);

            // HUD
            ui.HUD hud = new ui.HUD(sprites);
            hud.render(g, p, d, Constants.UI_W / 2, Constants.UI_H / 2);

            r.end();
            BufferedImage src = r.snapshot();

            int scale = 1;
            BufferedImage out = new BufferedImage(src.getWidth() * scale, src.getHeight() * scale, BufferedImage.TYPE_INT_RGB);
            Graphics2D og = out.createGraphics();
            og.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
            og.dispose();

            String filename = "room_" + i + "_" + room.getType().name().toLowerCase() + ".png";
            ImageIO.write(out, "png", new File(filename));
            System.out.println("Generated " + filename);
        }
    }
}
