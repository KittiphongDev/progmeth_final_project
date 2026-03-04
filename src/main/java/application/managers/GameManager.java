package application.managers;

import application.core.Collectible;
import application.core.Destroyable;
import application.core.GameObject;
import application.entities.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.*;

public class GameManager {
    public enum GameState { MAIN_MENU, PLAYING, PAUSED, GAME_OVER, YOU_WIN, P1_WIN, P2_WIN, DRAW }

    protected GameState currentState;
    protected int currentMode;
    protected List<GameObject> gameObjects;
    protected Map<Point2D, Item.ItemType> hiddenItems = new HashMap<>();
    protected Player player1;
    protected Player player2;
    protected Door hiddenDoor;

    protected final int MAX_COLS = 15;
    protected final int MAX_ROWS = 11;
    protected int gameTimer = 90;
    protected long lastTimeCheck;

    private final InputHandler inputHandler;
    private final SoundManager sm = SoundManager.getInstance();

    public GameManager() {
        gameObjects = new ArrayList<>();
        currentState = GameState.MAIN_MENU;
        inputHandler = new InputHandler(this);
    }

    public void startGame(int mode) {
        this.currentMode = mode;
        this.gameTimer = 90;
        this.lastTimeCheck = System.currentTimeMillis();
        gameObjects.clear();
        currentState = GameState.PLAYING;
        hiddenDoor = null;

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
        int attempts = 0;
        while (solidCount < 30 && attempts < 1000) {
            int rx = (int) (Math.random() * (MAX_COLS - 2)) + 1;
            int ry = (int) (Math.random() * (MAX_ROWS - 2)) + 1;
            if (!isSafe(rx, ry, mode) && !isOccupied(walls, rx, ry)) {
                if (isMapConnected(walls, rx, ry)) {
                    walls.add(new SolidWall(rx, ry));
                    solidCount++;
                }
            }
            attempts++;
        }

        int breakableCount = 0;
        while (breakableCount < 50) {
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
        generateItems(breakables, mode);

        player1 = new Player(1, 1, Color.BLUE, 1);
        gameObjects.add(player1);
        if (mode == 2) {
            player2 = new Player(13, 9, Color.GREEN, 2);
            gameObjects.add(player2);
        }

        sm.playBGM("bg_in_game.mp3");
    }

    private boolean isMapConnected(List<GameObject> currentWalls, int testX, int testY) {
        boolean[][] solidGrid = new boolean[MAX_COLS][MAX_ROWS];
        for (GameObject w : currentWalls) {
            if (w instanceof SolidWall) solidGrid[w.getX()][w.getY()] = true;
        }
        solidGrid[testX][testY] = true;

        int expectedOpen = 0;
        for (int x = 1; x < MAX_COLS - 1; x++) {
            for (int y = 1; y < MAX_ROWS - 1; y++) {
                if (!solidGrid[x][y]) expectedOpen++;
            }
        }

        boolean[][] visited = new boolean[MAX_COLS][MAX_ROWS];
        Queue<Point2D> queue = new LinkedList<>();
        queue.add(new Point2D(1, 1));
        visited[1][1] = true;

        int reachableCount = 0;
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        while (!queue.isEmpty()) {
            Point2D p = queue.poll();
            reachableCount++;
            for (int[] d : dirs) {
                int nx = (int) p.getX() + d[0];
                int ny = (int) p.getY() + d[1];
                if (nx >= 1 && nx < MAX_COLS - 1 && ny >= 1 && ny < MAX_ROWS - 1) {
                    if (!solidGrid[nx][ny] && !visited[nx][ny]) {
                        visited[nx][ny] = true;
                        queue.add(new Point2D(nx, ny));
                    }
                }
            }
        }
        return reachableCount == expectedOpen;
    }

    private void generateItems(List<BreakableWall> breakables, int mode) {
        hiddenItems.clear();
        if (breakables.isEmpty()) return;
        List<BreakableWall> shuffledBoxes = new ArrayList<>(breakables);
        if (hiddenDoor != null) {
            shuffledBoxes.removeIf(box -> box.getX() == hiddenDoor.getX() && box.getY() == hiddenDoor.getY());
        }
        Collections.shuffle(shuffledBoxes);
        int itemCount = (mode == 1) ? 2 : 4;
        List<Item.ItemType> itemPool = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) itemPool.add(Item.ItemType.EXTRA_BOMB);
        for (int i = 0; i < itemCount; i++) itemPool.add(Item.ItemType.FIRE_POWER);
        Collections.shuffle(itemPool);
        for (int i = 0; i < Math.min(itemPool.size(), shuffledBoxes.size()); i++) {
            hiddenItems.put(new Point2D(shuffledBoxes.get(i).getX(), shuffledBoxes.get(i).getY()), itemPool.get(i));
        }
    }

    public void updateGame() {
        if (currentState != GameState.PLAYING) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTimeCheck >= 1000) {
            gameTimer--;
            lastTimeCheck = currentTime;
        }
        if (gameTimer <=  0 && currentMode == 1) {
            gameTimer = 0;
            currentState = GameState.GAME_OVER;
            sm.playVictory();
        }
        else if (gameTimer <=  0 && currentMode == 2)
        {
            gameTimer = 0;
            currentState = GameState.DRAW;
            sm.playVictory();
        }

        List<GameObject> toRemove = new ArrayList<>();
        List<Bomb> readyBombs = new ArrayList<>();

        for (GameObject obj : gameObjects) {
            obj.update();
            if (obj instanceof Bomb b && b.isReadyToExplode()) {
                readyBombs.add(b);
                toRemove.add(b);
                if (b.getOwner() == player1) player1.decreaseActiveBombs();
                else if (player2 != null && b.getOwner() == player2) player2.decreaseActiveBombs();
            }
        }

        for (Bomb b : readyBombs) triggerExplosion(b.getX(), b.getY(), b.getRadius(), toRemove);
        gameObjects.removeAll(toRemove);

        List<GameObject> itemsToRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (obj instanceof Collectible item) {
                boolean collected = false;
                if (player1 != null && player1.getX() == obj.getX() && player1.getY() == obj.getY()) {
                    if (item.onCollect(player1)) collected = true;
                } else if (player2 != null && player2.getX() == obj.getX() && player2.getY() == obj.getY()) {
                    if (item.onCollect(player2)) collected = true;
                }

                if (collected) {
                    itemsToRemove.add(obj);
                    sm.playPop();
                }
            }
        }
        gameObjects.removeAll(itemsToRemove);

        gameObjects.removeIf(obj -> obj instanceof Explosion ex && ex.isFinished());
        checkGameOver();
    }

    private void checkGameOver() {
        if (currentMode == 1) {
            if (!player1.isAlive()) {
                currentState = GameState.GAME_OVER;
                sm.playVictory();
            }
            else if (hiddenDoor != null && player1.getX() == hiddenDoor.getX() && player1.getY() == hiddenDoor.getY()) {
                currentState = GameState.YOU_WIN;
                sm.playVictory();

                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(event -> {
                    gameObjects.remove(player1);
                });
                pause.play();
            }
        } else if (player2 != null) {
            if (!player1.isAlive() && !player2.isAlive()) {
                currentState = GameState.DRAW;
                sm.playVictory();
            }
            else if (!player1.isAlive()){
                currentState = GameState.P2_WIN;
                sm.playVictory();
            }
            else if (!player2.isAlive()) {
                currentState = GameState.P1_WIN;
                sm.playVictory();
            }
        }
    }

    public void triggerExplosion(int centerX, int centerY, int radius, List<GameObject> toRemove) {
        sm.playBoom(); // Trigger the explosion sound exactly when the blast generates

        List<GameObject> newObjects = new ArrayList<>();
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        newObjects.add(new Explosion(centerX, centerY));
        destroyAt(centerX, centerY, toRemove, newObjects);

        for (int[] dir : directions) {
            for (int step = 1; step <= radius; step++) {
                int tx = centerX + (dir[0] * step);
                int ty = centerY + (dir[1] * step);
                boolean stopped = false, hitBreakable = false;

                for (GameObject obj : gameObjects) {
                    if (obj.getX() == tx && obj.getY() == ty) {
                        if (obj instanceof SolidWall) { stopped = true; break; }
                        else if (obj instanceof BreakableWall) { hitBreakable = true; stopped = true; }
                        else if (obj instanceof Player) { ((Player) obj).onDestroy(); }
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
                    Point2D p = new Point2D(obj.getX(), obj.getY());
                    if (hiddenItems.containsKey(p)) {
                        newObjects.add(new Item(obj.getX(), obj.getY(), hiddenItems.get(p)));
                        hiddenItems.remove(p);
                    }
                }
            }
        }
    }

    private boolean isSafe(int x, int y, int mode) {
        boolean p1Area = (x == 1 && y == 1) || (x == 2 && y == 1) || (x == 1 && y == 2);
        boolean p2Area = (mode == 2) && ((x == 13 && y == 9) || (x == 12 && y == 9) || (x == 13 && y == 8));
        return p1Area || p2Area;
    }

    private boolean isOccupied(List<GameObject> list, int x, int y) {
        return list.stream().anyMatch(obj -> obj.getX() == x && obj.getY() == y);
    }

    public void handleInput(KeyCode keyCode) {
        inputHandler.processInput(keyCode);
    }

    public void drawGame(GraphicsContext gc) {
        if (currentState != GameState.MAIN_MENU) {
            gameObjects.stream()
                    .sorted((obj1, obj2) -> Double.compare(obj1.getY(), obj2.getY()))
                    .forEach(obj -> obj.draw(gc));
        }
    }

    public GameState getCurrentState() { return currentState; }
    public void setCurrentState(GameState s) { this.currentState = s; }
    public int getGameTimer() { return gameTimer; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
}
