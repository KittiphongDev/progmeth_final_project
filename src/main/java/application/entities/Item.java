package application.entities;

import application.core.Collectible;
import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // ✨ เพิ่ม Import สำหรับ Image
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.VPos;

public class Item extends GameObject implements Collectible {

    public enum ItemType { EXTRA_BOMB, FIRE_POWER, SPEED }
    private ItemType type;
    private long maxPopupTime = 0; // ✨ จับเวลาโชว์ข้อความ MAX

    // ✨ ประกาศตัวแปร Static สำหรับเก็บรูปภาพ (โหลดครั้งเดียวใช้ร่วมกันทุกไอเทม)
    private static Image firePowerImg;
    private static Image extraBombImg;

    // ✨ Static Initializer Block: โหลดรูปภาพเมื่อเริ่มโปรแกรม
    static {
        try {
            // พยายามโหลดรูปภาพจาก resources (โฟลเดอร์ที่เก็บ source code หรือ resources)
            // หมายเหตุ: คุณต้องนำไฟล์ upgrade.png และ upgrade_bomb.png ไปวางใน source folder ของโปรเจกต์
            firePowerImg = new Image(Item.class.getResourceAsStream("/upgrade_fire.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not load /upgrade.png for Item FIRE_POWER. Using fallback shape.");
        }

        try {
            extraBombImg = new Image(Item.class.getResourceAsStream("/upgrade_bomb.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not load /upgrade_bomb.png for Item EXTRA_BOMB. Using fallback shape.");
        }
    }


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
    public void draw(GraphicsContext gc) {
        // ตำแหน่งและขนาดที่จะวาด (จัดกึ่งกลางช่อง 50x50)
        double drawX = getX() * 50 + 5;  // ขยับนิดหน่อยให้สวยงามตามขนาดรูป
        double drawY = getY() * 50 + 5;
        double size = 40; // ปรับขนาดรูปภาพให้เหมาะสม (เดิม 30)

        Image imgToDraw = null;
        Color fallbackColor = Color.TRANSPARENT;

        // เลือกรูปภาพและสีสำรองตามประเภท
        switch (type) {
            case EXTRA_BOMB:
                imgToDraw = extraBombImg;
                fallbackColor = Color.BLACK;
                break;
            case FIRE_POWER:
                imgToDraw = firePowerImg;
                fallbackColor = Color.RED;
                break;
//            case SPEED: ...
        }

        // ✨ ตรวจสอบว่ามีรูปภาพหรือไม่
        if (imgToDraw != null) {
            // ถ้ามีรูป ให้วาดรูป
            gc.drawImage(imgToDraw, drawX, drawY, size, size);
        } else {
            // ถ้าไม่มีรูป (โหลดไม่เจอ) ให้วาดวงกลมสีแบบเดิม (Fallback)
            gc.setFill(fallbackColor);
            gc.fillOval(getX() * 50 + 10, getY() * 50 + 10, 30, 30);
        }

        // === ส่วนวาดคำว่า MAX (เหมือนเดิม) ===
        if (System.currentTimeMillis() - maxPopupTime < 1000) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setTextBaseline(VPos.BOTTOM);

            String text = "MAX";
            double textX = getX() * 50 + 8;
            double textY = getY() * 50 - 5;

            // 1. วาดเส้นขอบ (Stroke)
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2.5);
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