package dnd.board;

import dnd.units.Occupant;

public class Floor implements Cell {
    private Occupant occupant;

    @Override
    public void accept(CellVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return occupant != null ? occupant.toString() : ".";
    }
}
