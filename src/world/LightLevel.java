package world;

import items.Rarity;

public enum LightLevel implements LightBehavior {
    BRIGHT(0, 1.00, 1.00, 1.00, 0.00, 0.00, Rarity.FEEBLE),
    DIM(1, 0.85, 0.90, 0.85, 0.10, 0.15, Rarity.COMMON),
    DARK(2, 0.65, 0.70, 0.65, 0.20, 0.35, Rarity.COMMON),
    PITCH_BLACK(3, 0.40, 0.50, 0.40, 0.30, 0.60, Rarity.RARE);

    private final int enemyLevelBonus;
    private final double staminaDrainFactor;
    private final double restEffectiveness;
    private final double resourceFactor;
    private final double surpriseEncounterChance;
    private final double enemyAmbushChance;
    private final Rarity minimumQuality;

    LightLevel(int enemyLevelBonus, double staminaDrainFactor, double restEffectiveness, double resourceFactor,
               double surpriseEncounterChance, double enemyAmbushChance, Rarity minimumQuality) {
        this.enemyLevelBonus = enemyLevelBonus;
        this.staminaDrainFactor = staminaDrainFactor;
        this.restEffectiveness = restEffectiveness;
        this.resourceFactor = resourceFactor;
        this.surpriseEncounterChance = surpriseEncounterChance;
        this.enemyAmbushChance = enemyAmbushChance;
        this.minimumQuality = minimumQuality;
    }

    public static LightLevel fromValue(int light) {
        if (light >= 80) return BRIGHT;
        if (light >= 55) return DIM;
        if (light >= 25) return DARK;
        return PITCH_BLACK;
    }

    @Override
    public int getEnemyLevelBonus() {
        return enemyLevelBonus;
    }

    @Override
    public double getStaminaDrainFactor() {
        return staminaDrainFactor;
    }

    @Override
    public double getRestEffectiveness() {
        return restEffectiveness;
    }

    @Override
    public double getResourceFactor() {
        return resourceFactor;
    }

    @Override
    public double getSurpriseEncounterChance() {
        return surpriseEncounterChance;
    }

    @Override
    public double getEnemyAmbushChance() {
        return enemyAmbushChance;
    }

    @Override
    public Rarity getMinimumQuality() {
        return minimumQuality;
    }
}
