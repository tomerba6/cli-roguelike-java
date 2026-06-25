package dnd.board;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {


    /** Constructs Position(col, row) and verifies getX() and getY() return the constructor arguments. */
    @Test
    public void testInitializationAndGetters() {
        Position pos = new Position(5, -3);

        assertEquals(5, pos.getX(), "X coordinate should be 5");
        assertEquals(-3, pos.getY(), "Y coordinate should be -3");
    }


    /** range() to self is exactly 0.0. */
    @Test
    public void testRangeToSelf() {
        Position p1 = new Position(2, 2);
        assertEquals(0.0, p1.range(p1), "Distance to itself should be exactly 0.0");
    }

    /** range() on a vertical line equals the row difference; result is symmetric. */
    @Test
    public void testRangeStraightLine() {
        Position p1 = new Position(0, 0);
        Position p2 = new Position(0, 5);

        assertEquals(5.0, p1.range(p2), "Vertical distance should be exactly 5.0");
        assertEquals(5.0, p2.range(p1), "Distance calculation should be symmetric");
    }

    /** range() on a 3-4-5 triangle returns exactly 5.0 (Pythagorean check). */
    @Test
    public void testRangeDiagonal() {
        Position p1 = new Position(0, 0);
        Position p2 = new Position(3, 4);

        assertEquals(5.0, p1.range(p2), "Diagonal distance for a 3x4 triangle should be exactly 5.0");
    }


    /** equals() is true for same-coordinate positions, false for different coords, null, or other types. */
    @Test
    public void testEquals() {
        Position p1 = new Position(1, 2);
        Position p2 = new Position(1, 2);
        Position p3 = new Position(2, 1);

        assertEquals(p1, p1, "A position should be equal to itself");

        assertEquals(p1, p2, "Positions with identical coordinates should be equal");

        assertNotEquals(p1, p3, "Positions with different coordinates should not be equal");

        assertNotEquals(null, p1, "Position should not be equal to null");
        assertNotEquals("String", p1, "Position should not be equal to an object of a different type");
    }

    /** Equal positions must produce identical hash codes (equals/hashCode contract). */
    @Test
    public void testHashCode() {
        Position p1 = new Position(10, 20);
        Position p2 = new Position(10, 20);

        assertEquals(p1.hashCode(), p2.hashCode(), "Equal positions must generate identical hash codes");
    }


    /** range(null) throws an exception rather than returning silently. */
    @Test
    public void testRangeWithNullThrowsException() {
        Position p1 = new Position(0, 0);

        assertThrows(Exception.class, () -> {
            p1.range(null);
        }, "Calculating range to a null position should throw an exception");
    }
}
