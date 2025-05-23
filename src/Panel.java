import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
public class Panel extends JPanel implements Runnable, KeyListener{
    private Thread gameThread;
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
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void run() {

    }
}
