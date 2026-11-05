package world.sections;

import game.Game;
import game.GameMode;

import java.util.Set;

public class GraceSection extends Section {

    public GraceSection(SectionType role) {
        super(role);
        if (role != SectionType.ENTRY && role != SectionType.EXIT) {
            throw new IllegalArgumentException(" Grace section role must be ENTRY or EXIT");
        }
    }

    @Override
    public int getLightCost() {
        return 0;
    }

    @Override
    protected void onEnter(Game game) {
        if (type == SectionType.ENTRY) {
            game.setMode(GameMode.EXPLORING);
        } else {
            if (game.getCurrentRoom().isUnique()) {
                game.triggerVictory();
            } else {
                game.setMode(GameMode.AT_EXIT);
                game.logExitOptions();
            }
        }
    }

    @Override
    public Set<Character> allowedKeys() {
        return type == SectionType.ENTRY ? Set.of(' ') :
                                           Set.of('1', '2', '3', '4', '5', '6', '7', '8', '9');
    }
}
