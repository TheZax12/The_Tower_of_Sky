package combat;

import game.Game;
import items.Rarity;
import items.consumables.*;
import items.equippables.*;
import player.Aptitude;
import player.Player;
import player.Resource;
import enemy.Enemy;

import java.util.Random;

public class CombatManager {

    private static final double COMBAT_STAMINA_COST_PERCENTAGE = 0.15;
    private static final double MANA_SPELL_COST_PERCENTAGE = 0.10;
    private static final double MANA_HEAL_COST_PERCENTAGE = 0.20;
    private static final double FLEE_STAMINA_COST_PERCENTAGE = 0.10;
    private static final double FLEE_SUCCESS_CHANCE = 0.7;
    private static final int DAMAGE_SCALE = 10;
    private static final int HEALING_SCALE = 8;

    private final Game game;
    private final Random rng;

    public CombatManager(Game game) {
        this.game = game;
        this.rng = game.getRng();
    }

    public void resolve(CombatAction action) {
        if (action == null) return;
        Enemy enemy = game.getCurrentEnemy();
        if (enemy == null) return;
        Player player = game.getPlayer();

        boolean actionSucceeded;
        switch (action) {
            case ATTACK:
                applyDamageToEnemy(enemy, meleeDamage());
                actionSucceeded = true;
                break;
            case RANGED:
                applyDamageToEnemy(enemy, rangeDamage());
                actionSucceeded = true;
                break;
            case SPELL:
                if (player.getResource(Resource.MANA) <= 0) {
                    game.notifyLogObservers("Not enough mana to cast a spell.");
                    actionSucceeded = false;
                    break;
                }
                int spellManaCost = (int) Math.round(MANA_SPELL_COST_PERCENTAGE * player.getMaxResource(Resource.MANA));
                applyDamageToEnemy(enemy, spellDamage());
                player.drain(Resource.MANA, spellManaCost);
                actionSucceeded = true;
                break;
            case HEAL:
                if (player.getResource(Resource.MANA) <= 0) {
                    game.notifyLogObservers("Not enough mana to cast a healing spell.");
                    actionSucceeded = false;
                    break;
                }
                int healAmount = healAmount();
                int healManaCost = (int) Math.round(MANA_HEAL_COST_PERCENTAGE * player.getMaxResource(Resource.MANA));
                player.restore(Resource.HEALTH, healAmount);
                player.drain(Resource.MANA, healManaCost);
                game.notifyLogObservers("You cast a healing spell (+" + healAmount + " HP).");
                actionSucceeded = true;
                break;
            case FLEE:
                actionSucceeded = !attemptFlee();
                break;
            case USE_HEALTH_POTION:
                actionSucceeded = game.usePotion(HealthPotion.class);
                break;
            case USE_MANA_POTION:
                actionSucceeded = game.usePotion(ManaPotion.class);
                break;
            case USE_STAMINA_POTION:
                actionSucceeded = game.usePotion(StaminaPotion.class);
                break;
            default:
                actionSucceeded = false;
        }
        
        if (!actionSucceeded) return;

        if (!enemy.isAlive()) {
            onEnemyDefeat(enemy);
            return;
        }
        double staminaDrainFactor = game.getWorld().getLightLevel().getStaminaDrainFactor();
        int staminaCost = (int) Math.round(player.getMaxResource(Resource.STAMINA) * COMBAT_STAMINA_COST_PERCENTAGE / staminaDrainFactor);
        player.drain(Resource.STAMINA, staminaCost);
        enemyTurn(enemy);
        game.checkPlayerAlive();
    }

    private void applyDamageToEnemy(Enemy enemy, int damage) {
        enemy.takeDamage(damage);
        game.notifyLogObservers("You deal " + damage + " damage to " + enemy.getName() + ".");
    }

    private int meleeDamage() {
        double melee = game.getPlayer().getEffectiveAptitude(Aptitude.MELEE);
        int base = Math.max(1, (int) Math.round(melee * DAMAGE_SCALE));
        int min = base / 2;
        return min + rng.nextInt(base - min + 1);
    }

    private int rangeDamage() {
        double ranged = game.getPlayer().getEffectiveAptitude(Aptitude.RANGED);
        int base = Math.max(1, (int) Math.round(ranged * DAMAGE_SCALE));
        int min = base / 2;
        int damage = min + rng.nextInt(base - min + 1);
        if (rng.nextDouble() < ranged * 0.15) {
            damage *= 2;
            game.notifyLogObservers("Critical shot! Double damage.");
        }
        return damage;
    }

    private int spellDamage() {
        double cast = game.getPlayer().getEffectiveAptitude(Aptitude.MAGIC_ATK);
        return Math.max(1, (int) Math.round(cast * DAMAGE_SCALE));
    }

    private int healAmount() {
        double restore = game.getPlayer().getEffectiveAptitude(Aptitude.MAGIC_RST);
        return Math.max(1, (int) Math.round(restore *  HEALING_SCALE));
    }

    private boolean attemptFlee() {
        Player player = game.getPlayer();
        if (player.getResource(Resource.STAMINA) <= 0) {
            game.notifyLogObservers("Too exhausted to flee.");
            return false;
        }
        int staminaCost = (int) Math.round(player.getMaxResource(Resource.STAMINA) * FLEE_STAMINA_COST_PERCENTAGE);
        player.drain(Resource.STAMINA, staminaCost);
        if (rng.nextDouble() < FLEE_SUCCESS_CHANCE) {
            game.notifyLogObservers("You flee from the fight!");
            game.onCombatFled();
            return true;
        }
        game.notifyLogObservers("Flee attempt fails!");
        return false;
    }

    public void enemyTurn(Enemy enemy) {
        Player player = game.getPlayer();
        double staminaRatio = player.getMaxResource(Resource.STAMINA) == 0 ? 0 :
                (double) player.getResource(Resource.STAMINA) / player.getMaxResource(Resource.STAMINA);
        int effectiveDefense = (int) (player.getEffectiveAptitude(Aptitude.DEFENSE) * Player.DEFENSE_FACTOR * staminaRatio);
        int damageAmount = Math.max(1, enemy.getDamage() - effectiveDefense);
        player.drain(Resource.HEALTH, damageAmount);
        game.notifyLogObservers(enemy.getName() + " deals you " + damageAmount + " damage.");
    }

    private void onEnemyDefeat(Enemy enemy) {
        Player player = game.getPlayer();
        game.notifyLogObservers("You defeated the " + enemy.getName() + "! (+" + enemy.getXpReward() + " XP)");
        player.addXP(enemy.getXpReward());

        Equippable equippable = null;
        Rarity minimumQuality = game.getWorld().getLightLevel().getMinimumQuality();
        double roll = rng.nextDouble();
        if (roll < 0.05) {
            equippable = game.getItemGenerator().randomEquippable(player.getLevel(), minimumQuality);
            game.notifyLogObservers("The fallen " + enemy.getName() + " drops " + equippable.describe() + ". Press \"P\" to equip it.");
        } else if (roll < 0.30) {
            Consumable consumable = randomConsumable();
            player.addConsumable(consumable);
            game.notifyLogObservers("The fallen " + enemy.getName() + " drops " + consumable.getName() + ".");
        }
        game.onCombatWon(equippable);
    }

    private Consumable randomConsumable() {
        return switch (rng.nextInt(4)) {
            case 0 -> new HealthPotion();
            case 1 -> new ManaPotion();
            case 2 -> new StaminaPotion();
            default -> new Torch();
        };
    }
}