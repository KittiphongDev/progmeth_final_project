package application.entities;

import application.core.Collectible;
import application.core.GameObject;
import java.awt.*;

public class Item extends GameObject implements Collectible{

    @Override
    public void onCollect(Player player) {
        // ย้าย logic การให้บัฟจาก GameManager มาไว้ที่นี่แทน
        switch (this.type) {
            case EXTRA_BOMB: player.addMaxBombs(); break;
            case FIRE_POWER: player.addRadius(); break;
            case SPEED: player.addSpeed(); break;
        }
    }

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