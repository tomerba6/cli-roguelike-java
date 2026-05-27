package dnd.board;

import dnd.combat.CellVisitor;
import dnd.units.Occupant;

/**
 * Represents a walkable tile on the game board.
 * A floor cell can hold up to one Occupant (a Player or an Enemy) at a time.
 */
public class Floor implements Cell {
    private final Position position;
    private Occupant occupant;

    /**
     * Constructs a new Floor cell at a specific position.
     * Floors start unoccupied by default.
     *
     * @param position The (x, y) coordinates of this floor cell.
     * @throws IllegalArgumentException if position is null.
     */
    public Floor(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Fatal Error: Floor position cannot be null.");
        }
        this.position = position;
        this.occupant = null;
    }
    public Position getPosition() {
        return position;
    }

    public Occupant getOccupant() {
        return occupant;
    }

    /**
     * Updates the occupant of this cell.
     * Passing null clears the cell (e.g., when a unit moves away or dies).
     *
     * @param occupant The new occupant, or null to empty the cell.
     */
    public void setOccupant(Occupant occupant) {
        this.occupant = occupant;
    }

    /**
     * Accepts a visitor and explicitly routes the execution to the visit(Floor) method.
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
     * Returns the visual representation of this cell for the CLI rendering.
     * If an occupant is present, it returns the occupant's character.
     * Otherwise, it returns the standard '.' floor character.
     */
    @Override
    public String toString() {
        return occupant != null ? occupant.toString() : ".";
    }
}
