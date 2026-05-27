package dnd.board;

import dnd.combat.CellVisitor;

/**
 * Represents a solid wall on the game board.
 * Walls are impassable for all units.
 */
public class Wall implements Cell {

    /**
     * Accepts a visitor and explicitly routes the execution to the visit(Wall) method.
     *
     * @param visitor The entity attempting to interact with this cell.
     * @throws IllegalArgumentException if visitor is null.
     */
    @Override
    public void accept(CellVisitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("Fatal Error: visitor cannot be null");
        }
        visitor.visit(this);
    }

    /**
     * Returns the visual representation of a wall for the CLI rendering.
     *
     * @return The '#' character representing a wall.
     */
    @Override
    public String toString() {
        return "#";
    }
}
