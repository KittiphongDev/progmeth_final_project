package application.entities;

import application.core.Collectible;
import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.VPos;

public class Item extends GameObject implements Collectible {

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

    // ✨ เปลี่ยนพารามิเตอร์มารับ GraphicsContext ของ JavaFX
    @Override
    public void draw(GraphicsContext gc) {
        // เลือกสีตามประเภทไอเทม (ใช้ gc.setFill แทน g.setColor)
        switch (type) {
            case EXTRA_BOMB: gc.setFill(Color.BLACK); break;
            case FIRE_POWER: gc.setFill(Color.RED); break;
//            case SPEED: gc.setFill(Color.CYAN); break;
        }

        // ✨ เปลี่ยนจาก x เป็น getX() และจาก y เป็น getY()
        gc.fillOval(getX() * 50 + 10, getY() * 50 + 10, 30, 30);

        // ✨ วาดคำว่า MAX แบบมีขอบ (แก้ไขส่วนสีดำล้นและ Error VPos)
        if (System.currentTimeMillis() - maxPopupTime < 1000) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            // 🎯 แก้ไข Error โดยการใช้ VPos ที่ Import มาแล้ว
            gc.setTextBaseline(VPos.BOTTOM);

            String text = "MAX";
            double textX = getX() * 50 + 8;
            double textY = getY() * 50 - 5;

            // 1. วาดเส้นขอบ (Stroke)
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2.5);

            // ✨ แก้ไข Error โดยการใช้ StrokeLineJoin ที่ Import มาแล้ว
            // ช่วยให้มุมตัวอักษรไม่แหลมจนล้นออกมาครับ
            gc.setLineJoin(StrokeLineJoin.ROUND);
            gc.strokeText(text, textX, textY);

            // 2. วาดตัวหนังสือสีขาวทับ
            gc.setFill(Color.WHITE);
            gc.fillText(text, textX, textY);

            // คืนค่า Baseline กลับเป็นปกติ
            gc.setTextBaseline(VPos.BASELINE);
        }
    }

    @Override
    public void update() {}

    public ItemType getType() { return type; }
}