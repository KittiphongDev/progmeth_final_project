package application.entities;

import application.core.Destroyable;
import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // ✨ นำเข้าคลาส Image ของ JavaFX
import javafx.scene.paint.Color;

public class BreakableWall extends GameObject implements Destroyable {
    private boolean isDestroyed;

    // ✨ ใช้ static เพื่อให้กล่องทุกใบโหลดรูปลงแรมแค่ครั้งเดียว (ประหยัดหน่วยความจำ)
    private static Image boxIcon;

    public BreakableWall(int x, int y) {
        super(x, y);
        this.isDestroyed = false;

        // ✨ โหลดรูปภาพเฉพาะตอนที่ยังไม่เคยโหลดมาก่อนเท่านั้น
        if (boxIcon == null) {
            try {
                boxIcon = new Image(getClass().getResourceAsStream("/box.png"));
            } catch (Exception e) {
                System.out.println("⚠️ Warning: Could not load box.png");
            }
        }
    }

    @Override
    public void update(long deltaTime) {}

    @Override
    public void draw(GraphicsContext gc) {
        if (!isDestroyed) {
            // ✨ ถ้าระบบโหลดรูปภาพสำเร็จ ให้วาดรูปภาพขนาด 50x50 พอดีช่อง
            if (boxIcon != null) {
                gc.drawImage(boxIcon, getX() * 50, getY() * 50, 50, 50);
            } else {
                // ระบบสำรองกรณีไฟล์รูปหาย จะได้วาดกล่องสีน้ำตาลแทน
                gc.setFill(Color.rgb(139, 69, 19));
                gc.fillRect(getX() * 50, getY() * 50, 50, 50);
            }
        }
    }

    @Override
    public void onDestroy() {
        this.isDestroyed = true;
        System.out.println("Box destroyed! Drop an item maybe?");
    }
}