package dnd.units.player;

import dnd.board.Floor;
import dnd.board.Wall;
import dnd.combat.CellVisitor;
import dnd.combat.HeroicUnit;
import dnd.combat.OccupantVisitor;
import dnd.units.Occupant;
import dnd.units.Unit;
import dnd.units.enemy.Enemy;

/**
 * The abstract base class for all playable characters.
 * <p>
 * This class implements movement (CellVisitor) and defines the base leveling
 * system. It also implements the HeroicUnit interface for special abilities.
 */
public abstract class Player extends Unit implements CellVisitor, HeroicUnit {
    /** The current level of the player. Starts at 1. */
    protected int level;

    /** The current experience points accumulated. Rolls over upon level up. */
    protected int experience;

    /**
     * Constructs a new Player at Level 1 with 0 experience.
     *
     * @param name         The name of the player character.
     * @param healthPool   The starting max health.
     * @param attackPower  The starting attack power.
     * @param defensePower The starting defense power.
     */
    public Player(String name, int healthPool, int attackPower, int defensePower) {
        super(name, healthPool, attackPower, defensePower);
        this.level = 1;
        this.experience = 0;
    }

    /**
     * Adds experience to the player and triggers level ups if the threshold is met.
     *
     * @param experienceGain The amount of experience gained from killing an enemy.
     */
    public void addExperience(int experienceGain) {
        this.experience += experienceGain;
        int levelUpRequirement = 50 * this.level;

        while (this.experience >= levelUpRequirement) {
            this.experience -= levelUpRequirement;
            this.levelUp();
            levelUpRequirement = 50 * this.level;
        }
    }

    /**
     * Handles the base stat increases when leveling up.
     * Subclasses will override this to add their specific bonuses,
     * must call super.levelUp() to get these base stats first
     */
    protected void levelUp() {
        this.level++;
        this.getHealth().addHealthPool(10 * this.level);
        this.getHealth().heal(this.getHealth().getHealthPool());
        this.attackPower += 4 * this.level;
        this.defensePower += this.level;
    }

    /**
     * Accepts an incoming interaction from another entity.
     * Routes the execution via Double Dispatch to the visitor's {@code visit(Player p)} method.
     *
     * @param visitor The entity initiating the interaction (usually an Enemy).
     */
    @Override
    public void accept(OccupantVisitor visitor) {
        visitor.visit(this);
    }


    /**
     * Defines the behavior when the player attempts to move into a Wall cell.
     * Movement is completely blocked, ending the movement attempt.
     *
     * @param w The wall cell being visited.
     */
    @Override
    public void visit(Wall w) {
        // Hit a wall. Movement is blocked. Do nothing.
    }

    /**
     * Defines the behavior when the player attempts to move into a Floor cell.
     * <p>
     * If the floor is empty, the player successfully moves to that cell.
     * If the floor contains an Occupant, the player initiates Level 2 combat
     * by visiting that occupant.
     *
     * @param f The floor cell being visited.
     */
    @Override
    public void visit(Floor f) {
        Occupant target = f.getOccupant();
        if (target == null) {
            // The floor is empty. The Player successfully moves here.
            this.setPosition(f.getPosition());
        } else {
            // The floor is occupied. We initiate Level 2 Combat.
            target.accept(this);
        }
    }


    /**
     * Defines the interaction when this player initiates an attack on another player.
     * <p>
     * Because this is a single-player game, this state should never be reached.
     * If it is, it indicates a critical flaw in the game engine's coordinate system.
     *
     * @param p The friendly player being targeted.
     * @throws IllegalStateException always, as friendly fire is impossible.
     */
    @Override
    public void visit(Player p) {
        // This state should be physically impossible in a single-player game.
        throw new IllegalStateException("Error: A Player attempted to interact with another Player.");
    }

    /**
     * Resolves combat when the player attacks an enemy.
     * If the enemy dies, the player gains XP and takes their physical position.
     *
     * @param e The enemy being attacked.
     */
    @Override
    public void visit(Enemy e) {
        super.engageInCombat(e);

        if (e.getHealth().isDead()) {
            this.addExperience(e.getExperienceValue());
            this.setPosition(e.getPosition());
        }
    }

    /**
     * Returns the full status of the player, appending Level and Experience
     * to the base Unit description.
     *
     * @return A formatted string containing all base stats plus level and XP.
     */
    @Override
    public String description() {
        int nextLevelRequirement = 50 * this.level;

        return super.description() + String.format("\t\tLevel: %d\t\tExperience: %d/%d",
                this.level,
                this.experience,
                nextLevelRequirement);
    }

    /**
     * Returns the visual character representation of the player for the game board.
     *
     * @return The '@' character, indicating the main player.
     */
    @Override
    public String toString() {
        return "@"; // The standard assignment character for the main character
    }
}
