package dnd.board;

import dnd.combat.DummyCellVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WallTest {

    private Wall wall;

    @BeforeEach
    public void setUp() {
        wall = new Wall();
    }


    @Test
    public void testToString() {
        assertEquals("#", wall.toString(), "Wall should render as a hash '#'");
    }


    @Test
    public void testAcceptRoutesToVisitWall() {
        DummyCellVisitor visitor = new DummyCellVisitor();

        wall.accept(visitor);

        assertTrue(visitor.visitedWall, "Wall.accept() should successfully route to visitor.visit(Wall)");
        assertFalse(visitor.visitedFloor, "Wall.accept() should NEVER route to visitor.visit(Floor)");
    }


    @Test
    public void testAcceptNullVisitor() {
        assertThrows(Exception.class, () -> {
            wall.accept(null);
        }, "Accepting a null visitor should throw an exception");
    }
}