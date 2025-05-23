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

    private int score;
    private ArrayList <BufferedImage> walkFrames;
    private int currentFrame;
    private int frameCounter;
    private Vacuum vacuum;

    public Kirby(int x, int y){
        this.x = x;
        this.y = y;
        vacuum = new Vacuum (1);
        score = 0;
    }

    public void loadWalkFrame(String folderpath, int frameCount){
       walkFrames.clear();
       for(int i = 0; i < frameCount; i++){
           String fileName = folderpath + "\\Visuals" + "\\tile00" + i + ".png";
           try{
               BufferedImage frame = ImageIO.read(getClass().getResource(fileName));
           } catch (IOException e){
               System.out.println(e.getMessage());
           }
       }
    }
    public void move(int dx, int dy){
        if(dx != 0 || dy != 0){
            x += dx * speed;
            y += dy * speed;

            frameCounter++;
            if(frameCounter >= 10){
                currentFrame = (currentFrame + 1) % walkFrames.size();
                frameCounter = 0;
            }
        } else {
            currentFrame = 0;
        }
    }

    public void draw(Graphics g){
        if(walkFrames != null && walkFrames.get(currentFrame) != null){
            g.drawImage(walkFrames.get(currentFrame), x, y, null);
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
}
