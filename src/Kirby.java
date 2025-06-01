import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Kirby {
    private int health;
    private int x;
    private int y;
    private int speed = 4;
    private int frameIndex = 0;
    private boolean facingRight;
    private boolean facingLeft;
    private int score;
    private String animationState = "walk";
    private ArrayList<BufferedImage> eating;
    private ArrayList<BufferedImage> walkFrames;
    private int currentFrame;
    private int frameCounter;
    public Vacuum vacuum;
    private int velocityY = 0;
    private boolean isJumping = false;
    private final int GROUND_Y = 300;
    private final int JUMP_STRENGTH = -12;
    private final int GRAVITY = 1;

    public Kirby(int x, int y) {
        health = 100;
        this.x = x;
        this.y = y;
        vacuum = new Vacuum(1);
        score = 0;
        frameIndex = 0;
        walkFrames = new ArrayList<>();
        eating = new ArrayList<>();
    }

    public void loadWalkingFrames(String folderpath, int frameCount) {
        walkFrames.clear();
        for (int i = 0; i < frameCount; i++) {
            String fileName = folderpath + "/tile00" + i + ".png";
            try {
                BufferedImage frame = ImageIO.read(new File(fileName));
                walkFrames.add(frame);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void loadEatingFrames(String folderPath, int frameCount){
        eating.clear();
        for(int i = 0; i < frameCount; i++){
            String fileName = folderPath + "/tile00" + i + ".png";
            try{
                 BufferedImage frame = ImageIO.read(new File(fileName));
                 eating.add(frame);
            } catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    public void setAnimationState(String state) {
        if (state.equals("walk") || state.equals("eat")) {
            this.animationState = state;
            frameIndex = 0;      // Reset animation to first frame
            frameCounter = 0;    // Reset counter
        }
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;

        if (x < 0) {
            x = 0;
        }
        if (x > 800 - getCurrentFrameWidth()) {
            x = 800 - getCurrentFrameWidth();
        }

        if (dx < 0) {
            facingRight = false;
        } else if (dx > 0) {
            facingRight = true;
        }

        if (dx != 0 || dy != 0) {
            frameCounter++;
            if (frameCounter >= 10) {
                if (animationState.equals("walk") && frameIndex >= walkFrames.size()) {
                    frameIndex = 0;
                } else if (animationState.equals("eat") && frameIndex >= eating.size()) {
                    frameIndex = 0;
                }
                frameCounter = 0;
            }
        } else {
            frameIndex = 0;
            frameCounter = 0;
        }

        if (y < GROUND_Y || isJumping) {
            velocityY += GRAVITY;
            y += velocityY;
            if (y >= GROUND_Y) {
                y = GROUND_Y;
                isJumping = false;
                velocityY = 0;
            }
        }
    }

    public void draw(Graphics g) {
        BufferedImage frame = null;

        if (animationState.equals("eat") && !eating.isEmpty()) {
            frame = eating.get(frameIndex % eating.size());
        } else if (!walkFrames.isEmpty()) {
            frame = walkFrames.get(frameIndex % walkFrames.size());
        }

        if (frame != null) {
            if (facingRight) {
                g.drawImage(frame, x, y, null);
            } else {
                g.drawImage(frame, x + frame.getWidth(), y, -frame.getWidth(), frame.getHeight(), null);
            }
        }
    }

    public void explode() {
        System.out.println("KIRBY EXPLODED! GAME OVER");
    }

    public void upgradeVacuum() {
        if (score >= 500 && vacuum.getTier() < 3) {
            score -= 500;
            vacuum = new Vacuum(vacuum.getTier() + 1);
            System.out.println("Vacuum upgraded to tier " + vacuum.getTier());
        }
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            velocityY = JUMP_STRENGTH;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getScore() {
        return score;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void setPosition(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public void collectTrash() {
        score += 100;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            explode();
        }
    }

    public int getHealth() {
        return health;
    }

    private int getCurrentFrameWidth() {
        if (!walkFrames.isEmpty()) {
            return walkFrames.get(frameIndex).getWidth();
        }
        return 0;
    }
}
