package world;

import rendering.Renderer;
import rendering.SpriteManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-room dungeon floor manager supporting progression across multiple tiers and biomes:
 * Tier 1 (Frontier Village -> Enchanted Forest -> Underground Dungeon -> Ancient Castle)
 * Tier 2 (Ruined Kingdom -> Dark Caverns -> Ancient Castle -> Sanctum)
 */
public final class Dungeon {
    private final int tier;
    private final String floorTitle;
    private final List<Room> rooms = new ArrayList<>();
    private final boolean[] visited;
    private int currentRoomIndex;

    public Dungeon(SpriteManager sprites, Renderer renderer) {
        this(sprites, renderer, 1);
    }

    public Dungeon(SpriteManager sprites, Renderer renderer, int tier) {
        this.tier = tier;
        int cols = 36;
        int rows = 17;

        if (tier == 2) {
            floorTitle = "B2F - Sunken Catacombs & Ruins";
            rooms.add(new Room(0, Room.Type.START, "B2F - Ruined Keep Entrance", sprites, renderer, cols, rows, 2, "ruined_kingdom"));
            rooms.add(new Room(1, Room.Type.COMBAT_CATACOMBS, "B2F - Crypt of the Damned", sprites, renderer, cols, rows, 2, "dark_caverns"));
            rooms.add(new Room(2, Room.Type.CHASM_PLATFORMING, "B2F - The Spiked Abyss", sprites, renderer, 40, rows, 2, "dark_caverns"));
            rooms.add(new Room(3, Room.Type.TREASURE, "B2F - Royal Reliquary", sprites, renderer, cols, rows, 2, "ancient_castle"));
            rooms.add(new Room(4, Room.Type.FOOD_SHOP, "B2F - Ruined Outpost Market", sprites, renderer, cols, rows, 2, "ruined_kingdom"));
            rooms.add(new Room(5, Room.Type.TIER2_BOSS, "B2F - Sanctum of the Void Archon", sprites, renderer, 46, rows, 2, "dark_caverns"));
        } else {
            floorTitle = "B1F - Frontier & Upper Prison";
            rooms.add(new Room(0, Room.Type.START, "B1F - Outskirts Training Village", sprites, renderer, cols, rows, 1, "medieval_village"));
            rooms.add(new Room(1, Room.Type.COMBAT_SLIMES, "B1F - Enchanted Forest Glade", sprites, renderer, cols, rows, 1, "enchanted_forest"));
            rooms.add(new Room(2, Room.Type.COMBAT_SKELETONS, "B1F - Guardpost Ruins", sprites, renderer, cols, rows, 1, "underground_dungeon"));
            rooms.add(new Room(3, Room.Type.TREASURE, "B1F - Castle Secret Vault", sprites, renderer, cols, rows, 1, "ancient_castle"));
            rooms.add(new Room(4, Room.Type.FOOD_SHOP, "B1F - Town Bazaar & Forge", sprites, renderer, cols, rows, 1, "medieval_village"));
            rooms.add(new Room(5, Room.Type.BOSS, "B1F - The Warden's Lair", sprites, renderer, 44, rows, 1, "underground_dungeon"));
        }

        visited = new boolean[rooms.size()];
        currentRoomIndex = 0;
        visited[0] = true;
    }

    public Room getCurrentRoom() {
        return rooms.get(currentRoomIndex);
    }

    public void moveToRoom(int nextIndex) {
        if (nextIndex >= 0 && nextIndex < rooms.size()) {
            currentRoomIndex = nextIndex;
            visited[currentRoomIndex] = true;
        }
    }

    public int getTier() { return tier; }
    public String getFloorTitle() { return floorTitle; }
    public int getCurrentRoomIndex() { return currentRoomIndex; }
    public int getRoomCount() { return rooms.size(); }
    public boolean isVisited(int index) { return visited[index]; }
    public Room getRoom(int index) { return rooms.get(index); }
}
