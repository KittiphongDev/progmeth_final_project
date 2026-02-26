package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bomb extends GameObject {
    private long creationTime;
    private int radius;

    // ✨ 1. เพิ่มตัวแปรเก็บว่าใครเป็นเจ้าของระเบิดลูกนี้
    private Player owner;

    public Bomb(int x, int y, int radius) {
        super(x, y);
        this.radius = radius;
        this.creationTime = System.currentTimeMillis();
    }

    public int getRadius() { return radius; }

    // ✨ 2. เพิ่มเมธอด Getter และ Setter ให้ GameManager เรียกใช้ได้
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }

    public boolean isReadyToExplode() {
        return System.currentTimeMillis() - creationTime >= 2000;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillOval(getX() * 50 + 10, getY() * 50 + 10, 30, 30);
    }

    @Override
    public void update() {}
}