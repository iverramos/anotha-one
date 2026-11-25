package rendering;

import config.GameConfig;
import tools.AssetGenerator;
import utilities.Constants;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads, caches, and slices PNG sprite sheets and tilesets cleanly without texture bleeding.
 * Backed by JSON customization configs ({@link GameConfig}) with safe missing-texture fallbacks.
 */
public final class SpriteManager {
    private final Path root;
    private final Map<String, BufferedImage> cache = new HashMap<>();
    private final Map<String, BufferedImage[]> sliceCache = new HashMap<>();
    private final Map<String, BufferedImage> subimageCache = new HashMap<>();
    private final Map<String, BufferedImage[]> tilesetCache = new HashMap<>();
    private GameConfig config;

    public SpriteManager(Path assetRoot) {
        this.root = assetRoot;
        if (!Files.exists(root.resolve("tilesets/dungeon/tileset.png")) ||
            !Files.exists(root.resolve("sprites/player/player.png")) ||
            !Files.exists(root.resolve("config/sprite_config.json"))) {
            try {
                AssetGenerator.generateAll(root);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not generate assets in " + root.toAbsolutePath(), e);
            }
        }
        this.config = new GameConfig(root);
    }

    public GameConfig getConfig() {
        return config;
    }

    public void reloadConfig() {
        cache.clear();
        sliceCache.clear();
        subimageCache.clear();
        tilesetCache.clear();
        this.config = new GameConfig(root);
    }

    /** Load an image relative to the asset root with missing-texture fallback. */
    public BufferedImage get(String relPath) {
        return cache.computeIfAbsent(relPath, p -> {
            Path resolved = root.resolve(p);
            if (!Files.exists(resolved)) {
                System.err.println("[SpriteManager] Missing texture fallback for: " + p);
                return createFallbackImage(Constants.TILE, Constants.TILE);
            }
            try {
                BufferedImage raw = ImageIO.read(resolved.toFile());
                if (raw == null) return createFallbackImage(Constants.TILE, Constants.TILE);
                BufferedImage img = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.drawImage(raw, 0, 0, null);
                g.dispose();
                return img;
            } catch (IOException e) {
                System.err.println("[SpriteManager] Failed reading texture " + p + ": " + e.getMessage());
                return createFallbackImage(Constants.TILE, Constants.TILE);
            }
        });
    }

    /**
     * Slice a horizontal strip sheet into isolated, cached BufferedImages of fw x fh.
     * Copying each frame prevents texture bleeding during rotated or flipped draws.
     */
    public BufferedImage[] slice(String relPath, int fw, int fh) {
        String key = relPath + "#" + fw + "x" + fh;
        return sliceCache.computeIfAbsent(key, k -> {
            BufferedImage sheet = get(relPath);
            if (fw <= 0 || fh <= 0 || sheet.getWidth() < fw || sheet.getHeight() < fh) {
                return new BufferedImage[]{createFallbackImage(Math.max(1, fw), Math.max(1, fh))};
            }
            int cols = Math.max(1, sheet.getWidth() / fw);
            BufferedImage[] out = new BufferedImage[cols];
            for (int i = 0; i < cols; i++) {
                out[i] = copyRegion(sheet, i * fw, 0, fw, fh);
            }
            return out;
        });
    }

    /** Cached, isolated sub-rectangle of an asset sheet (no shared raster bleeding). */
    public BufferedImage subimage(String relPath, int x, int y, int w, int h) {
        String key = relPath + "@" + x + "," + y + "," + w + "x" + h;
        return subimageCache.computeIfAbsent(key, k -> {
            BufferedImage sheet = get(relPath);
            if (x < 0 || y < 0 || w <= 0 || h <= 0 || x + w > sheet.getWidth() || y + h > sheet.getHeight()) {
                return createFallbackImage(Math.max(1, w), Math.max(1, h));
            }
            return copyRegion(sheet, x, y, w, h);
        });
    }

    /** Load sprite frames using a JSON sprite ID from sprite_config.json. */
    public BufferedImage[] sprite(String spriteId) {
        GameConfig.SpriteDef def = config.getSpriteDef(spriteId);
        if (def == null) {
            System.err.println("[SpriteManager] Unknown sprite ID: " + spriteId);
            return new BufferedImage[]{createFallbackImage(24, 24)};
        }
        return slice(def.file, def.width, def.height);
    }

    /** Load a tile image from a named JSON tileset (supports 2D grid sheets as well as 1D strips). */
    public BufferedImage tile(String tilesetId, int index) {
        BufferedImage[] tiles = tilesetCache.computeIfAbsent(tilesetId, id -> {
            GameConfig.TilesetDef def = config.getTileset(id);
            String file = (def != null) ? def.file : "tilesets/dungeon/tileset.png";
            int tw = (def != null) ? def.tileWidth : Constants.TILE;
            int th = (def != null) ? def.tileHeight : Constants.TILE;

            BufferedImage sheet = get(file);
            int cols = Math.max(1, sheet.getWidth() / tw);
            int rows = Math.max(1, sheet.getHeight() / th);
            BufferedImage[] arr = new BufferedImage[cols * rows];
            int idx = 0;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    arr[idx++] = copyRegion(sheet, c * tw, r * th, tw, th);
                }
            }
            return arr;
        });

        if (index >= 0 && index < tiles.length) {
            return tiles[index];
        }
        return tiles.length > 0 ? tiles[0] : createFallbackImage(Constants.TILE, Constants.TILE);
    }

    /** Default tile lookup for backward compatibility. */
    public BufferedImage tile(int index) {
        return tile("dungeon", index);
    }

    private static BufferedImage copyRegion(BufferedImage src, int x, int y, int w, int h) {
        BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, w, h, x, y, x + w, y + h, null);
        g.dispose();
        return copy;
    }

    public static BufferedImage createFallbackImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        int cell = Math.max(4, w / 4);
        for (int y = 0; y < h; y += cell) {
            for (int x = 0; x < w; x += cell) {
                boolean alt = ((x / cell) + (y / cell)) % 2 == 0;
                g.setColor(alt ? new Color(0xd838b8) : new Color(0x1f1826));
                g.fillRect(x, y, cell, cell);
            }
        }
        g.dispose();
        return img;
    }
}
