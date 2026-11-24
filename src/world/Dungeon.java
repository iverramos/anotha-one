package world;

import rendering.Renderer;
import rendering.SpriteManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-room dungeon floor manager supporting progression across multiple tiers:
 * Tier 1 (B1F - The Upper Prison) and Tier 2 (B2F - The Sunken Catacombs).
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
            floorTitle = "B2F - The Sunken Catacombs";
            rooms.add(new Room(0, Room.Type.START, "B2F - Catacomb Descent", sprites, renderer, cols, rows, 2));
            rooms.add(new Room(1, Room.Type.COMBAT_CATACOMBS, "B2F - Crypt of the Damned", sprites, renderer, cols, rows, 2));
            rooms.add(new Room(2, Room.Type.CHASM_PLATFORMING, "B2F - The Spiked Abyss", sprites, renderer, 40, rows, 2));
            rooms.add(new Room(3, Room.Type.TREASURE, "B2F - Ancient Reliquary", sprites, renderer, cols, rows, 2));
            rooms.add(new Room(4, Room.Type.FOOD_SHOP, "B2F - Grim Market & Armory", sprites, renderer, cols, rows, 2));
            rooms.add(new Room(5, Room.Type.TIER2_BOSS, "B2F - Sanctum of the Void Archon", sprites, renderer, 46, rows, 2));
        } else {
            floorTitle = "B1F - The Upper Prison";
            rooms.add(new Room(0, Room.Type.START, "B1F - Prison Entrance", sprites, renderer, cols, rows, 1));
            rooms.add(new Room(1, Room.Type.COMBAT_SLIMES, "B1F - Slime Cavern", sprites, renderer, cols, rows, 1));
            rooms.add(new Room(2, Room.Type.COMBAT_SKELETONS, "B1F - Guardpost Ruins", sprites, renderer, cols, rows, 1));
            rooms.add(new Room(3, Room.Type.TREASURE, "B1F - Secret Vault", sprites, renderer, cols, rows, 1));
            rooms.add(new Room(4, Room.Type.FOOD_SHOP, "B1F - Han's Diner & Armory", sprites, renderer, cols, rows, 1));
            rooms.add(new Room(5, Room.Type.BOSS, "B1F - The Warden's Lair", sprites, renderer, 44, rows, 1));
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
