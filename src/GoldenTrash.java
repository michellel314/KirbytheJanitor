import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
public class GoldenTrash {
    private int x;
    private int y;
    private BufferedImage image;

    public GoldenTrash(int x, int y){
        this.x = x;
        this.y = y;
        try {
            image = ImageIO.read(new File("src/Visuals/goldenTrash.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void update(){
        x -= 2;
    }

    public void draw(Graphics g, int cameraX){
        g.drawImage(image, x - cameraX, y, null);
    }

    public void scrollwithBackground(int dx){
        x -= dx;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getWidth(){
        return image.getWidth();
    }

    public int getHeight(){
        return image.getHeight();
    }
}
