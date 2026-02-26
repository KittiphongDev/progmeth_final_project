package application.ui;

import application.entities.Player;
import application.managers.GameManager;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GamePanel extends Canvas {
    public static final int TILE_SIZE = 50;
    private final int COLS = 15;
    private final int ROWS = 11;

    // ประกาศตัวแปรเก็บรูปภาพ
    private Image fireIcon;
    private Image bombIcon;

    private GameManager gameManager;
    private AnimationTimer gameLoop;

    public GamePanel() {
        super(15 * 50, 11 * 50);

        try {
            fireIcon = new Image(getClass().getResourceAsStream("/fire.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not load fire.png");
        }
        try {
            bombIcon = new Image(getClass().getResourceAsStream("/bomb.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not load bomb.png");
        }

        this.gameManager = new GameManager();

        setFocusTraversable(true);

        setOnKeyPressed(e -> {
            gameManager.handleInput(e.getCode());
        });

        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_000_000) {
                    gameManager.updateGame();
                    draw();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, getWidth(), getHeight());

        if (gameManager.getCurrentState() == GameManager.GameState.MAIN_MENU) {
            drawMenu(gc);
            return;
        }

        gameManager.drawGame(gc);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.LEFT);

        String timeText = "TIME: " + gameManager.getGameTimer();
        gc.fillText(timeText, getWidth() / 2 - 40, 25);

        if (gameManager.getCurrentState() == GameManager.GameState.PAUSED) {
            drawOverlay(gc, "PAUSED", "Press 'P' to Resume", Color.YELLOW);
        }

        if (isGameOverState()) {
            String msg = getEndGameMessage();
            drawOverlay(gc, msg, "Press 'R' to Restart | 'M' for Menu", Color.WHITE);
        }

        // ✨ 1. กำหนดสเกลขนาดไอคอนและช่องไฟให้ใหญ่ขึ้นตรงนี้ครับ
        int ICON_SIZE = 26; // ขยายจาก 14 เป็น 26
        int ICON_GAP = 30;  // ระยะห่างระหว่างไอคอน
        int ICON_Y = 8;     // ตำแหน่งแนวตั้ง

        // ✨ วาดสถานะของ Player 1 (ซ้ายบน)
        if (gameManager.getPlayer1() != null) {
            Player p1 = gameManager.getPlayer1();
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            // --- 🔥 วาดไอคอนระยะไฟ (Player 1) ---
            int fireStartX = 15;
            int currentFire = p1.getBombRadius();
            int maxFire = p1.getMaxFireRadius();

            for (int i = 0; i < maxFire; i++) {
                if (i < currentFire) {
                    if (fireIcon != null) {
                        gc.drawImage(fireIcon, fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    } else {
                        gc.setFill(Color.ORANGE);
                        gc.fillOval(fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                } else {
                    if (fireIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(fireIcon, fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else {
                        gc.setFill(Color.rgb(100, 50, 0));
                        gc.fillOval(fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                }
            }

            // --- 💣 วาดไอคอนระเบิด (Player 1) ---
            int bombStartX = fireStartX + (maxFire * ICON_GAP) + 20;

            int maxBombs = p1.getMaxBombs();
            int activeBombs = p1.getActiveBombs();

            // ✨ 2. แก้ไขให้มืด "เฉพาะจำนวนลูกระเบิดที่วางอยู่บนพื้น (activeBombs)" เท่านั้น
            int brightCount = maxBombs - activeBombs;

            for (int i = 0; i < maxBombs; i++) {
                if (i < brightCount) {
                    if (bombIcon != null) {
                        gc.drawImage(bombIcon, bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    } else {
                        gc.setFill(Color.CYAN);
                        gc.fillOval(bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                } else {
                    if (bombIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(bombIcon, bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else {
                        gc.setFill(Color.rgb(0, 80, 100));
                        gc.fillOval(bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                }
            }

            // ⏳ โชว์ตัวเลข Cooldown ของ P1
            double cd1 = p1.getCooldownRemaining();
            if (cd1 > 0) gc.setFill(Color.YELLOW); else gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText(String.format("%.1fs", cd1), bombStartX + (maxBombs * ICON_GAP) + 5, 28);
        }

        // ✨ วาดสถานะของ Player 2 (ขวาบน)
        if (gameManager.getPlayer2() != null) {
            Player p2 = gameManager.getPlayer2();
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            double screenWidth = getWidth();
            int p2MaxFire = p2.getMaxFireRadius();
            int p2MaxBombs = p2.getMaxBombs();

            // --- 💣 วาดไอคอนระเบิด (Player 2) ---
            double bombStartX = screenWidth - (p2MaxBombs * ICON_GAP) - 50;

            int activeBombs = p2.getActiveBombs();

            // ✨ 2. แก้ไขให้มืดเฉพาะลูกระเบิดที่วางอยู่บนพื้นเท่านั้น
            int brightCount = p2MaxBombs - activeBombs;

            for (int i = 0; i < p2MaxBombs; i++) {
                if (i < brightCount) {
                    if (bombIcon != null) {
                        gc.drawImage(bombIcon, bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    } else {
                        gc.setFill(Color.GREEN);
                        gc.fillOval(bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                } else {
                    if (bombIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(bombIcon, bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else {
                        gc.setFill(Color.rgb(0, 80, 0));
                        gc.fillOval(bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                }
            }

            // ⏳ โชว์ตัวเลข Cooldown ของ P2
            double cd2 = p2.getCooldownRemaining();
            if (cd2 > 0) gc.setFill(Color.YELLOW); else gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText(String.format("%.1fs", cd2), bombStartX - 40, 28);


            // --- 🔥 วาดไอคอนระยะไฟ (Player 2) ---
            double fireStartX = bombStartX - (p2MaxFire * ICON_GAP) - 20;
            int currentFire = p2.getBombRadius();

            for (int i = 0; i < p2MaxFire; i++) {
                if (i < currentFire) {
                    if (fireIcon != null) {
                        gc.drawImage(fireIcon, fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    } else {
                        gc.setFill(Color.ORANGE);
                        gc.fillOval(fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                } else {
                    if (fireIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(fireIcon, fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else {
                        gc.setFill(Color.rgb(100, 50, 0));
                        gc.fillOval(fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    }
                }
            }
        }
    }

    private void drawMenu(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        gc.fillText("BOMB IT!", getWidth() / 2, getHeight() / 2 - 50);

        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", 25));
        gc.fillText("Press '1' for Single Player", getWidth() / 2, getHeight() / 2 + 30);
        gc.fillText("Press '2' for Two Players", getWidth() / 2, getHeight() / 2 + 80);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawOverlay(GraphicsContext gc, String title, String sub, Color titleColor) {
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(titleColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 45));
        gc.fillText(title, getWidth() / 2, getHeight() / 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText(sub, getWidth() / 2, getHeight() / 2 + 50);

        gc.setTextAlign(TextAlignment.LEFT);
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