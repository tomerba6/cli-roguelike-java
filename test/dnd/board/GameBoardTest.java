package dnd.board;

import dnd.units.DummyOccupant;
import dnd.units.Occupant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameBoardTest {

    private GameBoard gameBoard;
    private Position p00, p01, p10, p11;
    private Floor f00, f11;
    private Wall w01, w10;

    @BeforeEach
    public void setUp() {
        // We will build a 2x2 board:
        // [Floor] [Wall]
        // [Wall]  [Floor]

        p00 = new Position(0, 0);
        p01 = new Position(1, 0);
        p10 = new Position(0, 1);
        p11 = new Position(1, 1);

        f00 = new Floor(p00);
        w01 = new Wall();
        w10 = new Wall();
        f11 = new Floor(p11);

        Cell[][] grid = {
                {f00, w01},
                {w10, f11}
        };

        gameBoard = new GameBoard(grid);
    }


    /** Null or empty board array throws IllegalArgumentException at construction. */
    @Test
    public void testConstructorThrowsExceptionOnInvalidBoard() {
        assertThrows(IllegalArgumentException.class, () -> new GameBoard(null),
                "Should crash on null board");

        assertThrows(IllegalArgumentException.class, () -> new GameBoard(new Cell[0][0]),
                "Should crash on completely empty board");
    }


    /** getCell(pos) returns the correct Cell for a coordinate within the board bounds. */
    @Test
    public void testGetCellWithinBounds() {
        assertEquals(f00, gameBoard.getCell(p00), "Should retrieve the exact Floor object at 0,0");
        assertEquals(w01, gameBoard.getCell(p01), "Should retrieve the exact Wall object at 1,0");
    }

    /** getCell(pos) returns null for any coordinate outside the board bounds. */
    @Test
    public void testGetCellOutOfBoundsReturnsNull() {
        // Negative coordinates
        assertNull(gameBoard.getCell(new Position(-1, 0)), "Negative X should return null");
        assertNull(gameBoard.getCell(new Position(0, -5)), "Negative Y should return null");

        // Exceeding grid size
        assertNull(gameBoard.getCell(new Position(2, 0)), "X beyond width should return null");
        assertNull(gameBoard.getCell(new Position(0, 2)), "Y beyond height should return null");
    }

    /** getCell(null) returns null rather than throwing. */
    @Test
    public void testGetCellWithNullPosition() {
        assertNull(gameBoard.getCell(null), "A null position should safely return null, not crash");
    }


    /** setOccupant and getOccupant round-trip correctly on a Floor cell. */
    @Test
    public void testSetAndGetOccupantOnFloor() {
        Occupant dummy = new DummyOccupant("@");

        assertNull(gameBoard.getOccupant(p00), "Floor should start empty");

        gameBoard.setOccupant(p00, dummy);
        assertEquals(dummy, gameBoard.getOccupant(p00), "Floor should hold the assigned occupant");

        gameBoard.setOccupant(p00, null);
        assertNull(gameBoard.getOccupant(p00), "Floor should be empty after clearing");
    }

    /** getOccupant on a Wall cell returns null (walls can't hold occupants). */
    @Test
    public void testGetOccupantOnWallReturnsNull() {
        // p01 is mapped to a Wall in our setUp() grid
        assertNull(gameBoard.getOccupant(p01), "Retrieving an occupant from a Wall should safely return null");
    }

    /** setOccupant on a Wall cell throws an exception. */
    @Test
    public void testSetOccupantOnWallThrowsException() {
        Occupant dummy = new DummyOccupant("@");

        // Ensure the engine strictly prevents illegal entity merging
        assertThrows(UnsupportedOperationException.class, () -> {
            gameBoard.setOccupant(p01, dummy);
        }, "Attempting to place an occupant in a wall must crash the engine");
    }

    /** setOccupant with an out-of-bounds position is silently ignored, no exception thrown. */
    @Test
    public void testSetOccupantOutOfBoundsIsIgnored() {
        Occupant dummy = new DummyOccupant("@");

        assertDoesNotThrow(() -> {
            gameBoard.setOccupant(new Position(50, 50), dummy);
        }, "Setting an occupant out of bounds should be safely ignored");
    }


    /** toString() renders the full grid row-by-row with newlines between rows. */
    @Test
    public void testToStringRendersCorrectly() {
        gameBoard.setOccupant(p11, new DummyOccupant("@"));

        // Expected output string:
        // .#\n
        // #@\n
        String expected = ".#\n#@\n";

        assertEquals(expected, gameBoard.toString(), "Board should render rows and columns accurately");
    }
}
