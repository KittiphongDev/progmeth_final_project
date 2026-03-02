package application.ui;

import application.entities.Player;
import application.managers.GameManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GameRenderer {
    private final GamePanel panel;
    private final GameManager gm;

    private Image fireIcon, bombIcon, btnSinglePlayer, btnTwoPlayer;
    private final double BTN_WIDTH = 240;
    private double btn1X, btn1Y, btn2X, btn2Y, btn1Height, btn2Height;

    public GameRenderer(GamePanel panel, GameManager gm) {
        this.panel = panel;
        this.gm = gm;
        loadAssets();
    }

    private void loadAssets() {
        try { fireIcon = new Image(getClass().getResourceAsStream("/fire.png")); } catch (Exception e) {}
        try { bombIcon = new Image(getClass().getResourceAsStream("/bomb.png")); } catch (Exception e) {}
        try { btnSinglePlayer = new Image(getClass().getResourceAsStream("/single_player.png")); } catch (Exception e) {}
        try { btnTwoPlayer = new Image(getClass().getResourceAsStream("/two_player.png")); } catch (Exception e) {}
    }

    public void render(GraphicsContext gc) {
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        if (gm.getCurrentState() == GameManager.GameState.MAIN_MENU) {
            drawMenu(gc);
            return;
        }

        gm.drawGame(gc);
        drawHUD(gc);

        if (gm.getCurrentState() == GameManager.GameState.PAUSED) {
            drawOverlay(gc, "PAUSED", "Press 'Enter' to Resume | 'M' for Menu", Color.YELLOW);
        } else if (isGameOverState()) {
            drawOverlay(gc, getEndGameMessage(), "Press 'R' to Restart | 'M' for Menu", Color.WHITE);
        }

        drawPlayerStats(gc);
    }

    private void drawHUD(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("TIME: " + gm.getGameTimer(), panel.getWidth() / 2 - 40, 25);
    }

    private void drawMenu(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        gc.fillText("BOMB IT!", panel.getWidth() / 2, panel.getHeight() / 2 - 100);

        // Button 1: Single Player
        btn1X = panel.getWidth() / 2 - (BTN_WIDTH / 2);
        btn1Y = panel.getHeight() / 2 - 20;
        btn1Height = drawButton(gc, btnSinglePlayer, btn1X, btn1Y, "1 Player", Color.DARKGREEN);

        // Button 2: Two Players
        btn2X = panel.getWidth() / 2 - (BTN_WIDTH / 2);
        btn2Y = btn1Y + btn1Height + 25;
        btn2Height = drawButton(gc, btnTwoPlayer, btn2X, btn2Y, "2 Players", Color.DARKRED);
    }

    private double drawButton(GraphicsContext gc, Image img, double x, double y, String label, Color fallbackColor) {
        double height;
        if (img != null) {
            double ratio = img.getHeight() / img.getWidth();
            height = BTN_WIDTH * ratio;
            gc.drawImage(img, x, y, BTN_WIDTH, height);
        } else {
            height = 60;
            gc.setFill(fallbackColor);
            gc.fillRoundRect(x, y, BTN_WIDTH, height, 15, 15);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            gc.fillText(label, panel.getWidth() / 2, y + 38);
        }
        return height;
    }

    public int getMenuClickResult(double mx, double my) {
        if (mx >= btn1X && mx <= btn1X + BTN_WIDTH && my >= btn1Y && my <= btn1Y + btn1Height) return 1;
        if (mx >= btn2X && mx <= btn2X + BTN_WIDTH && my >= btn2Y && my <= btn2Y + btn2Height) return 2;
        return -1;
    }

    private void drawPlayerStats(GraphicsContext gc) {
        if (gm.getPlayer1() != null) drawStats(gc, gm.getPlayer1(), 15, true);
        if (gm.getPlayer2() != null) drawStats(gc, gm.getPlayer2(), (int)panel.getWidth() - 15 - 26, false);
    }

    private void drawStats(GraphicsContext gc, Player p, int startX, boolean leftToRight) {
        int size = 26, gap = 30, y = 8;
        // Fire Radius Icons
        for (int i = 0; i < p.getMaxFireRadius(); i++) {
            double x = leftToRight ? startX + (i * gap) : startX - (i * gap);
            gc.setGlobalAlpha(i < p.getBombRadius() ? 1.0 : 0.3);
            if (fireIcon != null) gc.drawImage(fireIcon, x, y, size, size);
            else { gc.setFill(Color.ORANGE); gc.fillOval(x, y, size, size); }
        }
        // Bomb Count Icons
        int available = p.getMaxBombs() - p.getActiveBombs();
        double bStartX = leftToRight ? startX + (p.getMaxFireRadius() * gap) + 20 : startX - (p.getMaxFireRadius() * gap) - 20;
        for (int i = 0; i < p.getMaxBombs(); i++) {
            double x = leftToRight ? bStartX + (i * gap) : bStartX - (i * gap);
            gc.setGlobalAlpha(i < available ? 1.0 : 0.3);
            if (bombIcon != null) gc.drawImage(bombIcon, x, y, size, size);
            else { gc.setFill(leftToRight ? Color.CYAN : Color.GREEN); gc.fillOval(x, y, size, size); }
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawOverlay(GraphicsContext gc, String title, String sub, Color titleColor) {
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(titleColor);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 45));
        gc.fillText(title, panel.getWidth() / 2, panel.getHeight() / 2);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText(sub, panel.getWidth() / 2, panel.getHeight() / 2 + 50);
    }

    private boolean isGameOverState() {
        GameManager.GameState s = gm.getCurrentState();
        return s != GameManager.GameState.PLAYING && s != GameManager.GameState.PAUSED && s != GameManager.GameState.MAIN_MENU;
    }

    private String getEndGameMessage() {
        return switch(gm.getCurrentState()) {
            case YOU_WIN -> "YOU WIN!";
            case GAME_OVER -> "GAME OVER";
            case P1_WIN -> "P1 WINS!";
            case P2_WIN -> "P2 WINS!";
            case DRAW -> "DRAW!";
            default -> "";
        };
    }
}