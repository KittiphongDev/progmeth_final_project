package grader;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import application.managers.SoundManager;

public class SoundManagerTest {

    // ทดสอบว่า SoundManager เป็น Singleton
    @Test
    void testSingleton() {
        SoundManager s1 = SoundManager.getInstance();
        SoundManager s2 = SoundManager.getInstance();

        assertEquals(s1, s2);
    }

    // ทดสอบว่า instance ไม่เป็น null
    @Test
    void testInstanceNotNull() {
        SoundManager sm = SoundManager.getInstance();

        assertNotNull(sm);
    }

    // ทดสอบว่าเล่น sound effect ได้โดยไม่เกิด exception
    @Test
    void testPlaySFX() {
        SoundManager sm = SoundManager.getInstance();

        assertDoesNotThrow(() -> {
            sm.playBoom();
            sm.playPop();
            sm.playVictory();
        });
    }

    // ทดสอบว่าเล่นเสียง fuse และ footstep ได้
    @Test
    void testPlayOtherSFX() {
        SoundManager sm = SoundManager.getInstance();

        assertDoesNotThrow(() -> {
            sm.playFuse();
            sm.playFootstep();
            sm.playCount();
        });
    }

    // ทดสอบว่า playBGM ไม่ทำให้โปรแกรม crash
    @Test
    void testPlayBGM() {
        SoundManager sm = SoundManager.getInstance();

        assertDoesNotThrow(() -> {
            sm.playBGM("bg_menu.mp3");
        });
    }

    // ทดสอบว่าเปลี่ยน BGM หลายครั้งไม่ error
    @Test
    void testSwitchBGM() {
        SoundManager sm = SoundManager.getInstance();

        assertDoesNotThrow(() -> {
            sm.playBGM("bg_menu.mp3");
            sm.playBGM("bg_in_game.mp3");
        });
    }
}