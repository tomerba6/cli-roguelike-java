package dnd.units;

import dnd.board.Floor;
import dnd.board.Position;
import dnd.board.Wall;
import dnd.combat.CellVisitor;
import dnd.combat.OccupantVisitor;
import dnd.units.enemy.Enemy;
import dnd.units.player.Player;
import dnd.utils.MessageCallback;

/**
 * The foundational abstract class for all living entities in the game.
 * <p>
 * This class establishes the core combat statistics (health, attack, defense),
 * physical position on the board, and enforces the Level 2 Visitor contracts
 * for resolving combat. It explicitly does NOT implement movement (CellVisitor)
 * to adhere to the Interface Segregation Principle, allowing non-moving units
 * (like Traps) to inherit safely.
 */
public abstract class Unit implements CellVisitor, OccupantVisitor, Occupant {
    /** Reference to the object responsible for I/O operations */
    protected MessageCallback messageCallback;

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
     * Retrieves the attack power of the unit.
     *
     * @return The Unit's attack power.
     */
    public int getAttackPower() {
        return attackPower;
    }

    /**
     * Retrieves the defense power of the unit.
     *
     * @return The Unit's defense power.
     */
    public int getDefensePower() {
        return defensePower;
    }

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
     * Returns the full status of the unit.
     * Used during combat and on the player's turn to display stats.
     *
     * @return A formatted string containing name, health, attack, and defense.
     */
    public String description() {
        return String.format("%s\t\tHealth: %s\t\tAttack: %d\t\tDefense: %d",
                this.getName(),
                this.getHealth().toString(),
                this.attackPower,
                this.defensePower);
    }

    /**
     * Executes the universal, silent mathematical resolution of a melee attack.
     * Calculates attack and defense rolls, and applies damage if the attack exceeds defense.
     *
     * @param defender The unit receiving the attack.
     */
    protected void engageInCombat(Unit defender) {
        logMessage(this.getName() + " engaged in combat with " + defender.getName() + ".");
        logMessage(this.description());
        logMessage(defender.description());

        int attackRoll = this.rollAttack();
        int defenseRoll = defender.rollDefense();

        logMessage(this.getName() + " rolled " + attackRoll + " attack points.");
        logMessage(defender.getName() + " rolled " + defenseRoll + " defense points.");

        int damage = attackRoll - defenseRoll;
        if (damage < 0) damage = 0;

        logMessage(this.getName() + " dealt " + damage + " damage to " + defender.getName() + ".");

        if (damage > 0) {
            defender.getHealth().takeDamage(damage);
        }
    }

    @Override
    public void visit(Wall w) {
        // Do nothing. The unit attempted to walk into a wall and is blocked.
    }

    @Override
    public void visit(Floor f) {
        // The unit successfully stepped onto a floor.
        // Check if someone is already standing there.
        Occupant occupant = f.getOccupant();

        if (occupant != null) {
            //  The floor is occupied. Trigger Level 2 Visitor (Combat)
            occupant.accept(this);
        } else {
            this.position = f.getPosition();
        }
    }

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

    /**
     * Sets the message callback reference of the Unit.
     *
     * @param messageCallback the reference to the MessageCallback object.
     */
    public void setMessageCallback(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    /**
     * Logs the string we want to display directly to the callback.
     *
     * @param message the message we want to display.
     */
    protected void logMessage(String message) {
        if (this.messageCallback != null) {
            this.messageCallback.send(message);
        }
    }
}
