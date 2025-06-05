import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Panel extends JPanel implements Runnable, KeyListener {

    private ArrayList<GoldenTrash> trashList = new ArrayList<>();
    private ArrayList<BufferedImage> backgroundList = new ArrayList<>();
    private BufferedImage homescreen;
    private BufferedImage background; // current background used for scrolling
    private int cameraX = 0;

    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private Kirby kirby;

    private boolean left, right, up, down;
    private boolean backgroundScrolling = false;

    private String gameState = "HOME";

    private JButton start;

    private boolean checkpointReached = false;
    private boolean canScrollBeyondCheckpoint = false;

    private final int requiredTrash = 5;

    public Panel() {
        kirby = new Kirby(200, 300);
        kirby.loadWalkingFrames("src/Visuals", 4);
        kirby.loadEatingFrames("src/Eating_Animation", 5);
        kirby.loadJumpingFrames("src/Jumping_Animation", 5);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        start = new JButton("Start Game");
        start.setBounds(310, 275, 150, 50);
        add(start);

        start.addActionListener(e -> {
            remove(start);
            gameState = "PLAYING";
            resetGame();
            requestFocusInWindow();
        });

        try {
            homescreen = ImageIO.read(new File("src/Visuals/HOMESCREEN.png"));
            background = ImageIO.read(new File("src/Visuals/Dreamscape.jpg"));

            // If you want to switch backgrounds later, you can add more here:
            // backgroundList.add(background);
            // ... and so on

        } catch (IOException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer(13, e -> {
            update();
            repaint();
        });
        timer.start();
    }

    public void update() {
        if (!gameState.equals("PLAYING")) return;

        int dx = 0;
        if (left) dx = -4;
        else if (right) dx = 4;

        // Check checkpoint crossing
        if (!checkpointReached && kirby.getX() >= 600) {
            checkpointReached = true;
            backgroundScrolling = false; // lock scrolling at checkpoint
        }

        // Unlock scrolling beyond checkpoint if enough trash collected
        if (checkpointReached && kirby.getScore() >= requiredTrash * 100) {
            canScrollBeyondCheckpoint = true;
        }

        // Movement & scrolling logic:
        if (!backgroundScrolling) {
            // Kirby moves freely until background scrolling starts
            kirby.move(dx, 0);
            if (kirby.getX() > 400 && !checkpointReached) {
                backgroundScrolling = true;
            }
        } else {
            if (canScrollBeyondCheckpoint) {
                // Scroll background and trash oppositely to Kirby movement to simulate camera following
                cameraX += dx;
                for (GoldenTrash t : trashList) {
                    t.scrollwithBackground(-dx); // scroll trash opposite to movement
                }
            } else {
                // Can't scroll beyond checkpoint, Kirby can only move to a limit
                kirby.move(dx, 0);
                if (kirby.getX() > 700) kirby.setPosition(700, kirby.getY());
            }
        }

        // Camera boundary enforcement to keep Kirby in view (optional, improves smoothness)
        int kirbyX = kirby.getX();
        int leftBound = 200;
        int rightBound = 600;

        if (kirbyX < leftBound) {
            int diff = leftBound - kirbyX;
            cameraX -= diff;
            kirby.setPosition(leftBound, kirby.getY());
        } else if (kirbyX > rightBound) {
            int diff = kirbyX - rightBound;
            cameraX += diff;
            kirby.setPosition(rightBound, kirby.getY());
        }

        kirby.updateVerticalMovement();
        kirby.updateAnimation();

        if (kirby.getHealth() <= 0) {
            gameState = "GAME_OVER";
        }

        checkTrashCollision();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState.equals("HOME")) {
            g.drawImage(homescreen, 0, 0, WIDTH, HEIGHT, null);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.setColor(Color.pink);
            g.drawString("Kirby the Janitor", 270, 200);

            // Draw Kirby home image here if you have it

        } else if (gameState.equals("PLAYING")) {
            // Draw scrolling background
            int bgWidth = background.getWidth();

            // Draw multiple tiles of background to cover entire width
            int startTile = cameraX / bgWidth;
            int offset = cameraX % bgWidth;

            for (int i = startTile - 1; i <= startTile + (WIDTH / bgWidth) + 1; i++) {
                int xPos = i * bgWidth - offset;
                g.drawImage(background, xPos, 0, null);
            }

            // Draw trash relative to cameraX
            for (GoldenTrash t : trashList) {
                t.draw(g, cameraX);
            }

            // Draw Kirby (his draw method should consider cameraX if needed)
            kirby.draw(g);

        } else if (gameState.equals("GAME_OVER")) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 48));
            g.drawString("GAME OVER", WIDTH / 2 - 140, HEIGHT / 2 - 20);

            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            g.drawString("Press R to Restart", WIDTH / 2 - 100, HEIGHT / 2 + 30);
        }
    }

    public void resetGame() {
        cameraX = 0;
        checkpointReached = false;
        canScrollBeyondCheckpoint = false;
        kirby.setPosition(200, 500);
        kirby.resetHealth();
        kirby.resetScore();
        kirby.resetState();

        trashList.clear();
        for (int i = 0; i < 5; i++) {
            int x = (int) (Math.random() * 700 + 50);
            int y = (int) (Math.random() * 200 + 300);
            trashList.add(new GoldenTrash(x, y));
        }
    }

    private void checkTrashCollision() {
        for (int i = 0; i < trashList.size(); i++) {
            GoldenTrash t = trashList.get(i);
            int dx = Math.abs(kirby.getX() - t.getX());
            int dy = Math.abs(kirby.getY() - t.getY());

            // Only collide when Kirby is eating
            if (dx < 50 && dy < 50 && kirby.getAnimationState().equals("eat")) {
                boolean exploded = Math.random() < 0.3;

                if (exploded && !kirby.vacuum.resistExplosion()) {
                    kirby.takeDamage(30);
                }

                kirby.collectTrash();

                trashList.remove(i);
                i--;

                // Spawn new trash somewhere else
                int newX = (int) (Math.random() * 700 + 50 + cameraX);
                int newY = (int) (Math.random() * 200 + 300);
                trashList.add(new GoldenTrash(newX, newY));
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // no op
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState.equals("PLAYING")) {
            if (key == KeyEvent.VK_W) {
                if (kirby.getJumpCount() < kirby.getMaxJumps()) {
                    up = true;
                    kirby.jump();
                }
            } else if (key == KeyEvent.VK_S) {
                down = true;
            } else if (key == KeyEvent.VK_A) {
                left = true;
            } else if (key == KeyEvent.VK_D) {
                right = true;
            } else if (key == KeyEvent.VK_E) {
                if (!kirby.isEating()) {
                    kirby.startEating();
                }
            }
        } else if (gameState.equals("GAME_OVER")) {
            if (key == KeyEvent.VK_R) {
                resetGame();
                gameState = "HOME";
                add(start);
                start.requestFocusInWindow();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W) {
            up = false;
        } else if (key == KeyEvent.VK_S) {
            down = false;
        } else if (key == KeyEvent.VK_A) {
            left = false;
        } else if (key == KeyEvent.VK_D) {
            right = false;
        }
    }


    @Override
    public void run() {
        // Not used here, but required by Runnable
    }
}
