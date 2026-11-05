package world;

import world.sections.Section;

import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;

public class Room {

    private String name;
    private final int layer;
    private List<Section> sections = new ArrayList<>();
    private int currentIndex = 0;
    private boolean visited = false;
    private boolean unique = false;

    private final List<Room> forwardConnections = new ArrayList<>();
    private final List<Room> backwardConnections = new ArrayList<>();

    public Room(String name, int layer) {
        this.name = name;
        this.layer = layer;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public int getLayer() {
        return layer;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isVisited() {
        return visited;
    }

    public void markUnique() {
        this.unique = true;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void replaceSection(int index, Section replacement) {
        sections.set(index, replacement);
    }

    public Section getCurrentSection() {
        return sections.get(currentIndex);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean advance() {
        if (currentIndex < sections.size() - 1) {
            currentIndex++;
            return true;
        }
        return false;
    }

    public void resetToEntry() {
        currentIndex = 0;
    }

    public void retreat() {
        if (currentIndex > 0) {
            currentIndex--;
        }
    }

    public void addForwardConnection(Room room) {
        forwardConnections.add(room);
    }

    public void addBackConnection(Room room) {
        backwardConnections.add(room);
    }

    public List<Room> getForwardConnections() {
        return forwardConnections;
    }

    public List<Room> getBackConnections() {
        return backwardConnections;
    }

    public List<Room> getAllConnections() {
        return Stream.concat(forwardConnections.stream(), backwardConnections.stream()).toList();
    }
}
