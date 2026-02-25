package application.ui;

import javax.swing.JFrame;

public class GameWindow extends JFrame {
    public GameWindow() {
        // ตั้งชื่อหัวหน้าต่าง
        setTitle("Bomb It - OOP Project");

        // กดกากบาทแล้วให้โปรแกรมหยุดรัน (สำคัญมาก ไม่งั้นกินแรมเครื่อง)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ล็อกไม่ให้ใช้เมาส์ลากขยายหน้าต่าง เพื่อไม่ให้สัดส่วนเกมเพี้ยน
        setResizable(false);

        // เอาผืนผ้าใบ (GamePanel) มาแปะบนหน้าต่าง
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        // จัดขนาดหน้าต่างให้พอดีกับผืนผ้าใบอัตโนมัติ
        pack();

        // ให้หน้าต่างเด้งขึ้นมากลางหน้าจอพอดี
        setLocationRelativeTo(null);

        // สั่งให้หน้าต่างแสดงผล
        setVisible(true);
    }
}