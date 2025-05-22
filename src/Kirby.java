import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Kirby {
    private int x;
    private int y;
    private int speed = 4;

    private int score;
    private BufferedImage[] walkFrames;
    private int currentFrame;
    private int frameCounter;
    private Vacuum vacuum;

    public Kirby(int x, int y){
        this.x = x;
        this.y = y;
        vacuum = new Vacuum (1);
        score = 0;
    }

    public void move(int dx, int dy){
        if(dx != 0 || dy != 0){
            x += dx * speed;
            y += dy * speed;

            frameCounter++;
            if(frameCounter >= 10){
                currentFrame = (currentFrame + 1) % walkFrames.length;
            }
        }
    }
}
