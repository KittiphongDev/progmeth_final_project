package application.entities;

import application.core.Destroyable;
import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player extends GameObject implements Destroyable {
    private boolean isAlive;
    private Color playerColor; // ✨ เปลี่ยนมาใช้ Color ของ JavaFX
    private static final int MAX_FIRE_RADIUS = 3;

    public Player(int x, int y, Color color) {
        super(x, y);
        this.isAlive = true;
        this.playerColor = color;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    @Override
    public void update() {}

    // ✨ เปลี่ยนพารามิเตอร์จาก Graphics เป็น GraphicsContext
    @Override
    public void draw(GraphicsContext gc) {
        if (!isAlive) return;

        // ✨ ใช้ setFill แทน setColor สำหรับ JavaFX
        gc.setFill(playerColor);
        gc.fillRect(getX() * 50, getY() * 50, 50, 50);
    }

    @Override
    public void onDestroy() {
        this.isAlive = false;
        System.out.println("Player at " + getX() + "," + getY() + " is dead!");
    }

    private int maxBombs = 1;
    private int activeBombs = 0;
    private int bombRadius = 1;

    private long lastBombTime = 0;
    private static final long BOMB_COOLDOWN = 500;

    public boolean canPlaceBomb() {
        long currentTime = System.currentTimeMillis();
        if (activeBombs < maxBombs && (currentTime - lastBombTime >= BOMB_COOLDOWN)) {
            return true;
        }
        return false;
    }

    public void increaseActiveBombs() {
        activeBombs++;
        lastBombTime = System.currentTimeMillis();
    }

    public void decreaseActiveBombs() {
        if (activeBombs > 0) activeBombs--;
    }

    public boolean addMaxBombs() {
        if (maxBombs < 3) {
            maxBombs++;
            return true;
        }
        return false;
    }

    public boolean addRadius() {
        if (bombRadius < MAX_FIRE_RADIUS) {
            bombRadius++;
            return true;
        }
        return false;
    }

    public int getMaxFireRadius() {
        return MAX_FIRE_RADIUS;
    }

    public int getBombRadius() {
        return bombRadius;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public int getActiveBombs() {
        return activeBombs;
    }

    public double getCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastBombTime;
        if (elapsed >= BOMB_COOLDOWN) return 0.0;
        return (BOMB_COOLDOWN - elapsed) / 1000.0;
    }
}