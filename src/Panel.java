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
    private ArrayList<BufferedImage> backgroundList = new ArrayList<>();
    private BufferedImage kirbyK;
    private BufferedImage background;
    private BufferedImage background1;
    private BufferedImage background2;
    private BufferedImage background3;
    private BufferedImage background4;
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
    private boolean checkpointReached = false;
    private boolean canScrollBeyondCheckpoint = false;
    private final int requiredTrash = 5;  // Number of trash Kirby must collect before unlocking scrolling beyond checkpoint

    public Panel(){
        kirby = new Kirby(200, 495);
        kirby.loadWalkingFrames("src/Visuals", 4);
        kirby.loadEatingFrames("src/Eating_Animation", 5);
        kirby.loadJumpingFrames("src/Jumping_Animation", 5);
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
            homescreen = ImageIO.read(new File("src/Visuals/HOMESCREEN.png"));
            background = ImageIO.read(new File(("src/Visuals/Dreamscape.jpg")));
            background1 = ImageIO.read(new File("src/Visuals/campfire.jpg"));
            background2 = ImageIO.read(new File("src/Visuals/Forest.jpg"));
            background3 = ImageIO.read(new File("src/Visuals/Sky.jpg"));
            background4 = ImageIO.read(new File("src/Visuals/Sky_2.jpg"));

            kirbyK = ImageIO.read(new File("src/Visuals/tile000.png"));
        } catch (IOException e){
            e.printStackTrace();
        }

        backgroundList.add(background);
        backgroundList.add(background1);
        backgroundList.add(background2);
        backgroundList.add(background3);
        backgroundList.add(background4);
        backgroundX = 0;
        Timer timer = new Timer(13, e->{
            update();
            repaint();
        });
        timer.start();

    }

    public void update() {
        int dx = 0;
        if (left) dx = -4;
        else if (right) dx = 4;

        // Check if Kirby has reached checkpoint position (e.g., x = 600)
        if (!checkpointReached && kirby.getX() >= 600) {
            checkpointReached = true;
            backgroundScrolling = false;  // Lock scrolling when at checkpoint
        }

        // Check if Kirby collected enough trash to unlock scrolling past checkpoint
        if (checkpointReached && kirby.getScore() >= requiredTrash * 100) {
            canScrollBeyondCheckpoint = true;
        }

        // Movement and scrolling logic
        if (kirby.getX() < 400 || !backgroundScrolling) {
            kirby.move(dx, 0);
            if (kirby.getX() > 400 && !checkpointReached) {
                backgroundScrolling = true;
            }
        } else {
            if (canScrollBeyondCheckpoint) {
                backgroundX -= dx;
                for (GoldenTrash t : trashList) {
                    t.scrollwithBackground(dx);
                }
            } else {
                // Prevent scrolling but let Kirby move within a range near checkpoint
                kirby.move(dx, 0);
                // Optionally clamp Kirby's x to prevent going too far past checkpoint
                if (kirby.getX() > 700) kirby.setPosition(700, kirby.getY());
            }
        }
        // Additional camera adjustment code (your existing logic) can remain here
        int kirbyX = kirby.getX();
        int leftBound = 200;
        int rightBound = 600;
        if (kirbyX < leftBound) {
            cameraX -= leftBound - kirbyX;
            kirby.setPosition(leftBound, kirby.getY());
        } else if (kirbyX > rightBound) {
            cameraX += kirbyX - rightBound;
            kirby.setPosition(rightBound, kirby.getY());
        }

        if (kirby.getHealth() <= 0) {
            gameState = "GAME_OVER";
        }

        if(kirby.isJumping()){
            kirby.applyGravity();
        }

        checkTrashCollision();
        kirby.updateAnimation();
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

            for(GoldenTrash t : trashList){
                t.draw(g, cameraX);
            }

            kirby.draw(g);
        } else if (gameState.equals("GAME_OVER")){
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 48));
            g.drawString("GAME OVER", WIDTH / 2 - 140, HEIGHT / 2 - 20);

            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            g.drawString("Press R to Restart", WIDTH / 2 - 100, HEIGHT / 2 + 30);
        }
    }

    public void resetGame(){
        cameraX = 0;
        kirby.setPosition(200, 500);
        trashList.clear();

        for(int i = 0; i < 5; i++){
            int x = (int) (Math.random() * 700 + 50);
            int y = (int)(Math.random() * 200 + 300);  // randomly vary height
            trashList.add(new GoldenTrash(x, y));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if(gameState.equals("PLAYING")){
            if(key == KeyEvent.VK_W && !kirby.isJumping()){
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
            if(key == KeyEvent.VK_E && !kirby.isJumping()){
                kirby.setAnimationState("eat");
            }
        } else if (gameState.equals("GAME_OVER")){
            if(key == KeyEvent.VK_R){
                resetGame();
                gameState = "HOME";
            }
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
        if (key == KeyEvent.VK_E) {
            if (!kirby.isEating()) {
                kirby.setAnimationState("eat");
            }
        }
    }

    @Override
    public void run() {

    }

    private void checkTrashCollision() {
        for (int i = 0; i < trashList.size(); i++) {
            GoldenTrash t = trashList.get(i);
            int dx = Math.abs(kirby.getX() - t.getX());
            int dy = Math.abs(kirby.getY() - t.getY());
            if (dx < 40 && dy < 40 && kirby.getAnimationState().equals("eat")) {
                boolean exploded = Math.random() < 0.3;

                if (exploded && !kirby.vacuum.resistExplosion()) {
                    kirby.takeDamage(30);
                }

                kirby.collectTrash();
                trashList.remove(i);
                int newX = (int) (Math.random() * 700 + 50);
                int newY = (int) (Math.random() * 200 + 300);
                trashList.add(new GoldenTrash(newX, newY));
                i--;
            }
        }
    }
}