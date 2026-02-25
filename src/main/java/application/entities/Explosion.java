package application.entities;

import application.core.GameObject;
import java.awt.Color;
import java.awt.Graphics;

public class Explosion extends GameObject {
    private long creationTime;
    private static final int DURATION = 300; // ระยะเวลาที่ไฟค้างอยู่บนจอ (มิลลิวินาที)

    public Explosion(int x, int y) {
        super(x, y);
        this.creationTime = System.currentTimeMillis();
    }

    // เมธอดเช็คว่าไฟดับหรือยัง
    public boolean isFinished() {
        return System.currentTimeMillis() - creationTime >= DURATION;
    }

    @Override
    public void draw(Graphics g) {
        // วาดพื้นหลังไฟสีส้มให้เต็มช่อง
        g.setColor(Color.ORANGE);
        g.fillRect(getX() * 50, getY() * 50, 50, 50);

        // วาดแกนกลางไฟสีแดงให้ดูมีมิติ
        g.setColor(Color.RED);
        g.fillRect(getX() * 50 + 10, getY() * 50 + 10, 30, 30);
    }

    @Override
    public void update() {}
}