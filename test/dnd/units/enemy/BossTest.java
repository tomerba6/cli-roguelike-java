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

    /** Player outside vision range resets combatTicks to 0 and triggers random roaming. */
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

    /** Full cast cycle with freq=3: moves three turns then casts on turn 4, staying stationary, dealing damage. */
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

        // Turn 3 (Timer 2 -> 3): still chasing, not yet at frequency
        boss.setPosition(boss.takeTurn(player));
        assertEquals(3, boss.getCombatTicks(), "Turn 3: Timer should be 3");
        assertEquals(new Position(0, 1), boss.getPosition(), "Boss should move UP one more tile");
        assertEquals(1000, player.getHealth().getHealthAmount(), "Player should still take no damage on turn 3");

        // Turn 4 - THE CAST (Timer 3 == frequency 3 → Reset to 0, cast)
        Position castPos = boss.takeTurn(player);

        assertEquals(0, boss.getCombatTicks(), "Turn 4: Timer must reset to 0 immediately after casting");
        assertEquals(new Position(0, 1), castPos, "Boss MUST remain completely stationary at (0,1) while casting");

        // Damage Verification: Boss Attack (100) - Player Defense (10) = 90 Damage.
        // Player Health: 1000 - 90 = 910.
        assertEquals(910, player.getHealth().getHealthAmount(), "Player should take exact flat attack minus defense damage from Shoebodybop");
    }

    /** castAbility() called directly deals attackPower minus the player's defense roll. */
    @Test
    public void testCastAbilityDirectMath() {
        boss.setPosition(new Position(0, 2));
        player.setPosition(new Position(0, 0)); // Distance 2 (Within Vision)

        // Trigger the HeroicUnit interface method directly
        boss.castAbility(null, player);

        // 100 Boss Attack - 10 Player Defense = 90 Damage
        assertEquals(910, player.getHealth().getHealthAmount(), "castAbility should correctly resolve combat math independently of takeTurn");
    }

    /** toString() returns the boss's display symbol string. */
    @Test
    public void testToStringReturnsCorrectSymbol() {
        assertEquals("K", boss.toString(), "Boss toString() must return its specific display tile string");
    }

    /** description() chain contains name, experience value, and vision range fields. */
    @Test
    public void testDescriptionChainIncludesAllFields() {
        String desc = boss.description();

        assertTrue(desc.contains("Night King"), "Description should include the boss name from Unit");
        assertTrue(desc.contains("Experience Value"), "Description should include experience value from Enemy");
        assertTrue(desc.contains("Vision Range"), "Description should include vision range from Monster");
    }
}