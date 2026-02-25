package application.entities;

import application.core.Destroyable;
import application.core.GameObject;
import java.awt.Graphics;
import java.awt.Color;

public class Player extends GameObject implements Destroyable {
    private boolean isAlive;
    private Color playerColor; // ✨ เพิ่มตัวแปรเก็บสีของตัวละคร

    // ✨ อัปเดต Constructor ให้รับค่าสีเข้ามาด้วย
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

    @Override
    public void draw(Graphics g) {
        if (!isAlive) return;
        // ✨ ใช้สีของตัวเองในการวาด
        g.setColor(playerColor);
        g.fillRect(getX() * 50, getY() * 50, 50, 50);
    }

    @Override
    public void onDestroy() {
        this.isAlive = false;
        System.out.println("Player at " + getX() + "," + getY() + " is dead!");
    }

    private int maxBombs = 1;
    private int activeBombs = 0;
    private int bombRadius = 1;

    // เมธอดสำหรับจัดการจำนวนระเบิด
    public boolean canPlaceBomb() {
        return activeBombs < maxBombs;
    }

    public void increaseActiveBombs() {
        activeBombs++;
    }

    public void decreaseActiveBombs() {
        if (activeBombs > 0) activeBombs--;
    }

    // เมธอดสำหรับไอเทมเรียกใช้
    public void addMaxBombs() {
        maxBombs++;
    }

    public void addRadius() {
        bombRadius++;
    }

    public int getBombRadius() {
        return bombRadius;
    }
}