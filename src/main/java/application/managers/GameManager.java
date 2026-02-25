package application.managers;
import java.awt.Point;
import java.util.Collections;

import application.core.Collectible;
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
    private List<Point> itemLocations = new ArrayList<>(); // ✨ เพิ่มตัวแปรนี้
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

        itemLocations.clear();
        if (breakables.size() > 0) {
            // ก๊อปปี้รายชื่อกล่องไม้มาสับสลับที่ (Shuffle)
            List<BreakableWall> shuffledBoxes = new ArrayList<>(breakables);
            Collections.shuffle(shuffledBoxes);

            // เลือกกล่อง 5 ใบแรกที่ถูกสับมาเป็นจุดซ่อนไอเทม
            for (int i = 0; i < 5 && i < shuffledBoxes.size(); i++) {
                itemLocations.add(new Point(shuffledBoxes.get(i).getX(), shuffledBoxes.get(i).getY()));
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

                    // คืนโควตาให้เจ้าของ
                    player1.decreaseActiveBombs();
                    if(player2 != null) player2.decreaseActiveBombs();
                }
            }
        }

        // 3. ทำลายวัตถุจากการระเบิด
        for (Bomb b : readyBombs) {
            triggerExplosion(b.getX(), b.getY(), b.getRadius(), toRemove);
        }
        gameObjects.removeAll(toRemove);

        // 4. เช็คการเก็บไอเทม (Item Collection)
        List<GameObject> itemsToRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (obj instanceof Collectible) {
                Collectible item = (Collectible) obj;
                GameObject gObj = (GameObject) obj;

                // เช็คว่า Player 1 เดินมาทับไหม
                if (player1 != null && player1.getX() == gObj.getX() && player1.getY() == gObj.getY()) {
                    item.onCollect(player1); // สั่งให้ไอเทมทำงาน
                    itemsToRemove.add(gObj); // ✨ แก้เป็น itemsToRemove แล้ว!
                }
                // เช็คว่า Player 2 เดินมาทับไหม
                else if (player2 != null && player2.getX() == gObj.getX() && player2.getY() == gObj.getY()) {
                    item.onCollect(player2);
                    itemsToRemove.add(gObj); // ✨ แก้เป็น itemsToRemove แล้ว!
                }
            }
        }
        gameObjects.removeAll(itemsToRemove);

        // 5. ลบเอฟเฟกต์ไฟเมื่อหมดเวลา
        List<GameObject> effectsToRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (obj instanceof Explosion && ((Explosion) obj).isFinished()) {
                effectsToRemove.add(obj);
            }
        }
        gameObjects.removeAll(effectsToRemove);

        // 6. เช็คสถานะจบเกม
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
        // 1. ถ้าอยู่หน้าเมนู
        if (currentState == GameState.MAIN_MENU) {
            if (keyCode == KeyEvent.VK_1) startGame(1);
            else if (keyCode == KeyEvent.VK_2) startGame(2);
            return;
        }

        // 2. ปุ่ม Pause
        if (keyCode == KeyEvent.VK_P) {
            if (currentState == GameState.PLAYING) currentState = GameState.PAUSED;
            else if (currentState == GameState.PAUSED) {
                currentState = GameState.PLAYING;
                lastTimeCheck = System.currentTimeMillis();
            }
            return;
        }

        // 3. ถ้าไม่ได้เล่นอยู่ (เช่น จบเกม) ให้ดักปุ่ม R กับ M
        if (currentState != GameState.PLAYING) {
            if (keyCode == KeyEvent.VK_R) startGame(currentMode);
            else if (keyCode == KeyEvent.VK_M) currentState = GameState.MAIN_MENU;
            return;
        }

        // 4. ถ้ากำลังเล่นอยู่ (PLAYING) ค่อยให้ขยับและวางระเบิดได้
        handlePlayerLogic(keyCode);
    }

    private void handlePlayerLogic(int keyCode) {
        // --- ควบคุม Player 1 ---
        if (player1 != null && player1.isAlive()) {
            int dx = 0, dy = 0; // ทิศทางที่จะไป

            // กำหนดทิศทางจากปุ่มกด
            if (keyCode == KeyEvent.VK_W) dy = -1;
            else if (keyCode == KeyEvent.VK_S) dy = 1;
            else if (keyCode == KeyEvent.VK_A) dx = -1;
            else if (keyCode == KeyEvent.VK_D) dx = 1;

                // การวางระเบิด
            else if (keyCode == KeyEvent.VK_SPACE) {
                if (player1.canPlaceBomb()) {
                    gameObjects.add(new Bomb(player1.getX(), player1.getY(), player1.getBombRadius()));
                    player1.increaseActiveBombs();
                }
            }

            // ✨ ระบบก้าวเท้าทีละช่อง ตามไอเดียของคุณเลยครับ!
            if (dx != 0 || dy != 0) {
                int moveX = 0, moveY = 0;
                // ลูปเช็คทางเดินข้างหน้าทีละช่อง ตามความเร็ว (Speed) ที่มี
                for (int s = 1; s <= player1.getSpeed(); s++) {
                    if (isValidMove(player1.getX() + (dx * s), player1.getY() + (dy * s))) {
                        // ถ้าช่องนั้นว่าง ให้จำระยะทางไว้
                        moveX = dx * s;
                        moveY = dy * s;
                    } else {
                        // ถ้าเจอช่องที่ไม่ว่าง (ชนกำแพง/ระเบิด) ให้หยุดเช็คทันที!
                        break;
                    }
                }
                // ขยับตัวไปตามระยะทางที่ไกลที่สุดที่เดินได้
                player1.setX(player1.getX() + moveX);
                player1.setY(player1.getY() + moveY);
            }
        }

        // --- ควบคุม Player 2 ---
        if (player2 != null && player2.isAlive()) {
            int dx = 0, dy = 0;

            if (keyCode == KeyEvent.VK_UP) dy = -1;
            else if (keyCode == KeyEvent.VK_DOWN) dy = 1;
            else if (keyCode == KeyEvent.VK_LEFT) dx = -1;
            else if (keyCode == KeyEvent.VK_RIGHT) dx = 1;

            else if (keyCode == KeyEvent.VK_ENTER) {
                if (player2.canPlaceBomb()) {
                    gameObjects.add(new Bomb(player2.getX(), player2.getY(), player2.getBombRadius()));
                    player2.increaseActiveBombs();
                }
            }

            if (dx != 0 || dy != 0) {
                int moveX = 0, moveY = 0;
                for (int s = 1; s <= player2.getSpeed(); s++) {
                    if (isValidMove(player2.getX() + (dx * s), player2.getY() + (dy * s))) {
                        moveX = dx * s;
                        moveY = dy * s;
                    } else {
                        break;
                    }
                }
                player2.setX(player2.getX() + moveX);
                player2.setY(player2.getY() + moveY);
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

    // เมธอดหลัก: จัดการการระเบิดและเสกเอฟเฟกต์ไฟ
    public void triggerExplosion(int centerX, int centerY, int radius, List<GameObject> toRemove) {
        // ใช้ลิสต์เดียวเก็บทั้ง Item ที่ดรอป และ Explosion เอฟเฟกต์ไฟ
        List<GameObject> newObjects = new ArrayList<>();

        // ทิศทาง: ขวา, ซ้าย, ลง, ขึ้น
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        // 1. จัดการจุดศูนย์กลางระเบิดก่อน
        newObjects.add(new Explosion(centerX, centerY));
        destroyAt(centerX, centerY, toRemove, newObjects);

        // 2. ปล่อยไฟพุ่งออกไป 4 ทิศทาง
        for (int[] dir : directions) {
            // ก้าวไปทีละช่องตามระยะความแรง (radius)
            for (int step = 1; step <= radius; step++) {
                int tx = centerX + (dir[0] * step);
                int ty = centerY + (dir[1] * step);

                boolean stopped = false;      // เช็คว่าไฟต้องหยุดไหม
                boolean hitBreakable = false; // เช็คว่าไฟชนกล่องไม้ไหม

                // ตรวจสอบว่าในช่องพิกัด (tx, ty) มีวัตถุอะไรอยู่บ้าง
                for (GameObject obj : gameObjects) {
                    if (obj.getX() == tx && obj.getY() == ty) {

                        if (obj instanceof SolidWall) {
                            stopped = true; // ชนกำแพงเหล็ก ไฟหยุดสนิท (ไม่วาดไฟทับกำแพงเหล็ก)
                            break;
                        }
                        else if (obj instanceof BreakableWall) {
                            hitBreakable = true;
                            stopped = true; // ชนกล่องไม้ ทำลายได้ แต่ไฟต้องหยุดทะลวงต่อ
                        }
                        else if (obj instanceof Player) {
                            ((Player) obj).onDestroy(); // ผู้เล่นโดนไฟระเบิดตาย
                        }
                    }
                }

                // ตัดสินใจวาดไฟและพังของตามสถานะการชน
                if (stopped) {
                    if (hitBreakable) {
                        newObjects.add(new Explosion(tx, ty)); // วาดเอฟเฟกต์ไฟทับตำแหน่งกล่องไม้
                        destroyAt(tx, ty, toRemove, newObjects); // สั่งพังกล่องและดรอปของ
                    }
                    break; // หยุดการทำงานของทิศทางนี้ทันที (ไม่เช็ค step ถัดไป)
                } else {
                    // ถ้าช่องนั้นว่าง (ไม่ชนกำแพง/กล่อง) ให้วาดเอฟเฟกต์ไฟไปเรื่อยๆ
                    newObjects.add(new Explosion(tx, ty));
                    // (เผื่อมีผู้เล่นยืนอยู่ จะได้ตายด้วย)
                    destroyAt(tx, ty, toRemove, newObjects);
                }
            }
        }

        // แอดทั้งเอฟเฟกต์ไฟและไอเทมเข้าสู่ระบบเกมหลัก
        gameObjects.addAll(newObjects);
    }

    // เมธอดผู้ช่วย: สั่งพังของและดรอปไอเทมในพิกัดที่กำหนด
    private void destroyAt(int tx, int ty, List<GameObject> toRemove, List<GameObject> newObjects) {
        for (GameObject obj : gameObjects) {
            if (obj.getX() == tx && obj.getY() == ty && obj instanceof Destroyable) {
                ((Destroyable) obj).onDestroy(); // สั่งให้วัตถุพัง/ตาย

                if (obj instanceof BreakableWall) {
                    toRemove.add(obj); // เอากล่องไม้ออก

                    // เช็คพิกัดว่ากล่องใบนี้มีไอเทมซ่อนอยู่ไหม
                    Point p = new Point(obj.getX(), obj.getY());
                    if (itemLocations.contains(p)) {
                        // สุ่มประเภทไอเทมที่จะดรอป
                        Item.ItemType[] types = Item.ItemType.values();
                        Item.ItemType randomType = types[(int)(Math.random() * types.length)];

                        newObjects.add(new Item(obj.getX(), obj.getY(), randomType));
                        itemLocations.remove(p); // เอาพิกัดออกเพื่อไม่ให้ดรอปซ้ำ
                    }
                }
            }
        }
    }

    public void drawGame(Graphics g) { if (currentState != GameState.MAIN_MENU) for (GameObject obj : gameObjects) obj.draw(g); }
    public GameState getCurrentState() { return currentState; }
    public int getGameTimer() { return gameTimer; }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
}