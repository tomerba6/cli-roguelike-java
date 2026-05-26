package dnd.units;

import dnd.board.Position;
import dnd.combat.OccupantVisitor;
import dnd.units.enemy.Enemy;
import dnd.units.player.Player;

/**
 * The foundational abstract class for all living entities in the game.
 * <p>
 * This class establishes the core combat statistics (health, attack, defense),
 * physical position on the board, and enforces the Level 2 Visitor contracts
 * for resolving combat. It explicitly does NOT implement movement (CellVisitor)
 * to adhere to the Interface Segregation Principle, allowing non-moving units
 * (like Traps) to inherit safely.
 */
public abstract class Unit implements OccupantVisitor, Occupant {
    /** The display name of the unit. */
    protected String name;


    protected Health health;

    /** The maximum potential damage this unit can deal in a single attack. */
    protected int attackPower;

    /** The maximum potential damage this unit can block in a single defense roll. */
    protected int defensePower;

    /** The unit's current (x, y) coordinates on the game board. */
    protected Position position;

    /**
     * Constructs a new Unit with base statistics.
     * Units always spawn with their current health fully topped up to their health pool.
     *
     * @param name         The display name of the unit.
     * @param healthPool   The maximum health capacity.
     * @param attackPower  The maximum attack roll value.
     * @param defensePower The maximum defense roll value.
     */
    public Unit(String name, int healthPool, int attackPower, int defensePower) {
        this.name = name;
        this.health = new Health(healthPool);
        this.attackPower = attackPower;
        this.defensePower = defensePower;
    }

    /**
     * Retrieves the current position of the unit on the board.
     *
     * @return The unit's Position object.
     */
    public Position getPosition() { return position; }

    /**
     * Updates the unit's position. This is typically called after a successful
     * movement sequence resolves.
     *
     * @param position The new Position coordinates.
     */
    public void setPosition(Position position) { this.position = position; }

    /**
     * Retrieves the name of the unit.
     *
     * @return The unit's name as a String.
     */
    public String getName() { return name; }

    /**
     * Retrieves the health of the unit.
     *
     * @return The unit's Health object.
     */
    public Health getHealth() { return health; }

    /**
     * Rolls a randomized attack value during combat.
     * The roll is an inclusive random integer between 0 and the unit's attackPower.
     *
     * @return The generated attack damage value.
     */
    public int rollAttack() { return (int) (Math.random() * (this.attackPower + 1)); }

    /**
     * Rolls a randomized defense value during combat.
     * The roll is an inclusive random integer between 0 and the unit's defensePower.
     *
     * @return The generated damage reduction value.
     */
    public int rollDefense() { return (int) (Math.random() * (this.defensePower + 1)); }


    /**
     * Defines the combat interaction when this unit initiates an interaction
     * with a Player.
     *
     * @param p The Player being targeted.
     */
    @Override
    public abstract void visit(Player p);

    /**
     * Defines the combat interaction when this unit initiates an interaction
     * with an Enemy.
     *
     * @param e The Enemy being targeted.
     */
    @Override
    public abstract void visit(Enemy e);
}
