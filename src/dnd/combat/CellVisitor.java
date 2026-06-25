package dnd.combat;

import dnd.board.Floor;
import dnd.board.Wall;

/**
 * The Level 1 Visitor interface for terrain interaction.
 * <p>
 * Any unit that moves across the GameBoard must implement this interface.
 * It uses Double Dispatch to allow the target Cell (Wall or Floor) to naturally
 * resolve whether movement is blocked or permitted without using instanceof.
 */
public interface CellVisitor {
    /**
     * Defines the interaction when the visitor attempts to step on a Wall.
     * Generally, this will result in movement being blocked.
     * @param wall The wall being visited.
     */
    public void visit(Wall wall);

    /**
     * Defines the interaction when the visitor attempts to step on a Floor.
     * Generally, this will result in checking the floor for an Occupant.
     * @param floor The floor being visited.
     */
    public void visit(Floor floor);
}
