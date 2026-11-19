package utilities;

/** Global tuning values. Internal resolution is fixed; the window scales it up with nearest-neighbor. */
public final class Constants {
    private Constants() {}

    public static final String TITLE = "Gloamroot";

    /** Internal render resolution (16:9). Everything is drawn at this size, then scaled up. */
    public static final int VIEW_W = 480;
    public static final int VIEW_H = 270;

    /** Tile edge length in internal pixels. */
    public static final int TILE = 16;

    /** Fixed logic updates per second. */
    public static final int UPS = 60;
    public static final double DT = 1.0 / UPS;

    /** Initial window = internal resolution * this. */
    public static final int DEFAULT_SCALE = 2;

    /** Asset root, relative to the working directory (project root). */
    public static final String ASSET_DIR = "assets";
}
