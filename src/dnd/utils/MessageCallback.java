package dnd.utils;

/**
 * A callback interface used to send messages from the business layer to the UI layer.
 */
public interface MessageCallback {
    /**
     * Sends a message to be displayed to the user.
     * @param message The string to display.
     */
    void send(String message);
}