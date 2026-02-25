package application.ui;

import application.managers.GameManager;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font; // <--- นำเข้า Font
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    public static final int TILE_SIZE = 50;
    private final int COLS = 15;
    private final int ROWS = 11;

    private GameManager gameManager;
    private Timer gameLoop;

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        setBackground(Color.DARK_GRAY);
        gameManager = new GameManager();

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

        // ✨ ถ้อยู่หน้าเมนูหลัก ให้วาดเมนูแล้วจบเลย
        if (gameManager.getCurrentState() == GameManager.GameState.MAIN_MENU) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 70));
            g.drawString("BOMB IT!", getWidth() / 2 - 160, getHeight() / 2 - 50);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Press '1' for Single Player", getWidth() / 2 - 140, getHeight() / 2 + 30);
            g.drawString("Press '2' for Two Players", getWidth() / 2 - 130, getHeight() / 2 + 80);
            return; // วาดเมนูเสร็จ ข้ามการวาดเกมด้านล่างไปเลย
        }

        // --- วาดฉากเกมปกติ ---
        g.setColor(new Color(100, 100, 100));
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                g.drawRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        gameManager.drawGame(g);

        // --- วาดข้อความตอนเกมจบ ---
        if (gameManager.getCurrentState() != GameManager.GameState.PLAYING) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setFont(new Font("Arial", Font.BOLD, 60));
            String msg = "";

            switch (gameManager.getCurrentState()) {
                case GAME_OVER: g.setColor(Color.RED); msg = "GAME OVER"; break;
                case YOU_WIN: g.setColor(new Color(34, 139, 34)); msg = "YOU WIN!"; break;
                case P1_WIN: g.setColor(Color.BLUE); msg = "PLAYER 1 WINS!"; break;
                case P2_WIN: g.setColor(Color.GREEN); msg = "PLAYER 2 WINS!"; break;
                case DRAW: g.setColor(Color.YELLOW); msg = "DRAW!"; break;
                default: break;
            }

            g.drawString(msg, getWidth() / 2 - (g.getFontMetrics().stringWidth(msg) / 2), getHeight() / 2);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press 'R' to Restart | Press 'M' to Menu", getWidth() / 2 - 170, getHeight() / 2 + 50);
        }
    }
}