package dnd.units;

import dnd.board.Position;
import dnd.combat.OccupantVisitor;

/**
 * A shared dummy implementation of the Occupant interface used strictly for testing.
 * <p>
 * This class allows testing of board mechanics (like Floor and Wall) without
 * relying on the complex, heavy logic of real entities.
 */
public class DummyOccupant implements Occupant {
    private final String renderCharacter;
    public boolean acceptWasCalled = false;

    /**
     * Constructs a dummy occupant with a specific character for rendering tests.
     *
     * @param renderCharacter The string this dummy should return when toString() is called.
     */
    public DummyOccupant(String renderCharacter) {
        this.renderCharacter = renderCharacter;
    }

    /**
     * Fakes the Double Dispatch receiver.
     * Flips a boolean flag so tests can verify that combat routing was triggered.
     *
     * @param visitor The entity initiating the interaction.
     */
    @Override
    public void accept(OccupantVisitor visitor) {
        this.acceptWasCalled = true;
    }

    @Override
    public void setPosition(Position p) {

    }

    @Override
    public Position getPosition() {
        return null;
    }

    /**
     * Returns the predefined string to verify rendering logic in cells.
     *
     * @return The character assigned during construction.
     */
    @Override
    public String toString() {
        return this.renderCharacter;
    }
}
