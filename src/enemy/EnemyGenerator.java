package enemy;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EnemyGenerator {

    private record Entry(String name, int minSpawnLevel, int maxSpawnLevel, int hp, int damage, int xpReward) {};
    
    private final List<Entry> enemiesList = List.of(
            new Entry("Wandering Shadow", 1, 2, 15, 3, 30),
            new Entry("Pale Warden", 2, 3, 30, 6, 60),
            new Entry("Hollow Knight", 3, 5, 50, 10, 100),
            new Entry("Chaos Specter", 4, 6, 70, 14, 160),
            new Entry("Void Sentinel", 5, 7, 100, 20, 250),
            new Entry("Palid Dancer", 6, 7, 60, 30, 400),
            new Entry("Ghoul", 6, 7, 120, 25, 300),
            new Entry("Umbral Colossus", 7, 9, 150, 28, 400),
            new Entry("Shoggoth", 8, 9, 400, 10, 600),
            new Entry("Mi-Go", 9, 9, 300, 30, 1000)
    );

    private final Random rng;

    public EnemyGenerator(Random rng) {
        this.rng = rng;
    }

    public Enemy spawn(int effectiveLevel) {
        List<Entry> activeEnemies = enemiesList.stream()
                                               .filter(lvl -> effectiveLevel >= lvl.minSpawnLevel() && effectiveLevel <= lvl.maxSpawnLevel())
                                               .toList();
        Entry chosen;
        if (activeEnemies.isEmpty()) {
            chosen = enemiesList.stream()
                                .min(Comparator.comparingInt(a -> Math.abs(a.minSpawnLevel() - effectiveLevel)))
                                .orElse(enemiesList.get(0));
        } else {
            chosen = activeEnemies.get(rng.nextInt(activeEnemies.size()));
        }
        return new Enemy(chosen.name(), chosen.hp(), chosen.damage(), chosen.xpReward());
    }

    public Enemy spawnFinalBoss() {
        return new Enemy("Avatar of Hastur", 1200, 50, 0);
    }
}
