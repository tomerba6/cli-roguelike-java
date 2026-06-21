package dnd.units.enemy;

import dnd.board.Position;
import dnd.units.player.Player;

/**
 * Represents a Trap enemy.
 * <p>
 * Traps are stationary hazards that toggle between visible and invisible states
 * based on a strict tick timer. If a player stands too close (range < 2), the trap will attack.
 */
public class Trap extends Enemy {

    private int visibilityTime;
    private int invisibilityTime;
    private int ticksCount;
    private boolean visible;
    private char displaySymbol;

    /**
     * Constructs a new Trap.
     *
     * @param name             The name of the trap.
     * @param healthPool       The max health pool.
     * @param attackPower      The attack power.
     * @param defensePower     The defense power.
     * @param experienceValue  The XP granted upon death.
     * @param visibilityTime   The number of ticks the trap remains visible.
     * @param invisibilityTime The number of ticks the trap remains invisible.
     * @param displaySymbol    The character used to render the trap on the board.
     */
    public Trap(String name, int healthPool, int attackPower, int defensePower, int experienceValue,
                int visibilityTime, int invisibilityTime, char displaySymbol) {
        super(name, healthPool, attackPower, defensePower, experienceValue);
        this.visibilityTime = visibilityTime;
        this.invisibilityTime = invisibilityTime;
        this.ticksCount = 0;
        this.visible = true;
        this.displaySymbol = displaySymbol;
    }

    // --- ACTIVE AI (ATTACK LOGIC) ---

    /**
     * Executes the trap's active turn logic.
     * Traps cannot move, but they will attack the player if within range.
     *
     * @param player The active player character.
     * @return The target Position (always its current position, as traps are stationary).
     */
    @Override
    public Position takeTurn(Player player) {
        // 1. Reset cycle BEFORE calculating visibility if we hit the limit
        if (this.ticksCount == (this.visibilityTime + this.invisibilityTime)) {
            this.ticksCount = 0;
        }

        // 2. Calculate visibility
        this.visible = this.ticksCount < this.visibilityTime;

        // 3. Increment for the next turn
        this.ticksCount++;

        // 4. Attack if player is in range < 2
        if (this.getPosition().range(player.getPosition()) < 2) {
            this.visit(player);
        }

        return this.getPosition();
    }

    // --- RENDERING OBLIGATIONS ---

    /**
     * Renders the trap's character for the CLI game board.
     * As per assignment requirements, this returns a different character depending on visibility.
     *
     * @return The trap's symbol if visible, or a floor tile ('.') if invisible.
     */
    @Override
    public String toString() {
        return this.visible ? String.valueOf(this.displaySymbol) : ".";
    }

    // Getters for potential unit testing
    public boolean isVisible() { return visible; }
    public int getTicksCount() { return ticksCount; }
}