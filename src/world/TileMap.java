package world;

import entities.Entity;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * TileMap managing dungeon/biome geometry, autotiling variations, decorative props,
 * swept-axis collision resolution (solids & one-way platforms including modular balconies),
 * and hazard spikes.
 */
public final class TileMap {
    public static final int EMPTY = -1;
    public static final int WALL = 0;
    public static final int FLOOR = 1;
    public static final int COBBLE = 2;
    public static final int WOOD_PLATFORM = 3;
    public static final int SPIKES = 4;
    public static final int BG_WALL = 5;
    public static final int BG_PILLAR = 6;
    public static final int PORTAL = 7;
    public static final int MOSSY_WALL = 8;
    public static final int BOSS_GATE = 9;

    // Environmental decoration tile indices (Row 1 of standardized biome tileset)
    public static final int DECOR_BUSH = 16;
    public static final int DECOR_CRATE = 17;
    public static final int DECOR_CHAIN_TOP = 18;
    public static final int DECOR_CHAIN_END = 19;
    public static final int DECOR_CRYSTAL_A = 20;
    public static final int DECOR_CRYSTAL_B = 21;
    public static final int DECOR_SIGN = 22;

    private final int cols;
    private final int rows;
    private final int[][] bgTiles;
    private final int[][] fgTiles;
    private final int[][] decorTiles;
    private final boolean[][] platformOverlay;
    private final SpriteManager sprites;
    private String tilesetId = "dungeon";

    public TileMap(SpriteManager sprites, int cols, int rows) {
        this.sprites = sprites;
        this.cols = cols;
        this.rows = rows;
        this.bgTiles = new int[rows][cols];
        this.fgTiles = new int[rows][cols];
        this.decorTiles = new int[rows][cols];
        this.platformOverlay = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bgTiles[r][c] = EMPTY;
                fgTiles[r][c] = EMPTY;
                decorTiles[r][c] = EMPTY;
            }
        }
    }

    public void setTileset(String tilesetId) {
        if (tilesetId != null && !tilesetId.isEmpty()) {
            this.tilesetId = tilesetId;
        }
    }

    public String getTilesetId() {
        return tilesetId;
    }

    public static boolean isSolid(int tile) {
        return tile == WALL || tile == FLOOR || tile == COBBLE || tile == MOSSY_WALL || tile == BOSS_GATE;
    }

    public static boolean isOneWayPlatform(int tile) {
        return tile == WOOD_PLATFORM;
    }

    public static boolean isHazard(int tile) {
        return tile == SPIKES;
    }

    public void setForeground(int col, int row, int tile) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            fgTiles[row][col] = tile;
        }
    }

    public void setBackground(int col, int row, int tile) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            bgTiles[row][col] = tile;
        }
    }

    public void setDecoration(int col, int row, int decorTile) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            decorTiles[row][col] = decorTile;
        }
    }

    public void setPlatformOverlay(int col, int row, boolean active) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            platformOverlay[row][col] = active;
        }
    }

    public boolean isPlatformOverlay(int col, int row) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            return platformOverlay[row][col];
        }
        return false;
    }

    public int getForeground(int col, int row) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            return fgTiles[row][col];
        }
        return WALL;
    }

    public int getDecor(int col, int row) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            return decorTiles[row][col];
        }
        return EMPTY;
    }

    /**
     * Resolves entity collisions against tiles with swept axis separation.
     */
    public void resolveCollisions(Entity e, double dt, boolean dropThrough) {
        int t = Constants.TILE;
        final double eps = 0.001;
        double newX = e.getX() + e.getVx() * dt;
        double newY = e.getY() + e.getVy() * dt;
        int w = e.getWidth();
        int h = e.getHeight();

        // 1. Horizontal movement & collision
        int leftCol = (int) Math.floor((newX + eps) / t);
        int rightCol = (int) Math.floor((newX + w - eps) / t);
        int topRow = (int) Math.floor((e.getY() + eps) / t);
        int bottomRow = (int) Math.floor((e.getY() + h - eps) / t);

        if (e.getVx() > 0) {
            for (int r = topRow; r <= bottomRow; r++) {
                if (isSolid(getForeground(rightCol, r))) {
                    newX = rightCol * t - w;
                    e.setVelocity(0, e.getVy());
                    break;
                }
            }
        } else if (e.getVx() < 0) {
            for (int r = topRow; r <= bottomRow; r++) {
                if (isSolid(getForeground(leftCol, r))) {
                    newX = (leftCol + 1) * t;
                    e.setVelocity(0, e.getVy());
                    break;
                }
            }
        }
        e.setPosition(newX, e.getY());

        // 2. Vertical movement & collision
        leftCol = (int) Math.floor((e.getX() + eps) / t);
        rightCol = (int) Math.floor((e.getX() + w - eps) / t);
        topRow = (int) Math.floor((newY + eps) / t);
        bottomRow = (int) Math.floor((newY + h - eps) / t);

        boolean ground = false;
        if (e.getVy() >= 0) {
            for (int c = leftCol; c <= rightCol; c++) {
                int tile = getForeground(c, bottomRow);
                boolean oneWay = isOneWayPlatform(tile) || isPlatformOverlay(c, bottomRow);
                if (isSolid(tile)) {
                    newY = bottomRow * t - h;
                    e.setVelocity(e.getVx(), 0);
                    ground = true;
                    break;
                } else if (oneWay && !dropThrough) {
                    double oldFoot = e.getY() + h;
                    double platformTop = bottomRow * t;
                    if (oldFoot <= platformTop + 6) {
                        newY = platformTop - h;
                        e.setVelocity(e.getVx(), 0);
                        ground = true;
                        break;
                    }
                }
            }
            if (!ground) {
                int supportRow = (int) Math.floor((newY + h + 0.02) / t);
                double surfaceY = supportRow * t;
                if (Math.abs((newY + h) - surfaceY) <= 0.05) {
                    for (int c = leftCol; c <= rightCol; c++) {
                        int tile = getForeground(c, supportRow);
                        boolean oneWay = isOneWayPlatform(tile) || isPlatformOverlay(c, supportRow);
                        if (isSolid(tile) || (oneWay && !dropThrough)) {
                            newY = surfaceY - h;
                            e.setVelocity(e.getVx(), 0);
                            ground = true;
                            break;
                        }
                    }
                }
            }
        } else {
            for (int c = leftCol; c <= rightCol; c++) {
                if (isSolid(getForeground(c, topRow))) {
                    newY = (topRow + 1) * t;
                    e.setVelocity(e.getVx(), 0);
                    break;
                }
            }
        }

        e.setPosition(e.getX(), newY);
        e.setOnGround(ground);
    }

    public boolean checkHazard(Rectangle r) {
        int t = Constants.TILE;
        int c0 = Math.max(0, r.x / t);
        int c1 = Math.min(cols - 1, (r.x + r.width) / t);
        int r0 = Math.max(0, r.y / t);
        int r1 = Math.min(rows - 1, (r.y + r.height) / t);

        for (int row = r0; row <= r1; row++) {
            for (int col = c0; col <= c1; col++) {
                if (isHazard(fgTiles[row][col])) {
                    Rectangle spikeRect = new Rectangle(col * t + 2, row * t + 8, t - 4, t - 8);
                    if (r.intersects(spikeRect)) return true;
                }
            }
        }
        return false;
    }

    public void renderBackground(Graphics2D g, int camX, int camY) {
        int t = Constants.TILE;
        int c0 = Math.max(0, camX / t);
        int c1 = Math.min(cols - 1, (camX + Constants.VIEW_W) / t + 1);
        int r0 = Math.max(0, camY / t);
        int r1 = Math.min(rows - 1, (camY + Constants.VIEW_H) / t + 1);

        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                int bg = bgTiles[r][c];
                if (bg >= 0) {
                    g.drawImage(sprites.tile(tilesetId, bg), c * t - camX, r * t - camY, null);
                }
            }
        }
    }

    public void renderDecorations(Graphics2D g, int camX, int camY) {
        int t = Constants.TILE;
        int c0 = Math.max(0, camX / t);
        int c1 = Math.min(cols - 1, (camX + Constants.VIEW_W) / t + 1);
        int r0 = Math.max(0, camY / t);
        int r1 = Math.min(rows - 1, (camY + Constants.VIEW_H) / t + 1);

        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                int dec = decorTiles[r][c];
                if (dec >= 0) {
                    g.drawImage(sprites.tile(tilesetId, dec), c * t - camX, r * t - camY, null);
                }
            }
        }
    }

    /** Renders foreground tiles with neighbor-aware autotiling and deterministic variation. */
    public void renderForeground(Graphics2D g, int camX, int camY) {
        int t = Constants.TILE;
        int c0 = Math.max(0, camX / t);
        int c1 = Math.min(cols - 1, (camX + Constants.VIEW_W) / t + 1);
        int r0 = Math.max(0, camY / t);
        int r1 = Math.min(rows - 1, (camY + Constants.VIEW_H) / t + 1);

        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                int fg = fgTiles[r][c];
                if (fg >= 0) {
                    int renderTile = resolveAutotile(c, r, fg);
                    g.drawImage(sprites.tile(tilesetId, renderTile), c * t - camX, r * t - camY, null);
                }
            }
        }
    }

    private int resolveAutotile(int c, int r, int baseTile) {
        int h = Math.abs((c * 73856093) ^ (r * 19349663));
        boolean upSolid = (r > 0) && isSolid(fgTiles[r - 1][c]);
        boolean leftSolid = (c > 0) && isSolid(fgTiles[r][c - 1]);
        boolean rightSolid = (c < cols - 1) && isSolid(fgTiles[r][c + 1]);

        if (baseTile == FLOOR) {
            if (!upSolid) {
                if (!leftSolid) return 12;
                if (!rightSolid) return 13;
                int m = h % 5;
                if (m == 0) return 10;
                if (m == 1) return 11;
                return 1;
            }
            return (h % 3 == 0) ? 15 : 2;
        }
        if (baseTile == WALL) {
            if (!upSolid && r > 1) {
                return (h % 3 == 0) ? 10 : 1;
            }
            return (h % 4 == 0) ? 14 : 0;
        }
        if (baseTile == COBBLE) {
            return (h % 3 == 0) ? 15 : 2;
        }
        return baseTile;
    }

    public int getPixelWidth() { return cols * Constants.TILE; }
    public int getPixelHeight() { return rows * Constants.TILE; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
}
