package application.entities;

import application.core.Destroyable;
import application.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Player extends GameObject implements Destroyable {
    private boolean isAlive;
    private Color playerColor;
    private static final int MAX_FIRE_RADIUS = 3;

    public enum Direction { FRONT, BACK, LEFT, RIGHT }
    private Direction currentDirection = Direction.FRONT;

    // REMOVED 'static' so each player instance can hold its own unique sprites
    private Image idleFront, idleBack, idleLeft, idleRight;
    private boolean spritesLoaded = false;
    private int playerNumber;

    // ADDED playerNumber to constructor
    public Player(int x, int y, Color color, int playerNumber) {
        super(x, y);
        this.isAlive = true;
        this.playerColor = color;
        this.playerNumber = playerNumber;
        loadSprites();
    }

    private void loadSprites() {
        if (!spritesLoaded) {
            try {
                // Dynamically load from Slime1 or Slime2 folders based on playerNumber
                String basePath = "/Slime" + playerNumber + "/idle/Slime" + playerNumber + "_Idle_";

                idleFront = new Image(getClass().getResourceAsStream(basePath + "front.gif"));
                idleBack = new Image(getClass().getResourceAsStream(basePath + "back.gif"));
                idleLeft = new Image(getClass().getResourceAsStream(basePath + "left.gif"));
                idleRight = new Image(getClass().getResourceAsStream(basePath + "right.gif"));

                // Verify at least one loaded successfully
                if (idleFront != null && !idleFront.isError()) {
                    spritesLoaded = true;
                } else {
                    System.err.println("Warning: Slime " + playerNumber + " sprites not found. Using fallback colors.");
                }
            } catch (Exception e) {
                System.err.println("Exception loading player " + playerNumber + " sprites: " + e.getMessage());
            }
        }
    }

    public void setDirection(Direction dir) {
        this.currentDirection = dir;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    @Override
    public void update() {}

    @Override
    public void draw(GraphicsContext gc) {
        if (!isAlive) return;

        double drawX = getX() * 50;
        double drawY = getY() * 50;

        if (spritesLoaded) {
            double spriteSize = 120.0;
            double offset = (spriteSize - 50) / 2.0;

            Image currentSprite = switch (currentDirection) {
                case FRONT -> idleFront;
                case BACK -> idleBack;
                case LEFT -> idleLeft;
                case RIGHT -> idleRight;
            };

            gc.drawImage(currentSprite, drawX - offset, drawY - offset, spriteSize, spriteSize);
        } else {
            gc.setFill(playerColor);
            gc.fillRect(drawX, drawY, 50, 50);
        }
    }

    @Override
    public void onDestroy() {
        this.isAlive = false;
        System.out.println("Player " + playerNumber + " at " + getX() + "," + getY() + " is dead!");
    }

    // --- Bomb Logic ---
    private int maxBombs = 1;
    private int activeBombs = 0;
    private int bombRadius = 1;

    private long lastBombTime = 0;
    private static final long BOMB_COOLDOWN = 500;

    public boolean canPlaceBomb() {
        long currentTime = System.currentTimeMillis();
        return (activeBombs < maxBombs && (currentTime - lastBombTime >= BOMB_COOLDOWN));
    }

    public void increaseActiveBombs() {
        activeBombs++;
        lastBombTime = System.currentTimeMillis();
    }

    public void decreaseActiveBombs() {
        if (activeBombs > 0) activeBombs--;
    }

    public boolean addMaxBombs() {
        if (maxBombs < 3) {
            maxBombs++;
            return true;
        }
        return false;
    }

    public boolean addRadius() {
        if (bombRadius < MAX_FIRE_RADIUS) {
            bombRadius++;
            return true;
        }
        return false;
    }

    public int getMaxFireRadius() { return MAX_FIRE_RADIUS; }
    public int getBombRadius() { return bombRadius; }
    public int getMaxBombs() { return maxBombs; }
    public int getActiveBombs() { return activeBombs; }

    public double getCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastBombTime;
        if (elapsed >= BOMB_COOLDOWN) return 0.0;
        return (BOMB_COOLDOWN - elapsed) / 1000.0;
    }
}