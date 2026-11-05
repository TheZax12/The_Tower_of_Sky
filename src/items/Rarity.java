package items;

public enum Rarity {
    FEEBLE(1),
    COMMON(3),
    RARE(6),
    SUPREME(10);

    private final int aptitudeBonus;

    Rarity(int aptitudeBonus) {
        this.aptitudeBonus = aptitudeBonus;
    }

    public int getAptitudeBonus() {
        return aptitudeBonus;
    }
}
