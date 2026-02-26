package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext; // ✨ เปลี่ยนจาก java.awt.Graphics
import javafx.scene.paint.Color;           // ✨ เปลี่ยนจาก java.awt.Color

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

    // ✨ เปลี่ยนมารับค่า GraphicsContext เพื่อให้สอดคล้องกับคลาสแม่ GameObject
    @Override
    public void draw(GraphicsContext gc) {
        // วาดพื้นหลังไฟสีส้มให้เต็มช่อง (ใช้ gc.setFill แทน g.setColor)
        gc.setFill(Color.ORANGE);
        gc.fillRect(getX() * 50, getY() * 50, 50, 50);

        // วาดแกนกลางไฟสีแดงให้ดูมีมิติ
        gc.setFill(Color.RED);
        gc.fillRect(getX() * 50 + 10, getY() * 50 + 10, 30, 30);
    }

    @Override
    public void update() {}
}