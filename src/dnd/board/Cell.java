package dnd.board;

import dnd.combat.CellVisitor;

/**
 * Represents the structural nature of a location on the game board (e.g., Wall, Floor).
 * <p>
 * This interface acts as the "Receiver" in the Level 1 Visitor Pattern. It forces all
 * concrete terrain types to implement the {@code accept} method, allowing a moving unit
 * (the visitor) to determine if movement is possible without using {@code instanceof}.
 */
public interface Cell {

    /**
     * Accepts a visitor (usually a moving Unit) and triggers the appropriate
     * terrain-specific interaction (e.g., movement blocked vs. movement allowed).
     *
     * @param visitor The entity attempting to interact with this cell.
     */
    void accept(CellVisitor visitor);

    /**
     * Returns the visual representation of this cell for the CLI rendering.
     * Overridden by concrete classes to return '.' for Floor or '#' for Wall.
     *
     * @return The character representing the cell's terrain or its occupant.
     */
    String toString();
}
