import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GoldenTrash {
    private int worldX;
    private int y;
    private int smokeTimer = 0;
    private final int smokeDuration = 60;
    private BufferedImage image;
    private BufferedImage explodedImage;
    private boolean isExplosive;
    private boolean wasEaten = false;
    private boolean showPuff = false;

    public GoldenTrash(int worldX, int y) {
        this.worldX = worldX;
        this.y = y;
        this.isExplosive = Math.random() < 0.2;

        try {
                explodedImage = ImageIO.read(new File("src/Visuals/Explosion.png"));
                image = ImageIO.read(new File("src/Visuals/goldenTrash.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void eat(){
        wasEaten = true;
        if(isExplosive){
            showPuff = true;
            smokeTimer = smokeDuration;
        }
    }
    public void update() {
        if(showPuff){
            smokeTimer--;
            if(smokeTimer <= 0){
                showPuff = false;
            }
        }
    }
    public void startSmoke(){
        showPuff = true;
        smokeTimer = smokeDuration;
    }

    public void draw(Graphics g, int cameraX) {
        if(!wasEaten){
            g.drawImage(image, worldX - cameraX, y, null);
        } else if (showPuff){
            if ((smokeTimer / 5) % 2 == 0) {  // flicker on/off every 5 frames
                g.drawImage(explodedImage, worldX - cameraX, y, null);
            }
        }
    }

    public void setWasEaten(boolean eaten){
        wasEaten = eaten;
    }

    public boolean isExplosive() {
        return isExplosive;
    }

    public boolean wasEaten(){
        return wasEaten;
    }

    public boolean isShowingPuff(){
        return showPuff;
    }
    public boolean isVisible(){
        return !wasEaten || showPuff;
    }
    public int getWorldX() { return worldX; }
    public int getY() { return y; }
    public int getWidth() { return image.getWidth(); }
    public int getHeight() { return image.getHeight(); }
    public void setWorldX(int newWorldX) { this.worldX = newWorldX; }
}