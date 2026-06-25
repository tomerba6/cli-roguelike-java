package dnd.units.player;

import dnd.units.enemy.Enemy;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Rogue player character.
 * <p>
 * The Rogue uses an Energy system (capped at 100) to cast "Fan of Knives",
 * an Area of Effect (AoE) ability that strikes all enemies within close range.
 */
public class Rogue extends Player {

    private static final int MAX_ENERGY = 100;

    private int cost;
    private int currentEnergy;

    /**
     * Constructs a new Rogue at Level 1.
     *
     * @param name         The name of the Rogue character.
     * @param healthPool   The starting max health.
     * @param attackPower  The starting attack power.
     * @param defensePower The starting defense power.
     * @param cost         The energy cost of the Fan of Knives ability.
     */
    public Rogue(String name, int healthPool, int attackPower, int defensePower, int cost) {
        super(name, healthPool, attackPower, defensePower);
        this.cost = cost;
        this.currentEnergy = MAX_ENERGY; // Starts fully topped up
    }

    // --- LEVELING UP ---

    /**
     * Applies the Rogue's specific stat bonuses upon leveling up.
     */
    @Override
    protected void levelUp() {
        // 1. Apply base Player updates (Level++, Base HP +10*lvl, Atk +4*lvl, Def +1*lvl)
        super.levelUp();

        // 2. Apply Rogue specific bonuses
        this.currentEnergy = MAX_ENERGY;
        this.attackPower += 3;

        // 3. Log the exact total stat gains (flat per level)
        // (Base +15 HP) + (Base +5 + Rogue +3 = +8 Atk) + (Base +1 Def)
        int totalHealthGain = 15;
        int totalAttackGain = 8;
        int totalDefenseGain = 1;

        logMessage(getName() + " reached level " + this.level + ": +" + totalHealthGain + " Health, +" + totalAttackGain + " Attack, +" + totalDefenseGain + " Defense");
    }

    // --- TIME PROCESSING ---

    /**
     * Called once per game loop tick by the engine.
     * Regenerates the Rogue's energy by a flat amount of 10.
     */
    @Override
    public void onGameTick() {
        this.currentEnergy = Math.min(this.currentEnergy + 10, MAX_ENERGY);
    }

    // --- SPECIAL ABILITY ---

    /**
     * Casts Fan of Knives.
     * Strikes every enemy within a range of < 2 for damage equal to the
     * Rogue's current attack power.
     *
     * @param activeEnemies The list of all currently alive enemies on the board.
     * @param player The player character.
     * @return true if the cast was successful, false if not enough energy.
     */
    @Override
    public boolean castAbility(List<Enemy> activeEnemies, Player player) {
        // 1. Fail-Fast: Not enough energy
        if (this.currentEnergy < this.cost) {
            logMessage(getName() + " tried to cast Fan of Knives, but there was not enough energy: " + this.currentEnergy + "/" + this.cost + ".");
            return false;
        }

        // 2. Cast successful
        logMessage(getName() + " cast Fan of Knives.");

        this.currentEnergy -= this.cost;

        // 3. Find all enemies in range
        List<Enemy> enemiesToHit = new ArrayList<>();
        for (Enemy enemy : activeEnemies) {
            if (!enemy.getHealth().isDead() && this.getPosition().range(enemy.getPosition()) < 2) {
                enemiesToHit.add(enemy);
            }
        }

        // 4. Strike all enemies in range
        for (Enemy target : enemiesToHit) {
            int defenseRoll = target.rollDefense();
            logMessage(target.getName() + " rolled " + defenseRoll + " defense points.");

            int damageDealt = this.attackPower - defenseRoll;
            if (damageDealt < 0) {
                damageDealt = 0;
            }

            logMessage(getName() + " hit " + target.getName() + " for " + damageDealt + " ability damage.");

            if (damageDealt > 0) {
                target.getHealth().takeDamage(damageDealt);
            }

            // Instant XP on kill
            if (target.getHealth().isDead()) {
                logMessage(target.getName() + " died. " + this.getName() + " gained " + target.getExperienceValue() + " experience");
                this.addExperience(target.getExperienceValue());
            }
        }

        return true;
    }

    // --- STATUS RENDERING ---

    /**
     * Appends the Rogue's class-specific properties (Energy)
     * to the standard player description.
     */
    @Override
    public String description() {
        return super.description() + String.format("\t\tEnergy: %d/%d",
                this.currentEnergy, MAX_ENERGY);
    }

    // Getters for potential unit testing
    public int getCurrentEnergy() { return currentEnergy; }
    public int getCost() { return cost; }
}