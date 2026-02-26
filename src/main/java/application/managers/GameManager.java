package application.managers;

import application.core.Collectible;
import application.core.Destroyable;
import application.core.GameObject;
import application.entities.*;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.*;

public class GameManager {
    public enum GameState { MAIN_MENU, PLAYING, PAUSED, GAME_OVER, YOU_WIN, P1_WIN, P2_WIN, DRAW }
    private GameState currentState;
    private int currentMode;
    private List<GameObject> gameObjects;
    private Map<Point2D, Item.ItemType> hiddenItems = new HashMap<>(); // Changed Point to Point2D
    private Player player1;
    private Player player2;
    private Door hiddenDoor;
    private final int MAX_COLS = 15;
    private final int MAX_ROWS = 11;

    private int gameTimer = 90;
    private long lastTimeCheck;

    public GameManager() {
        gameObjects = new ArrayList<>();
        currentState = GameState.MAIN_MENU;
    }

    public void startGame(int mode) {
        this.currentMode = mode;
        this.gameTimer = 90;
        this.lastTimeCheck = System.currentTimeMillis();
        gameObjects.clear();
        currentState = GameState.PLAYING;

        List<GameObject> walls = new ArrayList<>();
        List<BreakableWall> breakables = new ArrayList<>();

        for (int x = 0; x < MAX_COLS; x++) {
            for (int y = 0; y < MAX_ROWS; y++) {
                if (x == 0 || x == MAX_COLS - 1 || y == 0 || y == MAX_ROWS - 1) {
                    walls.add(new SolidWall(x, y));
                }
            }
        }

        int solidCount = 0;
        while (solidCount < 10) {
            int rx = (int) (Math.random() * (MAX_COLS - 2)) + 1;
            int ry = (int) (Math.random() * (MAX_ROWS - 2)) + 1;
            if (!isSafe(rx, ry, mode) && !isOccupied(walls, rx, ry)) {
                walls.add(new SolidWall(rx, ry));
                solidCount++;
            }
        }

        int breakableCount = 0;
        while (breakableCount < 10) {
            int rx = (int) (Math.random() * (MAX_COLS - 2)) + 1;
            int ry = (int) (Math.random() * (MAX_ROWS - 2)) + 1;
            if (!isSafe(rx, ry, mode) && !isOccupied(walls, rx, ry)) {
                BreakableWall bw = new BreakableWall(rx, ry);
                breakables.add(bw);
                walls.add(bw);
                breakableCount++;
            }
        }

        if (mode == 1 && !breakables.isEmpty()) {
            int randomIndex = (int) (Math.random() * breakables.size());
            BreakableWall targetBox = breakables.get(randomIndex);
            hiddenDoor = new Door(targetBox.getX(), targetBox.getY());
            gameObjects.add(hiddenDoor);
        }

        gameObjects.addAll(walls);

        hiddenItems.clear();
        if (breakables.size() > 0) {
            List<BreakableWall> shuffledBoxes = new ArrayList<>(breakables);
            Collections.shuffle(shuffledBoxes);

            int itemCount = (mode == 1) ? 2 : 4;

            List<Item.ItemType> itemPool = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) itemPool.add(Item.ItemType.EXTRA_BOMB);
            for (int i = 0; i < itemCount; i++) itemPool.add(Item.ItemType.FIRE_POWER);
            Collections.shuffle(itemPool);

            int itemsToHide = Math.min(itemPool.size(), breakables.size());
            for (int i = 0; i < itemsToHide; i++) {
                hiddenItems.put(new Point2D(shuffledBoxes.get(i).getX(), shuffledBoxes.get(i).getY()), itemPool.get(i)); // Changed Point to Point2D
            }
        }

        player1 = new Player(1, 1, Color.BLUE);
        gameObjects.add(player1);
        if (mode == 2) {
            player2 = new Player(13, 9, Color.GREEN);
            gameObjects.add(player2);
        }
    }

    private boolean isSafe(int x, int y, int mode) {
        boolean p1Area = (x == 1 && y == 1) || (x == 2 && y == 1) || (x == 1 && y == 2);
        boolean p2Area = (mode == 2) && ((x == 13 && y == 9) || (x == 12 && y == 9) || (x == 13 && y == 8));
        return p1Area || p2Area;
    }

    private boolean isOccupied(List<GameObject> list, int x, int y) {
        for (GameObject obj : list) {
            if (obj.getX() == x && obj.getY() == y) return true;
        }
        return false;
    }

    public void updateGame() {
        if (currentState != GameState.PLAYING) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTimeCheck >= 1000) {
            gameTimer--;
            lastTimeCheck = currentTime;
        }
        if (gameTimer <= 0) { gameTimer = 0; currentState = GameState.DRAW; }

        List<GameObject> toRemove = new ArrayList<>();
        List<Bomb> readyBombs = new ArrayList<>();

        for (GameObject obj : gameObjects) {
            obj.update();

            if (obj instanceof Bomb) {
                Bomb b = (Bomb) obj;
                if (b.isReadyToExplode()) {
                    readyBombs.add(b);
                    toRemove.add(b);

                    if (b.getOwner() == player1) {
                        player1.decreaseActiveBombs();
                    } else if (player2 != null && b.getOwner() == player2) {
                        player2.decreaseActiveBombs();
                    }
                }
            }
        }

        for (Bomb b : readyBombs) {
            triggerExplosion(b.getX(), b.getY(), b.getRadius(), toRemove);
        }
        gameObjects.removeAll(toRemove);

        List<GameObject> itemsToRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (obj instanceof Collectible) {
                Collectible item = (Collectible) obj;
                GameObject gObj = (GameObject) obj;

                if (player1 != null && player1.getX() == gObj.getX() && player1.getY() == gObj.getY()) {
                    if (item.onCollect(player1)) {
                        itemsToRemove.add(gObj);
                    }
                }
                else if (player2 != null && player2.getX() == gObj.getX() && player2.getY() == gObj.getY()) {
                    if (item.onCollect(player2)) {
                        itemsToRemove.add(gObj);
                    }
                }
            }
        }
        gameObjects.removeAll(itemsToRemove);

        List<GameObject> effectsToRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (obj instanceof Explosion && ((Explosion) obj).isFinished()) {
                effectsToRemove.add(obj);
            }
        }
        gameObjects.removeAll(effectsToRemove);

        checkGameOver();
    }

    private void checkGameOver() {
        if (currentMode == 1) {
            if (!player1.isAlive()) currentState = GameState.GAME_OVER;
            else if (hiddenDoor != null && player1.getX() == hiddenDoor.getX() && player1.getY() == hiddenDoor.getY())
                currentState = GameState.YOU_WIN;
        } else if (player2 != null) {
            if (!player1.isAlive() && !player2.isAlive()) currentState = GameState.DRAW;
            else if (!player1.isAlive()) currentState = GameState.P2_WIN;
            else if (!player2.isAlive()) currentState = GameState.P1_WIN;
        }
    }

    // Changed from int keyCode to KeyCode keyCode
    public void handleInput(KeyCode keyCode) {
        if (currentState == GameState.MAIN_MENU) {
            if (keyCode == KeyCode.DIGIT1 || keyCode == KeyCode.NUMPAD1) startGame(1);
            else if (keyCode == KeyCode.DIGIT2 || keyCode == KeyCode.NUMPAD2) startGame(2);
            return;
        }

        if (keyCode == KeyCode.P) {
            if (currentState == GameState.PLAYING) currentState = GameState.PAUSED;
            else if (currentState == GameState.PAUSED) {
                currentState = GameState.PLAYING;
                lastTimeCheck = System.currentTimeMillis();
            }
            return;
        }

        if (currentState != GameState.PLAYING) {
            if (keyCode == KeyCode.R) startGame(currentMode);
            else if (keyCode == KeyCode.M) currentState = GameState.MAIN_MENU;
            return;
        }

        handlePlayerLogic(keyCode);
    }

    // Changed from int keyCode to KeyCode keyCode
    private void handlePlayerLogic(KeyCode keyCode) {
        if (player1 != null && player1.isAlive()) {
            int dx = 0, dy = 0;

            if (keyCode == KeyCode.W) dy = -1;
            else if (keyCode == KeyCode.S) dy = 1;
            else if (keyCode == KeyCode.A) dx = -1;
            else if (keyCode == KeyCode.D) dx = 1;

            else if (keyCode == KeyCode.SPACE) {
                if (player1.canPlaceBomb()) {
                    Bomb newBomb = new Bomb(player1.getX(), player1.getY(), player1.getBombRadius());
                    newBomb.setOwner(player1); // Assuming Bomb has a setOwner method
                    gameObjects.add(newBomb);
                    player1.increaseActiveBombs();
                }
            }

            if (dx != 0 || dy != 0) {
                int targetX = player1.getX() + dx;
                int targetY = player1.getY() + dy;

                if (isValidMove(targetX, targetY)) {
                    player1.setX(targetX);
                    player1.setY(targetY);
                }
            }
        }

        if (player2 != null && player2.isAlive()) {
            int dx = 0, dy = 0;

            if (keyCode == KeyCode.UP) dy = -1;
            else if (keyCode == KeyCode.DOWN) dy = 1;
            else if (keyCode == KeyCode.LEFT) dx = -1;
            else if (keyCode == KeyCode.RIGHT) dx = 1;

            else if (keyCode == KeyCode.ENTER) {
                if (player2.canPlaceBomb()) {
                    Bomb newBomb = new Bomb(player2.getX(), player2.getY(), player2.getBombRadius());
                    newBomb.setOwner(player2); // Assuming Bomb has a setOwner method
                    gameObjects.add(newBomb);
                    player2.increaseActiveBombs();
                }
            }

            if (dx != 0 || dy != 0) {
                int targetX = player2.getX() + dx;
                int targetY = player2.getY() + dy;

                if (isValidMove(targetX, targetY)) {
                    player2.setX(targetX);
                    player2.setY(targetY);
                }
            }
        }
    }

    private boolean isValidMove(int tx, int ty) {
        if (tx < 0 || tx >= MAX_COLS || ty < 0 || ty >= MAX_ROWS) return false;
        for (GameObject obj : gameObjects) {
            if (obj.getX() == tx && obj.getY() == ty) {
                if (obj instanceof SolidWall || obj instanceof BreakableWall || obj instanceof Bomb) return false;
                if (obj instanceof Player && ((Player) obj).isAlive()) return false;
            }
        }
        return true;
    }

    public void triggerExplosion(int centerX, int centerY, int radius, List<GameObject> toRemove) {
        List<GameObject> newObjects = new ArrayList<>();

        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        newObjects.add(new Explosion(centerX, centerY));
        destroyAt(centerX, centerY, toRemove, newObjects);

        for (int[] dir : directions) {
            for (int step = 1; step <= radius; step++) {
                int tx = centerX + (dir[0] * step);
                int ty = centerY + (dir[1] * step);

                boolean stopped = false;
                boolean hitBreakable = false;

                for (GameObject obj : gameObjects) {
                    if (obj.getX() == tx && obj.getY() == ty) {

                        if (obj instanceof SolidWall) {
                            stopped = true;
                            break;
                        }
                        else if (obj instanceof BreakableWall) {
                            hitBreakable = true;
                            stopped = true;
                        }
                        else if (obj instanceof Player) {
                            ((Player) obj).onDestroy();
                        }
                    }
                }

                if (stopped) {
                    if (hitBreakable) {
                        newObjects.add(new Explosion(tx, ty));
                        destroyAt(tx, ty, toRemove, newObjects);
                    }
                    break;
                } else {
                    newObjects.add(new Explosion(tx, ty));
                    destroyAt(tx, ty, toRemove, newObjects);
                }
            }
        }

        gameObjects.addAll(newObjects);
    }

    private void destroyAt(int tx, int ty, List<GameObject> toRemove, List<GameObject> newObjects) {
        for (GameObject obj : gameObjects) {
            if (obj.getX() == tx && obj.getY() == ty && obj instanceof Destroyable) {
                ((Destroyable) obj).onDestroy();

                if (obj instanceof BreakableWall) {
                    toRemove.add(obj);

                    Point2D p = new Point2D(obj.getX(), obj.getY()); // Changed Point to Point2D
                    if (hiddenItems.containsKey(p)) {
                        Item.ItemType type = hiddenItems.get(p);
                        newObjects.add(new Item(obj.getX(), obj.getY(), type));
                        hiddenItems.remove(p);
                    }

                }
            }
        }
    }

    // Changed from Graphics g to GraphicsContext gc
    public void drawGame(GraphicsContext gc) {
        if (currentState != GameState.MAIN_MENU) {
            for (GameObject obj : gameObjects) {
                obj.draw(gc);
            }
        }
    }

    public GameState getCurrentState() { return currentState; }
    public int getGameTimer() { return gameTimer; }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
}