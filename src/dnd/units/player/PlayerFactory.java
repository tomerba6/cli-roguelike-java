package dnd.units.player;

import dnd.utils.MessageCallback;

/**
 * A factory utility class responsible for instantiating the correct Player character
 * based on the user's menu selection.
 * <p>
 * Strictly uses callbacks for all console outputs to adhere to MVC architecture.
 */
public class PlayerFactory {

    /**
     * Sends the character selection menu to the UI via callback.
     * * @param callback The UI messaging interface.
     */
    public static void printMenu(MessageCallback callback) {
        callback.send("Select player:");
        callback.send("1. Jon Snow             Health: 300/300         Attack: 30              Defense: 4              Level: 1                Experience: 0/50                Cooldown: 0/3");
        callback.send("2. The Hound            Health: 400/400         Attack: 20              Defense: 6              Level: 1                Experience: 0/50                Cooldown: 0/5");
        callback.send("3. Melisandre           Health: 100/100         Attack: 5               Defense: 1              Level: 1                Experience: 0/50                Mana: 75/300            Spell Power: 15");
        callback.send("4. Thoros of Myr        Health: 250/250         Attack: 25              Defense: 4              Level: 1                Experience: 0/50                Mana: 37/150            Spell Power: 20");
        callback.send("5. Arya Stark           Health: 150/150         Attack: 40              Defense: 2              Level: 1                Experience: 0/50                Energy: 100/100");
        callback.send("6. Bronn                Health: 250/250         Attack: 35              Defense: 3              Level: 1                Experience: 0/50                Energy: 100/100");
        callback.send("7. Ygritte              Health: 220/220         Attack: 30              Defense: 2              Level: 1                Experience: 0/50                Arrows: 10              Range: 6");
    }

    /**
     * Creates the appropriate player based on the menu choice.
     *
     * @param choice   The integer input from the user.
     * @param callback The UI messaging interface to report invalid choices.
     * @return A newly instantiated Player, or Jon Snow by default if input is invalid.
     */
    public static Player createPlayer(int choice, MessageCallback callback) {
        switch (choice) {
            // Warriors (Name, Health, Attack, Defense, Cooldown)
            case 1: return new Warrior("Jon Snow", 300, 30, 4, 3);
            case 2: return new Warrior("The Hound", 400, 20, 6, 5);

            // Mages (Name, Health, Attack, Defense, Mana Pool, Mana Cost, Spell Power, Hit Count, Range)
            case 3: return new Mage("Melisandre", 100, 5, 1, 300, 30, 15, 5, 6);
            case 4: return new Mage("Thoros of Myr", 250, 25, 4, 150, 20, 20, 3, 4);

            // Rogues (Name, Health, Attack, Defense, Energy Cost)
            case 5: return new Rogue("Arya Stark", 150, 40, 2, 20);
            case 6: return new Rogue("Bronn", 250, 35, 3, 50);

            // Hunter (Name, Health, Attack, Defense, Range)
            case 7: return new Hunter("Ygritte", 220, 30, 2, 6);

            // Default fallback
            default:
                // Now safely routed through the callback!
                callback.send("Invalid choice. Defaulting to Jon Snow.");
                return new Warrior("Jon Snow", 300, 30, 4, 3);
        }
    }
}