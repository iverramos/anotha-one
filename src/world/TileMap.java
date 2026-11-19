package world;

import entities.Entity;
import rendering.SpriteManager;
import utilities.Constants;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * TileMap managing dungeon geometry, collision resolution (solids & one-way drop-through platforms),
 * hazard spikes, and multi-layer rendering.
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

    private final int cols;
    private final int rows;
    private final int[][] bgTiles;
    private final int[][] fgTiles;
    private final SpriteManager sprites;

    public TileMap(SpriteManager sprites, int cols, int rows) {
        this.sprites = sprites;
        this.cols = cols;
        this.rows = rows;
        this.bgTiles = new int[rows][cols];
        this.fgTiles = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bgTiles[r][c] = BG_WALL;
                fgTiles[r][c] = EMPTY;
            }
        }
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

    public int getForeground(int col, int row) {
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            return fgTiles[row][col];
        }
        return WALL; // out-of-bounds is solid
    }

    /**
     * Resolves entity collisions against tiles with swept axis separation.
     */
    public void resolveCollisions(Entity e, double dt, boolean dropThrough) {
        int t = Constants.TILE;
        double newX = e.getX() + e.getVx() * dt;
        double newY = e.getY() + e.getVy() * dt;
        int w = e.getWidth();
        int h = e.getHeight();

        // 1. Horizontal movement & collision
        int leftCol = (int) Math.floor(newX / t);
        int rightCol = (int) Math.floor((newX + w - 1) / t);
        int topRow = (int) Math.floor(e.getY() / t);
        int bottomRow = (int) Math.floor((e.getY() + h - 1) / t);

        boolean collidedX = false;
        if (e.getVx() > 0) { // moving right
            for (int r = topRow; r <= bottomRow; r++) {
                if (isSolid(getForeground(rightCol, r))) {
                    newX = rightCol * t - w;
                    e.setVelocity(0, e.getVy());
                    collidedX = true;
                    break;
                }
            }
        } else if (e.getVx() < 0) { // moving left
            for (int r = topRow; r <= bottomRow; r++) {
                if (isSolid(getForeground(leftCol, r))) {
                    newX = (leftCol + 1) * t;
                    e.setVelocity(0, e.getVy());
                    collidedX = true;
                    break;
                }
            }
        }
        e.setPosition(newX, e.getY());

        // 2. Vertical movement & collision
        leftCol = (int) Math.floor(e.getX() / t);
        rightCol = (int) Math.floor((e.getX() + w - 1) / t);
        topRow = (int) Math.floor(newY / t);
        bottomRow = (int) Math.floor((newY + h - 1) / t);

        boolean ground = false;
        if (e.getVy() > 0) { // falling
            for (int c = leftCol; c <= rightCol; c++) {
                int tile = getForeground(c, bottomRow);
                if (isSolid(tile)) {
                    newY = bottomRow * t - h;
                    e.setVelocity(e.getVx(), 0);
                    ground = true;
                    break;
                } else if (isOneWayPlatform(tile) && !dropThrough) {
                    // One-way platform: only land if feet crossed from above
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
        } else if (e.getVy() < 0) { // jumping up
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
                    g.drawImage(sprites.tile(bg), c * t - camX, r * t - camY, null);
                }
            }
        }
    }

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
                    g.drawImage(sprites.tile(fg), c * t - camX, r * t - camY, null);
                }
            }
        }
    }

    public int getPixelWidth() { return cols * Constants.TILE; }
    public int getPixelHeight() { return rows * Constants.TILE; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
}
