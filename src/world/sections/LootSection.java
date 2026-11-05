package world.sections;

import game.Game;
import items.equippables.Equippable;
import items.equippables.Weapon;
import items.equippables.Armor;

import java.util.Set;

public class LootSection extends Section implements Lootable {

    private Equippable item;

    public LootSection(Equippable item) {
        super(SectionType.LOOT);
        this.item = item;
    }

    public Equippable getItem() {
        return item;
    }

    @Override
    public int getLightCost() {
        return 4;
    }

    @Override
    protected void onEnter(Game game) {
        game.notifyLogObservers("You found " + item.describe() + ". Press \"P\" to equip it.");
    }

    public boolean swap(Game game) {
        Equippable previous = null;
        if (item instanceof Weapon weapon) {
            previous = game.getPlayer().equipWeapon(weapon);
        } else if (item instanceof Armor armor) {
            previous = game.getPlayer().equipArmor(armor);
        }
        game.notifyLogObservers("You equip " + item.getName() + ".");

        if (previous != null) {
            this.item = previous;
            game.notifyLogObservers("You left behind " + previous.describe() + ".");
            return true;
        }
        return false;
    }

    @Override
    public Set<Character> allowedKeys() {
        return Set.of(' ', 'p', 'P');
    }
}
