package dnd.units.enemy;
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
     * Defines what happens when this Enemy moves into a Player's cell.
     */
    @Override
    public void visit(Player p) {
        super.engageInCombat(p);
    }

    /**
     * Prevents enemies from fighting each other.
     */
    @Override
    public void visit(Enemy e) {
        // Do nothing. Enemies do not engage in friendly fire.
    }
}