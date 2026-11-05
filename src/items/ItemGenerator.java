package items;

import items.equippables.Equippable;
import items.equippables.Weapon;
import items.equippables.Armor;
import player.Aptitude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemGenerator {

    private static final Rarity[] RARITY_TABLE = {
        Rarity.FEEBLE, Rarity.FEEBLE, Rarity.FEEBLE, Rarity.FEEBLE,
        Rarity.COMMON, Rarity.COMMON, Rarity.COMMON,
        Rarity.RARE, Rarity.RARE,
        Rarity.SUPREME
    };

    private static final List<String> PREFIXES =
            List.of("Ancient", "Cursed", "Radiant", "Forgotten", "Silent", "Whispering");
    private static final List<String> SUFFIXES =
            List.of("of the Void", "of Ruin", "of the Deep", "of Heavens", "of Fire", "of the Darkness");
    private static final List<String> WEAPON_NOUNS =
            List.of("Sword", "Staff", "Bow", "Dagger", "Blade");
    private static final List<String> ARMOR_NOUNS =
            List.of("Chain Mail", "Cloak", "Lamellar", "Hauberk", "Cuirass");

    private final Random rng;

    public ItemGenerator(Random rng) {
        this.rng = rng;
    }

    public Equippable randomEquippable(int playerLevel, Rarity minimumQuality) {
        Rarity rarity = getRarityAtLeast(minimumQuality);
        boolean isWeapon = rng.nextBoolean();
        String name = randomItemName(isWeapon);
        double bonus = rarity.getAptitudeBonus() + playerLevel / 2.0;

        if (isWeapon) {
            return buildWeapon(name, rarity, bonus);
        } else {
            return buildArmor(name, rarity, bonus);
        }
    }

    private Rarity getRarityAtLeast(Rarity minimumRarity) {
        List<Rarity> rarities = Arrays.stream(RARITY_TABLE)
                                      .filter(r -> r.ordinal() >= minimumRarity.ordinal())
                                      .toList();
        if (rarities.isEmpty()) return minimumRarity;
        return rarities.get(rng.nextInt(rarities.size()));
    }

    private Weapon buildWeapon(String name, Rarity rarity, double bonus) {
        List<Aptitude> attackAptitudes = List.of(Aptitude.MELEE, Aptitude.RANGED, Aptitude.MAGIC_ATK);
        Aptitude primaryAptitude = attackAptitudes.get(rng.nextInt(attackAptitudes.size()));
        double primaryBonus = getPrimaryBonus(bonus);

        if (rng.nextBoolean()) {
            List<Aptitude> pool = Arrays.stream(Aptitude.values())
                    .filter(apt -> apt != primaryAptitude)
                    .toList();
            Aptitude secondaryAptitude = pool.get(rng.nextInt(pool.size()));
            double secondaryBonus = bonus - primaryBonus;
            return new Weapon(name, rarity, primaryAptitude, primaryBonus, secondaryAptitude, secondaryBonus);
        }
        return new Weapon(name, rarity, primaryAptitude, primaryBonus);
    }

    private Armor buildArmor(String name, Rarity rarity, double bonus) {
        double primaryBonus = getPrimaryBonus(bonus);
        if (rng.nextBoolean()) {
            List<Aptitude> pool = Arrays.stream(Aptitude.values())
                                                      .filter(apt -> apt != Aptitude.DEFENSE)
                                                      .toList();
            Aptitude secondaryAptitude = pool.get(rng.nextInt(pool.size()));
            double secondaryBonus = bonus - primaryBonus;
            return new Armor(name, rarity, primaryBonus, secondaryAptitude, secondaryBonus);
        }
        return new Armor(name, rarity, primaryBonus);
    }

    private double getPrimaryBonus(double bonus) {
        return bonus * 0.7;
    }

    private String randomItemName(boolean isWeapon) {
        List<String> nouns = isWeapon ? WEAPON_NOUNS : ARMOR_NOUNS;
        String prefix = PREFIXES.get(rng.nextInt(PREFIXES.size()));
        String noun = nouns.get(rng.nextInt(nouns.size()));
        String suffix = SUFFIXES.get(rng.nextInt(SUFFIXES.size()));
        return prefix + " " + noun + " " + suffix;
    }
}
