package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// สังเกตว่าคลาสนี้ extends GameObject เฉยๆ แต่ไม่ได้ implements Destroyable
// เพราะมันเป็นกำแพงอมตะ โดนระเบิดก็ไม่พังครับ
public class SolidWall extends GameObject {

    public SolidWall(int x, int y) {
        super(x, y);
    }

    @Override
    public void update() {
        // กำแพงอยู่นิ่งๆ ไม่ต้องทำอะไร
    }

    // ✨ เปลี่ยนพารามิเตอร์จาก Graphics เป็น GraphicsContext สำหรับ JavaFX
    @Override
    public void draw(GraphicsContext gc) {
        // วาดกำแพงเหล็กเป็นสีเทา (ใช้ setFill แทน setColor)
        gc.setFill(Color.GRAY);
        gc.fillRect(getX() * 50, getY() * 50, 50, 50);
    }
}