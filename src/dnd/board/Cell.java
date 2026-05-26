package dnd.board;

public interface Cell {
    void accept(CellVisitor visitor);
    String toString();
}
