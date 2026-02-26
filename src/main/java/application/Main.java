package application;

import application.ui.GameWindow;
import javafx.application.Application; // ✨ นำเข้าไลบรารีของ JavaFX

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Bomb It Game UI (JavaFX Edition)...");

        // ✨ ใช้คำสั่ง Application.launch เพื่อเรียกใช้งานคลาส GameWindow
        // ระบบ JavaFX จะจัดการเรื่อง Thread ให้ทำงานได้อย่างปลอดภัยโดยอัตโนมัติ (Best Practice)
        Application.launch(GameWindow.class, args);
    }
}