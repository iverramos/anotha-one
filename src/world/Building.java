package world;

import config.GameConfig;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Instantiated modular building assembled from JSON component definitions
 * (assets/config/building_config.json). Supports roofs, walls, windows,
 * balconies (walkable one-way platforms), lamps, banners, and interior props.
 */
public final class Building {
    private final GameConfig.BuildingTemplate template;
    private final int baseCol;
    private final int baseRow; // top-left row of building bounding box

    public Building(GameConfig.BuildingTemplate template, int baseCol, int floorRow) {
        this.template = template;
        this.baseCol = baseCol;
        this.baseRow = floorRow - template.heightTiles;
    }

    /** Registers walkable balconies, rooftops, and stairs as invisible one-way platforms on the TileMap. */
    public void registerPlatforms(TileMap map, GameConfig config) {
        for (GameConfig.BuildingComponentPlacement p : template.components) {
            GameConfig.BuildingPieceDef def = config.getBuildingPiece(p.pieceId);
            if (def == null) continue;
            if (def.oneWay) {
                int colsSpan = Math.max(1, def.width / Constants.TILE);
                int worldRow = baseRow + p.tileY;
                for (int dc = 0; dc < colsSpan; dc++) {
                    map.setPlatformOverlay(baseCol + p.tileX + dc, worldRow, true);
                }
            }
        }
    }

    /** Renders all modular components in layer order (interior -> exterior -> platform -> furniture -> decoration). */
    public void render(Graphics2D g, SpriteManager sprites, int camX, int camY) {
        GameConfig config = sprites.getConfig();
        String[] layers = {"interior", "exterior", "platform", "furniture", "decoration"};
        for (String layer : layers) {
            for (GameConfig.BuildingComponentPlacement p : template.components) {
                GameConfig.BuildingPieceDef def = config.getBuildingPiece(p.pieceId);
                if (def == null || !layer.equals(def.layer)) continue;

                BufferedImage pieceImg = sprites.subimage(def.file, def.srcX, def.srcY, def.width, def.height);
                int wx = (baseCol + p.tileX) * Constants.TILE - camX;
                int wy = (baseRow + p.tileY) * Constants.TILE - camY;
                g.drawImage(pieceImg, wx, wy, null);
            }
        }
    }

    /** Returns world center points of glowing windows, lamps, and candles for environmental lighting. */
    public List<Point> getLightSources(GameConfig config) {
        List<Point> lights = new ArrayList<>();
        for (GameConfig.BuildingComponentPlacement p : template.components) {
            if ("street_lamp".equals(p.pieceId) || "shop_window".equals(p.pieceId)
                    || "dormer_window".equals(p.pieceId) || "candle_stand".equals(p.pieceId)) {
                GameConfig.BuildingPieceDef def = config.getBuildingPiece(p.pieceId);
                int cx = (baseCol + p.tileX) * Constants.TILE + (def != null ? def.width / 2 : 8);
                int cy = (baseRow + p.tileY) * Constants.TILE + (def != null ? def.height / 3 : 8);
                lights.add(new Point(cx, cy));
            }
        }
        return lights;
    }

    public String getName() { return template.name; }
    public int getBaseCol() { return baseCol; }
    public int getBaseRow() { return baseRow; }
    public int getWidthTiles() { return template.widthTiles; }
    public int getHeightTiles() { return template.heightTiles; }
}
