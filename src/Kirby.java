import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class Kirby {
    private int x;
    private int y;
    private int speed = 4;
    private int frameIndex = 0;
    private boolean facingRight;
    private boolean facingLeft;
    private int score;
    private ArrayList <BufferedImage> walkFrames;
    private int currentFrame;
    private int frameCounter;
    private Vacuum vacuum;
    private long lastFrameTime;
    private long frameDelay = 120;

    public Kirby(int x, int y){
        this.x = x;
        this.y = y;
        vacuum = new Vacuum (1);
        score = 0;
        frameIndex = 0;
        lastFrameTime = System.currentTimeMillis();
        walkFrames = new ArrayList<>();
    }

    public void loadWalkFrame(String folderpath, int frameCount){
       walkFrames.clear();
       for(int i = 0; i < frameCount; i++){
           String fileName = folderpath + "/tile00" + i + ".png";
           try{
               BufferedImage frame = ImageIO.read(new File(fileName));
               walkFrames.add(frame);
           } catch (IOException e){
               System.out.println(e.getMessage());
           }
       }
    }
    public void move(int dx, int dy) {
        x += dx;
        y += dy;

        // Change direction Kirby is facing

        if (x < 0) {
            x = 0;
        }
        if (x > 800 - getCurrentFrameWidth()) { // Screen width - Kirby sprite width
            x = 800 - getCurrentFrameWidth();
        }

        if (dx < 0) {
            facingRight = false;
        } else if (dx > 0) {
            facingRight = true;
        }

        // If Kirby is moving
        if (dx != 0 || dy != 0) {
            frameCounter++;

            // Every 10 updates, change to the next walking frame (1 to 3)
            if (frameCounter >= 10) {
                frameIndex++;
                if (frameIndex > 3) {
                    frameIndex = 1; // loop back to first walking frame
                }
                frameCounter = 0;
            }
        } else {
            // Kirby is not moving — show standing frame
            frameIndex = 0;
            frameCounter = 0;
        }
    }

    public void draw(Graphics g){
        if(!walkFrames.isEmpty()){
            BufferedImage frame = walkFrames.get(frameIndex);
            if(facingRight){
                g.drawImage(frame, x, y, null);
            } else {
                g.drawImage(frame, x + frame.getWidth(), y, -frame.getWidth(), frame.getHeight(), null);
            }
        }
    }

    public void explode(){
        System.out.println("KIRBY EXPLODED! GAME OVER");
    }

    public void upgradeVacuum(){
        if(score >= 500 && vacuum.getTier() < 3){
            score -= 500;
            vacuum = new Vacuum(vacuum.getTier() + 1);
            System.out.println("Vacuum upgrade to tier " + vacuum.getTier());
        }
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getScore(){
        return score;
    }

    public void setPosition(int newX, int newY){
        x = newX;
        y = newY;
    }

    private int getCurrentFrameWidth() {
        if (!walkFrames.isEmpty()) {
            return walkFrames.get(frameIndex).getWidth();
        }
        return 0;
    }
}
