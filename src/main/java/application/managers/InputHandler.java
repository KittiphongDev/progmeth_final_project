package application.managers;

import application.core.GameObject;
import application.entities.*;
import javafx.scene.input.KeyCode;

public class InputHandler {
    private final GameManager gm;

    public InputHandler(GameManager gm) {
        this.gm = gm;
    }

    public void processInput(KeyCode keyCode) {
        if (gm.getCurrentState() == GameManager.GameState.MAIN_MENU) {
            if (keyCode == KeyCode.DIGIT1 || keyCode == KeyCode.NUMPAD1) gm.startGame(1);
            else if (keyCode == KeyCode.DIGIT2 || keyCode == KeyCode.NUMPAD2) gm.startGame(2);
            return;
        }

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

        if (keyCode == KeyCode.M && gm.getCurrentState() == GameManager.GameState.PAUSED) {
            gm.setCurrentState(GameManager.GameState.MAIN_MENU);
            return;
        }

        if (gm.getCurrentState() == GameManager.GameState.PAUSED) return;

        if (gm.getCurrentState() != GameManager.GameState.PLAYING) {
            if (keyCode == KeyCode.R) gm.startGame(gm.currentMode);
            else if (keyCode == KeyCode.M) gm.setCurrentState(GameManager.GameState.MAIN_MENU);
            return;
        }

        handlePlayerMovement(keyCode);
    }

    private void handlePlayerMovement(KeyCode keyCode) {
        // Player 1
        if (gm.player1 != null && gm.player1.isAlive()) {
            if (keyCode == KeyCode.W) {
                gm.player1.setDirection(Player.Direction.BACK); // Facing away
                // your existing move up logic here (e.g., player1.setY(y - 1))
            }
            else if (keyCode == KeyCode.S) {
                gm.player1.setDirection(Player.Direction.FRONT); // Facing forward
                // your existing move down logic here
            }
            else if (keyCode == KeyCode.A) {
                gm.player1.setDirection(Player.Direction.LEFT); // Facing left
                // your existing move left logic here
            }
            else if (keyCode == KeyCode.D) {
                gm.player1.setDirection(Player.Direction.RIGHT); // Facing right
                // your existing move right logic here
            }
            processPlayerAction(gm.player1, keyCode, KeyCode.W, KeyCode.S, KeyCode.A, KeyCode.D, KeyCode.SPACE);
        }
        // Player 2
        if (gm.player2 != null && gm.player2.isAlive()) {
            if (keyCode == KeyCode.UP) {
                gm.player2.setDirection(Player.Direction.BACK); // Facing away
                // your existing move up logic here (e.g., player2.setY(y - 1))
            }
            else if (keyCode == KeyCode.DOWN) {
                gm.player2.setDirection(Player.Direction.FRONT); // Facing forward
                // your existing move down logic here
            }
            else if (keyCode == KeyCode.LEFT) {
                gm.player2.setDirection(Player.Direction.LEFT); // Facing left
                // your existing move left logic here
            }
            else if (keyCode == KeyCode.RIGHT) {
                gm.player2.setDirection(Player.Direction.RIGHT); // Facing right
                // your existing move right logic here
            }
            processPlayerAction(gm.player2, keyCode, KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ENTER);
        }
    }

    private void processPlayerAction(Player p, KeyCode pressed, KeyCode up, KeyCode down, KeyCode left, KeyCode right, KeyCode bombKey) {
        int dx = 0, dy = 0;
        if (pressed == up) dy = -1;
        else if (pressed == down) dy = 1;
        else if (pressed == left) dx = -1;
        else if (pressed == right) dx = 1;
        else if (pressed == bombKey) {
            if (p.canPlaceBomb()) {
                Bomb newBomb = new Bomb(p.getX(), p.getY(), p.getBombRadius());
                newBomb.setOwner(p);
                gm.gameObjects.add(newBomb);
                p.increaseActiveBombs();
            }
        }

        if (dx != 0 || dy != 0) {
            int tx = p.getX() + dx;
            int ty = p.getY() + dy;
            if (isValidMove(tx, ty)) {
                p.setX(tx);
                p.setY(ty);
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