import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Kirby {
    private int health;
    private int dx = 0;
    private int x;
    private int worldX;
    private int y;
    private int frameIndex = 0;
    private int highScore = 0;
    private boolean facingRight = true;  // Use only this for direction
    private boolean isEating = false;
    private boolean hasEatenTrash = false;
    private int score;
    private String animationState = "walk";
    private ArrayList<BufferedImage> eating;
    private ArrayList<BufferedImage> walkFrames;
    private ArrayList<BufferedImage> jumpingFrames;
    private int frameCounter = 0;
    public Vacuum vacuum;
    private int velocityY = 0;
    private boolean isJumping = false;
    private final int GROUND_Y = 495;
    private final int JUMP_STRENGTH = -15;
    private final double GRAVITY = 1;
    private int damageCooldown = 0;
    private final int DAMAGE_COOLDOWN_TIME = 10;

    // Double jump variables
    private int jumpCount = 0;
    private final int maxJumps = 2;

    public Kirby(int x, int y) {
        health = 100;
        this.x = x;
        this.y = y;
        vacuum = new Vacuum(1);
        score = 0;
        walkFrames = new ArrayList<>();
        eating = new ArrayList<>();
        jumpingFrames = new ArrayList<>();
        resetHighScore();
        loadHighScore();
    }

    public void loadWalkingFrames(String folderpath, int frameCount) {
        walkFrames.clear();
        for (int i = 0; i < frameCount; i++) {
            String fileName = String.format("%s/tile%03d.png", folderpath, i);
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
            String fileName = String.format("%s/tile00%d.png", folderPath, i);
            try{
                BufferedImage frame = ImageIO.read(new File(fileName));
                eating.add(frame);
            } catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    public void loadJumpingFrames(String folderPath, int frameCount){
        jumpingFrames.clear();
        for (int i = 0; i < frameCount; i++) {
            String fileName = String.format("%s/tile00%d.png", folderPath, i);
            try {
                BufferedImage frame = ImageIO.read(new File(fileName));
                jumpingFrames.add(frame);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void setAnimationState(String state) {
        if (!animationState.equals(state)) {
            animationState = state;
            frameIndex = 0;
            frameCounter = 0;
        }
    }

    public void move(int dx, int dy) {
        this.dx = dx;
        this.worldX += dx;
        this.y += dy;

        if (dx > 0) {
            facingRight = true;
        } else if (dx < 0) {
            facingRight = false;
        }

        if (!isJumping && !isEating) {
           animationState = "walk";
        }
    }

    public void updateVerticalMovement() {
        if (y < GROUND_Y || isJumping) {
            velocityY += GRAVITY;
            y += velocityY;

            if (y >= GROUND_Y) {
                y = GROUND_Y;
                isJumping = false;
                velocityY = 0;
                jumpCount = 0;
                if (!isEating) setAnimationState("walk");
            } else {
                // While in air, ensure jump animation active
                if (!isEating) setAnimationState("jump");
            }
        }
    }

    public void updateAnimation() {
        frameCounter++;
        if (isEating) {
            if (frameCounter >= 5) {
                frameCounter = 0;
                frameIndex++;
                if (frameIndex >= eating.size()) {
                    isEating = false;
                    hasEatenTrash = false;
                    setAnimationState("walk");
                }
            }
        } else if (isJumping) {
            if (frameCounter >= 4) {
                frameCounter = 0;
                frameIndex++;
                if (frameIndex >= jumpingFrames.size()) {
                    frameIndex = jumpingFrames.size() - 1; // hold last jump frame
                }
            }
        } else if (dx != 0) {
            if (frameCounter >= 6) {
                frameCounter = 0;
                frameIndex++;
                if (frameIndex >= walkFrames.size()) {
                    frameIndex = 1;
                }
            }
        } else {
            frameIndex = 0;
            frameCounter = 0;
        }
    }

    public void draw(Graphics g) {
        BufferedImage frame = null;

        // Priority: eating > jumping > walking
        if (isEating && !eating.isEmpty()) {
            frame = eating.get(frameIndex % eating.size());
        }
        else if (animationState.equals("jump") && !jumpingFrames.isEmpty()) {
            frame = jumpingFrames.get(frameIndex % jumpingFrames.size());
        }
        else if (!walkFrames.isEmpty()) {
            frame = walkFrames.get(frameIndex % walkFrames.size());
        }

        // Draw flipped if facing left
        if (frame != null) {
            if (!facingRight) {
                g.drawImage(frame,
                        x + frame.getWidth(), // Start drawing from right
                        y,
                        -frame.getWidth(),    // Negative width flips image
                        frame.getHeight(),
                        null);
            } else {
                g.drawImage(frame, x, y, null); // Normal right-facing
            }
        }
    }
    public void upgradeVacuum() {
        if (score >= 500 && vacuum.getTier() < 3) {
            score -= 500;
            vacuum = new Vacuum(vacuum.getTier() + 1);
            System.out.println("Vacuum upgraded to tier " + vacuum.getTier());
        }
    }

    public void jump() {
        if (jumpCount < maxJumps && !isEating) {
            velocityY = JUMP_STRENGTH;
            isJumping = true;
            jumpCount++;
            setAnimationState("jump");
            frameIndex = 0;
            frameCounter = 0;
        }
    }

    public void startEating() {
        if (!isEating) {
            isEating = true;
            hasEatenTrash = true;
            setAnimationState("eat");
            frameIndex = 0;
            frameCounter = 0;
        }
    }

    // Getters and setters for positions, states, health, etc.
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHighScore() { return highScore; }
    public int getMaxJumps(){ return maxJumps;}
    public int getScore() { return score; }
    public int getEatingFrameCount() {return eating.size();}
    public int getHealth() { return health; }
    public int getJumpCount() { return jumpCount; }
    public String getAnimationState() { return animationState; }
    public boolean isEating() { return isEating; }
    public void setWorldX(int worldX) { this.worldX = worldX; }
    public void setX(int newX) { x = newX; }
    public void setY(int newY) { y = newY; }
    public void setPosition(int newX, int newY) { x = newX; y = newY; }
    public void setDx(int newDx) { dx = newDx; }
    public void setHighScore(int score) {highScore = score;}

    public int getWidth() {
        BufferedImage frame = null;

        if (isEating && !eating.isEmpty()) {
            frame = eating.get(frameIndex % eating.size());
        } else if (animationState.equals("jump") && !jumpingFrames.isEmpty()) {
            frame = jumpingFrames.get(frameIndex % jumpingFrames.size());
        } else if (!walkFrames.isEmpty()) {
            frame = walkFrames.get(frameIndex % walkFrames.size());
        }

        if (frame != null) {
            return frame.getWidth();
        }
        return 77; // Default fallback width
    }

    public void collectTrash(boolean isExplosive) {
        if (isExplosive) {
            score -= 100;
            if (score < 0) {
                score = 0;
            }
            takeDamage(10);
        } else {
            score += 100;
        }
    }


    public void takeDamage(int amount) {
        if (damageCooldown == 0) {
            health -= amount;
            if (health < 0) {
                health = 0;
            }
            damageCooldown = DAMAGE_COOLDOWN_TIME;  // reset cooldown
        }
    }

    public void updateDamageCooldown(){
        if(damageCooldown > 0){
            damageCooldown--;
        }
    }

    public void saveHighScore() {
        try (FileWriter writer = new FileWriter("highscore.txt")) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.out.println("Failed to save high score: " + e.getMessage());
        }
    }

    public void loadHighScore() {
        File file = new File("highscore.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                highScore = Integer.parseInt(line.trim());
            } catch (IOException | NumberFormatException e) {
                System.out.println("Failed to load high score: " + e.getMessage());
                highScore = 0;
            }
        } else {
            highScore = 0;
        }
    }

    public void resetHighScore() {
        try (PrintWriter pw = new PrintWriter("highscore.txt")) {
            pw.print("0");
        } catch (IOException e) {
            System.out.println("Failed to reset high score: " + e.getMessage());
        }
    }


    public void resetJump() {
        jumpCount = 0;
        isJumping = false;
        velocityY = 0;
    }

    public void resetAnimation() {
        animationState = "walk";
        frameIndex = 0;
        isEating = false;
        isJumping = false;
        frameCounter = 0;
    }

    public void resetHealth() {
        health = 100;
    }

    public void resetScore() {
        score = 0;
    }

    public void resetState() {
        animationState = "walk";
    }

    public void setHasEatenTrash(boolean val) {
        hasEatenTrash = val;
    }

}
