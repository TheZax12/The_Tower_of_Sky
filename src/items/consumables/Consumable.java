package items.consumables;

import items.Item;
import game.Game;

public interface Consumable extends Item {
    boolean use(Game game);
}
