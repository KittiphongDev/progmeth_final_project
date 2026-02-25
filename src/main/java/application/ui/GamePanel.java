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
            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("P1 FIRE: " + p1.getBombRadius(), 10, 20);

            int max = p1.getMaxBombs();
            int active = p1.getActiveBombs();
            int brightCount = max - active;

            if (brightCount > 0 && !p1.canPlaceBomb()) {
                brightCount--;
            }

            int startX = 100;
            for (int i = 0; i < max; i++) {
                if (i < brightCount) g.setColor(Color.CYAN);
                else g.setColor(new Color(0, 80, 100));
                g.fillOval(startX + (i * 18), 8, 14, 14);
            }

            // ⏳ โชว์ตัวเลข Cooldown ของ P1 (โชว์ตลอดเวลา)
            double cd1 = p1.getCooldownRemaining();
            if (cd1 > 0) {
                g.setColor(Color.YELLOW); // กำลังติดดีเลย์ (สีเหลือง)
            } else {
                g.setColor(Color.WHITE);  // พร้อมแล้ว (สีขาว)
            }
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(String.format("%.1fs", cd1), startX + (max * 18) + 5, 20);
        }

        // ✨ วาดสถานะของ Player 2 (ขวาบน)
        if (gameManager.getPlayer2() != null) {
            Player p2 = gameManager.getPlayer2();
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 14));

            String fireText = "P2 FIRE: " + p2.getBombRadius();
            int textX = getWidth() - 100;
            g.drawString(fireText, textX, 20);

            int max = p2.getMaxBombs();
            int active = p2.getActiveBombs();
            int brightCount = max - active;

            if (brightCount > 0 && !p2.canPlaceBomb()) {
                brightCount--;
            }

            int startX = textX - (max * 18) - 10;
            for (int i = 0; i < max; i++) {
                if (i < brightCount) g.setColor(Color.GREEN);
                else g.setColor(new Color(0, 80, 0));
                g.fillOval(startX + (i * 18), 8, 14, 14);
            }

            // ⏳ โชว์ตัวเลข Cooldown ของ P2 (โชว์ตลอดเวลา)
            double cd2 = p2.getCooldownRemaining();
            if (cd2 > 0) {
                g.setColor(Color.YELLOW); // กำลังติดดีเลย์ (สีเหลือง)
            } else {
                g.setColor(Color.WHITE);  // พร้อมแล้ว (สีขาว)
            }
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(String.format("%.1fs", cd2), startX - 35, 20);
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