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
     */
    @Override
    public void accept(CellVisitor visitor) {
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
