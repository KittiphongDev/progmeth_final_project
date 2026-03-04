package application.managers;

import application.core.GameObject;
import application.entities.*;
import javafx.scene.input.KeyCode;

public class InputHandler {
    private final GameManager gm;
    private final SoundManager sm = SoundManager.getInstance();

    public InputHandler(GameManager gm) {
        this.gm = gm;
    }

    public void processInput(KeyCode keyCode) {
        // 1. Main Menu Logic
        if (gm.getCurrentState() == GameManager.GameState.MAIN_MENU) {
            if (keyCode == KeyCode.DIGIT1 || keyCode == KeyCode.NUMPAD1) {
                gm.startGame(1);
                sm.playBGM("bg_in_game.mp3");
            } else if (keyCode == KeyCode.DIGIT2 || keyCode == KeyCode.NUMPAD2) {
                gm.startGame(2);
                sm.playBGM("bg_in_game.mp3");
            }
            return;
        }

        // 2. Universal Navigation (Back to Menu / Pause)
        if (keyCode == KeyCode.M && (gm.getCurrentState() == GameManager.GameState.PAUSED ||
                gm.getCurrentState() == GameManager.GameState.GAME_OVER ||
                gm.getCurrentState() == GameManager.GameState.YOU_WIN ||
                gm.getCurrentState() == GameManager.GameState.P1_WIN ||
                gm.getCurrentState() == GameManager.GameState.P2_WIN ||
                gm.getCurrentState() == GameManager.GameState.DRAW)) {
            gm.setCurrentState(GameManager.GameState.MAIN_MENU);
            sm.playBGM("bg_menu.mp3");
            return;
        }

        // 3. Pause / Resume Logic
        if (keyCode == KeyCode.ESCAPE) {
            if (gm.getCurrentState() == GameManager.GameState.PLAYING)
                gm.setCurrentState(GameManager.GameState.PAUSED);
            return;
        }

        if (keyCode == KeyCode.ENTER && gm.getCurrentState() == GameManager.GameState.PAUSED) {
            gm.setCurrentState(GameManager.GameState.PLAYING);
            gm.lastTimeCheck = System.currentTimeMillis();
            return;
        }

        if (gm.getCurrentState() == GameManager.GameState.PAUSED) return;

        // 4. Non-Playing States (Restart Logic)
        if (gm.getCurrentState() != GameManager.GameState.PLAYING) {
            if (keyCode == KeyCode.R) {
                gm.startGame(gm.currentMode);
                sm.playBGM("bg_in_game.mp3");
            }
            return;
        }

        // 5. Active Gameplay Logic
        handlePlayerMovement(keyCode);
    }

    private void handlePlayerMovement(KeyCode keyCode) {
        // Player 1 (WASD)
        if (gm.player1 != null && gm.player1.isAlive()) {
            if (keyCode == KeyCode.W) gm.player1.setDirection(Player.Direction.BACK);
            else if (keyCode == KeyCode.S) gm.player1.setDirection(Player.Direction.FRONT);
            else if (keyCode == KeyCode.A) gm.player1.setDirection(Player.Direction.LEFT);
            else if (keyCode == KeyCode.D) gm.player1.setDirection(Player.Direction.RIGHT);

            processPlayerAction(gm.player1, keyCode, KeyCode.W, KeyCode.S, KeyCode.A, KeyCode.D, KeyCode.SPACE);
        }

        // Player 2 (Arrows)
        if (gm.player2 != null && gm.player2.isAlive()) {
            if (keyCode == KeyCode.UP) gm.player2.setDirection(Player.Direction.BACK);
            else if (keyCode == KeyCode.DOWN) gm.player2.setDirection(Player.Direction.FRONT);
            else if (keyCode == KeyCode.LEFT) gm.player2.setDirection(Player.Direction.LEFT);
            else if (keyCode == KeyCode.RIGHT) gm.player2.setDirection(Player.Direction.RIGHT);

            processPlayerAction(gm.player2, keyCode, KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ENTER);
        }
    }

    private void processPlayerAction(Player p, KeyCode pressed, KeyCode up, KeyCode down, KeyCode left, KeyCode right, KeyCode bombKey) {
        int dx = 0, dy = 0;
        boolean moving = false;

        if (pressed == up) { dy = -1; moving = true; }
        else if (pressed == down) { dy = 1; moving = true; }
        else if (pressed == left) { dx = -1; moving = true; }
        else if (pressed == right) { dx = 1; moving = true; }
        else if (pressed == bombKey) {
            if (p.canPlaceBomb()) {
                Bomb newBomb = new Bomb(p.getX(), p.getY(), p.getBombRadius());
                newBomb.setOwner(p);
                gm.gameObjects.add(newBomb);
                p.increaseActiveBombs();
                sm.playPop();
            }
        }

        if (moving) {
            int tx = p.getX() + dx;
            int ty = p.getY() + dy;
            if (isValidMove(tx, ty)) {
                p.setX(tx);
                p.setY(ty);
                sm.playFootstep();
            }
        }
    }

    private boolean isValidMove(int tx, int ty) {
        if (tx < 0 || tx >= gm.MAX_COLS || ty < 0 || ty >= gm.MAX_ROWS) return false;
        for (GameObject obj : gm.gameObjects) {
            if (obj.getX() == tx && obj.getY() == ty) {
                if (obj instanceof SolidWall || obj instanceof BreakableWall || obj instanceof Bomb) return false;
                if (obj instanceof Player player && player.isAlive()) return false;
            }
        }
        return true;
    }
}