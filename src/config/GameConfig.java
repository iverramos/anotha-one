package config;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads, validates, and exposes JSON customization configs for:
 * - Sprite sheets & animation timings (assets/config/sprite_config.json)
 * - Tilesets, collision rules & tile variations (assets/config/tileset_config.json)
 * - Modular building pieces & templates (assets/config/building_config.json)
 * - Biomes, lighting & parallax themes (assets/config/biome_config.json)
 */
public final class GameConfig {

    public static final class TilesetDef {
        public final String id;
        public final String file;
        public final int tileWidth;
        public final int tileHeight;

        public TilesetDef(String id, String file, int tileWidth, int tileHeight) {
            this.id = id;
            this.file = file;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
        }
    }

    public static final class TileDef {
        public final String id;
        public final String tileset;
        public final int tileId;
        public final boolean collision;
        public final boolean oneWay;
        public final boolean hazard;
        public final String layer;
        public final int[] variations;

        public TileDef(String id, String tileset, int tileId, boolean collision, boolean oneWay,
                       boolean hazard, String layer, int[] variations) {
            this.id = id;
            this.tileset = tileset;
            this.tileId = tileId;
            this.collision = collision;
            this.oneWay = oneWay;
            this.hazard = hazard;
            this.layer = layer;
            this.variations = variations;
        }
    }

    public static final class SpriteDef {
        public final String id;
        public final String file;
        public final int width;
        public final int height;
        public final int frames;
        public final double animationSpeed;
        public final int offsetX;
        public final int offsetY;

        public SpriteDef(String id, String file, int width, int height, int frames,
                         double animationSpeed, int offsetX, int offsetY) {
            this.id = id;
            this.file = file;
            this.width = width;
            this.height = height;
            this.frames = frames;
            this.animationSpeed = animationSpeed;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    public static final class BuildingPieceDef {
        public final String id;
        public final String file;
        public final int srcX;
        public final int srcY;
        public final int width;
        public final int height;
        public final boolean collision;
        public final boolean oneWay;
        public final String layer;

        public BuildingPieceDef(String id, String file, int srcX, int srcY, int width, int height,
                                boolean collision, boolean oneWay, String layer) {
            this.id = id;
            this.file = file;
            this.srcX = srcX;
            this.srcY = srcY;
            this.width = width;
            this.height = height;
            this.collision = collision;
            this.oneWay = oneWay;
            this.layer = layer;
        }
    }

    public static final class BuildingComponentPlacement {
        public final String pieceId;
        public final int tileX;
        public final int tileY;

        public BuildingComponentPlacement(String pieceId, int tileX, int tileY) {
            this.pieceId = pieceId;
            this.tileX = tileX;
            this.tileY = tileY;
        }
    }

    public static final class BuildingTemplate {
        public final String id;
        public final String name;
        public final String biome;
        public final int widthTiles;
        public final int heightTiles;
        public final List<BuildingComponentPlacement> components;

        public BuildingTemplate(String id, String name, String biome, int widthTiles, int heightTiles,
                                List<BuildingComponentPlacement> components) {
            this.id = id;
            this.name = name;
            this.biome = biome;
            this.widthTiles = widthTiles;
            this.heightTiles = heightTiles;
            this.components = components;
        }
    }

    public static final class BiomeDef {
        public final String id;
        public final String name;
        public final String tilesetId;
        public final String backgroundFile;
        public final double parallaxFar;
        public final double parallaxNear;
        public final Color skyColor;
        public final Color ambientColor;
        public final int ambientAlpha;
        public final Color torchColor;
        public final String particleType;

        public BiomeDef(String id, String name, String tilesetId, String backgroundFile,
                        double parallaxFar, double parallaxNear, Color skyColor,
                        Color ambientColor, int ambientAlpha, Color torchColor, String particleType) {
            this.id = id;
            this.name = name;
            this.tilesetId = tilesetId;
            this.backgroundFile = backgroundFile;
            this.parallaxFar = parallaxFar;
            this.parallaxNear = parallaxNear;
            this.skyColor = skyColor;
            this.ambientColor = ambientColor;
            this.ambientAlpha = ambientAlpha;
            this.torchColor = torchColor;
            this.particleType = particleType;
        }
    }

    private final Path assetRoot;
    private final Map<String, TilesetDef> tilesets = new LinkedHashMap<>();
    private final Map<String, TileDef> tiles = new LinkedHashMap<>();
    private final Map<String, SpriteDef> sprites = new LinkedHashMap<>();
    private final Map<String, BuildingPieceDef> buildingPieces = new LinkedHashMap<>();
    private final Map<String, BuildingTemplate> buildings = new LinkedHashMap<>();
    private final Map<String, BiomeDef> biomes = new LinkedHashMap<>();
    private final List<String> validationWarnings = new ArrayList<>();

    public GameConfig(Path assetRoot) {
        this.assetRoot = assetRoot;
        loadAll();
    }

    public void loadAll() {
        tilesets.clear();
        tiles.clear();
        sprites.clear();
        buildingPieces.clear();
        buildings.clear();
        biomes.clear();
        validationWarnings.clear();

        loadTilesetConfig(assetRoot.resolve("config/tileset_config.json"));
        loadSpriteConfig(assetRoot.resolve("config/sprite_config.json"));
        loadBuildingConfig(assetRoot.resolve("config/building_config.json"));
        loadBiomeConfig(assetRoot.resolve("config/biome_config.json"));
        validateReferences();
    }

    private void loadTilesetConfig(Path path) {
        if (!Files.exists(path)) {
            validationWarnings.add("Missing config file: " + path);
            return;
        }
        try {
            Map<String, Object> root = JsonParser.asObject(JsonParser.parse(path));
            Map<String, Object> tsObj = JsonParser.asObject(root.get("tilesets"));
            for (Map.Entry<String, Object> e : tsObj.entrySet()) {
                Map<String, Object> m = JsonParser.asObject(e.getValue());
                String file = normalizeAssetPath(JsonParser.str(m, "file", "tilesets/dungeon/tileset.png"));
                int tw = JsonParser.intVal(m, "tile_width", 16);
                int th = JsonParser.intVal(m, "tile_height", 16);
                tilesets.put(e.getKey(), new TilesetDef(e.getKey(), file, tw, th));
            }

            Map<String, Object> tObj = JsonParser.asObject(root.get("tiles"));
            for (Map.Entry<String, Object> e : tObj.entrySet()) {
                Map<String, Object> m = JsonParser.asObject(e.getValue());
                String ts = JsonParser.str(m, "tileset", "dungeon");
                int tid = JsonParser.intVal(m, "tile_id", 0);
                boolean col = JsonParser.boolVal(m, "collision", false);
                boolean oneWay = JsonParser.boolVal(m, "one_way", false);
                boolean haz = JsonParser.boolVal(m, "hazard", false);
                String layer = JsonParser.str(m, "layer", "ground");
                List<Object> varList = JsonParser.asArray(m.get("variations"));
                int[] vars;
                if (varList.isEmpty()) {
                    vars = new int[]{tid};
                } else {
                    vars = new int[varList.size()];
                    for (int i = 0; i < varList.size(); i++) {
                        vars[i] = (varList.get(i) instanceof Number) ? ((Number) varList.get(i)).intValue() : tid;
                    }
                }
                tiles.put(e.getKey(), new TileDef(e.getKey(), ts, tid, col, oneWay, haz, layer, vars));
            }
        } catch (Exception ex) {
            validationWarnings.add("Failed parsing " + path + ": " + ex.getMessage());
        }
    }

    private void loadSpriteConfig(Path path) {
        if (!Files.exists(path)) {
            validationWarnings.add("Missing config file: " + path);
            return;
        }
        try {
            Map<String, Object> root = JsonParser.asObject(JsonParser.parse(path));

            // Also support optional tilesets/tiles defined inside sprite_config.json
            Map<String, Object> extraTs = JsonParser.asObject(root.get("tilesets"));
            for (Map.Entry<String, Object> e : extraTs.entrySet()) {
                if (!tilesets.containsKey(e.getKey())) {
                    Map<String, Object> m = JsonParser.asObject(e.getValue());
                    tilesets.put(e.getKey(), new TilesetDef(
                            e.getKey(),
                            normalizeAssetPath(JsonParser.str(m, "file", "tilesets/dungeon/tileset.png")),
                            JsonParser.intVal(m, "tile_width", 16),
                            JsonParser.intVal(m, "tile_height", 16)));
                }
            }

            Map<String, Object> spObj = JsonParser.asObject(root.get("sprites"));
            for (Map.Entry<String, Object> e : spObj.entrySet()) {
                Map<String, Object> m = JsonParser.asObject(e.getValue());
                String file = normalizeAssetPath(JsonParser.str(m, "file", ""));
                int w = JsonParser.intVal(m, "width", 24);
                int h = JsonParser.intVal(m, "height", 24);
                int frames = JsonParser.intVal(m, "frames", 1);
                double speed = JsonParser.dblVal(m, "animation_speed", 8.0);
                int ox = JsonParser.intVal(m, "offset_x", 0);
                int oy = JsonParser.intVal(m, "offset_y", 0);
                sprites.put(e.getKey(), new SpriteDef(e.getKey(), file, w, h, frames, speed, ox, oy));
            }
        } catch (Exception ex) {
            validationWarnings.add("Failed parsing " + path + ": " + ex.getMessage());
        }
    }

    private void loadBuildingConfig(Path path) {
        if (!Files.exists(path)) {
            validationWarnings.add("Missing config file: " + path);
            return;
        }
        try {
            Map<String, Object> root = JsonParser.asObject(JsonParser.parse(path));
            Map<String, Object> piecesObj = JsonParser.asObject(root.get("components"));
            for (Map.Entry<String, Object> e : piecesObj.entrySet()) {
                Map<String, Object> m = JsonParser.asObject(e.getValue());
                String file = normalizeAssetPath(JsonParser.str(m, "file", "buildings/medieval/architecture.png"));
                int sx = JsonParser.intVal(m, "src_x", 0);
                int sy = JsonParser.intVal(m, "src_y", 0);
                int w = JsonParser.intVal(m, "width", 16);
                int h = JsonParser.intVal(m, "height", 16);
                boolean col = JsonParser.boolVal(m, "collision", false);
                boolean oneWay = JsonParser.boolVal(m, "one_way", false);
                String layer = JsonParser.str(m, "layer", "exterior");
                buildingPieces.put(e.getKey(), new BuildingPieceDef(e.getKey(), file, sx, sy, w, h, col, oneWay, layer));
            }

            Map<String, Object> bObj = JsonParser.asObject(root.get("buildings"));
            for (Map.Entry<String, Object> e : bObj.entrySet()) {
                Map<String, Object> m = JsonParser.asObject(e.getValue());
                String name = JsonParser.str(m, "name", e.getKey());
                String biome = JsonParser.str(m, "biome", "medieval_village");
                int bw = JsonParser.intVal(m, "width", 8);
                int bh = JsonParser.intVal(m, "height", 6);
                List<BuildingComponentPlacement> comps = new ArrayList<>();
                for (Object item : JsonParser.asArray(m.get("components"))) {
                    Map<String, Object> cm = JsonParser.asObject(item);
                    String pid = JsonParser.str(cm, "id", "");
                    int tx = JsonParser.intVal(cm, "x", 0);
                    int ty = JsonParser.intVal(cm, "y", 0);
                    comps.add(new BuildingComponentPlacement(pid, tx, ty));
                }
                buildings.put(e.getKey(), new BuildingTemplate(e.getKey(), name, biome, bw, bh, comps));
            }
        } catch (Exception ex) {
            validationWarnings.add("Failed parsing " + path + ": " + ex.getMessage());
        }
    }

    private void loadBiomeConfig(Path path) {
        if (!Files.exists(path)) {
            validationWarnings.add("Missing config file: " + path);
            return;
        }
        try {
            Map<String, Object> root = JsonParser.asObject(JsonParser.parse(path));
            Map<String, Object> bObj = JsonParser.asObject(root.get("biomes"));
            for (Map.Entry<String, Object> e : bObj.entrySet()) {
                Map<String, Object> m = JsonParser.asObject(e.getValue());
                String name = JsonParser.str(m, "name", e.getKey());
                String tsId = JsonParser.str(m, "tileset", "dungeon");
                String bgFile = normalizeAssetPath(JsonParser.str(m, "background", "backgrounds/dungeon/parallax.png"));
                double farSpeed = JsonParser.dblVal(m, "parallax_far", 0.15);
                double nearSpeed = JsonParser.dblVal(m, "parallax_near", 0.35);
                Color sky = new Color(JsonParser.colorHex(m, "sky_color", 0x0e1017));
                Color amb = new Color(JsonParser.colorHex(m, "ambient_color", 0x0a0814));
                int ambAlpha = Math.max(0, Math.min(200, JsonParser.intVal(m, "ambient_alpha", 35)));
                Color torch = new Color(JsonParser.colorHex(m, "torch_color", 0xffa63b));
                String pType = JsonParser.str(m, "particle_type", "EMBER");
                biomes.put(e.getKey(), new BiomeDef(e.getKey(), name, tsId, bgFile,
                        farSpeed, nearSpeed, sky, amb, ambAlpha, torch, pType));
            }
        } catch (Exception ex) {
            validationWarnings.add("Failed parsing " + path + ": " + ex.getMessage());
        }
    }

    private void validateReferences() {
        for (TilesetDef ts : tilesets.values()) {
            if (!Files.exists(assetRoot.resolve(ts.file))) {
                validationWarnings.add("Missing tileset asset [" + ts.id + "]: " + ts.file);
            }
        }
        for (SpriteDef sp : sprites.values()) {
            if (sp.file.isEmpty() || !Files.exists(assetRoot.resolve(sp.file))) {
                validationWarnings.add("Missing sprite asset [" + sp.id + "]: " + sp.file);
            }
        }
        for (BuildingPieceDef bp : buildingPieces.values()) {
            if (!Files.exists(assetRoot.resolve(bp.file))) {
                validationWarnings.add("Missing building sheet [" + bp.id + "]: " + bp.file);
            }
        }
        for (BuildingTemplate bt : buildings.values()) {
            for (BuildingComponentPlacement c : bt.components) {
                if (!buildingPieces.containsKey(c.pieceId)) {
                    validationWarnings.add("Building [" + bt.id + "] references unknown component: " + c.pieceId);
                }
            }
        }
        for (BiomeDef bd : biomes.values()) {
            if (!tilesets.containsKey(bd.tilesetId)) {
                validationWarnings.add("Biome [" + bd.id + "] references unknown tileset: " + bd.tilesetId);
            }
            if (!Files.exists(assetRoot.resolve(bd.backgroundFile))) {
                validationWarnings.add("Biome [" + bd.id + "] missing background: " + bd.backgroundFile);
            }
        }
        if (!validationWarnings.isEmpty()) {
            for (String w : validationWarnings) {
                System.err.println("[GameConfig] WARNING: " + w);
            }
        }
    }

    private static String normalizeAssetPath(String p) {
        String s = p.replace('\\', '/');
        if (s.startsWith("assets/")) return s.substring("assets/".length());
        return s;
    }

    public TilesetDef getTileset(String id) { return tilesets.get(id); }
    public TileDef getTileDef(String id) { return tiles.get(id); }
    public SpriteDef getSpriteDef(String id) { return sprites.get(id); }
    public BuildingPieceDef getBuildingPiece(String id) { return buildingPieces.get(id); }
    public BuildingTemplate getBuildingTemplate(String id) { return buildings.get(id); }
    public BiomeDef getBiome(String id) { return biomes.get(id); }
    public Map<String, BiomeDef> getBiomes() { return biomes; }
    public Map<String, BuildingTemplate> getBuildings() { return buildings; }
    public Map<String, TilesetDef> getTilesets() { return tilesets; }
    public Map<String, SpriteDef> getSprites() { return sprites; }
    public List<String> getValidationWarnings() { return validationWarnings; }
}
