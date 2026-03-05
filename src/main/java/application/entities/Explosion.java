package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Explosion extends GameObject {
    private long creationTime;
    private static final int DURATION = 300; // ระยะเวลาการแสดงผล (0.3 วินาที)

    private static Image explodeImg;

    static {
        try {
            explodeImg = new Image(Explosion.class.getResourceAsStream("/explode.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not load /explode.png. Using fallback colors.");
        }
    }

    public Explosion(int x, int y) {
        super(x, y);
        this.creationTime = System.currentTimeMillis();
    }

    public boolean isFinished() {
        return System.currentTimeMillis() - creationTime >= DURATION;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // 1. คำนวณความคืบหน้า (0.0 ถึง 1.0)
        long elapsed = System.currentTimeMillis() - creationTime;
        double progress = Math.min(1.0, (double) elapsed / DURATION);

        // 2. ✨ คำนวณการขยายตัว (Scale from Center)
        // ใช้ Math.sqrt(progress) เพื่อให้ระเบิดขยายตัวพรึ่บออกมาเร็วในช่วงแรก
        double maxTileSize = 50.0;
        double currentSize = maxTileSize * Math.sqrt(progress);

        // คำนวณตำแหน่ง Offset เพื่อให้รูปอยู่กึ่งกลางช่องเสมอขณะที่มันขยาย
        double offset = (maxTileSize - currentSize) / 2.0;
        double drawX = getX() * 50 + offset;
        double drawY = getY() * 50 + offset;

        // 3. ✨ คำนวณความโปร่งใส (Fade Out)
        // ให้เริ่มจางลงเรื่อยๆ ตามความคืบหน้า
        double alpha = 1.0 - (progress * 0.3);
        gc.setGlobalAlpha(alpha);

        if (explodeImg != null) {
            // วาดรูปที่คำนวณขนาดและตำแหน่งใหม่แล้ว
            gc.drawImage(explodeImg, drawX, drawY, currentSize, currentSize);
        } else {
            // Fallback: วาดวงกลมขยายตัวแทนถ้าโหลดรูปไม่ได้
            gc.setFill(Color.ORANGE);
            gc.fillOval(drawX, drawY, currentSize, currentSize);

            gc.setFill(Color.RED);
            double innerSize = currentSize * 0.6;
            double innerOffset = (currentSize - innerSize) / 2;
            gc.fillOval(drawX + innerOffset, drawY + innerOffset, innerSize, innerSize);
        }

        // คืนค่าความโปร่งใสเป็นปกติ เพื่อไม่ให้กระทบ Object อื่น
        gc.setGlobalAlpha(1.0);
    }

    @Override
    public void update(long deltaTime) {}
}