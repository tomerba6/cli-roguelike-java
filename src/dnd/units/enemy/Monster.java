package dnd.units.enemy;

import dnd.board.Floor;
import dnd.board.Wall;
import dnd.combat.CellVisitor;

public class Monster extends Enemy implements CellVisitor {
    /**
     * Constructs a new Enemy.
     * Matches the Unit super-constructor and adds the experience reward.
     *
     * @param name
     * @param healthPool
     * @param attackPower
     * @param defensePower
     * @param experienceValue
     */
    public Monster(String name, int healthPool, int attackPower, int defensePower, int experienceValue) {
        super(name, healthPool, attackPower, defensePower, experienceValue);
    }

    @Override
    public void visit(Wall wall) {

    }

    @Override
    public void visit(Floor floor) {

    }
}
