package enemy;

public class Enemy {
    
    private final String name;
    private final int maxHP;
    private int hp;
    private final int damage;
    private final int xpReward;

    public Enemy(String name, int hp, int damage, int xpReward) {
        this.name = name;
        this.maxHP = hp;
        this.hp = hp;
        this.damage = damage;
        this.xpReward = xpReward;
    }

    public String getName() {
        return name;
    }

    public int getHP() {
        return hp;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getDamage() {
        return damage;
    }

    public int getXpReward () {
        return xpReward;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }
}
