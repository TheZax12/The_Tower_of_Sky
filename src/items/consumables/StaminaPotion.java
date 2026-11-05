package items.consumables;

import game.Game;
import player.Player;
import player.Resource;

public class StaminaPotion implements Consumable {
    
    private static final double REPLENISH_PERCENTAGE = 0.15;

    @Override
    public String getName() {
        return "Stamina Potion";
    }

    @Override
    public String describe() {
        return "Restores" + REPLENISH_PERCENTAGE * 100 + "% of the maximum stamina points.";
    }

    @Override
    public boolean use(Game game) {
        Player player = game.getPlayer();
        if (player.getResource(Resource.STAMINA) >= player.getMaxResource(Resource.STAMINA)) {
            game.notifyLogObservers("Your stamina points are already full.");
            return false;
        }
        int amount = (int) Math.round(player.getMaxResource(Resource.STAMINA) * REPLENISH_PERCENTAGE);
        game.getPlayer().restore(Resource.STAMINA, amount);
        game.notifyLogObservers("You consume a stamina potion (+" + amount + " Stamina Points).");
        return true;
    }
}
