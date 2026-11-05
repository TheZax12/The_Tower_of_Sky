package world.sections;

import game.Game;
import items.equippables.Equippable;

import java.util.Set;

public class LootSectionDecorator extends Section implements Lootable {

    private final LootSection lootSectionBehavior;

    public LootSectionDecorator(Equippable item) {
        super(SectionType.LOOT);
        this.lootSectionBehavior = new LootSection(item);
        this.explored = true;
    }

    public Equippable getItem() {
        return lootSectionBehavior.getItem();
    }

    @Override
    public int getLightCost() {
        return 0;
    }

    @Override
    protected void onEnter(Game game) {
        lootSectionBehavior.onEnter(game);
    }

    @Override
    public boolean swap(Game game) {
        return lootSectionBehavior.swap(game);
    }

    @Override
    public Set<Character> allowedKeys() {
        return Set.of(' ', 'p', 'P');
    }
}
