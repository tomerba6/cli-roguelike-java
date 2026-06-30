package dnd.units.player;

import dnd.units.enemy.Enemy;

import java.util.List;

/**
 * Represents the Hunter player character (Bonus Class).
 * <p>
 * The Hunter uses Arrows to cast "Shoot", an ability that automatically targets
 * and strikes the closest enemy within their effective range.
 */
public class Hunter extends Player {

    private int range;
    private int arrowsCount;
    private int ticksCount;

    /**
     * Constructs a new Hunter at Level 1.
     *
     * @param name         The name of the Hunter character.
     * @param healthPool   The starting max health.
     * @param attackPower  The starting attack power.
     * @param defensePower The starting defense power.
     * @param range        The shooting range of the Hunter.
     */
    public Hunter(String name, int healthPool, int attackPower, int defensePower, int range) {
        super(name, healthPool, attackPower, defensePower);
        this.range = range;
        this.arrowsCount = 10 * this.level; // Starts at 10 * level (Level 1)
        this.ticksCount = 0;
    }

    // --- LEVELING UP ---

    /**
     * Applies the Hunter's specific stat bonuses upon leveling up.
     */
    @Override
    protected void levelUp() {
        // 1. Apply base Player updates (Level++, Base HP +10*lvl, Atk +4*lvl, Def +1*lvl)
        super.levelUp();

        // 2. Apply Hunter specific bonuses — fill quiver to new cap
        this.arrowsCount = 10 * this.level;
        this.attackPower += 2;
        this.defensePower += 1;

        // 3. Log the exact total stat gains (flat per level)
        int totalHealthGain = 15; // Base +15
        int totalAttackGain = 7;  // Base +5 + Hunter +2
        int totalDefenseGain = 2; // Base +1 + Hunter +1

        logMessage(getName() + " reached level " + this.level + ": +" + totalHealthGain + " Health, +" + totalAttackGain + " Attack, +" + totalDefenseGain + " Defense");
    }

    // --- TIME PROCESSING ---

    /**
     * Called once per game loop tick by the engine.
     * Regenerates exactly 1 arrow every 10 ticks, capped at 10 × current level.
     */
    @Override
    public void onGameTick() {
        if (this.ticksCount == 10) {
            this.arrowsCount = Math.min(this.arrowsCount + 1, 10 * this.level);
            this.ticksCount = 0;
        } else {
            this.ticksCount++;
        }
    }

    // --- SPECIAL ABILITY ---

    /**
     * Casts Shoot.
     * Consumes an arrow to deal flat attack damage to the closest enemy within range.
     *
     * @param activeEnemies The list of all currently alive enemies on the board.
     * @param player The player character.
     * @return true if successful, false if no arrows or no valid targets.
     */
    @Override
    public boolean castAbility(List<Enemy> activeEnemies, Player player) {
        // 1. Fail-Fast: No arrows
        if (this.arrowsCount == 0) {
            logMessage(getName() + " tried to shoot an arrow but they have no arrows left.");
            return false;
        }

        // 2. Find the closest living enemy within range
        Enemy closestEnemy = null;
        double minDistance = Double.MAX_VALUE;

        for (Enemy enemy : activeEnemies) {
            if (!enemy.getHealth().isDead()) {
                double distance = this.getPosition().range(enemy.getPosition());

                if (distance <= this.range && distance < minDistance) {
                    minDistance = distance;
                    closestEnemy = enemy;
                }
            }
        }

        // 3. Fail-Fast: No valid targets
        // If the player presses 'e' but no one is around, the cast fails and the engine ticks.
        if (closestEnemy == null) {
            logMessage(getName() + " tried to shoot an arrow but there were no enemies in range.");
            return false;
        }

        // 4. Ability successful! Deduct exactly one arrow.
        this.arrowsCount -= 1;
        logMessage(getName() + " fired an arrow at " + closestEnemy.getName() + ".");

        // 5. Deal damage
        int defenseRoll = closestEnemy.rollDefense();
        logMessage(closestEnemy.getName() + " rolled " + defenseRoll + " defense points.");

        int damageDealt = this.attackPower - defenseRoll;
        if (damageDealt < 0) {
            damageDealt = 0;
        }

        logMessage(getName() + " hit " + closestEnemy.getName() + " for " + damageDealt + " ability damage.");

        if (damageDealt > 0) {
            closestEnemy.getHealth().takeDamage(damageDealt);
        }

        // 6. Instant XP on kill
        if (closestEnemy.getHealth().isDead()) {
            logMessage(closestEnemy.getName() + " died. " + this.getName() + " gained " + closestEnemy.getExperienceValue() + " experience");
            this.addExperience(closestEnemy.getExperienceValue());
        }

        return true;
    }

    // --- STATUS RENDERING ---

    /**
     * Appends the Hunter's class-specific properties (Arrows and Range)
     * to the standard player description.
     */
    @Override
    public String description() {
        return super.description() + String.format("\t\tArrows: %d/%d\t\tRange: %d",
                this.arrowsCount, 10 * this.level, this.range);
    }

    // Getters for potential unit testing
    public int getArrowsCount() { return arrowsCount; }
    public int getTicksCount() { return ticksCount; }
    public int getRange() { return range; }
}