package application.ui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

// ใน JavaFX คลาสหลักที่ใช้เปิดหน้าต่างต้อง extends Application เสมอครับ
public class GameWindow extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. ตั้งชื่อหัวหน้าต่าง
        primaryStage.setTitle("Bomb It - OOP Project");

        // 2. ล็อกไม่ให้ใช้เมาส์ลากขยายหน้าต่าง เพื่อไม่ให้สัดส่วนเกมเพี้ยน
        primaryStage.setResizable(false);

        // 3. สร้างผืนผ้าใบ (GameCanvas) ที่เราเพิ่งเขียนกันไป
        GamePanel gameCanvas = new GamePanel();

        // 4. เอาผืนผ้าใบไปแปะบน Layout (ผมใช้ Group เพื่อให้มันห่อหุ้ม Canvas พอดีเป๊ะ)
        Group root = new Group(gameCanvas);

        // 5. สร้าง Scene (ฉาก) ขึ้นมา (ขั้นตอนนี้เทียบเท่ากับการ pack() ใน Swing ครับ)
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // 6. บังคับให้ Canvas โฟกัสเพื่อรับ Event การกดปุ่ม Keyboard ทันทีที่เปิดเกม
        gameCanvas.requestFocus();

        // 7. ให้หน้าต่างเด้งขึ้นมากลางหน้าจอ
        primaryStage.centerOnScreen();

        // 8. สั่งให้หน้าต่างแสดงผล
        primaryStage.show();

        // หมายเหตุ: ใน JavaFX ไม่ต้องสั่ง setDefaultCloseOperation แล้วครับ
        // พอกดกากบาท (X) ระบบ Application จะเคลียร์ตัวเองและปิด Thread ให้โดยอัตโนมัติเลยครับ
    }

    // ฟังก์ชัน main สำหรับรันโปรแกรม
    public static void main(String[] args) {
        launch(args); // สั่งจุดระเบิดระบบ JavaFX
    }
}