package utilities;

/** Global tuning values. World camera runs at 480x270; UI & back buffer render at 960x540 for crisp text. */
public final class Constants {
    private Constants() {}

    public static final String TITLE = "Dungeons Descent";
    public static final String SUBTITLE = "Dungreed-Inspired Action Roguelite";

    /** World viewport dimensions (16:9). Camera & tile layouts operate in these units. */
    public static final int VIEW_W = 480;
    public static final int VIEW_H = 270;

    /** Internal UI & back-buffer scale over world units. */
    public static final int UI_SCALE = 2;
    public static final int UI_W = VIEW_W * UI_SCALE; // 960
    public static final int UI_H = VIEW_H * UI_SCALE; // 540

    /** Tile edge length in world pixels. */
    public static final int TILE = 16;

    /** Fixed logic updates per second. */
    public static final int UPS = 60;
    public static final double DT = 1.0 / UPS;

    /** Initial window size (16:9 HD). Window is resizable and supports F11 fullscreen. */
    public static final int WINDOW_W = 1280;
    public static final int WINDOW_H = 720;

    /** Asset root, relative to the working directory (project root). */
    public static final String ASSET_DIR = "assets";
}
