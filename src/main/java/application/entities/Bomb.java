package application.entities;

import application.core.GameObject;
import java.awt.Color;
import java.awt.Graphics;

public class Bomb extends GameObject {
    private long creationTime;
    private int radius; // ✨ เพิ่มตัวแปรเก็บความแรง

    // ✨ แก้ Constructor ให้รับค่า radius จาก Player ด้วย
    public Bomb(int x, int y, int radius) {
        super(x, y);
        this.radius = radius;
        this.creationTime = System.currentTimeMillis();
    }

    public int getRadius() { return radius; } // ✨ เมธอดส่งค่าความแรงให้ GameManager

    public boolean isReadyToExplode() {
        return System.currentTimeMillis() - creationTime >= 2000;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(getX() * 50 + 10, getY() * 50 + 10, 30, 30);
    }

    @Override
    public void update() {}
}