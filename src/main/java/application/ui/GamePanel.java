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

    // ประกาศตัวแปรเก็บรูปภาพไอคอน
    private Image fireIcon;
    private Image bombIcon;

    // ✨ 1. ประกาศตัวแปรเก็บรูปภาพปุ่มเมนู
    private Image btnSinglePlayer;
    private Image btnTwoPlayer;

    // ✨ 2. กำหนดขนาดและตำแหน่งของปุ่มเพื่อให้คำนวณการคลิกได้
    private final double BTN_WIDTH = 240;
    private final double BTN_HEIGHT = 60;
    private double btn1X, btn1Y, btn2X, btn2Y;

    private GameManager gameManager;
    private AnimationTimer gameLoop;

    public GamePanel() {
        super(15 * 50, 11 * 50);

        // โหลดรูปภาพไอเทม
        try { fireIcon = new Image(getClass().getResourceAsStream("/fire.png")); } catch (Exception e) {}
        try { bombIcon = new Image(getClass().getResourceAsStream("/bomb.png")); } catch (Exception e) {}

        // ✨ 3. โหลดรูปภาพปุ่ม (ต้องนำไฟล์ single_player.png และ two_player.png ไปใส่ใน resources)
        try { btnSinglePlayer = new Image(getClass().getResourceAsStream("/single_player.png")); } catch (Exception e) { System.out.println("⚠️ No single_player.png found"); }
        try { btnTwoPlayer = new Image(getClass().getResourceAsStream("/two_player.png")); } catch (Exception e) { System.out.println("⚠️ No two_player.png found"); }

        this.gameManager = new GameManager();
        setFocusTraversable(true);

        // ระบบรับอินพุตคีย์บอร์ดเดิม
        setOnKeyPressed(e -> {
            gameManager.handleInput(e.getCode());
        });

        // ✨ 4. เพิ่มระบบรับอินพุตเมาส์ (Mouse Click)
        setOnMouseClicked(e -> {
            requestFocus(); // ให้ Canvas โฟกัสเสมอเมื่อถูกคลิก (เพื่อไม่ให้หลุดการคุมคีย์บอร์ด)

            if (gameManager.getCurrentState() == GameManager.GameState.MAIN_MENU) {
                double mouseX = e.getX();
                double mouseY = e.getY();

                // เช็คว่าคลิกโดนปุ่ม Single Player หรือไม่
                if (mouseX >= btn1X && mouseX <= btn1X + BTN_WIDTH &&
                        mouseY >= btn1Y && mouseY <= btn1Y + BTN_HEIGHT) {
                    gameManager.startGame(1);
                }
                // เช็คว่าคลิกโดนปุ่ม Two Players หรือไม่
                else if (mouseX >= btn2X && mouseX <= btn2X + BTN_WIDTH &&
                        mouseY >= btn2Y && mouseY <= btn2Y + BTN_HEIGHT) {
                    gameManager.startGame(2);
                }
            }
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
            drawOverlay(gc, "PAUSED", "Press 'Enter' to Resume", Color.YELLOW);
        }

        if (isGameOverState()) {
            String msg = getEndGameMessage();
            drawOverlay(gc, msg, "Press 'R' to Restart | 'M' for Menu", Color.WHITE);
        }

        // --- ส่วนวาด UI พลังของ Player (เหมือนเดิม ไม่ได้แก้ไข) ---
        int ICON_SIZE = 26;
        int ICON_GAP = 30;
        int ICON_Y = 8;

        if (gameManager.getPlayer1() != null) {
            Player p1 = gameManager.getPlayer1();
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            int fireStartX = 15;
            int currentFire = p1.getBombRadius();
            int maxFire = p1.getMaxFireRadius();

            for (int i = 0; i < maxFire; i++) {
                if (i < currentFire) {
                    if (fireIcon != null) gc.drawImage(fireIcon, fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    else { gc.setFill(Color.ORANGE); gc.fillOval(fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE); }
                } else {
                    if (fireIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(fireIcon, fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else { gc.setFill(Color.rgb(100, 50, 0)); gc.fillOval(fireStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE); }
                }
            }

            int bombStartX = fireStartX + (maxFire * ICON_GAP) + 20;
            int maxBombs = p1.getMaxBombs();
            int activeBombs = p1.getActiveBombs();
            int brightCount = maxBombs - activeBombs;

            for (int i = 0; i < maxBombs; i++) {
                if (i < brightCount) {
                    if (bombIcon != null) gc.drawImage(bombIcon, bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                    else { gc.setFill(Color.CYAN); gc.fillOval(bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE); }
                } else {
                    if (bombIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(bombIcon, bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else { gc.setFill(Color.rgb(0, 80, 100)); gc.fillOval(bombStartX + (i * ICON_GAP), ICON_Y, ICON_SIZE, ICON_SIZE); }
                }
            }

            double cd1 = p1.getCooldownRemaining();
            if (cd1 > 0) gc.setFill(Color.YELLOW); else gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 14));
            gc.fillText(String.format("%.1fs", cd1), bombStartX + (maxBombs * ICON_GAP) + 5, 28);
        }

        if (gameManager.getPlayer2() != null) {
            Player p2 = gameManager.getPlayer2();
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            double screenWidth = getWidth();
            int p2MaxFire = p2.getMaxFireRadius();
            int p2MaxBombs = p2.getMaxBombs();
            double startX = screenWidth - 15 - ICON_SIZE;

            int currentFire = p2.getBombRadius();
            for (int i = 0; i < p2MaxFire; i++) {
                double x = startX - (i * ICON_GAP);
                if (i < currentFire) {
                    if (fireIcon != null) gc.drawImage(fireIcon, x, ICON_Y, ICON_SIZE, ICON_SIZE);
                    else { gc.setFill(Color.ORANGE); gc.fillOval(x, ICON_Y, ICON_SIZE, ICON_SIZE); }
                } else {
                    if (fireIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(fireIcon, x, ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else { gc.setFill(Color.rgb(100, 50, 0)); gc.fillOval(x, ICON_Y, ICON_SIZE, ICON_SIZE); }
                }
            }

            double bombStartX = startX - (p2MaxFire * ICON_GAP) - 20;
            int activeBombs = p2.getActiveBombs();
            int brightCount = p2MaxBombs - activeBombs;

            for (int i = 0; i < p2MaxBombs; i++) {
                double x = bombStartX - (i * ICON_GAP);
                if (i < brightCount) {
                    if (bombIcon != null) gc.drawImage(bombIcon, x, ICON_Y, ICON_SIZE, ICON_SIZE);
                    else { gc.setFill(Color.GREEN); gc.fillOval(x, ICON_Y, ICON_SIZE, ICON_SIZE); }
                } else {
                    if (bombIcon != null) {
                        gc.setGlobalAlpha(0.3);
                        gc.drawImage(bombIcon, x, ICON_Y, ICON_SIZE, ICON_SIZE);
                        gc.setGlobalAlpha(1.0);
                    } else { gc.setFill(Color.rgb(0, 80, 0)); gc.fillOval(x, ICON_Y, ICON_SIZE, ICON_SIZE); }
                }
            }

            double cd2 = p2.getCooldownRemaining();
            if (cd2 > 0) gc.setFill(Color.YELLOW); else gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 14));
            double textX = bombStartX - (p2MaxBombs * ICON_GAP) - 15;
            gc.fillText(String.format("%.1fs", cd2), textX, 28);
        }
    }

    // ✨ 5. อัปเดตเมธอดวาดเมนูให้วาดปุ่มแทนตัวหนังสือ
    private void drawMenu(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);

        // วาดชื่อเกม
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        gc.fillText("BOMB IT!", getWidth() / 2, getHeight() / 2 - 80);

        // คำนวณจุดที่จะวาดปุ่ม
        btn1X = getWidth() / 2 - (BTN_WIDTH / 2);
        btn1Y = getHeight() / 2 - 10;

        btn2X = getWidth() / 2 - (BTN_WIDTH / 2);
        btn2Y = getHeight() / 2 + 70;

        // วาดปุ่มที่ 1 (Single Player)
        if (btnSinglePlayer != null) {
            gc.drawImage(btnSinglePlayer, btn1X, btn1Y, BTN_WIDTH, BTN_HEIGHT);
        } else {
            // Fallback: ถ้ายังไม่มีรูป ให้วาดสี่เหลี่ยมคล้ายๆ ปุ่มแทน
            gc.setFill(Color.DARKGREEN);
            gc.fillRoundRect(btn1X, btn1Y, BTN_WIDTH, BTN_HEIGHT, 15, 15);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            gc.fillText("1 Player", getWidth() / 2, btn1Y + 38);
        }

        // วาดปุ่มที่ 2 (Two Players)
        if (btnTwoPlayer != null) {
            gc.drawImage(btnTwoPlayer, btn2X, btn2Y, BTN_WIDTH, BTN_HEIGHT);
        } else {
            // Fallback
            gc.setFill(Color.DARKRED);
            gc.fillRoundRect(btn2X, btn2Y, BTN_WIDTH, BTN_HEIGHT, 15, 15);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            gc.fillText("2 Players", getWidth() / 2, btn2Y + 38);
        }

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