package dnd.board;

import dnd.units.Occupant;

/**
 * Represents the game board, managing a 2D grid of cells.
 * This class encapsulates the internal array structure to prevent direct
 * external modification, providing controlled access to cells and their occupants.
 */
public class GameBoard {

    /**
     * The internal 2D array representing the board's structural layout.
     */
    private final Cell[][] board;

    /**
     * Constructs a new GameBoard with the specified 2D cell array.
     * @param board a pre-initialized 2D array of Cells representing the level layout
     * @throws IllegalArgumentException if board is null or empty.
     */
    public GameBoard(Cell[][] board) {
        if (board == null || board.length == 0 || board[0].length == 0) {
            throw new IllegalArgumentException("Fatal Error: Game board cannot be null or empty.");
        }
        this.board = board;
    }

    /**
     * Retrieves the terrain cell at the specified position.
     * @param p the position of the desired cell
     * @return the {@code Cell} at the given position, or {@code null} if the position is out of bounds
     */
    public Cell getCell(Position p) {
        if (isOutOfBounds(p)) {
            return null;
        }
        return board[p.getY()][p.getX()];
    }

    /**
     * Retrieves the occupant currently standing on the cell at the specified position.
     * @param p the position to check for an occupant
     * @return the {@code Occupant} at the given position, or {@code null} if the cell is unoccupied,
     * is a Wall, or is out of bounds
     */
    public Occupant getOccupant(Position p) {
        Cell cell = getCell(p);

        if (cell instanceof Floor) {
            return ((Floor) cell).getOccupant();
        }
        return null;
    }

    /**
     * Places an occupant on the cell at the specified position.
     * If the given position is not a Floor or is out of bounds, no action is taken.
     * Passing {@code null} clears the occupant from the cell.
     * * @param p the position where the occupant should be placed
     * @param o the {@code Occupant} to place, or {@code null} to clear the cell
     */
    public void setOccupant(Position p, Occupant o) {
        Cell cell = getCell(p);

        if (cell instanceof Floor) {
            ((Floor) cell).setOccupant(o);
        }
    }

    /**
     * Checks if the given position falls outside the dimensions of the board array.
     * * @param p the position to check
     * @return {@code true} if the position is out of bounds or null, {@code false} otherwise
     */
    private boolean isOutOfBounds(Position p) {
        if (p == null) return true;
        return p.getY() < 0 || p.getY() >= board.length ||
                p.getX() < 0 || p.getX() >= board[0].length;
    }

    /**
     * Returns a string representation of the entire game board.
     * Iterates through the internal 2D array and concatenates the string representation
     * of each cell, creating a multi-line visual layout of the map.
     * * @return a multi-line {@code String} visually representing the game board
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Cell[] row : board) {
            for (Cell cell : row) {
                sb.append(cell.toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}