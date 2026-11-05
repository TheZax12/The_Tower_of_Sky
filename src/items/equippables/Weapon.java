package items.equippables;

import items.Rarity;
import player.Aptitude;

public class Weapon extends Equippable {

    public Weapon(String name, Rarity rarity, Aptitude primaryAptitude, double primaryBonus) {
        super(name, rarity);
        addBonus(primaryAptitude, primaryBonus);
    }

    public Weapon(String name, Rarity rarity,
                  Aptitude primaryAptitude, double primaryBonus,
                  Aptitude secondaryAptitude, double secondaryBonus) {
        super(name, rarity);
        addBonus(primaryAptitude, primaryBonus);
        addBonus(secondaryAptitude, secondaryBonus);
    }
}
