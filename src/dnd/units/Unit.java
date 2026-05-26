package dnd.units;

import dnd.combat.CellVisitor;
import dnd.combat.OccupantVisitor;

public abstract class Unit implements CellVisitor, OccupantVisitor, Occupant {
}
