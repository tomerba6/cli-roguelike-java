package dnd.game;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main entry point for the Dungeons & Dragons game.
 */
public class DndGame {

    public static void main(String[] args) {
        // 1. Validate Command Line Arguments
        if (args.length == 0) {
            System.out.println("Error: Please provide the path to the levels directory as a command-line argument.");
            System.out.println("Usage: java Main <path_to_levels_directory>");
            return;
        }

        String levelsDirPath = args[0];
        File levelsDir = new File(levelsDirPath);

        if (!levelsDir.exists() || !levelsDir.isDirectory()) {
            System.out.println("Error: The provided path '" + levelsDirPath + "' is not a valid directory.");
            return;
        }

        // 2. Load and Sort Level Files
        File[] levelFiles = levelsDir.listFiles();
        if (levelFiles == null || levelFiles.length == 0) {
            System.out.println("Error: No level files found in the directory '" + levelsDirPath + "'.");
            return;
        }

        // Sort files alphabetically to ensure level1.txt is played before level2.txt
        Arrays.sort(levelFiles);

        List<String> levelPaths = new ArrayList<>();
        for (File file : levelFiles) {
            if (file.isFile()) {
                levelPaths.add(file.getAbsolutePath());
            }
        }

        // 3. Ignite the Engine and Start the Game
        GameEngine engine = new GameEngine(levelPaths);
        CLI cli = new CLI(engine);

        cli.start();
    }
}