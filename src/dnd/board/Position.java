package dnd.board;

import java.util.Objects;

/**
 * Represents an immutable two-dimensional coordinate on the game board.
 * This class acts as a Value Object to track the location of entities
 * and calculate distances between them.
 */
public class Position {

    /**
     * The x-coordinate (horizontal position) on the board.
     */
    private final int x;

    /**
     * The y-coordinate (vertical position) on the board.
     */
    private final int y;

    /**
     * Constructs a new Position with the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retrieves the x-coordinate of this position.
     *
     * @return the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Retrieves the y-coordinate of this position.
     *
     * @return the y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Calculates the Euclidean distance between this position and another given position.
     * @param other the target position to calculate the distance to
     * @return the exact Euclidean distance as a double
     * @throws IllegalArgumentException if other is null.
     */
    public double range(Position other) {
        if (other == null) {
            throw new IllegalArgumentException("Fatal Math Error: Cannot calculate distance to a null position.");
        }
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    /**
     * Compares this position to the specified object. The result is {@code true} if and only if
     * the argument is not {@code null} and is a {@code Position} object that represents
     * the same {@code x} and {@code y} coordinates as this object.
     *
     * @param o the object to compare this {@code Position} against
     * @return {@code true} if the given object represents a {@code Position}
     * equivalent to this position, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    /**
     * Returns a hash code for this {@code Position}.
     *
     * @return a hash code value for this object, based on its x and y coordinates
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}