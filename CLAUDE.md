# CLAUDE.md

MyFirstUPBGame is a small dungeon-exploration game used as a first programming exercise. Students only edit three methods in `src/juego/MyFirstUPBGame.java`; everything else is the engine. README.md describes the game rules and the student API in detail. Read it before changing game behavior.

## Hard rule: UPBGame library only, never Swing/AWT

- Do not import or use `javax.swing.*`, `java.awt.*` (including `JOptionPane`, `JFrame`, `Color`, `KeyEvent`, `Timer`...), or JavaFX anywhere in `src/`.
- All UI, input, timing, sound and storage go through the interfaces in `lib/upb-game.jar`, package `edu.upb.lp.game.core`:
  - `GameController`: callbacks (`onKeyPressed`, `onButtonPressed`, `onCellPressed`, `initialiseInterface`, `setLibrary`)
  - `GraphicsLibrary`: grid, cell images/text, buttons, top labels (`setLabel`)
  - `MessagesLibrary`: `showMessage`, `showTemporaryMessage`, `askText`
  - `TimeLibrary`: `executeLater`, `executeRepeatedly`, `stopLoop` (use instead of threads or `javax.swing.Timer`)
  - `SoundLibrary`, `StorageLibrary`
- `edu.upb.lp.game.internal` (`SwingWindow`, `MainSwingLibrary`...) is the backend. Only `LaunchMyFirstUPBGame` may reference `MainSwingLibrary`, to wire things up. Nothing else imports `internal`.
- If the library cannot do something, say so and stop. Do not work around it with Swing, reflection, or by casting to internal classes. Changes to the library belong in the UPB-Game-Swing repo, not here; `lib/upb-game.jar` is not edited by hand.
- The library's sources and javadoc are inside the jar: `unzip -p lib/upb-game.jar edu/upb/lp/game/core/GraphicsLibrary.java`.

## Layout

- `src/juego/`: student-facing. `MyFirstUPBGame.java` (`crearMapa`, `construirMundo`, `getPersonaje`) and the enums `Terreno`, `Elemento`, `Evento`, `Personaje`. Each `Elemento`/`Terreno`/`Personaje` maps to an image name in `resources/images/` (no extension).
- `src/base/`: `MyFirstUPBGameBase` (the whole engine: validation, movement, events, fog of war, drawing) and `LaunchMyFirstUPBGame` (entry point).
- `resources/images/`: PNG sprites. Adding an element means adding the PNG and an enum constant pointing to it.
- `lib/upb-game.jar`: the UPBGame library (binary dependency).

## Conventions

- Code, comments, identifiers and all player-facing text are in Spanish **without accents or ñ** (`anadirElemento`, `Corrigelo`), to avoid encoding problems on student machines. README.md is in English.
- Coordinates are always `(v, h)`: `v` = row (0-13, top to bottom), `h` = column (0-27). Grid is 14 x 28, player starts at (1,1).
- Keep the student API small and beginner-readable: `protected` helpers in `MyFirstUPBGameBase`, no generics, lambdas or advanced Java in anything students see or call.
- Mistakes students can make (bad coordinates, element on a wall, contradictory events, border gaps...) must throw `IllegalArgumentException`/`IllegalStateException` with a clear Spanish message. `mostrarError` shows it in the window with the offending line of `MyFirstUPBGame.java`. Do not let them fail silently or as raw exceptions.
- Must compile with Java 8 (`--release 8`): no `var`, records, switch expressions, text blocks, `List.of`, etc.
- When game rules or the student API change, update README.md (and the comments in `MyFirstUPBGame.java` / `MyFirstUPBGameBase`) in the same change.
- `MyFirstUPBGame.java` is the students' starting template: keep it nearly empty (border walls, a small example, TODO comments). Do not commit a test dungeon in it.

## Build and run

No build tool and no automated tests. From the project root (Git Bash on Windows):

```bash
javac -encoding UTF-8 -cp lib/upb-game.jar -d bin src/base/*.java src/juego/*.java
java -cp "bin;lib/upb-game.jar" base.LaunchMyFirstUPBGame     # ':' instead of ';' on macOS/Linux
```

Run from the project root so `resources/images` is found. `./build.sh` produces `MyFirstUPBGame.jar`; `./package.sh` produces a self-contained app in `dist/` (needs JDK 17+ with jpackage). Verify changes by compiling and, for behavior changes, running the game.
