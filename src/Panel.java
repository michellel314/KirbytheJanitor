import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Panel extends JPanel implements Runnable, KeyListener{
    private BufferedImage background;
    private int backgroundX;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private Kirby kirby;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    public Panel(){
        kirby = new Kirby(100, 300);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        try {
            background = ImageIO.read(new File(("src\\Visuals\\Dreamscape.jpg")));
        } catch (IOException e){
            e.printStackTrace();
        }
        backgroundX = 0;
        Timer timer = new Timer(16, e->{
            update();
            repaint();
        });
        timer.start();
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

        g.drawImage(background, 0, 0, null);
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
