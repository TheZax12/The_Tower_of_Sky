package world.sections;

import game.Game;
import player.Player;
import player.Resource;

import java.util.Random;
import java.util.Set;

public class TrapSection extends Section {

    private final Random rng;

    public TrapSection(Random rng) {
        super(SectionType.TRAP);
        this.rng = rng;
    }

    @Override
    public int getLightCost() {
        return 4;
    }

    @Override
    protected void onEnter(Game game) {
        Player player = game.getPlayer();
        int maxHP = player.getMaxResource(Resource.HEALTH);
        double percentage = 0.10 + rng.nextDouble() * 0.15;
        int damage = Math.max(1, (int) Math.round(maxHP * percentage));
        player.drain(Resource.HEALTH, damage);
        game.notifyLogObservers("You stepped into a trap! You take " + damage + " damage.");
    }

    @Override
    public Set<Character> allowedKeys() {
        return Set.of(' ');
    }
}
