package dnd.units.player;

import dnd.board.Floor;
import dnd.board.Position;
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
public abstract class Player extends Unit implements HeroicUnit {
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
        int levelUpRequirement = 100 * this.level;

        while (this.experience >= levelUpRequirement) {
            this.experience -= levelUpRequirement;
            this.levelUp();
            levelUpRequirement = 100 * this.level;
        }
    }

    /**
     * Handles the base stat increases when leveling up.
     * Subclasses will override this to add their specific bonuses,
     * must call super.levelUp() to get these base stats first
     */
    protected void levelUp() {
        this.level++;
        this.getHealth().addHealthPool(15);
        this.getHealth().heal(this.getHealth().getHealthPool() / 4);
        this.attackPower += 5;
        this.defensePower += 1;
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
     * Handles the interaction when the Player attempts to move into a Wall.
     * Prevents movement and sends a collision notification to the user interface.
     *
     * @param wall The Wall object the Player collided with.
     */
    @Override
    public void visit(Wall wall) {
        if (this.messageCallback != null) {
            this.messageCallback.send(this.getName() + " Hit a wall.");
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
            logMessage(e.getName() + " died. " + this.getName() + " gained " + e.getExperienceValue() + " experience");
            this.addExperience(e.getExperienceValue());

            Position playerOldPos = this.getPosition();

            // 2. The player steps onto the enemy's tile
            this.setPosition(e.getPosition());

            // 3. Move the corpse to the player's old tile so GameEngine can safely delete it
            e.setPosition(playerOldPos);
        }
    }

    /**
     * Called once per game loop tick when the player did not cast an ability.
     * Subclasses use this to regenerate resources (mana, energy, arrows, cooldown).
     */
    public abstract void onGameTick();

    /**
     * Returns the full status of the player, appending Level and Experience
     * to the base Unit description.
     *
     * @return A formatted string containing all base stats plus level and XP.
     */
    @Override
    public String description() {
        int nextLevelRequirement = 100 * this.level;

        return super.description() + String.format("\t\tLevel: %d\t\tExperience: %d/%d",
                this.level,
                this.experience,
                nextLevelRequirement);
    }

    /**
     * Retrieves the level of the player.
     *
     * @return The Player's level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Retrieves the experience points of the player.
     *
     * @return The Player's experience.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Returns the visual character representation of the player for the game board.
     *
     * @return The '@' character, indicating the main player.
     */
    @Override
    public String toString() {
        return this.getHealth().isDead() ? "X" : "@";
    }
}
