package dnd.units.enemy;
import dnd.board.Position;
import dnd.combat.OccupantVisitor;
import dnd.units.Unit;
import dnd.units.player.Player;

/**
 * Represents an abstract hostile unit on the game board.
 * Implements the combat resolutions when an Enemy initiates movement into another entity.
 */
public abstract class Enemy extends Unit {

    private final int experienceValue;

    /**
     * Constructs a new Enemy.
     * Matches the Unit super-constructor and adds the experience reward.
     */
    public Enemy(String name, int healthPool, int attackPower, int defensePower, int experienceValue) {
        super(name, healthPool, attackPower, defensePower);
        this.experienceValue = experienceValue;
    }

    public int getExperienceValue() {
        return experienceValue;
    }

    /**
     * Called when another unit attempts to step onto this Enemy's tile.
     */
    @Override
    public void accept(OccupantVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Defines what happens when this Enemy initiates a move into a Player's cell.
     * Triggers standard melee combat.
     */
    @Override
    public void visit(Player p) {
        super.engageInCombat(p);

        if (p.getHealth().isDead()) {
            logMessage(p.getName() + " was killed by " + this.getName() + ".");
            logMessage("You lost.");
        }
    }

    /**
     * Prevents enemies from fighting each other.
     * If an enemy tries to walk into another enemy, nothing happens.
     */
    @Override
    public void visit(Enemy e) {
        // Do nothing. Enemies do not engage in friendly fire.
    }

    /**
     * Appends the Enemy's base properties (Experience Value)
     * to the standard unit description.
     */
    @Override
    public String description() {
        return super.description() + String.format("\t\tExperience Value: %d", this.experienceValue);
    }

    /**
     * Executes the enemy's active turn logic (AI).
     * <p>
     * Monsters will use this to calculate pathfinding and move.
     * Traps will use this to attack if the player is close enough.
     *
     * @param player The active player character on the board.
     */
    public abstract Position takeTurn(Player player);
}