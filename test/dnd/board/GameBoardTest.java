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


    @Test
    public void testConstructorThrowsExceptionOnInvalidBoard() {
        assertThrows(IllegalArgumentException.class, () -> new GameBoard(null),
                "Should crash on null board");

        assertThrows(IllegalArgumentException.class, () -> new GameBoard(new Cell[0][0]),
                "Should crash on completely empty board");
    }


    @Test
    public void testGetCellWithinBounds() {
        assertEquals(f00, gameBoard.getCell(p00), "Should retrieve the exact Floor object at 0,0");
        assertEquals(w01, gameBoard.getCell(p01), "Should retrieve the exact Wall object at 1,0");
    }

    @Test
    public void testGetCellOutOfBoundsReturnsNull() {
        // Negative coordinates
        assertNull(gameBoard.getCell(new Position(-1, 0)), "Negative X should return null");
        assertNull(gameBoard.getCell(new Position(0, -5)), "Negative Y should return null");

        // Exceeding grid size
        assertNull(gameBoard.getCell(new Position(2, 0)), "X beyond width should return null");
        assertNull(gameBoard.getCell(new Position(0, 2)), "Y beyond height should return null");
    }

    @Test
    public void testGetCellWithNullPosition() {
        assertNull(gameBoard.getCell(null), "A null position should safely return null, not crash");
    }


    @Test
    public void testSetAndGetOccupantOnFloor() {
        Occupant dummy = new DummyOccupant("@");

        assertNull(gameBoard.getOccupant(p00), "Floor should start empty");

        gameBoard.setOccupant(p00, dummy);
        assertEquals(dummy, gameBoard.getOccupant(p00), "Floor should hold the assigned occupant");

        gameBoard.setOccupant(p00, null);
        assertNull(gameBoard.getOccupant(p00), "Floor should be empty after clearing");
    }

    @Test
    public void testSetOccupantOnWallIsIgnored() {
        Occupant dummy = new DummyOccupant("@");

        gameBoard.setOccupant(p01, dummy);

        // The game board should silently ignore this (or return null),
        // ensuring monsters can't accidentally be merged into walls.
        assertNull(gameBoard.getOccupant(p01), "Walls should always return null for occupants");
    }

    @Test
    public void testSetOccupantOutOfBoundsIsIgnored() {
        Occupant dummy = new DummyOccupant("@");

        assertDoesNotThrow(() -> {
            gameBoard.setOccupant(new Position(50, 50), dummy);
        }, "Setting an occupant out of bounds should be safely ignored");
    }


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
