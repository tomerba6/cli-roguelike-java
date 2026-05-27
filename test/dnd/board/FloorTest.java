package dnd.board;

import dnd.combat.DummyCellVisitor;
import dnd.units.DummyOccupant;
import dnd.units.Occupant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class FloorTest {
    private Floor floor;
    private Position position;

    @BeforeEach
    public void setUp() {
        // Create a standard floor at coordinates (2, 5) before every test
        position = new Position(2, 5);
        floor = new Floor(position);
    }


    @Test
    public void testInitialization() {
        assertEquals(position, floor.getPosition(), "Floor should retain the exact Position it was given");
        assertNull(floor.getOccupant(), "Floor should spawn completely empty");
    }

    @Test
    public void testConstructorThrowsExceptionOnNullPosition() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Floor(null);
        }, "Creating a floor with a null position should throw an exception");
    }

    @Test
    public void testSetAndClearOccupant() {
        Occupant dummy = new DummyOccupant("Z");

        floor.setOccupant(dummy);
        assertEquals(dummy, floor.getOccupant(), "Floor should hold the assigned occupant");

        floor.setOccupant(null);
        assertNull(floor.getOccupant(), "Floor should be empty after setting occupant to null");
    }


    @Test
    public void testToStringUnoccupied() {
        assertEquals(".", floor.toString(), "Empty floor should render as a dot '.'");
    }

    @Test
    public void testToStringOccupied() {
        Occupant dummy = new DummyOccupant("Z");
        floor.setOccupant(dummy);

        assertEquals("Z", floor.toString(), "Occupied floor should render the occupant's string");
    }


    @Test
    public void testAcceptRoutesToVisitFloor() {
        DummyCellVisitor visitor = new DummyCellVisitor();

        floor.accept(visitor);

        assertTrue(visitor.visitedFloor, "Floor.accept() should successfully route to visitor.visit(Floor)");
        assertFalse(visitor.visitedWall, "Floor.accept() should NEVER route to visitor.visit(Wall)");
    }

    @Test
    public void testOverwriteOccupant() {
        Occupant dummy1 = new DummyOccupant("A");
        Occupant dummy2 = new DummyOccupant("B");

        floor.setOccupant(dummy1);
        floor.setOccupant(dummy2);

        assertEquals(dummy2, floor.getOccupant(), "Floor should allow direct overwriting of occupants");
        assertEquals("B", floor.toString(), "Floor should render the newly overwritten occupant");
    }

    @Test
    public void testAcceptNullVisitor() {
        // If your Floor class doesn't explicitly check for null, this will throw a NullPointerException.
        // In a perfect architecture, you would update Floor.accept() to throw an IllegalArgumentException instead.
        assertThrows(Exception.class, () -> {
            floor.accept(null);
        }, "Accepting a null visitor should throw an exception");
    }
}
