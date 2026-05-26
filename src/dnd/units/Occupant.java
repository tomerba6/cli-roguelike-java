package dnd.units;

import dnd.combat.OccupantVisitor;

/**
 * Represents any entity that can physically stand on a Floor cell.
 * <p>
 * This interface acts as the "Receiver" in the Level 2 Visitor Pattern.
 * It forces all concrete occupants (like Players and Enemies) to implement
 * the {@code accept} method, allowing combat to resolve naturally through
 */
public interface Occupant {
    /**
     * Accepts a visitor (the unit initiating the interaction) and routes
     * the execution to the correct combat method (visit(Player) or visit(Enemy)).
     *
     * @param visitor The entity attempting to interact with (or attack) this occupant.
     */
    void accept(OccupantVisitor visitor);

    /**
     * Returns the visual representation of this occupant for the CLI rendering.
     * Overridden by concrete classes (e.g., returning '@' for the Player).
     *
     * @return The character representing the occupant.
     */
    String toString();
}
