package dnd.game;

import dnd.board.*;
import dnd.units.Unit;
import dnd.units.enemy.Enemy;
import dnd.units.player.Player;
import dnd.units.player.PlayerFactory;
import dnd.utils.MessageCallback;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class GameEngine {

    private final LevelLoader levelLoader;
    private final List<String> levelPaths;
    private int currentLevelIndex;

    private Player player;
    private Level currentLevel;
    private MessageCallback messageCallback;

    private boolean gameOver;
    private boolean gameWon;

    public GameEngine(List<String> levelPaths) {
        this.levelLoader = new LevelLoader();
        this.levelPaths = levelPaths;
        this.currentLevelIndex = 0;
        this.gameOver = false;
        this.gameWon = false;
    }

    /**
     * Initializes the player and loads the first level.
     */
    public void initialize(int playerChoice, MessageCallback callback) {
        this.messageCallback = callback;
        this.player = PlayerFactory.createPlayer(playerChoice, callback);

        this.player.setMessageCallback(callback); // Wire the player to the CLI

        loadNextLevel();
    }

    private void loadNextLevel() {
        if (currentLevelIndex >= levelPaths.size()) {
            this.gameWon = true;
            return;
        }

        String path = levelPaths.get(currentLevelIndex);
        try {
            this.currentLevel = levelLoader.loadLevel(path, player);

            // Wire the callback to all newly spawned enemies
            for (Enemy enemy : currentLevel.getActiveEnemies()) {
                enemy.setMessageCallback(this.messageCallback);
            }

            currentLevelIndex++;

        } catch (IOException e) {
            messageCallback.send("Fatal Error: Could not load level " + path);
            this.gameOver = true;
        }
    }

    /**
     * Executes a single round of the game based on UI input.
     */
    public void gameTick(char input) {
        if (gameOver || gameWon) return;
        if (!"wasdeq".contains(String.valueOf(input))) return;

        // 1. Process Player Turn (Instantly handles XP via Visitor/Abilities)
        boolean abilityCastSuccess = processPlayerTurn(input);

        // 2. Clear Corpses (Syncs the Board and List safely)
        cleanupDeadEnemies();

        // 3. The Tick
        if (!abilityCastSuccess && !player.getHealth().isDead() && !gameOver) {
            player.onGameTick();
        }

        // 4. Process Enemy Turns
        if (!player.getHealth().isDead() && !gameOver) {
            processEnemyTurns();
        }

        if (currentLevel.getActiveEnemies().isEmpty() && !gameOver) {
            loadNextLevel();
        }
    }

    /**
     * Translates input into movement or ability casting.
     * @return true if an ability was successfully cast, false otherwise.
     */
    private boolean processPlayerTurn(char input) {
        Position currentPos = player.getPosition();
        Position targetPos;

        switch (input) {
            case 'w': targetPos = new Position(currentPos.getX(), currentPos.getY() - 1); break;
            case 's': targetPos = new Position(currentPos.getX(), currentPos.getY() + 1); break;
            case 'a': targetPos = new Position(currentPos.getX() - 1, currentPos.getY()); break;
            case 'd': targetPos = new Position(currentPos.getX() + 1, currentPos.getY()); break;
            case 'e':
                // Return whatever the ability cast returns (true if success, false if cooldown/no mana)
                return player.castAbility(currentLevel.getActiveEnemies(), player);
            case 'q':
                return false; // Rest is a valid action, but not an ability
            default:
                // Invalid input detected
                return false;
        }

        executeMovement(player, targetPos);
        return false; // Movement is not an ability
    }

    private void processEnemyTurns() {
        for (Enemy enemy : currentLevel.getActiveEnemies()) {
            // Stops remaining enemies from wailing on a corpse
            if (player.getHealth().isDead()) {
                this.gameOver = true;
                break;
            }

            Position targetPos = enemy.takeTurn(player);
            if (!targetPos.equals(enemy.getPosition())) {
                executeMovement(enemy, targetPos);
            }
        }

        // Catch the case where the final enemy in the list strikes the fatal blow!
        if (player.getHealth().isDead()) {
            this.gameOver = true;
        }
    }

    private void executeMovement(Unit unit, Position targetPos) {
        Cell targetCell = currentLevel.getBoard().getCell(targetPos);
        if (targetCell != null) {

            Position oldPos = unit.getPosition();
            targetCell.accept(unit);
            Position newPos = unit.getPosition();

            // If the unit's internal position changed, physically synchronize the board
            if (!oldPos.equals(newPos)) {
                currentLevel.getBoard().setOccupant(oldPos, null);
                currentLevel.getBoard().setOccupant(newPos, unit);
            }
        }
    }

    private void cleanupDeadEnemies() {
        Iterator<Enemy> iterator = currentLevel.getActiveEnemies().iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (enemy.getHealth().isDead()) {
                currentLevel.getBoard().setOccupant(enemy.getPosition(), null);
                iterator.remove();
            }
        }
    }

    // Getters for CLI
    public GameBoard getCurrentBoard() { return currentLevel != null ? currentLevel.getBoard() : null; }
    public Player getPlayer() { return player; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
}