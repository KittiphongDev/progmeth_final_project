package application.ui;

import application.managers.GameManager;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class GamePanel extends Canvas {
    public static final int TILE_SIZE = 50;
    private GameManager gameManager;
    private GameRenderer renderer;
    private AnimationTimer gameLoop;

    public GamePanel() {
        super(15 * TILE_SIZE, 11 * TILE_SIZE);

        this.gameManager = new GameManager();
        this.renderer = new GameRenderer(this, gameManager);

        setFocusTraversable(true);

        // Input Handling
        setOnKeyPressed(e -> gameManager.handleInput(e.getCode()));

        setOnMouseMoved(e -> renderer.updateMousePos(e.getX(), e.getY()));

        setOnMouseClicked(e -> {
            requestFocus();

            // Main Menu
            if (gameManager.getCurrentState() == GameManager.GameState.MAIN_MENU) {
                int result = renderer.getMenuClickResult(e.getX(), e.getY());

                if (result == 1 || result == 2) {
                    gameManager.startGame(result);
                }

                // ⭐ How To Play
                if (result == 3) {
                    gameManager.setCurrentState(GameManager.GameState.HOW_TO_PLAY);
                }
            }

            // ⭐ Click กลับ Menu จาก How To Play
            else if (gameManager.getCurrentState() == GameManager.GameState.HOW_TO_PLAY) {
                gameManager.setCurrentState(GameManager.GameState.MAIN_MENU);
            }
        });

        // Game Loop
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_000_000) { // ~60 FPS
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
        renderer.render(gc);
    }
}