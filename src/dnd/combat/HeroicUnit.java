package dnd.combat;
import java.util.List;

import dnd.units.enemy.Enemy;
import dnd.units.player.Player;


/**
 * Represents a game unit capable of casting a special ability during combat.
 * This interface is implemented by player classes and boss enemies to standardize
 * how abilities are triggered, targeted, and resolved within the game engine.
 */
public interface HeroicUnit {

    /**
     * Casts the unit's special ability, applying its effects to valid targets.
     * * @param activeEnemies a list of all currently living enemies on the board.
     * Player classes use this to calculate distances, find
     * targets within ability range, and apply damage.
     * @param player        the current player character in the game. Boss units
     * use this reference to check if the player is within
     * their vision range to target them.
     */
    void castAbility(List<Enemy> activeEnemies, Player player);
}