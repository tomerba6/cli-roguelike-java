package dnd.board;

public class Wall implements Cell {
    @Override
    public void accept(CellVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "#";
    }
}
