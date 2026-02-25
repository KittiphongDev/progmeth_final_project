package application.managers;

import application.core.Destroyable;
import application.core.GameObject;
import application.entities.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class GameManager {
    // ✨ ต้องมี PAUSED ตรงนี้ด้วย
    public enum GameState { MAIN_MENU, PLAYING, PAUSED, GAME_OVER, YOU_WIN, P1_WIN, P2_WIN, DRAW }
    private GameState currentState;
    private int currentMode;
    private List<GameObject> gameObjects;
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

        // 1. ระบบเวลา
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTimeCheck >= 1000) {
            gameTimer--;
            lastTimeCheck = currentTime;
        }
        if (gameTimer <= 0) { gameTimer = 0; currentState = GameState.DRAW; }

        List<GameObject> toRemove = new ArrayList<>();
        List<Bomb> readyBombs = new ArrayList<>();

        // 2. อัปเดตวัตถุ และเช็คระเบิด
        for (GameObject obj : gameObjects) {
            obj.update(); // ✨ ต้องมีบรรทัดนี้ เพื่อให้เวลาในระเบิดเดิน!

            if (obj instanceof Bomb) {
                Bomb b = (Bomb) obj;
                if (b.isReadyToExplode()) {
                    readyBombs.add(b);
                    toRemove.add(b);

                    // คืนโควตาให้เจ้าของ (แบบชั่วคราว: เช็คว่าใครอยู่ใกล้ระเบิดที่สุดตอนนั้น หรือคืนให้ทั้งคู่ถ้าทำระบบง่ายๆ)
                    // หรือถ้าคุณแก้ Class Bomb ให้เก็บ owner แล้ว ให้ใช้: b.getOwner().decreaseActiveBombs();
                    player1.decreaseActiveBombs();
                    if(player2 != null) player2.decreaseActiveBombs();
                }
            }
        }

        // 3. ทำลายวัตถุจากการระเบิด
        for (Bomb b : readyBombs) triggerExplosion(b.getX(), b.getY(), toRemove);
        gameObjects.removeAll(toRemove);

        // 4. เช็คการเก็บไอเทม (Item Collection)
        List<GameObject> itemsToRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (obj instanceof Item) {
                Item item = (Item) obj;
                if (player1.getX() == item.getX() && player1.getY() == item.getY()) {
                    applyItem(player1, item);
                    itemsToRemove.add(item);
                } else if (player2 != null && player2.getX() == item.getX() && player2.getY() == item.getY()) {
                    applyItem(player2, item);
                    itemsToRemove.add(item);
                }
            }
        }
        gameObjects.removeAll(itemsToRemove);

        // 5. เช็คสถานะจบเกม
        checkGameOver();
    }
    private void checkGameOver() {
        if (currentMode == 1) {
            if (!player1.isAlive()) currentState = GameState.GAME_OVER;
            else if (hiddenDoor != null && player1.getX() == hiddenDoor.getX() && player1.getY() == hiddenDoor.getY())
                currentState = GameState.YOU_WIN;
        } else if (player2 != null) { // กรณีเล่น 2 คน
            if (!player1.isAlive() && !player2.isAlive()) currentState = GameState.DRAW;
            else if (!player1.isAlive()) currentState = GameState.P2_WIN;
            else if (!player2.isAlive()) currentState = GameState.P1_WIN;
        }
    }
    public void handleInput(int keyCode) {
        // ในส่วน handleInput (หรือ handlePlayerLogic) ของ GameManager
        if (keyCode == KeyEvent.VK_SPACE) {
            if (player1.canPlaceBomb()) {
                // สร้างระเบิดโดยส่งรัศมีของ Player 1 ไปด้วย
                Bomb newBomb = new Bomb(player1.getX(), player1.getY(), player1.getBombRadius());
                gameObjects.add(newBomb);
                player1.increaseActiveBombs(); // นับว่าวางเพิ่ม 1 ลูก
            }
        }

        if (currentState == GameState.MAIN_MENU) {
            if (keyCode == KeyEvent.VK_1) startGame(1);
            else if (keyCode == KeyEvent.VK_2) startGame(2);
            return;
        }

        // ปุ่ม Pause
        if (keyCode == KeyEvent.VK_P) {
            if (currentState == GameState.PLAYING) currentState = GameState.PAUSED;
            else if (currentState == GameState.PAUSED) {
                currentState = GameState.PLAYING;
                lastTimeCheck = System.currentTimeMillis();
            }
            return;
        }

        if (currentState != GameState.PLAYING) {
            if (keyCode == KeyEvent.VK_R) startGame(currentMode);
            else if (keyCode == KeyEvent.VK_M) currentState = GameState.MAIN_MENU;
            return;
        }

        // ควบคุม Player (เหมือนเดิม)
        handlePlayerLogic(keyCode);
    }

    private void handlePlayerLogic(int keyCode) {
        if (player1 != null && player1.isAlive()) {
            int nx = player1.getX(), ny = player1.getY();
            if (keyCode == KeyEvent.VK_W) ny--;
            else if (keyCode == KeyEvent.VK_S) ny++;
            else if (keyCode == KeyEvent.VK_A) nx--;
            else if (keyCode == KeyEvent.VK_D) nx++;
            else if (keyCode == KeyEvent.VK_SPACE) gameObjects.add(new Bomb(player1.getX(), player1.getY(), player1.getBombRadius()));
            if (isValidMove(nx, ny)) { player1.setX(nx); player1.setY(ny); }
        }
        if (player2 != null && player2.isAlive()) {
            int nx = player2.getX(), ny = player2.getY();
            if (keyCode == KeyEvent.VK_UP) ny--;
            else if (keyCode == KeyEvent.VK_DOWN) ny++;
            else if (keyCode == KeyEvent.VK_LEFT) nx--;
            else if (keyCode == KeyEvent.VK_RIGHT) nx++;
            else if (keyCode == KeyEvent.VK_ENTER) gameObjects.add(new Bomb(player2.getX(), player2.getY(), player1.getBombRadius()));
            if (isValidMove(nx, ny)) { player2.setX(nx); player2.setY(ny); }
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

    public void triggerExplosion(int cx, int cy, List<GameObject> toRemove) {
        int[][] dirs = {{0,0}, {1,0}, {-1,0}, {0,1}, {0,-1}};
        List<Item> itemsToAdd = new ArrayList<>(); // ✨ เพิ่มบรรทัดนี้!

        for (int[] d : dirs) {
            int tx = cx + d[0], ty = cy + d[1];
            for (GameObject obj : gameObjects) {
                if (obj.getX() == tx && obj.getY() == ty && obj instanceof Destroyable) {
                    ((Destroyable) obj).onDestroy();
                    if (obj instanceof BreakableWall) {
                        toRemove.add(obj);
                        if (Math.random() < 0.3) {
                            itemsToAdd.add(new Item(obj.getX(), obj.getY(), Item.ItemType.values()[(int)(Math.random()*3)]));
                        }
                    }
                }
            }
        }
        gameObjects.addAll(itemsToAdd);
    }

    public void drawGame(Graphics g) { if (currentState != GameState.MAIN_MENU) for (GameObject obj : gameObjects) obj.draw(g); }
    public GameState getCurrentState() { return currentState; }
    public int getGameTimer() { return gameTimer; }

    private void applyItem(Player p, Item item) {
        switch (item.getType()) {
            case EXTRA_BOMB: p.addMaxBombs(); break;
            case FIRE_POWER: p.addRadius(); break;
            case SPEED: /* เพิ่มระบบความเร็วที่นี่ */ break;
        }
    }
}