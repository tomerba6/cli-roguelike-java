package dnd.units.enemy;

import dnd.board.Position;
import dnd.units.DummyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrapTest {

    private Trap trap;
    private DummyPlayer player;

    @BeforeEach
    public void setUp() {
        // Spawning a "Death Trap": 1 Health, 50 Attack, 0 Defense. 250 XP.
        // Visibility Time: 3 turns. Invisibility Time: 4 turns. Symbol: 'D'.
        trap = new Trap("Death Trap", 1, 50, 0, 250, 3, 4, 'D');
        trap.setPosition(new Position(5, 5));

        // Player with 0 defense
        player = new DummyPlayer("Target", 1000, 10, 0);
        player.setPosition(new Position(5, 5)); // Player sits ON the trap to start
    }

    // --- VISIBILITY TIMER TESTS ---

    /** First takeTurn call: trap is visible and toString returns its display symbol. */
    @Test
    public void testInitialization() {
        // Trap logic is updated in takeTurn, so we call it once to establish the initial state
        trap.takeTurn(player);

        assertTrue(trap.isVisible(), "Traps should start visible");
        assertEquals("D", trap.toString(), "toString should return the display symbol when visible");
    }

    /** Full 7-turn cycle: 3 visible turns, 4 invisible turns, then visible again on turn 8. */
    @Test
    public void testVisibilityTimerCycle() {
        // Total cycle = 3 (visible) + 4 (invisible) = 7 turns.
        // Logic:
        // Turns 1, 2, 3: Visible (ticksCount 0, 1, 2)
        // Turn 4: Invisible (ticksCount 3)
        // Turns 5, 6, 7: Invisible (ticksCount 4, 5, 6)

        // Turn 1
        trap.takeTurn(player);
        assertTrue(trap.isVisible(), "Turn 1: Trap should be visible");
        assertEquals(1, trap.getTicksCount(), "Tick count should be 1");

        // Turn 2
        trap.takeTurn(player);
        assertTrue(trap.isVisible(), "Turn 2: Trap should be visible");
        assertEquals(2, trap.getTicksCount(), "Tick count should be 2");

        // Turn 3
        trap.takeTurn(player);
        assertTrue(trap.isVisible(), "Turn 3: Trap should be visible");
        assertEquals(3, trap.getTicksCount(), "Tick count should be 3");

        // Turn 4
        trap.takeTurn(player);
        assertFalse(trap.isVisible(), "Turn 4: Trap should now be invisible");
        assertEquals(".", trap.toString(), "toString should return floor tile '.' when invisible");

        // Turns 5, 6, 7
        trap.takeTurn(player); // Turn 5
        trap.takeTurn(player); // Turn 6
        trap.takeTurn(player); // Turn 7
        assertFalse(trap.isVisible(), "Turn 7: Trap should remain invisible");
        assertEquals(7, trap.getTicksCount(), "Tick count should be 7");

        // Turn 8 (Cycle restarts)
        trap.takeTurn(player);
        assertTrue(trap.isVisible(), "Turn 8: Trap should be visible again");
    }

    // --- AI AND MOVEMENT TESTS ---

    /** takeTurn always returns the trap's own position regardless of player location. */
    @Test
    public void testTrapIsStationary() {
        player.setPosition(new Position(10, 10)); // Move player away

        Position target = trap.takeTurn(player);

        assertEquals(new Position(5, 5), target, "Traps cannot move. takeTurn must return current position");
    }

    /** Player at distance < 2 receives damage after takeTurn. */
    @Test
    public void testTrapAttacksWhenInRange() {
        // Player is at (6, 5). Distance 1.0 (< 2).
        player.setPosition(new Position(6, 5));

        int healthBefore = player.getHealth().getHealthAmount();

        // RNG-Buster: the trap attacks with a random roll (0..50) against the player's 0 defense,
        // so a single swing CAN roll 0 and deal no damage. The trap re-attacks every takeTurn while
        // the player stays in range < 2, so keep ticking until a non-zero roll registers damage.
        for (int i = 0; i < 50 && player.getHealth().getHealthAmount() == healthBefore; i++) {
            trap.takeTurn(player);
        }

        assertTrue(player.getHealth().getHealthAmount() < healthBefore,
                "Player should have taken damage because they are within range < 2");
    }

    /** Player at distance >= 2 receives no damage after takeTurn. */
    @Test
    public void testTrapIgnoresPlayerOutOfRange() {
        // Player at (5, 7). Distance 2.0 (>= 2).
        player.setPosition(new Position(5, 7));

        int healthBefore = player.getHealth().getHealthAmount();
        trap.takeTurn(player);

        assertEquals(healthBefore, player.getHealth().getHealthAmount(),
                "Player should take zero damage when range is >= 2");
    }
}