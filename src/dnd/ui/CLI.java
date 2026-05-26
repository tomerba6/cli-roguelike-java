package dnd.ui;

/**
 * Handles all command-line interface output and rendering.
 * This is the ONLY class permitted to interact with the system console.
 */
public class CLI {
    /**
     * Prints a message to the console.
     *
     * @param message The string to print.
     */
    public void print(String message) {
        System.out.println(message);
    }
}
