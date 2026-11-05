package world;

import items.Rarity;

public interface LightBehavior {
    int getEnemyLevelBonus();
    double getStaminaDrainFactor();
    double getRestEffectiveness();
    double getResourceFactor();
    double getSurpriseEncounterChance();
    double getEnemyAmbushChance();
    Rarity getMinimumQuality();
}
