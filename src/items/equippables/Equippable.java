package items.equippables;

import items.Item;
import items.Rarity;
import player.Aptitude;

import java.util.Map;
import java.util.EnumMap;

public abstract class Equippable implements Item {

    protected final String name;
    protected final Rarity rarity;
    protected final Map<Aptitude, Double> bonuses = new EnumMap<>(Aptitude.class);

    protected Equippable(String name, Rarity rarity) {
        this.name = name;
        this.rarity = rarity;
    }

    @Override
    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public double getBonus(Aptitude aptitude) {
        return bonuses.getOrDefault(aptitude, 0.0);
    }

    protected void addBonus(Aptitude aptitude, double amount) {
        bonuses.merge(aptitude, amount, Double::sum);
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder(name).append("[").append(rarity).append("]");
        bonuses.forEach((apt, val) -> sb.append(" ").append(apt).append("+").append(String.format("%.1f", val)));
        return sb.toString();
    }
}
