package dnd.game;

import dnd.board.GameBoard;
import dnd.units.player.PlayerFactory;
import dnd.utils.MessageCallback;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Scanner;

/**
 * The Command Line Interface.
 * Handles all System.out.println and Scanner interactions.
 * Implements MessageCallback to receive text from the business layer.
 */
public class CLI implements MessageCallback {

    private final Scanner scanner;
    private final GameEngine engine;

    // The volatile keyword ensures the GUI thread and Game thread sync perfectly
    private volatile char lastKeyPressed = '\0';

    /**
     * Constructs a CLI bound to the given engine.
     * @param engine the GameEngine this CLI will drive
     */
    public CLI(GameEngine engine) {
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

        // Launch our invisible keystroke interceptor right before the game loop starts!
        setupInstantInputController();

        // Main Game Loop
        playGame();
    }

    /**
     * Creates a tiny window to instantly catch raw keystrokes, bypassing the OS console buffer.
     */
    private void setupInstantInputController() {
        JFrame frame = new JFrame("D&D Keystroke Controller");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Centers on screen

        JLabel label = new JLabel("Keep this window clicked/focused to move!", SwingConstants.CENTER);
        frame.add(label);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                lastKeyPressed = Character.toLowerCase(e.getKeyChar());
            }
        });

        frame.setVisible(true);
    }

    private void playGame() {
        while (!engine.isGameOver() && !engine.isGameWon()) {
            // 1. Print the board and player stats EXACTLY ONCE per turn
            GameBoard currentBoard = engine.getCurrentBoard();
            if (currentBoard != null) {
                send(currentBoard.toString());
            }
            send(engine.getPlayer().description());

            // 2. Reset the key press tracker for the new turn
            lastKeyPressed = '\0';

            // 3. Wait silently until the GUI window registers a keystroke
            while (lastKeyPressed == '\0') {
                try {
                    Thread.sleep(15); // Sleep 15ms to prevent CPU overload
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 4. We instantly have the key!
            char input = lastKeyPressed;

            // 5. Validate and Tick
            if ("wasdeq".indexOf(input) != -1) {
                engine.gameTick(input);
            } else {
                send("Invalid input. Press w, a, s, d, e, or q in the Controller window.");
            }
        }

        if (engine.isGameWon()) {
            send("You won!");
        } else {
            send(engine.getCurrentBoard().toString());
            send(engine.getPlayer().description());
            send("Game Over.");
        }

        // Safely close the hidden GUI window and terminate background threads when the game ends
        System.exit(0);
    }
}