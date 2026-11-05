package world.sections;

import game.Game;

import java.util.Set;

public abstract class Section {

    protected final SectionType type;
    protected boolean explored = false;

    protected Section(SectionType sectionType) {
        this.type = sectionType;
    }

    public final void enter(Game game) {
        game.getWorld().drainLight(getLightCost());
        onEnter(game);
        explored = true;
    }

    public SectionType getType() {
        return type;
    }

    public boolean isExplored() {
        return explored;
    }

    public String label() {
        return type.name().charAt(0) + type.name().substring(1).toLowerCase();
    }
    
    public abstract int getLightCost();
    protected abstract void onEnter(Game game);
    public abstract Set<Character> allowedKeys();
}
