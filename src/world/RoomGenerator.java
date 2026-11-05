package world;

import game.Game;
import items.Rarity;
import world.sections.*;
import items.ItemGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RoomGenerator {

    private static final int MIN_SECTIONS = 6;
    private static final int MAX_SECTIONS = 8;

    private static final String[] PREFIX = 
            {"Forgotten", "Silent", "Whispering", "Hollow", "Dark", "Mysterious"};
    private static final String[] ROOM_TYPE =
            {"Corridor", "Chamber", "Passage", "Sanctum", "Landing", "Cellar"};
    
    private final Random rng;
    private final ItemGenerator itemGenerator;
    
    public RoomGenerator(Random rng, ItemGenerator itemGenerator) {
        this.rng = rng;
        this.itemGenerator = itemGenerator;
    }

    public Room expand(Room current, DungeonGraph graph, Game game) {
        if (current.isVisited()) {
            return current;
        }

        boolean layerNeedsRestOrResource = layerLacksRestOrResource(graph, current.getLayer());
        current.setSections(buildSections(game, layerNeedsRestOrResource));
        if (current.getName().equals("???")) {
            current.setName(randomRoomName());
        }
        current.setVisited(true);

        int nextLayer = current.getLayer() + 1;
        int forwardCount = 1 + rng.nextInt(2);
        for (int i = 0; i < forwardCount; i++) {
            Room placeholder = new Room("???", nextLayer);
            graph.addRoom(placeholder);
            current.addForwardConnection(placeholder);
        }

        List<Room> previous = graph.getRoomsAtLayer(current.getLayer() - 1);
        if (!previous.isEmpty()) {
            current.addBackConnection(previous.get(rng.nextInt(previous.size())));
        }
        return current;
    }

    private List<Section> buildSections(Game game, boolean layerNeedsRestOrResource) {
        int sections = MIN_SECTIONS + rng.nextInt(MAX_SECTIONS - MIN_SECTIONS + 1);
        int middle = sections - 2;
        int maxSpecials = middle / 2;
        int specialCount = rng.nextInt(maxSpecials + 1);

        List<SectionType> specialTypes = new ArrayList<>(List.of(
            SectionType.ENCOUNTER,
            SectionType.RESOURCE,
            SectionType.REST,
            SectionType.TRAP,
            SectionType.LOOT
        ));
        Collections.shuffle(specialTypes, rng);

        List<Section> middleSections = new ArrayList<>();

        if (layerNeedsRestOrResource) {
            SectionType forced = rng.nextBoolean() ? SectionType.RESOURCE : SectionType.REST;
            middleSections.add(createSection(forced, game));
            specialTypes.remove(forced);
        }

        int remainingSlots = Math.max(0, specialCount - middleSections.size());
        for (int i = 0; i < remainingSlots; i++) {
            middleSections.add(createSection(specialTypes.get(i), game));
        }
        while (middleSections.size() < middle) {
            middleSections.add(new WalkSection(rng));
        }
        Collections.shuffle(middleSections, rng);

        List<Section> sectionsList = new ArrayList<>();
        sectionsList.add(new GraceSection(SectionType.ENTRY));
        sectionsList.addAll(middleSections);
        sectionsList.add(new GraceSection(SectionType.EXIT));
        return sectionsList;
    }

    private Section createSection(SectionType type, Game game) {
        int playerLevel = game.getPlayer().getLevel();
        Rarity minimumQuality = game.getWorld().getLightLevel().getMinimumQuality();

        return switch (type) {
            case ENCOUNTER -> new EncounterSection();
            case REST -> new RestSection(rng);
            case RESOURCE -> new ResourceSection(rng);
            case LOOT -> new LootSection(itemGenerator.randomEquippable(playerLevel, minimumQuality));
            case TRAP -> new TrapSection(rng);
            default -> throw new IllegalArgumentException("Not a special type " + type);
        };
    }

    private boolean layerLacksRestOrResource(DungeonGraph graph, int layer) {
        List<Room> roomsOnLayer = graph.getRoomsAtLayer(layer);
        boolean hasOnThisLayer = roomsOnLayer.stream()
                                             .filter(Room::isVisited)
                                             .flatMap(r -> r.getSections().stream())
                                             .anyMatch(s -> s.getType() == SectionType.RESOURCE || s.getType() == SectionType.REST);
        if (hasOnThisLayer) return false;

        if (layer > 0) {
            List<Room> previousLayer = graph.getRoomsAtLayer(layer - 1);
            boolean hasOnPreviousLayer = previousLayer.stream()
                                              .filter(Room::isVisited)
                                              .flatMap(r -> r.getSections().stream())
                                              .anyMatch(s -> s.getType() == SectionType.RESOURCE || s.getType() == SectionType.REST);
            if (hasOnPreviousLayer) return false;
        }
        return true;
    }

    private String randomRoomName() {
        String prefix = PREFIX[rng.nextInt(PREFIX.length)];
        String roomType = ROOM_TYPE[rng.nextInt(ROOM_TYPE.length)];
        return prefix + " " + roomType;
    }

    public Room buildFinalBossRoom(int layer) {
        Room room = new Room("Portal to Carcossa", layer);
        List<Section> sectionsList = new ArrayList<>();
        sectionsList.add(new GraceSection(SectionType.ENTRY));
        sectionsList.add(new RestSection(rng));
        sectionsList.add(new EncounterSection());
        sectionsList.add(new GraceSection(SectionType.EXIT));
        room.setSections(sectionsList);
        room.setVisited(true);
        room.markUnique();
        return room;
    }
}
