package rendering;

import tools.AssetGenerator;
import utilities.Constants;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads PNGs from the assets directory (and caches them).
 * If the asset pack is missing, it is regenerated automatically by {@link AssetGenerator}.
 */
public final class SpriteManager {
    private final Path root;
    private final Map<String, BufferedImage> cache = new HashMap<>();
    private BufferedImage[] tiles;

    public SpriteManager(Path assetRoot) {
        this.root = assetRoot;
        if (!Files.exists(root.resolve("environment/tileset.png")) ||
            !Files.exists(root.resolve("characters/player.png"))) {
            try {
                AssetGenerator.generateAll(root);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not generate assets in " + root.toAbsolutePath(), e);
            }
        }
    }

    /** Load an image relative to the asset root, e.g. "characters/player.png". */
    public BufferedImage get(String relPath) {
        return cache.computeIfAbsent(relPath, p -> {
            try {
                BufferedImage raw = ImageIO.read(root.resolve(p).toFile());
                BufferedImage img = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.drawImage(raw, 0, 0, null);
                g.dispose();
                return img;
            } catch (IOException e) {
                throw new UncheckedIOException("Missing asset: " + p, e);
            }
        });
    }

    /** Slice a horizontal strip sheet into frames of fw x fh. */
    public BufferedImage[] slice(String relPath, int fw, int fh) {
        BufferedImage sheet = get(relPath);
        int cols = sheet.getWidth() / fw;
        BufferedImage[] out = new BufferedImage[cols];
        for (int i = 0; i < cols; i++) {
            out[i] = sheet.getSubimage(i * fw, 0, fw, fh);
        }
        return out;
    }

    /** Sub-rectangle of an asset sheet. */
    public BufferedImage subimage(String relPath, int x, int y, int w, int h) {
        return get(relPath).getSubimage(x, y, w, h);
    }

    /** Tile image by index from the terrain tileset (16x16). */
    public BufferedImage tile(int index) {
        if (tiles == null) {
            tiles = slice("environment/tileset.png", Constants.TILE, Constants.TILE);
        }
        if (index >= 0 && index < tiles.length) {
            return tiles[index];
        }
        return tiles[0];
    }
}
