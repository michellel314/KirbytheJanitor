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

    public void draw(Graphics g){
        g.drawImage(image, x, y, null);
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
}
