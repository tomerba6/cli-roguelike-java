package dnd.units.enemy;

import dnd.board.Position;
import dnd.units.player.Player;

import java.util.Random;

/**
 * Represents a Monster enemy.
 * <p>
 * Monsters actively chase the player if the player is within their vision range.
 * If the player is out of range, they roam randomly around the board.
 */
public class Monster extends Enemy {

    protected int visionRange;
    private char displaySymbol; // Stores the unique ASCII tile for this monster type

    /**
     * Constructs a new Monster.
     *
     * @param name            The name of the monster.
     * @param healthPool      The max health pool.
     * @param attackPower     The attack power.
     * @param defensePower    The defense power.
     * @param experienceValue The XP granted upon death.
     * @param visionRange     The range at which the monster spots the player.
     * @param displaySymbol   The ASCII character representing this monster on the board.
     */
    public Monster(String name, int healthPool, int attackPower, int defensePower, int experienceValue, int visionRange, char displaySymbol) {
        super(name, healthPool, attackPower, defensePower, experienceValue);
        this.visionRange = visionRange;
        this.displaySymbol = displaySymbol;
    }

    /**
     * Appends the Monster's class-specific properties (Vision Range)
     * to the standard enemy description.
     */
    @Override
    public String description() {
        return super.description() + String.format("\t\tVision Range: %d", this.visionRange);
    }

    /**
     * Renders the monster's character for the CLI game board.
     *
     * @return The unique character string representing this monster type.
     */
    @Override
    public String toString() {
        return String.valueOf(this.displaySymbol);
    }

    /**
     * Executes the monster's pathfinding AI.
     *
     * @param player The active player character.
     * @return The target Position the monster wants to step into.
     */
    @Override
    public Position takeTurn(Player player) {
        Position currentPos = this.getPosition();
        Position targetPos = currentPos; // Default action is staying in place

        double distance = currentPos.range(player.getPosition());

        // 1. Chasing the Player
        if (distance < this.visionRange) {

            // Calculate dx and dy exactly as the assignment specifies
            int dx = currentPos.getX() - player.getPosition().getX();
            int dy = currentPos.getY() - player.getPosition().getY();

            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) {
                    targetPos = new Position(currentPos.getX() - 1, currentPos.getY()); // Move left
                } else {
                    targetPos = new Position(currentPos.getX() + 1, currentPos.getY()); // Move right
                }
            } else {
                if (dy > 0) {
                    targetPos = new Position(currentPos.getX(), currentPos.getY() - 1); // Move up
                } else {
                    targetPos = new Position(currentPos.getX(), currentPos.getY() + 1); // Move down
                }
            }
        }
        // 2. Random Roaming
        else {
            // Perform a random movement action: left, right, up, down, or stay
            // Generate a random integer between 0 and 4 inclusive using Math.random()
            int moveChoice = (int) (Math.random() * 5);

            switch (moveChoice) {
                case 0: targetPos = new Position(currentPos.getX() - 1, currentPos.getY()); break; // Left
                case 1: targetPos = new Position(currentPos.getX() + 1, currentPos.getY()); break; // Right
                case 2: targetPos = new Position(currentPos.getX(), currentPos.getY() - 1); break; // Up
                case 3: targetPos = new Position(currentPos.getX(), currentPos.getY() + 1); break; // Down
                case 4: targetPos = currentPos; break; // Stay
            }
        }

        return targetPos;
    }
}