# D&D: The Long Night - a CLI Roguelike in Java

> A turn-based, terminal-rendered dungeon crawler. Pick a hero, fight your way through four
> hand-crafted levels of monsters, traps, and bosses, and survive to face the Night's King.

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-JUnit_5.14-25A162?logo=junit5&logoColor=white)
![Build](https://img.shields.io/badge/Build-IntelliJ_IDEA-000000?logo=intellijidea&logoColor=white)
![Dependencies](https://img.shields.io/badge/Runtime_Dependencies-None_(pure_JDK)-success)
![Status](https://img.shields.io/badge/Status-Playable-brightgreen)

This project is an object-oriented design exercise: a complete, playable game built around classic
GoF design patterns (Visitor / double-dispatch, Factory, Template Method) with a strict separation
between game logic and presentation, and an extensive JUnit 5 test suite.

---

## Table of Contents

- [Gameplay](#gameplay)
  - [Objective](#objective)
  - [Controls](#controls)
  - [Heroes](#heroes)
  - [Enemies & Tiles](#enemies--tiles)
  - [Combat & Leveling](#combat--leveling)
- [Architecture](#architecture)
  - [Design patterns](#design-patterns)
  - [Class hierarchy](#class-hierarchy)
  - [The two-level Visitor (double dispatch)](#the-two-level-visitor-double-dispatch)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Platform support](#platform-support)
  - [Run in IntelliJ IDEA](#option-a-run-in-intellij-idea-easiest)
  - [Run from the command line](#option-b-run-from-the-command-line)
  - [Run the packaged JAR](#option-c-run-the-packaged-jar)
- [Level file format](#level-file-format)
- [Testing](#testing)
- [Design highlights](#design-highlights)
- [License & acknowledgements](#license--acknowledgements)

---

## Gameplay

You start by choosing one of **12 heroes** spanning four playable classes. Each level is a grid map
loaded from a text file. The game is **turn-based**: every key you press advances the world one tick -
you act first, then every enemy takes its turn (chasing, roaming, casting, or springing a trap).

### Objective

- **Clear a level** by killing every enemy on it. The engine then auto-loads the next level.
- **Win** by clearing all four levels.
- **Lose** the moment your health hits `0`.

### Controls

The game renders the board to **standard output**, but reads keystrokes through a small always-on-top
**Swing "Controller" window** that pops up when play begins. Keep that window focused for your keys to
register.

| Key | Action            | Effect on the board                |
|-----|-------------------|------------------------------------|
| `w` | Move **up**       | `y - 1`                            |
| `s` | Move **down**     | `y + 1`                            |
| `a` | Move **left**     | `x - 1`                            |
| `d` | Move **right**    | `x + 1`                            |
| `e` | Use class ability | Casts your special; skips movement |
| `q` | Rest              | Pass the turn (enemies still act)  |

> Walking into a tile occupied by an enemy initiates **melee combat** instead of moving.

### Heroes

Four classes, each with a distinct resource system and special ability:

| Class       | Resource             | Ability            | Behaviour                                                           |
|-------------|----------------------|--------------------|---------------------------------------------------------------------|
| **Warrior** | Cooldown (ticks)     | *Avenger's Shield* | Heals self, then strikes one random enemy in range for % of max HP. |
| **Mage**    | Mana (regen/tick)    | *Blizzard*         | Multiple hits, each on a random enemy in range, dealing spell power.|
| **Rogue**   | Energy (regen/tick)  | *Fan of Knives*    | AoE - hits **every** enemy within range `< 2`.                      |
| **Hunter**  | Arrows (timed regen) | *Shoot*            | Fires at the **closest** enemy within range.                        |

<details>
<summary><b>Full hero roster (12 selectable characters)</b></summary>

| #  | Name             | Class   | Health | Attack | Defense | Notable resource           |
|----|------------------|---------|-------:|-------:|--------:|----------------------------|
| 1  | Jon Snow         | Warrior |    300 |     30 |       4 | Cooldown 3                 |
| 2  | The Hound        | Warrior |    400 |     20 |       6 | Cooldown 5                 |
| 3  | Melisandre       | Mage    |    100 |      5 |       1 | Mana 300, Spell Power 15   |
| 4  | Thoros of Myr    | Mage    |    250 |     25 |       4 | Mana 150, Spell Power 20   |
| 5  | Arya Stark       | Rogue   |    150 |     40 |       2 | Energy 100, cost 35        |
| 6  | Bronn            | Rogue   |    250 |     35 |       3 | Energy 100, cost 50        |
| 7  | Ygritte          | Hunter  |    220 |     30 |       2 | Arrows 10, Range 6         |
| 8  | Aragorn          | Warrior |    350 |     32 |       3 | Cooldown 4                 |
| 9  | Gandalf the Grey | Mage    |     90 |      5 |       1 | Mana 320, Spell Power 40   |
| 10 | Bilbo Baggins    | Rogue   |    110 |     28 |       2 | Energy 100, cost 15        |
| 11 | Legolas          | Hunter  |    180 |     38 |       3 | Arrows 10, Range 9         |
| 12 | Hermione Granger | Mage    |    130 |      6 |       2 | Mana 220, Spell Power 22   |

</details>

### Enemies & Tiles

Maps are plain ASCII. Each character maps to terrain, the player spawn, or an enemy:

| Tile | Meaning                       |
|------|-------------------------------|
| `#`  | Wall (impassable)             |
| `.`  | Floor (walkable)              |
| `@`  | Player spawn                  |
| `X`  | Rendered when the player dies |

<details>
<summary><b>Enemy bestiary (spawned by the EnemyFactory)</b></summary>

**Monsters** - chase the player when within vision range, otherwise roam randomly:

| Tile | Name          | HP   | ATK | DEF | XP   | Vision |
|------|---------------|-----:|----:|----:|-----:|-------:|
| `s`  | Gold Cloak    |   80 |   8 |   3 |   25 |      4 |
| `k`  | Knight        |  200 |  14 |   8 |   50 |      5 |
| `q`  | Queen's Guard |  400 |  20 |  15 |  100 |      6 |
| `z`  | Wright        |  600 |  30 |  15 |  100 |      4 |
| `b`  | Bear          | 1000 |  75 |  30 |  250 |      5 |
| `g`  | Giant         | 1500 | 100 |  40 |  500 |      6 |
| `w`  | White Walker  | 2000 | 150 |  50 | 1000 |      7 |

**Bosses** - like Monsters, but cast a ranged special on a fixed timer:

| Tile | Name         | HP   | ATK | DEF | XP   | Vision | Ability every |
|------|--------------|-----:|----:|----:|-----:|-------:|--------------:|
| `M`  | The Mountain | 1000 |  60 |  25 |  500 |      7 |      5 ticks  |
| `C`  | Queen Cersei |  500 |  10 |  10 |  400 |      4 |      8 ticks  |
| `K`  | Night's King | 5000 | 300 | 150 | 5000 |      8 |      3 ticks  |

**Traps** - stationary; cycle between visible/invisible and strike any player within range `< 2`:

| Tile | Name         | HP  | ATK | DEF | XP  | Visible / Invisible |
|------|--------------|----:|----:|----:|----:|---------------------|
| `B`  | Bonus Trap   |   1 |   1 |   1 |  15 | 1 / 5 ticks         |
| `Q`  | Queen's Trap | 250 |  50 |  10 | 100 | 3 / 7 ticks         |
| `D`  | Death Trap   | 500 | 100 |  20 | 250 | 1 / 10 ticks        |

</details>

### Combat & Leveling

Combat is a contested dice roll. Each side rolls a uniform random integer between `0` and its
respective power, inclusive:

```
attackRoll  = random(0 .. attacker.attackPower)
defenseRoll = random(0 .. defender.defensePower)
damage      = max(0, attackRoll - defenseRoll)
```

Killing an enemy grants its experience instantly. Leveling needs `100 * currentLevel` XP, with
leftover XP rolling over. Every level-up applies flat base gains (**+15 max HP**, heal 25% of the new
pool, **+5 attack**, **+1 defense**) **plus** class-specific bonuses (e.g. a Warrior adds a further
+5 HP, +2 attack, +2 defense and resets its cooldown).

---

## Architecture

The codebase is organized into cohesive packages under `dnd`, with a hard wall between **game logic**
and **I/O**. No domain class ever calls `System.out`; instead, everything routes user-facing text
through a `MessageCallback`, which the CLI implements. This keeps the entire engine headless and
fully unit-testable.

| Package            | Responsibility                                                               |
|--------------------|------------------------------------------------------------------------------|
| `dnd.game`         | `DndGame` (entry point), `GameEngine` (turn loop), `CLI` (rendering + input) |
| `dnd.board`        | `Position`, `Cell`/`Wall`/`Floor`, `GameBoard`, `Level`, `LevelLoader`       |
| `dnd.combat`       | Visitor interfaces: `CellVisitor`, `OccupantVisitor`, plus `HeroicUnit`      |
| `dnd.units`        | `Occupant`, abstract `Unit`, `Health`                                        |
| `dnd.units.enemy`  | abstract `Enemy`, `Monster`, `Boss`, `Trap`, `EnemyFactory`                  |
| `dnd.units.player` | abstract `Player`, `Warrior`, `Mage`, `Rogue`, `Hunter`, `PlayerFactory`     |
| `dnd.utils`        | `MessageCallback` (the logic <-> UI boundary)                                |

### Design patterns

- **Visitor / Double Dispatch** - movement and combat are resolved polymorphically, with **zero
  `instanceof` checks**, across two layers (terrain and occupants). See below.
- **Factory** - `EnemyFactory` turns a map character into the right `Enemy`; `PlayerFactory` turns a
  menu choice into the right `Player`.
- **Template Method** - `Player.levelUp()` defines the base growth algorithm; each subclass overrides
  it and calls `super.levelUp()` to layer on class bonuses. `Unit.engageInCombat()` similarly fixes
  the combat skeleton.
- **Callback / Observer (MVC seam)** - `MessageCallback` decouples the engine from the console so the
  UI is swappable and the logic is testable in isolation.
- **Interface Segregation** - combatants implement `OccupantVisitor`, but only things that *move*
  implement `CellVisitor` (a `Trap` deliberately doesn't), and only ability-casters implement
  `HeroicUnit` (all `Player`s and `Boss`es).
- **Value Object** - `Position` is immutable with proper `equals`/`hashCode`.

### Class hierarchy

```
Occupant (interface)                         Cell (interface)
   |                                            |
Unit (abstract)                              +-- Wall    (blocks movement)
implements CellVisitor,                      \-- Floor   (holds <= 1 Occupant)
           OccupantVisitor, Occupant
   |
   +-- Player (abstract) implements HeroicUnit
   |     +-- Warrior   (Avenger's Shield - cooldown)
   |     +-- Mage      (Blizzard - mana)
   |     +-- Rogue     (Fan of Knives - energy)
   |     \-- Hunter    (Shoot - arrows)
   |
   \-- Enemy (abstract)
         +-- Monster   (chase / roam AI)
         |     \-- Boss   implements HeroicUnit  (timed special ability)
         \-- Trap      (stationary; toggles visibility)
```

### The two-level Visitor (double dispatch)

A single move resolves through two dispatches, letting each object decide the outcome by its own type:

1. **Terrain (Level 1 - `CellVisitor`)**: the engine asks the target `Cell` to `accept(unit)`. A
   `Wall` answers by blocking; a `Floor` answers by letting the unit step on - or, if occupied,
   escalating to Level 2.
2. **Occupants (Level 2 - `OccupantVisitor`)**: the occupied `Floor` asks its `Occupant` to
   `accept(unit)`, which calls back into `unit.visit(player)` or `unit.visit(enemy)` - resolving
   combat, friendly-fire rules, XP, and the corpse "position swap" entirely through polymorphism.

```
Engine -> cell.accept(unit) --+--> unit.visit(Wall)    -> movement blocked
                              |
                              +--> unit.visit(Floor)   -> if occupied:
                                       floor.occupant.accept(unit)
                                         --> unit.visit(Player) / unit.visit(Enemy)  -> combat
```

---

## Tech stack

| Concern      | Choice                                                                       |
|--------------|------------------------------------------------------------------------------|
| Language     | **Java 21** (project language level `JDK_21`)                                |
| Testing      | **JUnit 5 (Jupiter) 5.14.0**                                                 |
| Input        | **Java Swing** - a hidden `JFrame` `KeyListener` captures keystrokes instantly|
| Rendering    | Plain `System.out` via the `MessageCallback` seam                           |
| Build        | **IntelliJ IDEA** module (no Maven/Gradle); plain `javac`/`java` also works  |
| Runtime deps | **None** - pure JDK                                                          |

---

## Project structure

```
cli-roguelike-java/
+-- src/
|   +-- dnd/
|   |   +-- game/        DndGame, GameEngine, CLI
|   |   +-- board/       Position, Cell, Wall, Floor, GameBoard, Level, LevelLoader
|   |   +-- combat/      CellVisitor, OccupantVisitor, HeroicUnit
|   |   +-- units/       Occupant, Unit, Health
|   |   |   +-- enemy/   Enemy, Monster, Boss, Trap, EnemyFactory
|   |   |   \-- player/  Player, Warrior, Mage, Rogue, Hunter, PlayerFactory
|   |   \-- utils/       MessageCallback
|   +-- levels_dir/      level1.txt ... level4.txt  (the shipped campaign)
|   \-- META-INF/        MANIFEST.MF  (Main-Class: dnd.game.DndGame)
\-- test/
    \-- dnd/             Mirrors src/ - 17 JUnit 5 test classes + Dummy test doubles
```

---

## Getting started

### Prerequisites

- **JDK 21** or newer (`java -version` should report 21+).
- A **desktop/GUI environment** - the input controller is a Swing window, so the game does not run on
  a headless server or a plain SSH session.

### Platform support

The game is **pure Java with no native dependencies, no OS-specific calls, and no hardcoded paths**,
so it runs identically on every desktop that has a JDK 21:

| Platform                      | Status    | Recommended way to build & run    |
|-------------------------------|-----------|-----------------------------------|
| macOS (Apple Silicon & Intel) | Supported | `bash` (Option B) or IntelliJ     |
| Linux                         | Supported | `bash` (Option B) or IntelliJ     |
| Windows                       | Supported | PowerShell (Option B) or IntelliJ |

- **macOS / Linux are the primary path** - use the `bash` commands in
  [Option B](#option-b-run-from-the-command-line) with forward-slash paths. The Swing controller
  window renders natively and needs no extra permissions; on macOS it may open *behind* your terminal,
  so click it once to bring it into focus.
- **Windows** - use the PowerShell variant in [Option B](#option-b-run-from-the-command-line), or just
  run it from IntelliJ.
- **Every** platform needs a **graphical desktop session** (the input controller is a Swing window),
  so none can run headless or over a plain SSH connection.
- Level files are read with line-ending-agnostic parsing, so maps authored on Windows (CRLF) load
  correctly on macOS and Linux (LF) without conversion.
- The only non-portable artifact in the repo is the prebuilt `out/artifacts/*.jar` (compiled on
  Windows) - ignore it and build from source as shown below.

### Option A: Run in IntelliJ IDEA (easiest)

1. Open the project folder in IntelliJ (it already contains the module and a wired JUnit 5 library).
2. Open **Run > Edit Configurations...**, create an *Application* config for `dnd.game.DndGame`.
3. Set **Program arguments** to the levels directory, e.g. `src/levels_dir`.
4. Run it, and keep the pop-up **"D&D Keystroke Controller"** window focused while you play.

### Option B: Run from the command line

The program takes exactly **one argument**: the path to a directory of level files (played in
alphabetical order).

**bash / macOS / Linux:**

```bash
# from the project root
mkdir -p build
javac -d build $(find src -name "*.java")
java -cp build dnd.game.DndGame src/levels_dir
```

**Windows PowerShell:**

```powershell
# from the project root
New-Item -ItemType Directory -Force build | Out-Null
javac -d build (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp build dnd.game.DndGame src\levels_dir
```

### Option C: Run the packaged JAR

The manifest already declares `Main-Class: dnd.game.DndGame`, so a built jar is runnable directly:

```bash
java -jar cli-roguelike-java.jar src/levels_dir
```

To build the jar yourself from `build/` (after compiling as in Option B):

```bash
jar --create --file cli-roguelike-java.jar --manifest src/META-INF/MANIFEST.MF -C build .
```

---

## Level file format

A level is a **rectangular** grid of ASCII characters in a `.txt` file. Coordinates are `(x, y)` with
the origin at the top-left; `x` grows right, `y` grows down. Rules:

- Use the [tile legend](#enemies--tiles) above: `#` wall, `.` floor, `@` player spawn, and any enemy
  letter from the bestiary.
- All rows should be the **same width** (the loader takes the grid width from the first line).
- Exactly one `@` is expected; an unrecognized character aborts loading with an error.
- Files are played in **alphabetical order**, so name them `level1.txt`, `level2.txt`, and so on.

Minimal example:

```
#######
#@..s.#
#..#..#
#.M...#
#######
```

The shipped campaign lives in [`src/levels_dir/`](src/levels_dir) - four progressively harder maps
ending in a `K` (Night's King) boss arena.

---

## Testing

The suite is built around **deterministic test doubles**. Combat uses `Math.random()`, so
`DummyPlayer` / `DummyEnemy` override the attack/defense rolls to return fixed maximums, while
`GameEngineTest` neutralizes randomness with log-based assertions and bounded "swing-until-resolved"
loops. This keeps every test reproducible.

Coverage spans all layers:

- **Units & combat** - `HealthTest`, `UnitTest`, `VisitorPatternTest`, `EnemyTest`
- **Players** - `PlayerTest`, `WarriorTest`, `MageTest`, `RogueTest`, `HunterTest`
- **Enemies** - `MonsterTest`, `BossTest`, `TrapTest`
- **Board & loading** - `PositionTest`, `WallTest`, `FloorTest`, `GameBoardTest`, `LevelLoaderTest`
- **End-to-end engine** - `GameEngineTest` (level transitions, win/lose, AI, abilities)

**Run them in IntelliJ:** right-click the `test/` folder > *Run All Tests*.

**Run from the command line** with the JUnit Platform Console Standalone jar:

```bash
# compile production + test sources against the JUnit jar, then launch the console runner
javac -cp junit-platform-console-standalone.jar -d build \
    $(find src test -name "*.java")
java  -jar junit-platform-console-standalone.jar \
    --class-path build --scan-class-path
```

---

## Design highlights

- **No `instanceof` in the engine.** Movement, combat, friendly-fire prevention, and the corpse
  position-swap are all resolved through double dispatch.
- **Headless, testable core.** Because all output flows through `MessageCallback`, the entire game can
  be driven and asserted on without a console - which is exactly what `GameEngineTest` does.
- **Open for extension.** Adding an enemy is a new `Enemy` subclass + one line in `EnemyFactory`;
  adding a hero is a new `Player` subclass + one line in `PlayerFactory`. No engine changes required.
- **Defensive domain model.** `Health`, `Position`, `GameBoard`, and the cells validate their inputs
  and fail fast with clear exceptions.

---

## License & acknowledgements

Built as an academic **Object-Oriented Programming** assignment. Characters and enemies are themed
after *A Song of Ice and Fire*, *The Lord of the Rings*, and *Harry Potter* and are used here purely
for educational, non-commercial purposes; all rights to those names belong to their respective owners.

No formal open-source license is currently attached to this repository. If you intend to reuse the
code, please contact the author first (or add a `LICENSE` file - MIT is a sensible default).
