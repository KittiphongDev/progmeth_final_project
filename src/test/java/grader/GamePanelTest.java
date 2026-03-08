package grader;

import static org.junit.jupiter.api.Assertions.*;

import application.ui.GamePanel;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GamePanelTest {

    @BeforeAll
    static void initJFX() {
        Platform.startup(() -> {}); // start JavaFX toolkit
    }

    // ทดสอบว่าขนาด Canvas ถูกต้อง
    @Test
    void testCanvasSize() {
        GamePanel panel = new GamePanel();

        assertEquals(750, panel.getWidth());
        assertEquals(550, panel.getHeight());
    }

    // ทดสอบว่า panel สามารถรับ focus ได้
    @Test
    void testPanelFocusable() {
        GamePanel panel = new GamePanel();

        assertTrue(panel.isFocusTraversable());
    }

    // ทดสอบว่า panel ถูกสร้างได้โดยไม่เป็น null
    @Test
    void testPanelCreation() {
        GamePanel panel = new GamePanel();

        assertNotNull(panel);
    }

    // ทดสอบค่า TILE_SIZE constant
    @Test
    void testTileSizeConstant() {
        assertEquals(50, GamePanel.TILE_SIZE);
    }
}