package game;

import items.equippables.Equippable;
import player.*;
import world.*;
import world.sections.*;
import combat.*;
import enemy.*;
import items.*;
import items.consumables.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game implements LogSubject, GameStateSubject {

    private static final int FINAL_LEVEL = 10;
    
    private final Random rng = new Random();
    
    private final Player player;
    private final World world = new World();
    private final DungeonGraph graph = new DungeonGraph();
    private final EnemyGenerator enemyGenerator = new EnemyGenerator(rng);
    private final ItemGenerator itemGenerator = new ItemGenerator(rng);
    private final RoomGenerator roomGenerator = new RoomGenerator(rng, itemGenerator);
    private final CombatManager combatManager = new CombatManager(this);

    private final List<LogObserver> logObservers = new ArrayList<>();
    private final List<GameStateObserver> stateObservers = new ArrayList<>();

    private Room currentRoom;
    private Enemy currentEnemy;
    private Enemy finalBoss;
    private GameMode mode = GameMode.EXPLORING;
    private boolean finalRoomUnlocked = false;
    private boolean gameOver = false;
    private boolean victory = false;

    public Game(Player player) {
        this.player = player;
//        initializeStartingKit();
        Room root = new Room("The Entrance", 0);
        graph.addRoom(root);
        enterRoom(root);
    }

    @Override
    public void attachLogObserver(LogObserver observer) {
        logObservers.add(observer);
    }

    @Override
    public void notifyLogObservers(String message) {
        logObservers.forEach(o -> o.updateLog(message));
    }

    @Override
    public void attachGameStateObserver(GameStateObserver observer) {
        stateObservers.add(observer);
    }

    @Override
    public void notifyGameStateObservers() {
        stateObservers.forEach(o -> o.updateGameState(this));
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public DungeonGraph getGraph() {
        return graph;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public GameMode getMode() {
        return mode;
    }

    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    public EnemyGenerator getEnemyGenerator() {
        return enemyGenerator;
    }

    public ItemGenerator getItemGenerator() {
        return itemGenerator;
    }

    public Random getRng() {
        return rng;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean getVictory() {
        return victory;
    }

    public void handleInput(char key) {
        if (gameOver) return;
        switch (mode) {
            case EXPLORING -> handleExploring(key);
            case IN_COMBAT -> handleCombat(key);
            case AT_EXIT -> handleExit(key);
            default -> { /* VICTORY / GAME_OVER: no input handled */}
        }
        notifyGameStateObservers();
    }

    private void handleExploring(char key) {
        switch (Character.toUpperCase(key)) {
            case ' ' -> advanceSection();
            case 'H' -> usePotion(HealthPotion.class);
            case 'M' -> usePotion(ManaPotion.class);
            case 'S' -> usePotion(StaminaPotion.class);
            case 'T' -> useTorch();
            case 'R' -> restIfPossible();
            case 'P' -> lootIfPossible();
        }
    }

    private void handleCombat(char key) {
        switch (Character.toUpperCase(key)) {
            case 'X' -> combatManager.resolve(CombatAction.ATTACK);
            case 'V' -> combatManager.resolve(CombatAction.RANGED);
            case 'C' -> combatManager.resolve(CombatAction.SPELL);
            case 'Z' -> combatManager.resolve(CombatAction.HEAL);
            case 'F' -> combatManager.resolve(CombatAction.FLEE);
            case 'H' -> combatManager.resolve(CombatAction.USE_HEALTH_POTION);
            case 'M' -> combatManager.resolve(CombatAction.USE_MANA_POTION);
            case 'S' -> combatManager.resolve(CombatAction.USE_STAMINA_POTION);
        }
    }

    public boolean usePotion(Class<? extends Consumable> type) {
        Consumable consumable = player.takeConsumable(type);
        if (consumable == null) {
            String name = switch (type.getSimpleName()) {
                case "HealthPotion" -> "Health Potion";
                case "ManaPotion" -> "Mana Potion";
                case "StaminaPotion" -> "Stamina Potion";
                case "Torch" -> "Torch";
                default -> type.getSimpleName();
            };
            notifyLogObservers("No " + name + " in inventory.");
            return false;
        }
        boolean consumed = consumable.use(this);
        if (!consumed) {
            player.addConsumable(consumable);
        }
        return consumed;
    }

    public void useTorch() {
        Consumable consumable = player.takeConsumable(Torch.class);
        if (consumable == null) {
            notifyLogObservers("No torches in inventory.");
            return;
        }
        boolean consumed = consumable.use(this);
        if (!consumed) {
            player.addConsumable(consumable);
        }
    }

    // Optional kit to help the player advance at the early stages of the game
    private void initializeStartingKit() {
        for (int i = 0; i < 2; i++) {
            player.addConsumable(new HealthPotion());
        }
        for (int i = 0; i < 2; i++) {
            player.addConsumable(new ManaPotion());
        }
        for (int i = 0; i < 2; i++) {
            player.addConsumable(new StaminaPotion());
        }
        for (int i = 0; i < 4; i++) {
            player.addConsumable(new Torch());
        }
    }

    public void restIfPossible() {
        if (currentRoom.getCurrentSection() instanceof RestSection restSection) {
            restSection.rest(this);
        }
    }

    private void lootIfPossible() {
        Section section = currentRoom.getCurrentSection();
        if (section instanceof Lootable lootable) {
            boolean stillHasItem = lootable.swap(this);
            if (!stillHasItem) {
                int idx = currentRoom.getCurrentIndex();
                currentRoom.replaceSection(idx, new WalkSectionDecorator());
            }
        }
    }

    public void advanceSection() {
        if (!currentRoom.advance()) {
            return;
        }
        enterCurrentSection();
    }

    private void enterCurrentSection() {
        int idx = currentRoom.getCurrentIndex();
        Section section = currentRoom.getCurrentSection();
        section.enter(this);
        if (section.getType() == SectionType.TRAP && !(section instanceof WalkSectionDecorator)) {
            currentRoom.replaceSection(idx, new WalkSectionDecorator());
        }
        checkPlayerAlive();
        notifyGameStateObservers();
    }

    public void handleExit(char key) {
        if (key < '1' || key > '9') {
            notifyLogObservers("Press a number key (1-9) to choose the next room.");
            return;
        }
        int idx = key - '1';
        List<Room> connections = currentRoom.getAllConnections();
        if (idx >= connections.size()) {
            notifyLogObservers("There's no room " + key + " in this level.");
            return;
        }
        Room nextRoom = connections.get(idx);
        notifyLogObservers("You move on to " + (nextRoom.isVisited() ? nextRoom.getName() : "an unexplored room") + ".");
        enterRoom(nextRoom);
    }

    public void logExitOptions() {
        List<Room> connections = currentRoom.getAllConnections();
        StringBuilder sb = new StringBuilder("Choose where to go:");
        for (int i = 0; i < connections.size(); i++) {
            Room room = connections.get(i);
            String name = room.isVisited() ? room.getName() : "an unexplored room";
            String direction = currentRoom.getForwardConnections().contains(room) ? "forward" : "backward";
            sb.append(String.format("%n  [%d] %s (%s, floor %d)", i + 1, name, direction, room.getLayer()));
        }
        notifyLogObservers(sb.toString());
    }

    private void enterRoom(Room room) {
        currentRoom = room;
        roomGenerator.expand(room, graph, this);
        room.resetToEntry();
        mode = GameMode.EXPLORING;
        enterCurrentSection();
    }

    public void startEncounter() {
        int effectiveLevel = player.getLevel() + world.getLightLevel().getEnemyLevelBonus();
        if (currentRoom.isUnique()) {
            if (finalBoss == null) {
                finalBoss = enemyGenerator.spawnFinalBoss();
            }
            currentEnemy = finalBoss;
        } else {
            currentEnemy = enemyGenerator.spawn(effectiveLevel);
        }
        mode = GameMode.IN_COMBAT;
        if (rng.nextDouble() < world.getLightLevel().getEnemyAmbushChance()) {
            notifyLogObservers("A " + currentEnemy.getName() + " ambushes you out of the darkness! (HP: " + currentEnemy.getHP() + ")");
            combatManager.enemyTurn(currentEnemy);
            checkPlayerAlive();
        } else {
            notifyLogObservers("A " + currentEnemy.getName() + " appears! (HP: " + currentEnemy.getHP() + ")");
        }
    }

    public void onCombatWon(Equippable droppedItem) {
        Section replacement;
        if (droppedItem != null) {
            replacement = new LootSectionDecorator(droppedItem);
        } else {
            replacement = new WalkSectionDecorator();
        }
        replaceEncounterSection(replacement);

        currentEnemy = null;
        mode = GameMode.EXPLORING;
        unlockFinalRoomCheck();
    }

    public void onCombatFled() {
        boolean fledFromBoss = currentRoom.isUnique();
        currentEnemy = null;
        if (!fledFromBoss) {
            replaceEncounterSection(new WalkSectionDecorator());
        }
        mode = GameMode.EXPLORING;

        if (fledFromBoss) {
            fleeToRandomVisitedRoom();
        } else {
            currentRoom.retreat();
        }
    }

    private void fleeToRandomVisitedRoom() {
        List<Room> visitedRooms = graph.getAllVisitedRooms();
        visitedRooms.remove(currentRoom);
        Room randomRoom = visitedRooms.get(rng.nextInt(visitedRooms.size()));
        notifyLogObservers("You flee into a random room of the tower.");
        enterRoom(randomRoom);
    }

    private void replaceEncounterSection(Section replacement) {
        int idx = currentRoom.getCurrentIndex();
        Section section = currentRoom.getCurrentSection();
        boolean droppedLoot = replacement instanceof Lootable;

        boolean shouldReplace = section.getType() == SectionType.ENCOUNTER || (section.getType() == SectionType.WALK && droppedLoot);

        if (shouldReplace) {
            currentRoom.replaceSection(idx, replacement);
        }
    }

    private void unlockFinalRoomCheck() {
        if (finalRoomUnlocked || player.getLevel() < FINAL_LEVEL) return;
        finalRoomUnlocked = true;
        Room finalRoom = roomGenerator.buildFinalBossRoom(currentRoom.getLayer() + 1);
        graph.addRoom(finalRoom);
        currentRoom.addForwardConnection(finalRoom);
        notifyLogObservers("A portal to an unknown place has opened nearby...");
    }

    public void checkPlayerAlive() {
        if (!gameOver && !player.isAlive()) {
            gameOver = true;
            victory = false;
            mode = GameMode.GAME_OVER;
            notifyLogObservers("You have fallen hero. The tower claims another soul. Game over!");
        }
    }

    public void triggerVictory() {
        gameOver = true;
        victory = true;
        mode = GameMode.VICTORY;
        notifyLogObservers("The Avatar of Hastur has been defeated! The Tower of Sky is freed from its curse!");
    }

//    // Cheating methods for debugging
//    /** DEV TEST: Teleports the player to the boss room. */
//    public void jumpToBoss() {
//        player.addXP(48000);
//        unlockFinalRoomCheck();
//
//        Room bossRoom = graph.getRoomsAtLayer(currentRoom.getLayer() + 1).stream()
//                .filter(Room::isUnique)
//                .findFirst()
//                .orElse(null);
//        if (bossRoom == null) {
//            for (int layer = 0; layer <= currentRoom.getLayer() + 3; layer++) {
//                bossRoom = graph.getRoomsAtLayer(layer).stream()
//                        .filter(Room::isUnique)
//                        .findFirst()
//                        .orElse(null);
//                if (bossRoom != null) break;
//            }
//        }
//        if (bossRoom != null) {
//            notifyLogObservers("CHEAT SUCCEED: Teleporting to the Avatar of Hastur's chamber.");
//            enterRoom(bossRoom);
//        } else {
//            notifyLogObservers("CHEAT FAILED: Boss room doesn't exist yet.");
//        }
//    }
//
//    /** DEV TEST: Instantly defeating the current enemy. */
//    public void cheatWinCombat() {
//        if (mode != GameMode.IN_COMBAT || currentEnemy == null) {
//            notifyLogObservers("CHEAT FAILED: Not currently in combat.");
//            return;
//        }
//        notifyLogObservers("CHEAT SUCCEED: Instantly defeating " + currentEnemy.getName() + ".");
//        currentEnemy.takeDamage(currentEnemy.getMaxHP());
//        player.addXP(currentEnemy.getXpReward());
//        onCombatWon(null); // loot drop doesn't matter, we're cheating
//    }
}
