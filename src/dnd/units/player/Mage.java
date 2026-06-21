package dnd.units.player;

import dnd.units.enemy.Enemy;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Mage player character.
 * <p>
 * The Mage relies on a Mana pool to cast "Blizzard", a powerful multi-hit spell
 * that targets random enemies within range using Spell Power.
 */
public class Mage extends Player {

    private int manaPool;
    private int currentMana;
    private int manaCost;
    private int spellPower;
    private int hitsCount;
    private int abilityRange;

    /**
     * Constructs a new Mage at Level 1.
     *
     * @param name         The name of the Mage character.
     * @param healthPool   The starting max health.
     * @param attackPower  The starting attack power.
     * @param defensePower The starting defense power.
     * @param manaPool     The starting max mana.
     * @param manaCost     The mana cost of the Blizzard ability.
     * @param spellPower   The base damage of the Blizzard ability.
     * @param hitsCount    The maximum number of times Blizzard can hit.
     * @param abilityRange The range of the Blizzard ability.
     */
    public Mage(String name, int healthPool, int attackPower, int defensePower,
                int manaPool, int manaCost, int spellPower, int hitsCount, int abilityRange) {
        super(name, healthPool, attackPower, defensePower);
        this.manaPool = manaPool;
        this.currentMana = manaPool / 4;
        this.manaCost = manaCost;
        this.spellPower = spellPower;
        this.hitsCount = hitsCount;
        this.abilityRange = abilityRange;
    }

    // --- LEVELING UP ---

    /**
     * Applies the Mage's specific stat bonuses upon leveling up.
     */
    @Override
    protected void levelUp() {
        // 1. Apply base Player updates (Level++, Base HP +10*lvl, Atk +4*lvl, Def +1*lvl)
        super.levelUp();

        // 2. Calculate Mage specific stat gains
        int manaGain = 25 * this.level;
        int spellPowerGain = 10 * this.level;

        // 3. Apply Mage specific bonuses
        this.manaPool += manaGain;
        this.currentMana = Math.min(this.currentMana + (this.manaPool / 4), this.manaPool);
        this.spellPower += spellPowerGain;

        // Note: No extra HP/Atk/Def to add here, the Mage takes exactly the base gains.
        int totalHealthGain = 10 * this.level;
        int totalAttackGain = 4 * this.level;
        int totalDefenseGain = 1 * this.level;

        // 4. Log the exact total stat gains
        logMessage(getName() + " reached level " + this.level + ": +" + totalHealthGain + " Health, +" + totalAttackGain + " Attack, +" + totalDefenseGain + " Defense");
        logMessage("                +" + manaGain + " maximum mana, +" + spellPowerGain + " spell power");
    }

    // --- TIME PROCESSING ---

    /**
     * Called once per game loop tick by the engine.
     * Regenerates the Mage's mana based on their current level.
     */
    @Override
    public void onGameTick() {
        this.currentMana = Math.min(this.manaPool, this.currentMana + (this.level));
    }

    // --- SPECIAL ABILITY ---

    /**
     * Casts Blizzard.
     * Strikes random enemies within range multiple times. Each hit deals damage equal
     * to the Mage's spell power minus the enemy's defense roll.
     *
     * @param activeEnemies The list of all currently alive enemies on the board.
     * @param player The player character.
     * @return true if the cast was successful, false if not enough mana.
     */
    @Override
    public boolean castAbility(List<Enemy> activeEnemies, Player player) {
        // 1. Fail-Fast: Not enough mana
        if (this.currentMana < this.manaCost) {
            logMessage(getName() + " tried to cast Blizzard, but there was not enough mana: " + this.currentMana + "/" + this.manaCost + ".");
            return false;
        }

        // 2. Cast successful
        logMessage(getName() + " cast Blizzard.");

        this.currentMana -= this.manaCost;

        int hits = 0;

        // 3. Execute hits
        while (hits < this.hitsCount) {
            // Find all currently living enemies within range
            List<Enemy> enemiesInRange = new ArrayList<>();
            for (Enemy enemy : activeEnemies) {
                if (!enemy.getHealth().isDead() && this.getPosition().range(enemy.getPosition()) < this.abilityRange) {
                    enemiesInRange.add(enemy);
                }
            }

            // Break the loop if the area is clear
            if (enemiesInRange.isEmpty()) {
                break;
            }

            // Select a random living enemy in range
            int randomIndex = (int) (Math.random() * enemiesInRange.size());
            Enemy target = enemiesInRange.get(randomIndex);

            // Deal damage
            int defenseRoll = target.rollDefense();
            logMessage(target.getName() + " rolled " + defenseRoll + " defense points.");

            int damageDealt = this.spellPower - defenseRoll;
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

            hits++;
        }

        return true;
    }

    /**
     * Appends the Mage's class-specific properties (Mana and Spell Power)
     * to the standard player description.
     */
    @Override
    public String description() {
        return super.description() + String.format("\t\tMana: %d/%d\t\tSpell Power: %d",
                this.currentMana, this.manaPool, this.spellPower);
    }

    // Getters for potential unit testing
    public int getCurrentMana() { return currentMana; }
    public int getManaPool() { return manaPool; }
    public int getSpellPower() { return spellPower; }
}