package world.sections;

import game.Game;

import java.util.Set;

public class EncounterSection extends Section {

    public EncounterSection() {
        super(SectionType.ENCOUNTER);
    }

    @Override
    public int getLightCost() {
        return 0;
    }

    @Override
    protected void onEnter(Game game) {
        game.startEncounter();
    }

    @Override
    public Set<Character> allowedKeys() {
        return Set.of();
    }
}
