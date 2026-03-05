package application.core;

import javafx.scene.canvas.GraphicsContext; // ✨ เปลี่ยนจาก java.awt.Graphics

public abstract class GameObject {
    private int x;
    private int y;

    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

//    public abstract void update();

    // ✨ เปลี่ยนพารามิเตอร์ที่รับเข้ามาเป็น GraphicsContext ของ JavaFX
    public abstract void draw(GraphicsContext gc);

    public abstract void update(long deltaTime);
}