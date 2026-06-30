package dnd.units;

import dnd.board.Position;
import dnd.units.enemy.Enemy;
import dnd.units.player.Player;

/**
 * A shared dummy implementation of the Enemy class used strictly for testing.
 * <p>
 * This class locks the Random Number Generators (RNG) to always return maximum
 * values, allowing for 100% predictable mathematical tests for combat and abilities.
 */
public class DummyEnemy extends Enemy {

    /**
     * Constructs a dummy enemy with fixed stats for testing.
     *
     * @param name            The name of the enemy.
     * @param healthPool      The max health pool.
     * @param attackPower     The fixed attack roll.
     * @param defensePower    The fixed defense roll.
     * @param experienceValue The amount of XP granted when killed.
     */
    public DummyEnemy(String name, int healthPool, int attackPower, int defensePower, int experienceValue) {
        // Calls the Enemy abstract class constructor
        super(name, healthPool, attackPower, defensePower, experienceValue);
    }

    @Override
    public Position takeTurn(Player player) {
        return this.position;
    }

    // --- DETERMINISTIC MATH OVERRIDES ---

    /**
     * Overrides the RNG in Unit.java to always return the maximum attack power.
     * This guarantees combat tests don't randomly fail.
     */
    @Override
    public int rollAttack() {
        return this.attackPower;
    }

    /**
     * Overrides the RNG in Unit.java to always return the maximum defense power.
     */
    @Override
    public int rollDefense() {
        return this.defensePower;
    }

    // --- STUBBED METHODS ---

    /**
     * Stubs out the enemy turn logic.
     * Because we are only testing combat math and ability targeting in unit tests,
     * this dummy doesn't need to pathfind or move.
     */
    public void takeTurn() {
        // Do nothing in tests unless explicitly testing AI
    }

    // Note: If you haven't defined takeTurn() in your abstract Enemy class yet,
    // you can safely leave this out until you build the Enemy base class!
}