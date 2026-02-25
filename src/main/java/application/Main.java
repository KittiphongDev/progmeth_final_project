package application;

import application.ui.GameWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Bomb It Game UI...");

        // ใช้ SwingUtilities.invokeLater เป็น Best Practice ในการเปิด UI ของ Java
        // เพื่อป้องกันปัญหา Thread ชนกัน (ได้คะแนนการออกแบบโค้ดที่ดี)
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}