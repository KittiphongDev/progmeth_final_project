package application.managers;

import application.core.Destroyable;
import application.core.GameObject;
import application.entities.Bomb;
import application.entities.BreakableWall;
import application.entities.Player;
import application.entities.SolidWall;
import application.entities.Door;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class GameManager {
    // ✨ เพิ่ม MAIN_MENU และรวมสถานะจบเกมทั้งหมดไว้ด้วยกัน
    public enum GameState { MAIN_MENU, PLAYING, GAME_OVER, YOU_WIN, P1_WIN, P2_WIN, DRAW }
    private GameState currentState;
    private int currentMode; // 1 = โหมดคนเดียว, 2 = โหมดต่อสู้

    private List<GameObject> gameObjects;
    private Player player1;
    private Player player2;
    private Door hiddenDoor;

    private final int MAX_COLS = 15;
    private final int MAX_ROWS = 11;

    public GameManager() {
        gameObjects = new ArrayList<>();
// ✨ เริ่มต้นโปรแกรมให้เข้าหน้า Menu ก่อนเลย
        currentState = GameState.MAIN_MENU;
    }

    // ✨ เมธอดใหม่สำหรับเริ่มเกมตามโหมดที่เลือก
// 1. เมธอดเริ่มเกม (แก้ไขใหม่ให้เป็น Java ที่ถูกต้อง)
    public void startGame(int mode) {
        this.currentMode = mode;
        gameObjects.clear();
        currentState = GameState.PLAYING;

        List<GameObject> walls = new ArrayList<>();
        List<BreakableWall> breakables = new ArrayList<>();

        // สร้างขอบสนาม
        for (int x = 0; x < MAX_COLS; x++) {
            for (int y = 0; y < MAX_ROWS; y++) {
                if (x == 0 || x == MAX_COLS - 1 || y == 0 || y == MAX_ROWS - 1) {
                    walls.add(new SolidWall(x, y));
                }
            }
        }

        // สุ่มวาง SolidWall (กำแพงเหล็ก) 10 ตำแหน่ง
        int solidCount = 0;
        while (solidCount < 10) {
            int rx = (int) (Math.random() * (MAX_COLS - 2)) + 1;
            int ry = (int) (Math.random() * (MAX_ROWS - 2)) + 1;

            if (!isSafe(rx, ry, mode) && !isOccupied(walls, rx, ry)) {
                walls.add(new SolidWall(rx, ry));
                solidCount++;
            }
        }

        // สุ่มวาง BreakableWall (กล่องไม้) 10 ตำแหน่ง
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

        // ซ่อนประตู (โหมด 1 คน)
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

    // 2. เมธอดช่วยตรวจสอบพื้นที่ปลอดภัย (Safe Start)
    private boolean isSafe(int x, int y, int mode) {
        // รอบตัว Player 1 (1,1)
        boolean p1Area = (x == 1 && y == 1) || (x == 2 && y == 1) || (x == 1 && y == 2);

        // รอบตัว Player 2 (13,9) - เช็คเฉพาะเมื่อเล่นโหมด 2 คน
        boolean p2Area = (mode == 2) && ((x == 13 && y == 9) || (x == 12 && y == 9) || (x == 13 && y == 8));

        return p1Area || p2Area;
    }

    // 3. เมธอดช่วยเช็คพิกัดซ้ำ
    private boolean isOccupied(List<GameObject> list, int x, int y) {
        for (GameObject obj : list) {
            if (obj.getX() == x && obj.getY() == y) return true;
        }
        return false;
    }

    public GameState getCurrentState() { return currentState; }

    public void updateGame() {
        if (currentState != GameState.PLAYING) return;

        List<GameObject> toRemove = new ArrayList<>();
        List<Bomb> readyBombs = new ArrayList<>();

        for (GameObject obj : gameObjects) {
            obj.update();
            if (obj instanceof Bomb && ((Bomb) obj).isReadyToExplode()) {
                readyBombs.add((Bomb) obj);
                toRemove.add(obj);
            }
        }

        for (Bomb b : readyBombs) triggerExplosion(b.getX(), b.getY(), toRemove);
        gameObjects.removeAll(toRemove);

// ✨ ตรวจสอบเงื่อนไขการจบเกมแยกตามโหมด
        if (currentMode == 1) {
            if (!player1.isAlive()) {
                currentState = GameState.GAME_OVER;
            } else if (hiddenDoor != null && player1.getX() == hiddenDoor.getX() && player1.getY() == hiddenDoor.getY()) {
                currentState = GameState.YOU_WIN;
            }
        } else if (currentMode == 2) {
            if (!player1.isAlive() && !player2.isAlive()) currentState = GameState.DRAW;
            else if (!player1.isAlive()) currentState = GameState.P2_WIN;
            else if (!player2.isAlive()) currentState = GameState.P1_WIN;
        }
    }

    public void drawGame(Graphics g) {
// วาดวัตถุเกมเฉพาะตอนเล่นหรือตอนจบเกม
        if (currentState != GameState.MAIN_MENU) {
            for (GameObject obj : gameObjects) obj.draw(g);
        }
    }

    public void triggerExplosion(int centerX, int centerY, List<GameObject> toRemove) {
        int[][] directions = {{0,0}, {1,0}, {-1,0}, {0,1}, {0,-1}};
        for (int[] dir : directions) {
            int targetX = centerX + dir[0];
            int targetY = centerY + dir[1];
            for (GameObject obj : gameObjects) {
                if (obj.getX() == targetX && obj.getY() == targetY && obj instanceof Destroyable) {
                    ((Destroyable) obj).onDestroy();
                    if (obj instanceof BreakableWall) toRemove.add(obj);
                }
            }
        }
    }

    private boolean isValidMove(int targetX, int targetY) {
        // 1. เช็คขอบสนาม (ป้องกันเดินออกนอกจอ)
        if (targetX < 0 || targetX >= MAX_COLS || targetY < 0 || targetY >= MAX_ROWS) return false;

        for (GameObject obj : gameObjects) {
            // เช็คตำแหน่งวัตถุในพิกัดเป้าหมาย
            if (obj.getX() == targetX && obj.getY() == targetY) {

                // 2. ห้ามเดินผ่าน กำแพงเหล็ก และ กล่องไม้
                if (obj instanceof SolidWall || obj instanceof BreakableWall) {
                    return false;
                }

                // 3. ✨ ห้ามเดินทับระเบิด
                if (obj instanceof Bomb) {
                    return false;
                }

                // 4. ✨ ห้ามเดินซ้อนทับผู้เล่นคนอื่น (เฉพาะโหมด 2 คน)
                if (obj instanceof Player) {
                    // เราจะเช็คสถานะว่าผู้เล่นคนนั้นยังไม่ตาย ถึงจะนับเป็นสิ่งกีดขวาง
                    if (((Player) obj).isAlive()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void handleInput(int keyCode) {
// ✨ ดักปุ่มกดตอนอยู่หน้า Menu
        if (currentState == GameState.MAIN_MENU) {
            if (keyCode == KeyEvent.VK_1) startGame(1); // กด 1 เล่นคนเดียว
            else if (keyCode == KeyEvent.VK_2) startGame(2); // กด 2 เล่นสองคน
            return;
        }

// ✨ ดักปุ่มตอนเกมจบ (เพิ่มปุ่ม M กลับเมนูหลัก)
        if (currentState != GameState.PLAYING) {
            if (keyCode == KeyEvent.VK_R) startGame(currentMode); // กด R เล่นใหม่โหมดเดิม
            else if (keyCode == KeyEvent.VK_M) currentState = GameState.MAIN_MENU; // กด M กลับหน้าแรก
            return;
        }

// --- ควบคุม Player 1 ---
        if (player1 != null && player1.isAlive()) {
            int p1NextX = player1.getX(), p1NextY = player1.getY();
            if (keyCode == KeyEvent.VK_W) p1NextY--;
            else if (keyCode == KeyEvent.VK_S) p1NextY++;
            else if (keyCode == KeyEvent.VK_A) p1NextX--;
            else if (keyCode == KeyEvent.VK_D) p1NextX++;
            else if (keyCode == KeyEvent.VK_SPACE) gameObjects.add(new Bomb(player1.getX(), player1.getY()));

            if (isValidMove(p1NextX, p1NextY)) { player1.setX(p1NextX); player1.setY(p1NextY); }
        }

// --- ควบคุม Player 2 ---
        if (player2 != null && player2.isAlive()) {
            int p2NextX = player2.getX(), p2NextY = player2.getY();
            if (keyCode == KeyEvent.VK_UP) p2NextY--;
            else if (keyCode == KeyEvent.VK_DOWN) p2NextY++;
            else if (keyCode == KeyEvent.VK_LEFT) p2NextX--;
            else if (keyCode == KeyEvent.VK_RIGHT) p2NextX++;
            else if (keyCode == KeyEvent.VK_ENTER) gameObjects.add(new Bomb(player2.getX(), player2.getY()));

            if (isValidMove(p2NextX, p2NextY)) { player2.setX(p2NextX); player2.setY(p2NextY); }
        }
    }

}