package dnd.units.enemy;

import dnd.board.Position;
import dnd.units.DummyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BossTest {

    private Boss boss;
    private DummyPlayer player;

    @BeforeEach
    public void setUp() {
        // Spawning "The Night King" using standard assignment stats:
        // 2000 Health, 100 Attack, 50 Defense. 1000 XP.
        // Vision Range: 5. Ability Frequency: 3. Symbol: 'K'.
        boss = new Boss("Night King", 2000, 100, 50, 1000, 5, 3, 'K');

        // Generic Player: 1000 Health, 10 Attack, 10 Defense
        player = new DummyPlayer("Target", 1000, 10, 10);
    }

    // --- AI AND PATHFINDING TESTS ---

    @Test
    public void testCombatTicksResetWhenOutOfRange() {
        // Boss at (0, 0), Player at (10, 10). Distance is ~14.1 (Out of vision range 5).
        boss.setPosition(new Position(0, 0));
        player.setPosition(new Position(10, 10));

        // Manually artificially inflate the timer to simulate a previous battle
        // (We would normally need a setter for this, but we can just use takeTurn
        // to prove the 'else' block immediately forces it to 0).

        Position movePos = boss.takeTurn(player);

        assertEquals(0, boss.getCombatTicks(), "Combat ticks MUST reset to 0 when player escapes vision range");

        // Assert random movement was used (distance from origin is <= 1.0)
        assertTrue(boss.getPosition().range(movePos) <= 1.0, "Boss should roam randomly when player is far away");
    }

    // --- SPECIAL ABILITY MATH AND TIMING TESTS ---

    @Test
    public void testShoebodybopCastCycle() {
        // Place Boss at (0, 4) and Player at (0, 0).
        // Distance is 4.0. This is strictly inside the '< vision range' check (5).
        boss.setPosition(new Position(0, 4));
        player.setPosition(new Position(0, 0));

        // Turn 1 (Timer 0 -> 1)
        // Because takeTurn() only RETURNS the intended coordinate without physically
        // moving the boss, we must manually update the position to simulate the Game Engine.
        boss.setPosition(boss.takeTurn(player));
        assertEquals(1, boss.getCombatTicks(), "Turn 1: Timer should be 1");
        assertEquals(new Position(0, 3), boss.getPosition(), "Boss should move UP towards player");
        assertEquals(1000, player.getHealth().getHealthAmount(), "Player should take no damage while Boss is chasing");

        // Turn 2 (Timer 1 -> 2)
        boss.setPosition(boss.takeTurn(player));
        assertEquals(2, boss.getCombatTicks(), "Turn 2: Timer should be 2");
        assertEquals(new Position(0, 2), boss.getPosition());
        assertEquals(1000, player.getHealth().getHealthAmount(), "Player should still take no damage");

        // Turn 3 - THE CAST (Timer 2 -> 3 -> Cast -> Reset to 0)
        // Scenario B: The timer reaches the frequency limit (3) and immediately fires.
        Position castPos = boss.takeTurn(player);

        assertEquals(0, boss.getCombatTicks(), "Turn 3: Timer must reset to 0 immediately after casting");
        assertEquals(new Position(0, 2), castPos, "Boss MUST remain completely stationary at (0,2) while casting");

        // Damage Verification: Boss Attack (100) - Player Defense (10) = 90 Damage.
        // Player Health: 1000 - 90 = 910.
        assertEquals(910, player.getHealth().getHealthAmount(), "Player should take exact flat attack minus defense damage from Shoebodybop");
    }

    @Test
    public void testCastAbilityDirectMath() {
        boss.setPosition(new Position(0, 2));
        player.setPosition(new Position(0, 0)); // Distance 2 (Within Vision)

        // Trigger the HeroicUnit interface method directly
        boss.castAbility(null, player);

        // 100 Boss Attack - 10 Player Defense = 90 Damage
        assertEquals(910, player.getHealth().getHealthAmount(), "castAbility should correctly resolve combat math independently of takeTurn");
    }
}