package world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DungeonGraph {

    private final Map<Integer, List<Room>> layers = new LinkedHashMap<>();

    public void addRoom(Room room) {
        layers.computeIfAbsent(room.getLayer(), k -> new ArrayList<>()).add(room);
    }

    public List<Room> getRoomsAtLayer(int layer) {
        return layers.getOrDefault(layer, List.of());
    }

    public List<Room> getAllVisitedRooms() {
        return new ArrayList<>(layers.values().stream()
                                     .flatMap(List::stream)
                                     .filter(Room::isVisited)
                                     .toList());
    }
}
