package world;

import rendering.Renderer;
import rendering.SpriteManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedural multi-room dungeon floor (B1F - The Upper Prison).
 * Manages interconnected rooms, door transit, and discovery for the minimap.
 */
public final class Dungeon {
    private final List<Room> rooms = new ArrayList<>();
    private final boolean[] visited;
    private int currentRoomIndex;

    public Dungeon(SpriteManager sprites, Renderer renderer) {
        int cols = 36;
        int rows = 17;

        rooms.add(new Room(0, Room.Type.START, "B1F - Prison Entrance", sprites, renderer, cols, rows));
        rooms.add(new Room(1, Room.Type.COMBAT_SLIMES, "B1F - Slime Cavern", sprites, renderer, cols, rows));
        rooms.add(new Room(2, Room.Type.COMBAT_SKELETONS, "B1F - Guardpost Ruins", sprites, renderer, cols, rows));
        rooms.add(new Room(3, Room.Type.TREASURE, "B1F - Secret Vault", sprites, renderer, cols, rows));
        rooms.add(new Room(4, Room.Type.FOOD_SHOP, "B1F - Han's Diner", sprites, renderer, cols, rows));
        rooms.add(new Room(5, Room.Type.BOSS, "B1F - The Warden's Lair", sprites, renderer, 44, rows));

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

    public int getCurrentRoomIndex() { return currentRoomIndex; }
    public int getRoomCount() { return rooms.size(); }
    public boolean isVisited(int index) { return visited[index]; }
    public Room getRoom(int index) { return rooms.get(index); }
}
