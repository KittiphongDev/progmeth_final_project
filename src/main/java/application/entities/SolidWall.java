package application.entities;

import application.core.GameObject;
import java.awt.Graphics;
import java.awt.Color;

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

    @Override
    public void draw(Graphics g) {
        // วาดกำแพงเหล็กเป็นสีเทา
        g.setColor(Color.GRAY);
        g.fillRect(getX() * 50, getY() * 50, 50, 50);
    }
}