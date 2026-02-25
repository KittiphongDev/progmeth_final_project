package application.entities;

import application.core.Collectible;
import application.core.GameObject;
import java.awt.*;

public class Item extends GameObject implements Collectible{

    public enum ItemType { EXTRA_BOMB, FIRE_POWER, SPEED }
    private ItemType type;
    private long maxPopupTime = 0; // ✨ จับเวลาโชว์ข้อความ MAX

    public Item(int x, int y, ItemType type) {
        super(x, y);
        this.type = type;
    }

    @Override
    public boolean onCollect(Player player) {
        boolean isCollected = false;

        switch (this.type) {
            case EXTRA_BOMB:
                isCollected = player.addMaxBombs();
                break;
            case FIRE_POWER:
                isCollected = player.addRadius();
                break;
        }

        // ✨ ถ้าเก็บไม่สำเร็จ (เต็มแล้ว) ให้เริ่มจับเวลาโชว์คำว่า MAX
        if (!isCollected) {
            maxPopupTime = System.currentTimeMillis();
        }
        return isCollected;
    }
    @Override
    public void draw(Graphics g) {
        // เลือกสีตามประเภทไอเทม
        switch (type) {
            case EXTRA_BOMB: g.setColor(Color.BLACK); break;
            case FIRE_POWER: g.setColor(Color.RED); break;
//            case SPEED: g.setColor(Color.CYAN); break;
        }
        // ✨ เปลี่ยนจาก x เป็น getX() และจาก y เป็น getY()
        g.fillOval(getX() * 50 + 10, getY() * 50 + 10, 30, 30);

        // ✨ วาดคำว่า MAX ลอยขึ้นมา เป็นเวลา 1 วินาที (1000 ms) หลังจากเดินชน
        if (System.currentTimeMillis() - maxPopupTime < 1000) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("MAX", getX() * 50 + 8, getY() * 50 - 5);
        }
    }

    @Override
    public void update() {}
    public ItemType getType() { return type; }

}