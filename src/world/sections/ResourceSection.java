package world.sections;

import game.Game;
import world.LightLevel;

import java.util.Random;
import java.util.Set;

public class ResourceSection extends Section {

    private static final int MAX_FOOD_GAIN = 10;
    private static final int MAX_LIGHT_GAIN = 60;
    private static final double REFRESH_CHANCE = 0.35;

    private final Random rng;

    public ResourceSection(Random rng) {
        super(SectionType.RESOURCE);
        this.rng = rng;
    }

    @Override
    public int getLightCost() {
        return 4;
    }

    @Override
    protected void onEnter(Game game) {
        if (explored && rng.nextDouble() >= REFRESH_CHANCE) {
            game.notifyLogObservers("This cache has already picked clean.");
            return;
        }
        LightLevel lightLevel = game.getWorld().getLightLevel();
        double factor = lightLevel.getResourceFactor();
        if (rng.nextBoolean()) {
            int amount = (int) Math.round(factor * MAX_FOOD_GAIN);
            game.getWorld().replenishFood(amount);
            game.notifyLogObservers("You found supplies: +" + amount + " food.");
        } else {
            int amount = (int) Math.round(factor * MAX_LIGHT_GAIN);
            game.getWorld().replenishLight(amount);
            game.notifyLogObservers("You find a light source: +" + amount + " light");
        }
    }

    @Override
    public Set<Character> allowedKeys() {
        return Set.of(' ');
    }
}
