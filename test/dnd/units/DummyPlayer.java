package dnd.units;

import dnd.units.enemy.Enemy;
import dnd.units.player.Player;
import java.util.List;

/**
 * A shared dummy implementation of the Player class used strictly for testing.
 * Locks the RNG to always return maximum values for deterministic combat math.
 */
public class DummyPlayer extends Player {

    public DummyPlayer(String name, int healthPool, int attackPower, int defensePower) {
        super(name, healthPool, attackPower, defensePower);
    }

    // --- DETERMINISTIC MATH OVERRIDES ---
    @Override
    public int rollAttack() {
        return this.attackPower; // Always hits for max damage
    }

    @Override
    public int rollDefense() {
        return this.defensePower; // Always blocks for max defense
    }

    // --- STUBBED METHODS ---
    @Override
    public boolean castAbility(List<Enemy> activeEnemies, Player player) {
        // Do nothing. We are only testing core combat, not abilities.
        return false;
    }

    @Override
    public void onGameTick() {

    }
}