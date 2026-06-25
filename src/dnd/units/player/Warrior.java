package dnd.units.player;

import dnd.units.enemy.Enemy;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Warrior player character.
 * <p>
 * The Warrior uses a cooldown system to cast "Avenger's Shield",
 * an ability that heals the Warrior and randomly strikes one
 * nearby enemy for a percentage of the Warrior's max health.
 */
public class Warrior extends Player {
    /** The base number of ticks required to recharge the ability. */
    private final int abilityCooldown;

    /** The number of ticks remaining until the ability can be cast again. */
    private int remainingCooldown;

    /**
     * Constructs a new Warrior at Level 1 with 0 experience.
     *
     * @param name         The name of the Warrior character.
     * @param healthPool   The starting max health.
     * @param attackPower  The starting attack power.
     * @param defensePower The starting defense power.
     * @param abilityCooldown The number of game ticks required to recharge the ability.
     */
    public Warrior(String name, int healthPool, int attackPower, int defensePower, int abilityCooldown) {
        super(name, healthPool, attackPower, defensePower);
        this.abilityCooldown = abilityCooldown;
        this.remainingCooldown = 0; // Starts ready to use
    }

    /**
     * Applies the Warrior's specific stat bonuses upon leveling up,
     * in addition to the standard Player base stat increases.
     */
    @Override
    protected void levelUp() {
        // 1. Apply base Player updates
        super.levelUp();

        // 2. Reset Cooldown
        this.remainingCooldown = 0;

        // 3. Apply Warrior specific bonuses
        this.getHealth().addHealthPool(5);
        this.getHealth().heal(this.getHealth().getHealthPool() / 4); // Heal 25% of the new cap
        this.attackPower += 2;
        this.defensePower += 2; // (+2 here plus +1 in base = +3 total)

        // 4. Log the exact total stat gains
        int totalHealthGain = 20;
        int totalAttackGain = 7;
        int totalDefenseGain = 3;

        logMessage(getName() + " reached level " + this.level + ": +" + totalHealthGain + " Health, +" + totalAttackGain + " Attack, +" + totalDefenseGain + " Defense");
    }

    /**
     * Called once per game loop tick by the engine.
     * Reduces the remaining cooldown of the Avenger's Shield.
     */
    @Override
    public void onGameTick() {
        if (this.remainingCooldown > 0) {
            this.remainingCooldown--;
        }
    }

    /**
     * Casts Avenger's Shield.
     * Heals the warrior and deals direct damage to a random enemy within range.
     *
     * @param activeEnemies The list of all currently alive enemies on the board.
     * @param player The player character (ignored by the Warrior, as it is the player).
     * @return true if the cast was successful, false if on cooldown.
     */
    @Override
    public boolean castAbility(List<Enemy> activeEnemies, Player player) {
        // 1. Cooldown Check
        if (this.remainingCooldown > 0) {
            logMessage(getName() + " tried to cast Avenger's Shield, but there is a cooldown: " + this.remainingCooldown + ".");
            return false;
        }

        // 2. Set to abilityCooldown
        this.remainingCooldown = this.abilityCooldown;

        // 3. Heal
        int healAmount = Math.min(10 * this.defensePower, 50 * this.level);
        logMessage(getName() + " used Avenger's Shield, healing for " + healAmount + ".");
        this.getHealth().heal(healAmount);

        // 4. Find all enemies within range < 3
        List<Enemy> enemiesInRange = new ArrayList<>();
        for (Enemy enemy : activeEnemies) {
            if (this.getPosition().range(enemy.getPosition()) < 3) {
                enemiesInRange.add(enemy);
            }
        }

        // 5. Randomly hit one enemy and calculate mitigated damage
        if (!enemiesInRange.isEmpty()) {
            int randomIndex = (int) (Math.random() * enemiesInRange.size());
            Enemy target = enemiesInRange.get(randomIndex);

            int defenseRoll = target.rollDefense();
            logMessage(target.getName() + " rolled " + defenseRoll + " defense points.");

            // Base ability damage is 10% of Max HP, reduced by enemy defense roll
            int damageDealt = (int) (this.getHealth().getHealthPool() * 0.10) - defenseRoll;
            if (damageDealt < 0) {
                damageDealt = 0;
            }

            logMessage(getName() + " hit " + target.getName() + " for " + damageDealt + " ability damage.");

            if (damageDealt > 0) {
                target.getHealth().takeDamage(damageDealt);
            }

            if (target.getHealth().isDead()) {
                logMessage(target.getName() + " died. " + this.getName() + " gained " + target.getExperienceValue() + " experience");
                this.addExperience(target.getExperienceValue());
            }
        }

        return true;
    }

    /**
     * Appends the Warrior's class-specific properties (Ability Cooldowns)
     * to the standard player description.
     */
    @Override
    public String description() {
        return super.description() + String.format("\t\tCooldown: %d/%d",
                this.remainingCooldown, this.abilityCooldown);
    }

    public int getRemainingCooldown() { return remainingCooldown; }
    public int getAbilityCooldown() { return abilityCooldown; }
}
