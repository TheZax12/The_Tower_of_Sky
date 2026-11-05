package world.sections;

import game.Game;
import world.LightLevel;

import java.util.Random;
import java.util.Set;

public class WalkSection extends Section {

    private final Random rng;

    public WalkSection(Random rng) {
        super(SectionType.WALK);
        this.rng = rng;
    }

    @Override
    public int getLightCost() {
        int cost;
        if (!isExplored()) {
        cost = 6;
       } else {
        cost = 2;
       }
       return cost;
    }

    @Override
    protected void onEnter(Game game) {
        LightLevel lightLevel = game.getWorld().getLightLevel();
        if (rng.nextDouble() < lightLevel.getSurpriseEncounterChance()) {
            game.startEncounter();
        }
    }

    @Override
    public Set<Character> allowedKeys() {
        return Set.of(' ');
    }
}
