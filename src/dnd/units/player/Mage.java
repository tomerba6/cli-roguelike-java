package dnd.units.player;

import dnd.units.enemy.Enemy;

import java.util.List;

public class Mage extends Player {
    /**
     * Constructs a new Player at Level 1 with 0 experience.
     *
     * @param name         The name of the player character.
     * @param healthPool   The starting max health.
     * @param attackPower  The starting attack power.
     * @param defensePower The starting defense power.
     */
    public Mage(String name, int healthPool, int attackPower, int defensePower) {
        super(name, healthPool, attackPower, defensePower);
    }

    @Override
    public void castAbility(List<Enemy> activeEnemies, Player player) {

    }
}
