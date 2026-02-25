package application.entities;

import application.core.Destroyable;
import application.core.GameObject;
import java.awt.Graphics;
import java.awt.Color;

public class BreakableWall extends GameObject implements Destroyable {
    private boolean isDestroyed;

    public BreakableWall(int x, int y) {
        super(x, y);
        this.isDestroyed = false;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        if (!isDestroyed) {
            g.setColor(new Color(139, 69, 19));
            g.fillRect(getX() * 50, getY() * 50, 50, 50);
        }
    }

    @Override
    public void onDestroy() {
        this.isDestroyed = true;
        System.out.println("Box destroyed! Drop an item maybe?");
    }
}