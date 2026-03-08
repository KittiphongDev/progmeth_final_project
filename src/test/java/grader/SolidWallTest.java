package grader;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.entities.SolidWall;

public class SolidWallTest {

    SolidWall wall1;
    SolidWall wall2;

    @BeforeEach
    void setup() {
        // สร้างกำแพงสองอันไว้ใช้ในการทดสอบ
        wall1 = new SolidWall(2, 3);
        wall2 = new SolidWall(7, 1);
    }

    @Test
    void testConstructor() {
        // ทดสอบว่า constructor กำหนดตำแหน่ง x,y ถูกต้อง

        assertEquals(2, wall1.getX());
        assertEquals(3, wall1.getY());

        assertEquals(7, wall2.getX());
        assertEquals(1, wall2.getY());
    }

    @Test
    void testUpdateDoesNothing() {
        // กำแพงเป็นวัตถุ static ในเกม
        // ดังนั้นการเรียก update ไม่ควรเปลี่ยนตำแหน่ง

        wall1.update(System.currentTimeMillis());

        assertEquals(2, wall1.getX());
        assertEquals(3, wall1.getY());
    }

    @Test
    void testMultipleUpdatesStillSamePosition() {
        // เรียก update หลายครั้ง
        // ตำแหน่งของกำแพงยังต้องเหมือนเดิม

        wall1.update(System.currentTimeMillis());
        wall1.update(System.currentTimeMillis());
        wall1.update(System.currentTimeMillis());

        assertEquals(2, wall1.getX());
        assertEquals(3, wall1.getY());
    }

    @Test
    void testWallObjectsAreDifferent() {
        // ทดสอบว่า wall1 และ wall2 เป็นคนละ object

        assertNotEquals(wall1, wall2);
    }

    @Test
    void testWallNotNull() {
        // ตรวจสอบว่า object ถูกสร้างขึ้นจริง ไม่เป็น null

        assertNotNull(wall1);
        assertNotNull(wall2);
    }

    @Test
    void testWallPositionValues() {
        // ตรวจสอบว่าค่าพิกัดของกำแพงตรงกับที่กำหนดตอนสร้าง

        int x1 = wall1.getX();
        int y1 = wall1.getY();

        int x2 = wall2.getX();
        int y2 = wall2.getY();

        assertEquals(2, x1);
        assertEquals(3, y1);

        assertEquals(7, x2);
        assertEquals(1, y2);
    }
}