package items.consumables;

import game.Game;
import world.World;

public class Torch implements Consumable {
    private static final int REPLENISH_AMOUNT = World.MAX_LIGHT;

    @Override
    public String getName() {
        return "Torch";
    }

    @Override
    public String describe() {
        return "Restores the maximum light level of the world.";
    }

    @Override
    public boolean use(Game game) {
        World world = game.getWorld();
        if (world.getLight() >= World.MAX_LIGHT) {
            game.notifyLogObservers("The world's light level is already at its brightest.");
            return false;
        }
        game.getWorld().replenishLight(REPLENISH_AMOUNT);
        game.notifyLogObservers("You light a torch.");
        return true;
    }
}
