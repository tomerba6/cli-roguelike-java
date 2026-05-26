package dnd.combat;

import dnd.units.enemy.Enemy;
import dnd.units.player.Player;

/**
 * The Level 2 Visitor interface for combat and entity interaction.
 * <p>
 * Any unit that can engage in combat must implement this interface.
 * Once a unit successfully steps onto a Floor, it "visits" the Occupant
 * of that floor to resolve combat mathematically without using instanceof.
 */
public interface OccupantVisitor {

    /**
     * Defines the interaction when the visitor targets a Player.
     * For Enemies, this initiates an attack against the Player.
     * * @param p The player being targeted.
     */
    public void visit(Player player);

    /**
     * Defines the interaction when the visitor targets an Enemy.
     * For Players, this initiates an attack against the Monster or Trap.
     * * @param e The enemy being targeted.
     */
    public void visit(Enemy enemy);
}
