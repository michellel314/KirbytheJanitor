import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Panel extends JPanel implements Runnable, KeyListener{
    private ArrayList<GoldenTrash> trashList = new ArrayList<>();
    private BufferedImage kirbyK;
    private BufferedImage background;
    private BufferedImage homescreen;
    private int backgroundX;
    private int cameraX = 0;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private Kirby kirby;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private boolean backgroundScrolling = false;
    private String gameState = "HOME";
    private JButton start;
    public Panel(){
        kirby = new Kirby(200, 500);
        kirby.loadWalkFrame("src/Visuals", 4);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        setLayout(null); // Let us manually place components

        start = new JButton("Start Game");
        start.setBounds(310, 275, 150, 50); // x, y, width, height
        add(start);

        start.addActionListener(e -> {
            remove(start);     // Hide the button
            gameState = "PLAYING";   // Switch game state
            resetGame();             // Reset Kirby & background
            requestFocusInWindow();  // Make sure keys work again
        });

        try {
            homescreen = ImageIO.read(new File("src/Visuals/HOMESCREEN.PNG"));
            background = ImageIO.read(new File(("src/Visuals/Dreamscape.jpg")));
            kirbyK = ImageIO.read(new File("src/Visuals/tile000.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
        backgroundX = 0;
        Timer timer = new Timer(13, e->{
            update();
            repaint();
        });
        timer.start();

    }

    public void update(){
        int dx = 0;
        int dy = 0;
        if (left) {
            dx = -4;
        } else if (right) {
            dx =  4;
        }

        if(kirby.getX() < 400 || backgroundScrolling == false){
            kirby.move(dx, 0);
            if(kirby.getX() > 400){
                backgroundScrolling = true;
            }
        } else {
            backgroundX -= dx;
            for(GoldenTrash t : trashList){
                t.scrollwithBackground(dx);
            }
        }
        kirby.move(dx, 0);
        int kirbyX = kirby.getX();
        int leftBound = 200;
        int rightBound = 600;
        if (kirbyX < leftBound) {
            cameraX -= leftBound - kirbyX;
            kirby.setPosition(leftBound, kirby.getY());
        } else if (kirbyX > rightBound){
            cameraX += kirbyX - rightBound;
            kirby.setPosition(rightBound, kirby.getY());
        }

    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if (gameState.equals("HOME")) {
            g.drawImage(homescreen, 0, 0, null);
            g.setFont(new Font("Sansserif", Font.BOLD, 30));
            g.setColor(Color.pink);
            g.drawString("Kirby the Janitor", 270, 200);
            g.drawImage(kirbyK, 270, 150, null);
        } else if (gameState.equals("PLAYING")) {
            int bgWidth = background.getWidth();
            for (int i = -1; i < getWidth() / bgWidth + 2; i++) {
                int xPos = i * bgWidth - cameraX % bgWidth;
                g.drawImage(background, xPos, 0, null);
            }
            for (GoldenTrash t : trashList){
               t.update();
            }
            kirby.draw(g);
        }
    }

    public void resetGame(){
        cameraX = 0;
        kirby.setPosition(200, 500);
        trashList.clear();

        for(int i = 0; i < 5; i++){
            int x = (int) (Math.random() * 700 + 50);
            int y = 400;
            trashList.add(new GoldenTrash(x, y));
        }
    }

    public void spawnTrash(){

    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_W && !kirby.isJumping()){
            up = true;
            kirby.jump();
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
