import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Panel extends JPanel implements Runnable, KeyListener{
    private BufferedImage background;
    private int backgroundX;
    private final int WIDTH = 500;
    private final int HEIGHT = 500;
    private Kirby kirby;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    public Panel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        try {
            background = ImageIO.read(getClass().getResource("/src/Visuals/game_background_4.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
        backgroundX = 0;
    }

    public void update(){
        int dx = 0;
        int dy = 0;
        if (left) {
            dx = -2;
        }
        if (right) {
            dx =  2;
        }

        if (dx != 0) {
            backgroundX -= dx;

            // Optional: loop the background when scrolling
            int bgWidth = background.getWidth();
            if (backgroundX <= -bgWidth) {
                backgroundX += bgWidth;
            }
            if (backgroundX >= bgWidth) {
                backgroundX -= bgWidth;
            }
        }

        kirby.move(dx, dy);
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        int bgWidth = background.getWidth();
        int numImages = WIDTH / bgWidth + 2; // draw enough to cover the screen

        for (int i = 0; i < numImages; i++) {
            g.drawImage(background, backgroundX + i * bgWidth, 0, null);
        }

        kirby.draw(g);
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_W){
            up = true;
        }
        if(key == KeyEvent.VK_S){
            down = true;
        }
        if(key == KeyEvent.VK_A){
            left = true;
        }
        if(key == KeyEvent.VK_D){
            right = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_W){
            up = false;
        }
        if(key == KeyEvent.VK_S){
            down = false;
        }
        if(key == KeyEvent.VK_A){
            left = false;
        }
        if(key == KeyEvent.VK_D){
            right = false;
        }
    }

    @Override
    public void run() {

    }
}
