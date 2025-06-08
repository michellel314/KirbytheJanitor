import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GoldenTrash {
    private int worldX;
    private int y;
    private int explosionFlashFrames = 0;
    private BufferedImage image;
    private BufferedImage explodedImage;
    private boolean isExplosive;

    public GoldenTrash(int worldX, int y) {
        this.worldX = worldX;
        this.y = y;
        this.isExplosive = Math.random() < 0.3;

        try {
                image = ImageIO.read(new File("src/Visuals/Explosion.png"));
                image = ImageIO.read(new File("src/Visuals/goldenTrash.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void triggerExplosion() {
        isExplosive = true;
        explosionFlashFrames = 10; // Flash for 10 frames
    }

    public void update() {
        if (explosionFlashFrames > 0) {
            explosionFlashFrames--;
        }
    }

    public void draw(Graphics g, int cameraX) {
        BufferedImage currentImage = (isExplosive && explosionFlashFrames % 2 == 0) ?
                explodedImage : image;
        g.drawImage(currentImage, worldX - cameraX, y, null);
    }
    public boolean isExplosive() {
        return isExplosive;
    }

    public int getWorldX() { return worldX; }
    public int getY() { return y; }
    public int getWidth() { return image.getWidth(); }
    public int getHeight() { return image.getHeight(); }
    public void setWorldX(int newWorldX) { this.worldX = newWorldX; }

    public int getExplosionFrames(){
        return explosionFlashFrames;
    }
}