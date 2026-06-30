package dnd.combat;

import dnd.board.Floor;
import dnd.board.Wall;
/**
 * A shared dummy implementation of the CellVisitor interface used strictly for testing.
 * <p>
 * This class tracks which visit method was invoked, allowing test files to mathematically
 * prove that Double Dispatch routing (like Floor.accept()) is working correctly
 * without needing a real Player object.
 */
public class DummyCellVisitor implements CellVisitor {
    /** Flag indicating if the visit(Floor) method was successfully routed to. */
    public boolean visitedFloor = false;

    /** Flag indicating if the visit(Wall) method was successfully routed to. */
    public boolean visitedWall = false;

    /**
     * Fakes the movement onto a Floor cell by flipping the tracking flag.
     *
     * @param f The floor cell being visited.
     */
    @Override
    public void visit(Floor f) {
        this.visitedFloor = true;
    }

    /**
     * Fakes the movement into a Wall cell by flipping the tracking flag.
     *
     * @param w The wall cell being visited.
     */
    @Override
    public void visit(Wall w) {
        this.visitedWall = true;
    }

    /**
     * Utility method to reset the flags between tests if the same
     * dummy visitor is reused.
     */
    public void reset() {
        this.visitedFloor = false;
        this.visitedWall = false;
    }
}
