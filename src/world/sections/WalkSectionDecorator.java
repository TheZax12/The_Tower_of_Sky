package world.sections;

import game.Game;

import java.util.Set;

public class WalkSectionDecorator extends Section {


    public WalkSectionDecorator() {
        super(SectionType.WALK);
        this.explored = true;
    }

    @Override
    public int getLightCost() {
        return 0;
    }

    @Override
    protected void onEnter(Game game) {}

    @Override
    public Set<Character> allowedKeys() {
        return Set.of(' ');
    }
}
