package application.ui;

import application.entities.Player;
import application.managers.GameManager;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    public static final int TILE_SIZE = 50;
    private final int COLS = 15;
    private final int ROWS = 11;

    private GameManager gameManager;
    private Timer gameLoop;

    // ✨ ปรับตรงนี้ให้ไม่ต้องรับค่าอะไร เพื่อให้ GameWindow.java ไม่ Error
    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        setBackground(Color.DARK_GRAY);

        // สร้าง Manager ภายในนี้เลย
        this.gameManager = new GameManager();

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                gameManager.handleInput(e.getKeyCode());
            }
        });

        gameLoop = new Timer(16, e -> {
            gameManager.updateGame();
            repaint();
        });
        gameLoop.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameManager.getCurrentState() == GameManager.GameState.MAIN_MENU) {
            drawMenu(g);
            return;
        }

        // วาดฉากเกมปกติ
        gameManager.drawGame(g);

        // วาดตัวเลขเวลา (ด้านบน)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("TIME: " + gameManager.getGameTimer(), getWidth() / 2 - 40, 25);

        // วาดหน้าจอ Pause
        if (gameManager.getCurrentState() == GameManager.GameState.PAUSED) {
            drawOverlay(g, "PAUSED", "Press 'P' to Resume", Color.YELLOW);
        }

        // วาดข้อความตอนเกมจบ
        if (isGameOverState()) {
            String msg = getEndGameMessage();
            drawOverlay(g, msg, "Press 'R' to Restart | 'M' for Menu", Color.WHITE);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String timeText = "TIME: " + gameManager.getGameTimer();
        g.drawString(timeText, getWidth() / 2 - 40, 25);

        // ✨ วาดสถานะของ Player 1 (ซ้ายบน)
        if (gameManager.getPlayer1() != null) {
            Player p1 = gameManager.getPlayer1();
            g.setFont(new Font("Arial", Font.BOLD, 14));

            // --- 🔥 วาดไอคอนระยะไฟ (สีส้ม) ---
            int fireStartX = 10; // เริ่มวาดที่ขอบซ้าย
            int currentFire = p1.getBombRadius();
            int maxFire = p1.getMaxFireRadius(); // ดึงค่า Max ที่เราเพิ่งสร้างมาใช้

            for (int i = 0; i < maxFire; i++) {
                if (i < currentFire) {
                    g.setColor(Color.ORANGE); // สว่าง (มีพลัง)
                } else {
                    g.setColor(new Color(100, 50, 0)); // มืด (ยังไม่ได้เก็บ)
                }
                g.fillOval(fireStartX + (i * 18), 8, 14, 14);
            }

            // --- 💣 วาดไอคอนระเบิด (สีฟ้า) ---
            int bombStartX = fireStartX + (maxFire * 18) + 20; // ขยับไปทางขวาต่อจากไอคอนไฟ
            g.setColor(Color.CYAN);
            // g.drawString("P1 BOMB:", bombStartX - 70, 20); // (บรรทัดนี้ลบทิ้งได้เลยครับ ไม่ต้องโชว์ข้อความแล้ว)

            int maxBombs = p1.getMaxBombs();
            int activeBombs = p1.getActiveBombs();
            int brightCount = maxBombs - activeBombs;

            if (brightCount > 0 && !p1.canPlaceBomb()) {
                brightCount--;
            }

            for (int i = 0; i < maxBombs; i++) {
                if (i < brightCount) g.setColor(Color.CYAN);
                else g.setColor(new Color(0, 80, 100));
                g.fillOval(bombStartX + (i * 18), 8, 14, 14);
            }

            // ⏳ โชว์ตัวเลข Cooldown ของ P1
            double cd1 = p1.getCooldownRemaining();
            if (cd1 > 0) g.setColor(Color.YELLOW); else g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(String.format("%.1fs", cd1), bombStartX + (maxBombs * 18) + 5, 20);
        }

        // ✨ วาดสถานะของ Player 2 (ขวาบน)
        if (gameManager.getPlayer2() != null) {
            Player p2 = gameManager.getPlayer2();
            g.setFont(new Font("Arial", Font.BOLD, 14));

            // คำนวณตำแหน่งเริ่มต้นจากขวาสุดของจอ
            int screenWidth = getWidth();
            int p2MaxFire = p2.getMaxFireRadius();
            int p2MaxBombs = p2.getMaxBombs();

            // --- 💣 วาดไอคอนระเบิด (สีเขียว) ไว้ขวาสุด ---
            int bombStartX = screenWidth - (p2MaxBombs * 18) - 40; // เผื่อที่ให้ตัวเลข cooldown นิดหน่อย

            int activeBombs = p2.getActiveBombs();
            int brightCount = p2MaxBombs - activeBombs;
            if (brightCount > 0 && !p2.canPlaceBomb()) brightCount--;

            for (int i = 0; i < p2MaxBombs; i++) {
                if (i < brightCount) g.setColor(Color.GREEN);
                else g.setColor(new Color(0, 80, 0));
                g.fillOval(bombStartX + (i * 18), 8, 14, 14);
            }

            // ⏳ โชว์ตัวเลข Cooldown ของ P2 (อยู่ทางซ้ายของระเบิด)
            double cd2 = p2.getCooldownRemaining();
            if (cd2 > 0) g.setColor(Color.YELLOW); else g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(String.format("%.1fs", cd2), bombStartX - 35, 20);


            // --- 🔥 วาดไอคอนระยะไฟ (สีส้ม) ไว้ทางซ้ายของระเบิดอีกที ---
            int fireStartX = bombStartX - (p2MaxFire * 18) - 20; // เว้นระยะห่างจากกลุ่มระเบิด 20px
            int currentFire = p2.getBombRadius();

            for (int i = 0; i < p2MaxFire; i++) {
                if (i < currentFire) {
                    g.setColor(Color.ORANGE);
                } else {
                    g.setColor(new Color(100, 50, 0));
                }
                g.fillOval(fireStartX + (i * 18), 8, 14, 14);
            }
        }
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 70));
        g.drawString("BOMB IT!", getWidth() / 2 - 160, getHeight() / 2 - 50);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 25));
        g.drawString("Press '1' for Single Player", getWidth() / 2 - 140, getHeight() / 2 + 30);
        g.drawString("Press '2' for Two Players", getWidth() / 2 - 130, getHeight() / 2 + 80);
    }

    private void drawOverlay(Graphics g, String title, String sub, Color titleColor) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(titleColor);
        g.setFont(new Font("Arial", Font.BOLD, 45));
        g.drawString(title, getWidth() / 2 - (g.getFontMetrics().stringWidth(title) / 2), getHeight() / 2);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString(sub, getWidth() / 2 - (g.getFontMetrics().stringWidth(sub) / 2), getHeight() / 2 + 50);
    }

    private boolean isGameOverState() {
        GameManager.GameState s = gameManager.getCurrentState();
        return s != GameManager.GameState.PLAYING && s != GameManager.GameState.PAUSED && s != GameManager.GameState.MAIN_MENU;
    }

    private String getEndGameMessage() {
        switch(gameManager.getCurrentState()) {
            case YOU_WIN: return "YOU WIN!";
            case GAME_OVER: return "GAME OVER";
            case P1_WIN: return "P1 WINS!";
            case P2_WIN: return "P2 WINS!";
            case DRAW: return "DRAW!";
            default: return "";
        }
    }


}