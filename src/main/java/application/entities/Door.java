package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Door extends GameObject {
    public Door(int x, int y) {
        super(x, y);
    }

    @Override
    public void update() {
        // ประตูอยู่นิ่งๆ
    }

    @Override
    public void draw(GraphicsContext gc) {
        // วาดประตูเป็นสีเหลือง
        gc.setFill(Color.YELLOW);
        gc.fillRect(getX() * 50, getY() * 50, 50, 50);
    }
}