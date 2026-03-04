package application.ui;

import application.managers.GameManager;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GameWindow extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Slime Warz");
        primaryStage.setResizable(false);

        try {
            Image gameLogo = new Image(getClass().getResourceAsStream("/logo.png"));
            primaryStage.getIcons().add(gameLogo);
        } catch (Exception e) {
            System.out.println("⚠️ Warning: ไม่สามารถโหลดรูปภาพโลโก้ได้ กรุณาตรวจสอบว่ามีไฟล์ '/logo.png' ในโฟลเดอร์ resources หรือไม่");
        }

        GamePanel gameCanvas = new GamePanel();
        Group root = new Group(gameCanvas);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.sizeToScene();

        gameCanvas.requestFocus();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}