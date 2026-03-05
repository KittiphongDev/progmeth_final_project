package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

// สังเกตว่าคลาสนี้ extends GameObject เฉยๆ แต่ไม่ได้ implements Destroyable
// เพราะมันเป็นกำแพงอมตะ โดนระเบิดก็ไม่พังครับ
public class SolidWall extends GameObject {

    // โหลดภาพแค่ครั้งเดียวและใช้ร่วมกันทุกๆ Object ของ SolidWall
    private static Image wallImage;
    private static boolean imageLoaded = false;

    public SolidWall(int x, int y) {
        super(x, y);
        loadImage();
    }

    private void loadImage() {
        if (!imageLoaded) {
            try {
                // อย่าลืมเช็ค Path ของรูปภาพให้ตรงกับโฟลเดอร์ resources ของคุณนะครับ
                wallImage = new Image(getClass().getResourceAsStream("/bush_wall.png"));
                if (wallImage != null && !wallImage.isError()) {
                    imageLoaded = true;
                } else {
                    System.err.println("Warning: bush_wall.png not found.");
                }
            } catch (Exception e) {
                System.err.println("Exception loading wall image: " + e.getMessage());
            }
        }
    }

    @Override
    public void update(long deltaTime) {
        // กำแพงอยู่นิ่งๆ ไม่ต้องทำอะไร
    }

    // ✨ เปลี่ยนพารามิเตอร์จาก Graphics เป็น GraphicsContext สำหรับ JavaFX
    @Override
    public void draw(GraphicsContext gc) {
        double tileSize = 50.0; // ขนาดของ Collision Box (ช่องที่เดินชน)

        if (imageLoaded) {
            // กำหนดขนาดภาพที่มองเห็น (Visual Box) ให้ใหญ่กว่ากล่องชน
            // ตัวอย่าง: ให้ใหญ่ขึ้น 20% (60x60 pixels)
            double visualWidth = tileSize * 1.2;
            double visualHeight = tileSize * 1.2;

            // คำนวณระยะขยับ (Offset) เพื่อให้ศูนย์กลางของพุ่มไม้อยู่ตรงกับกึ่งกลางกล่อง
            // แกน X: ขยับไปทางซ้ายครึ่งนึงของส่วนที่ล้นออกมา
            double offsetX = (visualWidth - tileSize) / 2.0;

            // แกน Y: ขยับขึ้นด้านบนทั้งหมด เพื่อให้ฐานของพุ่มไม้ตั้งอยู่บนพื้นของ Tile พอดี
            double offsetY = visualHeight - tileSize;

            double drawX = (getX() * tileSize) - offsetX;
            double drawY = (getY() * tileSize) - offsetY;

            // วาดรูปพุ่มไม้ที่ใหญ่กว่ากล่องชน
            gc.drawImage(wallImage, drawX, drawY, visualWidth, visualHeight);

            /* * 🛠️ DEBUG MODE: เอาคอมเมนต์ด้านล่างออก เพื่อวาดเส้นสีแดง
             * ให้คุณมองเห็นขนาดของ Collision Box (50x50) ว่าอยู่ตรงไหนเมื่อเทียบกับภาพ
             */
            // gc.setStroke(Color.RED);
            // gc.setLineWidth(2);
            // gc.strokeRect(getX() * tileSize, getY() * tileSize, tileSize, tileSize);

        } else {
            gc.setFill(Color.GRAY);
            gc.fillRect(getX() * tileSize, getY() * tileSize, tileSize, tileSize);
        }
    }
}