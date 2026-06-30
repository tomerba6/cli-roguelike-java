package dnd.units.enemy;

/**
 * A factory utility class responsible for translating ASCII characters from the
 * level files into fully initialized Enemy objects based on the assignment tables.
 */
public class EnemyFactory {

    /**
     * Creates the appropriate enemy based on the provided map character.
     *
     * @param tile The character from the level.txt file.
     * @return A newly instantiated Enemy, or null if the character doesn't match an enemy.
     */
    public static Enemy createEnemy(char tile) {
        return switch (tile) {
            // --- Monsters ---
            case 's' -> new Monster("Gold Cloak", 80, 8, 3, 25, 4, 's');
            case 'k' -> new Monster("Knight", 200, 14, 8, 50, 5, 'k');
            case 'q' -> new Monster("Queen's Guard", 400, 20, 15, 100, 6, 'q');
            case 'z' -> new Monster("Wright", 600, 30, 15, 100, 4, 'z');
            case 'b' -> new Monster("Bear", 1000, 75, 30, 250, 5, 'b');
            case 'g' -> new Monster("Giant", 1500, 100, 40, 500, 6, 'g');
            case 'w' -> new Monster("White Walker", 2000, 150, 50, 1000, 7, 'w');

            // --- Bosses (Upgraded Monsters) ---
            case 'M' -> new Boss("The Mountain", 1000, 60, 25, 500, 7, 5, 'M');
            case 'C' -> new Boss("Queen Cersei", 500, 10, 10, 400, 4, 8, 'C');
            case 'K' -> new Boss("Night's King", 5000, 300, 150, 5000, 8, 3, 'K');

            // --- Traps ---
            case 'B' -> new Trap("Bonus Trap", 1, 1, 1, 15, 1, 5, 'B');
            case 'Q' -> new Trap("Queen's Trap", 250, 50, 10, 100, 3, 7, 'Q');
            case 'D' -> new Trap("Death Trap", 500, 100, 20, 250, 1, 10, 'D');
            default -> null; // Character is not an enemy (e.g., '#', '.', '@')
        };
    }
}