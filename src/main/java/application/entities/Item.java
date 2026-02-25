package application.entities;

import application.core.GameObject;
import java.awt.*;

public class Item extends GameObject {
    public enum ItemType { EXTRA_BOMB, FIRE_POWER, SPEED }
    private ItemType type;

    public Item(int x, int y, ItemType type) {
        super(x, y);
        this.type = type;
    }

    public ItemType getType() { return type; }

    @Override
    public void draw(Graphics g) {
        // เลือกสีตามประเภทไอเทม
        switch (type) {
            case EXTRA_BOMB: g.setColor(Color.BLACK); break;
            case FIRE_POWER: g.setColor(Color.RED); break;
            case SPEED: g.setColor(Color.CYAN); break;
        }
        // ✨ เปลี่ยนจาก x เป็น getX() และจาก y เป็น getY()
        g.fillOval(getX() * 50 + 10, getY() * 50 + 10, 30, 30);
    }

    @Override
    public void update() {}
}