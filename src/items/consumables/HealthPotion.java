package items.consumables;

import game.Game;
import player.Player;
import player.Resource;

public class HealthPotion implements Consumable {

    private static final double REPLENISH_PERCENTAGE = 0.20;

    @Override
    public String getName() {
        return "Health Potion";
    }

    @Override
    public String describe() {
        return "Restores " + REPLENISH_PERCENTAGE * 100 + "% of the maximum health points.";
    }

    @Override
    public boolean use(Game game) {
        Player player = game.getPlayer();
        if (player.getResource(Resource.HEALTH) >= player.getMaxResource(Resource.HEALTH)) {
            game.notifyLogObservers("Your health points are already full.");
            return false;
        }
        int amount = (int) Math.round(player.getMaxResource(Resource.HEALTH) * REPLENISH_PERCENTAGE);
        game.getPlayer().restore(Resource.HEALTH, amount);
        game.notifyLogObservers("You consume a healing potion (+" + amount + " Health Points).");
        return true;
    }
}
