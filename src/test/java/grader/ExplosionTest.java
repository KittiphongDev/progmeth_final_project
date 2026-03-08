package grader;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.entities.Explosion;

public class ExplosionTest {

    Explosion ex1;
    Explosion ex2;

    // ทำงานก่อนทุก test เพื่อสร้าง object ใหม่
    @BeforeEach
    void setup() {
        ex1 = new Explosion(5, 6);
        ex2 = new Explosion(1, 2);
    }

    // Test ว่า constructor กำหนดตำแหน่ง x,y ถูกต้อง
    @Test
    void testConstructor() {
        assertEquals(5, ex1.getX());
        assertEquals(6, ex1.getY());

        assertEquals(1, ex2.getX());
        assertEquals(2, ex2.getY());
    }

    // Test ว่า explosion ที่เพิ่งสร้างขึ้นมายังไม่หมดเวลา
    @Test
    void testNotFinishedImmediately() {
        assertFalse(ex1.isFinished());
    }

    // Test ว่า explosion จะหมดเวลาเมื่อผ่าน duration (ประมาณ 300 ms)
    @Test
    void testExplosionEventuallyFinished() throws InterruptedException {
        Thread.sleep(350);
        assertTrue(ex1.isFinished());
    }

    // Test ว่า explosion ที่เวลายังไม่ครบ duration จะยังไม่ finished
    @Test
    void testExplosionBeforeDuration() throws InterruptedException {
        Thread.sleep(100);
        assertFalse(ex1.isFinished());
    }

    // Test ว่า explosion หลายตัวทำงานแยกจากกัน (independent)
    @Test
    void testMultipleExplosionsIndependent() throws InterruptedException {
        Thread.sleep(350);

        assertTrue(ex1.isFinished());
        assertTrue(ex2.isFinished());
    }

    // Test ว่า update() ไม่ทำให้ตำแหน่งของ explosion เปลี่ยน
    @Test
    void testUpdateDoesNotChangePosition() {
        int x = ex1.getX();
        int y = ex1.getY();

        ex1.update(System.currentTimeMillis());

        assertEquals(x, ex1.getX());
        assertEquals(y, ex1.getY());
    }

    // เพิ่ม test: เรียก update หลายครั้ง ตำแหน่งยังเหมือนเดิม
    @Test
    void testMultipleUpdatesStillSamePosition() {
        int x = ex1.getX();
        int y = ex1.getY();

        ex1.update(System.currentTimeMillis());
        ex1.update(System.currentTimeMillis());
        ex1.update(System.currentTimeMillis());

        assertEquals(x, ex1.getX());
        assertEquals(y, ex1.getY());
    }

    // เพิ่ม test: explosion คนละตำแหน่งต้องไม่เท่ากัน
    @Test
    void testExplosionObjectsAreDifferent() {
        assertNotEquals(ex1, ex2);
    }
}