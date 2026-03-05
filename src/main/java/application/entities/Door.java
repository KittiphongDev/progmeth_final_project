package application.entities;

import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Door extends GameObject {

    // เปลี่ยนเป็น static เพื่อโหลดเข้าหน่วยความจำแค่ครั้งเดียว
    private static boolean doorLoaded = false;
    private static Image doorImage;

    public Door(int x, int y) {
        super(x, y);
        loadDoor(); // ✨ ต้องเรียกใช้เมธอดนี้ตอนสร้าง Object ด้วยครับ
    }

    private void loadDoor() {
        if (!doorLoaded) {
            try {
                // แนะนำให้ใช้ getResourceAsStream เหมือนคลาสอื่นๆ เพื่อป้องกันปัญหาระบบหาไฟล์ไม่เจอ
                doorImage = new Image(getClass().getResourceAsStream("/portal.png"));

                if (doorImage != null && !doorImage.isError()) {
                    doorLoaded = true;
                } else {
                    System.err.println("Warning: portal.png not found");
                }
            } catch (Exception e) {
                System.err.println("Exception loading door: " + e.getMessage());
            }
        }
    }

    @Override
    public void update(long deltaTime) {} // ประตูอยู่นิ่งๆ

    @Override
    public void draw(GraphicsContext gc) {
        double tileSize = 50.0;

        if (doorLoaded) {
            // ✨ คูณตำแหน่ง x, y ด้วยขนาด Tile (50) และกำหนดความกว้าง/สูง
            gc.drawImage(doorImage, getX() * tileSize, getY() * tileSize, tileSize, tileSize);
        } else {
            gc.setFill(Color.YELLOW);
            gc.fillRect(getX() * tileSize, getY() * tileSize, tileSize, tileSize);
        }
    }
}