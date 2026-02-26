package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // ✨ เพิ่ม Import
import javafx.scene.paint.Color;

public class Bomb extends GameObject {
    private long creationTime;
    private int radius;
    private Player owner;

    // ✨ 1. โหลดรูปภาพแบบ Static (โหลดครั้งเดียวใช้ทั้งเกม)
    private static Image bombImg;

    static {
        try {
            bombImg = new Image(Bomb.class.getResourceAsStream("/bomb.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not load /bomb.png. Using fallback shape.");
        }
    }

    public Bomb(int x, int y, int radius) {
        super(x, y);
        this.radius = radius;
        this.creationTime = System.currentTimeMillis();
    }

    public int getRadius() { return radius; }

    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }

    public boolean isReadyToExplode() {
        return System.currentTimeMillis() - creationTime >= 2000; // ระเบิดใน 2 วินาที
    }

    @Override
    public void draw(GraphicsContext gc) {
        double elapsed = System.currentTimeMillis() - creationTime;

        // ✨ 2. เพิ่มแอนิเมชัน Pulsing (เต้นตามจังหวะ)
        // ใช้ Math.sin เพื่อสร้างค่าที่แกว่งไปมา ทำให้ระเบิดดูเหมือนกำลังนับถอยหลัง
        double pulse = Math.sin(elapsed / 100.0) * 3;
        double size = 35 + pulse; // ขนาดปกติ 35 และขยาย/หดตามค่า pulse

        // คำนวณตำแหน่งให้กึ่งกลางช่อง 50x50 เสมอ
        double offset = (50 - size) / 2;
        double drawX = getX() * 50 + offset;
        double drawY = getY() * 50 + offset;

        if (bombImg != null) {
            // วาดรูประเบิด
            gc.drawImage(bombImg, drawX, drawY, size, size);
        } else {
            // --- Fallback: ถ้าไม่มีรูปให้วาดวงกลมสีดำแทน ---
            gc.setFill(Color.BLACK);
            gc.fillOval(drawX, drawY, size, size);

            // เพิ่มเงาไฮไลท์สีขาวเล็กๆ ให้ดูเป็นทรงกลม
            gc.setFill(Color.WHITE);
            gc.fillOval(drawX + (size*0.2), drawY + (size*0.2), size*0.2, size*0.2);
        }
    }

    @Override
    public void update() {
        // สามารถใส่ Logic เพิ่มเติมที่นี่ได้ถ้าต้องการ
    }
}