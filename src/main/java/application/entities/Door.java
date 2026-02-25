package application.entities;

import application.core.GameObject;
import java.awt.Graphics;
import java.awt.Color;

public class Door extends GameObject {
    public Door(int x, int y) {
        super(x, y);
    }

    @Override
    public void update() {
        // ประตูอยู่นิ่งๆ
    }

    @Override
    public void draw(Graphics g) {
        // วาดประตูเป็นสีเหลือง
        g.setColor(Color.YELLOW);
        g.fillRect(getX() * 50, getY() * 50, 50, 50);
    }
}