package application.entities;

import application.core.Destroyable;
import application.core.GameObject;
import java.awt.Graphics;
import java.awt.Color;

public class Player extends GameObject implements Destroyable {
    private boolean isAlive;
    private Color playerColor; // ✨ เพิ่มตัวแปรเก็บสีของตัวละคร

    // ✨ อัปเดต Constructor ให้รับค่าสีเข้ามาด้วย
    public Player(int x, int y, Color color) {
        super(x, y);
        this.isAlive = true;
        this.playerColor = color;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    @Override
    public void update() {}

    @Override
    public void draw(Graphics g) {
        if (!isAlive) return;
        // ✨ ใช้สีของตัวเองในการวาด
        g.setColor(playerColor);
        g.fillRect(getX() * 50, getY() * 50, 50, 50);
    }

    @Override
    public void onDestroy() {
        this.isAlive = false;
        System.out.println("Player at " + getX() + "," + getY() + " is dead!");
    }

    private int maxBombs = 1;
    private int activeBombs = 0;
    private int bombRadius = 1;

    private long lastBombTime = 0; // เก็บเวลาตอนที่วางระเบิดล่าสุด
    private static final long BOMB_COOLDOWN = 500; // ตั้งคูลดาวน์ไว้ 0.5 วินาที (500 มิลลิวินาที เปลี่ยนได้ครับ)

    // เมธอดสำหรับจัดการจำนวนระเบิด
    public boolean canPlaceBomb() {
        long currentTime = System.currentTimeMillis();
        // ✨ เช็ค 2 อย่าง: 1. ระเบิดในฉากยังไม่เกินโควตา 2. เวลาผ่านไปเกินคูลดาวน์แล้ว
        if (activeBombs < maxBombs && (currentTime - lastBombTime >= BOMB_COOLDOWN)) {
            return true;
        }
        return false;
    }

    public void increaseActiveBombs() {
        activeBombs++;
        lastBombTime = System.currentTimeMillis(); // ✨ เริ่มนับคูลดาวน์ใหม่ทันทีที่วางระเบิด
    }

    public void decreaseActiveBombs() {
        if (activeBombs > 0) activeBombs--;
    }

    // สมมติโควตาสูงสุดคือ 3 ลูก
    public boolean addMaxBombs() {
        if (maxBombs < 3) {
            maxBombs++;
            return true;  // อัปเกรดสำเร็จ
        }
        return false; // เต็มแล้ว
    }

    // สมมติระยะไฟสูงสุดคือ 5 ช่อง
    // กำหนดระยะไฟสูงสุดคือ 3 ช่อง
    public boolean addRadius() {
        if (bombRadius < 3) { // ✨ เปลี่ยนตรงนี้เป็นเลข 3 ครับ
            bombRadius++;
            return true;  // อัปเกรดสำเร็จ
        }
        return false; // เต็ม MAX แล้ว (ส่งค่า false กลับไปให้ไอเทมโชว์ข้อความ)
    }

    public int getBombRadius() {
        return bombRadius;
    }
    public int getMaxBombs() {
        return maxBombs;
    }
    // ✨ เพิ่มเมธอดนี้เพื่อส่งค่าให้ UI เอาไปวาดรูป
    public int getActiveBombs() {
        return activeBombs;
    }

    // ✨ เพิ่มเมธอดนี้เข้าไปครับ
    public double getCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastBombTime;
        if (elapsed >= BOMB_COOLDOWN) return 0.0;
        return (BOMB_COOLDOWN - elapsed) / 1000.0; // หาร 1000 เพื่อแปลงเป็นหน่วยวินาที (เช่น 0.5)
    }
}