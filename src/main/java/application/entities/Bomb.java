package application.entities;

import application.core.GameObject;
import java.awt.Graphics;
import java.awt.Color;

public class Bomb extends GameObject {
    private long plantTime; // เก็บเวลาตอนที่วางระเบิด
    private final int EXPLOSION_DELAY = 3000; // นับถอยหลัง 3 วินาที (3000 มิลลิวินาที)

    public Bomb(int x, int y, int bombRadius) {
        super(x, y);
        // จดจำเวลาปัจจุบันทันทีที่ระเบิดถูกสร้างขึ้นมา
        this.plantTime = System.currentTimeMillis();
    }

    // ฟังก์ชันเช็คว่าครบ 3 วินาทีหรือยัง
    public boolean isReadyToExplode() {
        return System.currentTimeMillis() - plantTime >= EXPLOSION_DELAY;
    }

    @Override
    public void update() {
        // ระเบิดอยู่นิ่งๆ นับเวลาในใจ ไม่ต้องขยับ
    }

    @Override
    public void draw(Graphics g) {
        // วาดระเบิดเป็นวงกลมสีแดง
        g.setColor(Color.RED);
        // +10 และลดขนาดเหลือ 30 เพื่อให้วงกลมอยู่ตรงกลางช่องพอดี (ช่องเราขนาด 50x50)
        g.fillOval(getX() * 50 + 10, getY() * 50 + 10, 30, 30);
    }
}