package items.equippables;

import items.Rarity;
import player.Aptitude;

public class Armor extends Equippable {

    public Armor(String name, Rarity rarity, double defenseBonus) {
        super(name, rarity);
        addBonus(Aptitude.DEFENSE, defenseBonus);
    }

    public Armor(String name, Rarity rarity, double defenseBonus, Aptitude secondaryAptitude, double secondaryBonus) {
        super(name, rarity);
        addBonus(Aptitude.DEFENSE, defenseBonus);
        addBonus(secondaryAptitude, secondaryBonus);
    }
}
