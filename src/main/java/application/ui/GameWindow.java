package application.ui;

import application.managers.GameManager;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameWindow extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Slime Wars");
        primaryStage.setResizable(false);

        GamePanel gameCanvas = new GamePanel();
        Group root = new Group(gameCanvas);

        // This implicitly assumes the canvas will be completely unaltered
        // by window decorations (title bar, borders).
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // --- NEW: Forces the window to tightly pack the contents ---
        // 'sizeToScene()' tells the window to adjust its outer borders
        // so that the internal Canvas gets EXACTLY the width/height it asked for.
        primaryStage.sizeToScene();

        gameCanvas.requestFocus();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}