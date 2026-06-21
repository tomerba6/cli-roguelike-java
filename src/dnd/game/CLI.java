package dnd.game;

import dnd.board.GameBoard;
import dnd.units.player.PlayerFactory;
import dnd.utils.MessageCallback;

import java.util.Scanner;

/**
 * The Command Line Interface.
 * Handles all System.out.println and Scanner interactions.
 * Implements MessageCallback to receive text from the business layer.
 */
public class CLI implements MessageCallback {

    private final Scanner scanner;
    private final GameEngine engine;

    public CLI (GameEngine engine) {
        this.scanner = new Scanner(System.in);
        this.engine = engine;
    }

    /**
     * Fulfills the MessageCallback contract.
     * When the engine/units call this, the CLI prints it.
     */
    @Override
    public void send(String message) {
        System.out.println(message);
    }

    /**
     * Starts the CLI application.
     */
    public void start() {
        int choice;

        // Print the menu EXACTLY ONCE before the loop begins
        PlayerFactory.printMenu(this);

        // Loop until a valid choice (1-7) is entered
        while (true) {
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (choice >= 1 && choice <= 7) {
                    break; // Valid choice, break the loop and start the game!
                }
            } else {
                scanner.nextLine(); // Consume the invalid non-integer string
            }

            // Print the error message, and the loop will naturally wait for new input!
            send("Invalid input. Please enter a number between 1 and 7.");
        }

        // Initialize Engine
        engine.initialize(choice, this);

        send("You have selected:");
        send(engine.getPlayer().getName());

        // Main Game Loop
        playGame();
    }

    private void playGame() {
        while (!engine.isGameOver() && !engine.isGameWon()) {
            // 1. Print the board and player stats EXACTLY ONCE per turn
            GameBoard currentBoard = engine.getCurrentBoard();
            if (currentBoard != null) {
                send(currentBoard.toString());
            }
            send(engine.getPlayer().description());

            char input;

            // 2. The Validation Mini-Loop
            while (true) {
                String rawInput = scanner.nextLine();

                if (!rawInput.isEmpty()) {
                    input = rawInput.toLowerCase().charAt(0);

                    // Check if it is a valid game command
                    if ("wasdeq".contains(String.valueOf(input))) {
                        break; // Valid input! Break the loop and process the tick.
                    }
                }

                // 3. If we reach this line, the input was invalid.
                // Print the error message. The loop will wait for the next scanner.nextLine()
                send("Invalid input. Please enter w, a, s, d, e, or q.");
            }

            // 4. Send the strictly validated input to the Enginet
            engine.gameTick(input);
        }


        if (engine.isGameWon()) {
            send("You won!");
        } else {
            send(engine.getCurrentBoard().toString());
            send(engine.getPlayer().description());
            send("Game Over.");
        }
    }
}