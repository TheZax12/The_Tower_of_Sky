package world.sections;

import game.Game;
import player.Player;
import player.Resource;

import java.util.Random;
import java.util.Set;

public class RestSection extends Section {

    private static final double REPLENISH_PERCENTAGE = 0.30;
    private final Random rng;

    public RestSection(Random rng) {
        super(SectionType.REST);
        this.rng = rng;
    }

    @Override
    public int getLightCost() {
        return 2;
    }

    @Override
    protected void onEnter(Game game) {
        game.notifyLogObservers("A quiet campfire. Press \"R\" to rest.");
    }

    public void rest(Game game) {
        Player player = game.getPlayer();
        if (game.getWorld().getFood() <= 0) {
            game.notifyLogObservers("There is no food left to eat. You can't benefit from resting.");
            return;
        }
        int foodCost = 1 + rng.nextInt(2);
        game.getWorld().drainFood(foodCost);

        double effectiveness = game.getWorld().getLightLevel().getRestEffectiveness();

        for (Resource resource : Resource.values()) {
            int amount = (int) Math.round(player.getMaxResource(resource) * REPLENISH_PERCENTAGE * effectiveness);
            player.restore(resource, amount);
        }
        game.notifyLogObservers("You rest by the fire and recover " + Math.round(REPLENISH_PERCENTAGE * effectiveness * 100)
                + "% of your health, stamina and mana points.");
        game.advanceSection();
    }

    @Override
    public Set<Character> allowedKeys() {
        return Set.of(' ', 'r', 'R');
    }
}
