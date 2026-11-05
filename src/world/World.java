package world;

public class World {

    public static final int MAX_FOOD = 20;
    public static final int MAX_LIGHT = 100;

    private int food = MAX_FOOD;
    private int light = MAX_LIGHT;


    public int getFood() {
        return food;
    }

    public int getLight() {
        return light;
    }

    public void drainFood(int amount) {
        food = Math.max(0, food - amount);
    }

    public void drainLight(int amount) {
        light = Math.max(0, light - amount);
    }

    public void replenishFood(int amount) {
        food = Math.min(MAX_FOOD, food + amount);
    }

    public void replenishLight(int amount) {
        light = Math.min(MAX_LIGHT, light + amount);
    }

    public LightLevel getLightLevel() {
        return LightLevel.fromValue(light);
    }
}
