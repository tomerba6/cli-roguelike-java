package dnd.units.enemy;

import dnd.board.Position;
import dnd.combat.HeroicUnit;
import dnd.units.player.Player;

import java.util.List;

/**
 * Represents a Boss enemy.
 * <p>
 * Bosses behave like Monsters, but they maintain a combat timer.
 * When the timer reaches their ability frequency, they cast 'Shoebodybop' instead of moving.
 */
public class Boss extends Monster implements HeroicUnit {

    private int abilityFrequency;
    private int combatTicks;

    /**
     * Constructs a new Boss.
     */
    public Boss(String name, int healthPool, int attackPower, int defensePower, int experienceValue,
                int visionRange, int abilityFrequency, char displaySymbol) {
        super(name, healthPool, attackPower, defensePower, experienceValue, visionRange, displaySymbol);
        this.abilityFrequency = abilityFrequency;
        this.combatTicks = 0; // combat ticks initially 0
    }

    // --- ACTIVE AI & PATHFINDING ---

    /**
     * Executes the Boss's active turn logic.
     * Replaces standard Monster movement with ability casting based on combat ticks.
     *
     * @param player The active player character.
     * @return The target Position the boss wants to step into.
     */
    @Override
    public Position takeTurn(Player player) {
        Position currentPos = this.getPosition();
        double distance = currentPos.range(player.getPosition());

        // 1. Player is within vision range
        if (distance < this.visionRange) {

            if (this.combatTicks == this.abilityFrequency) {
                this.combatTicks = 0;
                this.castAbility(null, player);
                return currentPos;
            } else {
                this.combatTicks++;
                return super.takeTurn(player);
            }

        }
        // 2. Player is out of vision range
        else {
            this.combatTicks = 0;
            // Delegate the random roaming logic directly to the Monster superclass
            return super.takeTurn(player);
        }
    }

    // --- SPECIAL ABILITY ---

    /**
     * Casts the Shoebodybop ability.
     * Shoots the player for flat attack points if they are within vision range.
     *
     * @param activeEnemies Unused by the Boss.
     * @param player The target of the ability.
     * @return true (Boss abilities always successfully fire when triggered by AI).
     */
    @Override
    public boolean castAbility(List<Enemy> activeEnemies, Player player) {
        // 1. Log the initial cast
        logMessage(this.getName() + " shoots " + player.getName() + " for " + this.getAttackPower() + " damage");

        // 2. Player attempts to defend
        int defenseRoll = player.rollDefense();
        logMessage(player.getName() + " rolled " + defenseRoll + " defense points.");

        // 3. Calculate mitigated damage
        int damageDealt = this.getAttackPower() - defenseRoll;
        if (damageDealt < 0) {
            damageDealt = 0;
        }

        logMessage(this.getName() + " hit " + player.getName() + " for " + damageDealt + " ability damage.");

        // 4. Apply damage
        if (damageDealt > 0) {
            player.getHealth().takeDamage(damageDealt);
        }

        if (player.getHealth().isDead()) {
            logMessage(player.getName() + " was killed by " + this.getName() + ".");
            logMessage("You lost.");
        }

        return true;
    }

    public int getCombatTicks() { return combatTicks; }
}