package items.consumables;

import game.Game;
import player.Player;
import player.Resource;

public class ManaPotion implements Consumable {

    private static final double REPLENISH_PERCENTAGE = 0.10;

    @Override
    public String getName() {
        return "Mana Potion";
    }

    @Override
    public String describe() {
        return "Restores " + REPLENISH_PERCENTAGE * 100 + "% of the maximum mana points.";
    }

    @Override
    public boolean use(Game game) {
        Player player = game.getPlayer();
        if (player.getResource(Resource.MANA) >= player.getMaxResource(Resource.MANA)) {
            game.notifyLogObservers("Your mana points are already full.");
            return false;
        }
        int amount = (int) Math.round(player.getMaxResource(Resource.MANA) * REPLENISH_PERCENTAGE);
        game.getPlayer().restore(Resource.MANA, amount);
        game.notifyLogObservers("You consume a mana potion (+" + amount + " Mana Points).");
        return true;
    }
}
