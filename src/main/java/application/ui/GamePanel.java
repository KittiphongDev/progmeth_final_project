package application.ui;

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