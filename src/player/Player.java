package player;

import items.equippables.Weapon;
import items.equippables.Armor;
import items.consumables.Consumable;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;

public class Player {
    
    public static final int BASE_HEALTH = 30;
    public static final int BASE_MANA = 15;
    public static final int BASE_STAMINA = 20;
    public static final double HEALTH_FACTOR = 20;
    public static final double MANA_FACTOR = 10;
    public static final double STAMINA_FACTOR = 15;
    public static final double DEFENSE_FACTOR = 5;

    private static final int[] XP_TABLE = {0, 100, 300, 700, 1500, 3000, 6000, 12000, 24000, 48000};

    private final String name;
    
    private final Map<Aptitude, Double> aptitudes = new EnumMap<>(Aptitude.class);
    private final Map<Resource, Integer> resources = new EnumMap<>(Resource.class);

    private int xp = 0;
    private int level = 1;

    private Weapon weapon;
    private Armor armor;
    private final List<Consumable> inventory = new ArrayList<>();

    public Player(String name, Map<Aptitude, Double> aptitudes) {
        this.name = name;
        this.aptitudes.putAll(aptitudes);
        for (Resource r : Resource.values()) {
            resources.put(r, getMaxResource(r));
        }
    }

    public String getName() {
        return name;
    }

    public double getAptitude(Aptitude aptitude) {
        return aptitudes.getOrDefault(aptitude, 0.0);
    }

    public double getEffectiveAptitude(Aptitude aptitude) {
        double base = getAptitude(aptitude);
        double weaponBonus = weapon == null ? 0.0 : weapon.getBonus(aptitude);
        double armorBonus = armor == null ? 0.0 : armor.getBonus(aptitude);
        return base + weaponBonus + armorBonus;
    }

    public int getResource(Resource resource) {
        return resources.get(resource);
    }

    public int getMaxResource(Resource resource) {
        double melee = getEffectiveAptitude(Aptitude.MELEE);
        double defense = getEffectiveAptitude(Aptitude.DEFENSE);
        double magicAtk = getEffectiveAptitude(Aptitude.MAGIC_ATK);
        double magicRst = getEffectiveAptitude(Aptitude.MAGIC_RST);

        return switch (resource) {
            case HEALTH -> (int) Math.round(BASE_HEALTH + level * (defense * HEALTH_FACTOR));
            case STAMINA -> (int) Math.round(BASE_STAMINA + level * ((0.6 * melee + 0.4 * defense) * STAMINA_FACTOR));
            case MANA -> (int) Math.round(BASE_MANA + level * ((magicAtk + magicRst) * MANA_FACTOR));
        };
    }

    public void drain(Resource r, int amount) {
        int updated = Math.max(0, resources.get(r) - amount);
        resources.put(r, updated);
    }

    public void restore(Resource r, int amount) {
        int updated = Math.min(getMaxResource(r), resources.get(r) + amount);
        resources.put(r, updated);
    }

    public boolean isAlive() {
        return getResource(Resource.HEALTH) > 0;
    }

    public int getXP() {
        return xp;
    }

    public int getXPForNextLevel() {
        return level < XP_TABLE.length ? XP_TABLE[level] : XP_TABLE[XP_TABLE.length - 1];
    }
    
    public void setLevel(int level) {
        this.level = level;
    } 

    public int getLevel() {
        return level;
    }

    public void addXP(int amount) {
        xp += amount;

        int newLevel = 1;
        for (int i = 0; i < XP_TABLE.length && xp >= XP_TABLE[i]; i++) {
            newLevel = i + 1;
        }
        boolean leveledUp = newLevel > level;
        setLevel(newLevel);
        if (leveledUp) {
            for (Resource resource : Resource.values()) {
                resources.put(resource, getMaxResource(resource));
            }
        }
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Armor getArmor() {
        return armor;
    }

    public Weapon equipWeapon(Weapon weapon) {
        Weapon previous = this.weapon;
        this.weapon = weapon;
        revaluateResource();
        return previous;
    }

    public Armor equipArmor(Armor armor) {
        Armor previous = this.armor;
        this.armor = armor;
        revaluateResource();
        return previous;
    }

    private void revaluateResource() {
        for (Resource resource : Resource.values()) {
            if (resources.containsKey(resource)) {
                resources.put(resource, Math.min(resources.get(resource), getMaxResource(resource)));
            }
        }
    }

    public List<Consumable> getInventory() {
        return inventory;
    }

    public void addConsumable(Consumable consumable) {
        inventory.add(consumable);
    }

    public Consumable takeConsumable(Class<? extends Consumable> type) {
        return inventory.stream().filter(type::isInstance)
                                 .findFirst()
                                 .map(c -> {inventory.remove(c); return c;})
                                 .orElse(null);
    }
}
