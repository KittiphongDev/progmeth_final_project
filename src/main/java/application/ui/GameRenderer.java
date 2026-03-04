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

    private Image fireIcon, bombIcon, btnSinglePlayer, btnTwoPlayer, mapBackground,titleText;

    private final double BASE_BTN_WIDTH = 320.0;
    private final double HOVER_SCALE = 1.15; // 15% size increase on hover

    // --- NEW: Animation state variables ---
    private double currentW1 = BASE_BTN_WIDTH;
    private double currentW2 = BASE_BTN_WIDTH;
    private final double ANIMATION_SPEED = 0.15; // Adjust this to make it faster/slower (0.0 to 1.0)

    private double btn1X, btn1Y, btn1Width, btn1Height;
    private double btn2X, btn2Y, btn2Width, btn2Height;

    private double mouseX, mouseY;

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
        try { mapBackground = new Image(getClass().getResourceAsStream("/map_bg.png")); } catch (Exception e) { System.out.println("Error loading background"); }
        try { titleText = new Image(getClass().getResourceAsStream("/title.png")); } catch (Exception e) { System.out.println("Error loading title"); }
    }

    public void updateMousePos(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public void render(GraphicsContext gc) {
        if (mapBackground != null) {
            gc.drawImage(mapBackground, 0, 0, panel.getWidth(), panel.getHeight());
        } else {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        }

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
        // --- UPDATED: Centered and Scalable Title Image ---
        if (titleText != null) {
            // Set your desired width for the title here
            double titleTargetWidth = 550.0;

            // Calculate proportional height to maintain aspect ratio
            double titleRatio = titleText.getHeight() / titleText.getWidth();
            double titleTargetHeight = titleTargetWidth * titleRatio;

            // Calculate perfectly centered X coordinate
            double titleX = (panel.getWidth() / 2) - (titleTargetWidth / 2);

            // Calculate Y coordinate (adjust the -180 to move it up or down)
            double titleY = (panel.getHeight() / 2) - 400;

            gc.drawImage(titleText, titleX, titleY, titleTargetWidth, titleTargetHeight);
        } else {
            // Fallback text if the image fails to load
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GAME TITLE", panel.getWidth() / 2, panel.getHeight() / 2 - 120);
        }

        // --- Logic for Button 1 (Single Player) ---
        boolean hover1 = isMouseOver(mouseX, mouseY, btn1X, btn1Y, btn1Width, btn1Height);
        double targetW1 = hover1 ? BASE_BTN_WIDTH * HOVER_SCALE : BASE_BTN_WIDTH;

        // Smooth interpolation formula
        currentW1 += (targetW1 - currentW1) * ANIMATION_SPEED;

        btn1X = panel.getWidth() / 2 - (currentW1 / 2);
        btn1Y = panel.getHeight() / 2 + 25;

        double[] size1 = drawButton(gc, btnSinglePlayer, btn1X, btn1Y, currentW1, "1 Player", Color.DARKGREEN);
        btn1Width = size1[0];
        btn1Height = size1[1];

        // --- Logic for Button 2 (Two Players) ---
        boolean hover2 = isMouseOver(mouseX, mouseY, btn2X, btn2Y, btn2Width, btn2Height);
        double targetW2 = hover2 ? BASE_BTN_WIDTH * HOVER_SCALE : BASE_BTN_WIDTH;

        // Smooth interpolation formula
        currentW2 += (targetW2 - currentW2) * ANIMATION_SPEED;

        btn2X = panel.getWidth() / 2 - (currentW2 / 2);
        btn2Y = btn1Y + 100; // Dynamic spacing

        double[] size2 = drawButton(gc, btnTwoPlayer, btn2X, btn2Y, currentW2, "2 Players", Color.DARKRED);
        btn2Width = size2[0];
        btn2Height = size2[1];
    }

    private double[] drawButton(GraphicsContext gc, Image img, double x, double y, double width, String label, Color fallbackColor) {
        double height;
        if (img != null) {
            double ratio = img.getHeight() / img.getWidth();
            height = width * ratio;
            gc.drawImage(img, x, y, width, height);
        } else {
            height = 75;
            gc.setFill(fallbackColor);
            gc.fillRoundRect(x, y, width, height, 20, 20);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
            gc.fillText(label, x + (width / 2), y + (height / 2) + 10);
        }
        return new double[]{width, height};
    }

    private boolean isMouseOver(double mx, double my, double bx, double by, double bw, double bh) {
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    public int getMenuClickResult(double mx, double my) {
        if (isMouseOver(mx, my, btn1X, btn1Y, btn1Width, btn1Height)) return 1;
        if (isMouseOver(mx, my, btn2X, btn2Y, btn2Width, btn2Height)) return 2;
        return -1;
    }

    private void drawPlayerStats(GraphicsContext gc) {
        if (gm.getPlayer1() != null) drawStats(gc, gm.getPlayer1(), 15, true);
        if (gm.getPlayer2() != null) drawStats(gc, gm.getPlayer2(), (int)panel.getWidth() - 15 - 26, false);
    }

    private void drawStats(GraphicsContext gc, Player p, int startX, boolean leftToRight) {
        int size = 26, gap = 30, y = 8;
        for (int i = 0; i < p.getMaxFireRadius(); i++) {
            double x = leftToRight ? startX + (i * gap) : startX - (i * gap);
            gc.setGlobalAlpha(i < p.getBombRadius() ? 1.0 : 0.3);
            if (fireIcon != null) gc.drawImage(fireIcon, x, y, size, size);
            else { gc.setFill(Color.ORANGE); gc.fillOval(x, y, size, size); }
        }
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