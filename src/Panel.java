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
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private Kirby kirby;
    private int cameraX = 0;
    private String gameState = "HOME";
    private JButton start;

    private boolean[] checkpointsUnlocked = new boolean[5]; // Tracks which backgrounds are accessible
    private int currentCheckpoint = 0;
    private int trashCollectedForCheckpoint = 0;
    private final int[] trashRequirements = {5, 10, 15, 20, 25};
    private String checkpointMessage = "";
    private int messageTimer = 0;
    // Constants for checkpoint size and Kirby screen X clamp
    private final int CHECKPOINT_WIDTH = 0; // Will calculate dynamically from backgrounds
    private final int KIRBY_SCREEN_X_MIN = 100;
    private final int KIRBY_SCREEN_X_MAX = WIDTH - 100;

    public Panel() {
        kirby = new Kirby(200, 300);
        kirby.loadWalkingFrames("src/Visuals", 4);
        kirby.loadEatingFrames("src/Eating_Animation", 5);
        kirby.loadJumpingFrames("src/Jumping_Animation", 5);
        checkpointsUnlocked[0] = true;
        checkpointMessage = "Area 1 Unlocked! Collect 5 trash";
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
            backgroundList.add(ImageIO.read(new File("src/Visuals/Dreamscape.jpg")));
            backgroundList.add(ImageIO.read(new File("src/Visuals/campfire.jpg")));
            backgroundList.add(ImageIO.read(new File("src/Visuals/Forest.jpg")));
            backgroundList.add(ImageIO.read(new File("src/Visuals/Sky.jpg")));
            backgroundList.add(ImageIO.read(new File("src/Visuals/Sky_2.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer(12, e -> {
            update();
            repaint();
        });
        timer.start();
    }

    public void update() {
        if (!gameState.equals("PLAYING")){
            return;
        }
        for (GoldenTrash t : trashList) {
            t.update();
        }
        int dx = 0;
        if (leftPressed) {
            dx = -4;
            kirby.setFacingLeft(true);
        }
        else if (rightPressed) {
            dx = 4;
            kirby.setFacingLeft(false);

            // Boundary check
            int kirbyRightEdge = kirby.getWorldX() + kirby.getWidth();
            if (kirbyRightEdge >= getTotalWorldWidth() || (kirbyRightEdge > getCheckpointBoundary() && (currentCheckpoint < checkpointsUnlocked.length - 1 && !checkpointsUnlocked[currentCheckpoint + 1]))) {
                dx = 0;
            }
        }
        kirby.setDx(dx);

        // Calculate total width of all backgrounds combined
        int totalBackgroundWidth = 0;
        for (BufferedImage bg : backgroundList) {
            totalBackgroundWidth += bg.getWidth();
        }

        // Kirby worldX before movement
        int kirbyWorldX = cameraX + kirby.getX();

        // Update Kirby worldX by dx
        kirbyWorldX += dx;

        // Clamp Kirby worldX inside total background
        kirbyWorldX = Math.max(0, Math.min(totalBackgroundWidth - kirby.getWidth(), kirbyWorldX));

        // Clamp Kirby's screen X to range on screen
        int kirbyScreenX = kirbyWorldX - cameraX;
        if (currentCheckpoint == 0) {
            // No scrolling yet: Kirby stays fully inside screen bounds
            kirbyScreenX = Math.max(0, Math.min(WIDTH - kirby.getWidth(), kirbyScreenX));
            cameraX = 0;
            kirbyWorldX = kirbyScreenX; // worldX == screenX if no scrolling
        } else {
            // Scroll background if past first checkpoint
            kirbyScreenX = Math.max(KIRBY_SCREEN_X_MIN, Math.min(KIRBY_SCREEN_X_MAX - kirby.getWidth(), kirbyScreenX));

            // Update cameraX so Kirby screen position is maintained
            cameraX = kirbyWorldX - kirbyScreenX;
            cameraX = Math.max(0, Math.min(cameraX, totalBackgroundWidth - WIDTH));
        }

        // Update Kirby screen position
        kirby.setPosition(kirbyScreenX, kirby.getY());

        // Update Kirby vertical movement and animation
        kirby.updateVerticalMovement();
        kirby.updateAnimation();

        // Check collisions with trash using Kirby worldX (cameraX + kirbyScreenX)
        checkTrashCollision(kirbyWorldX);
        // Game over condition
        if (kirby.getHealth() <= 0) {
            gameState = "GAME_OVER";
        }
    }

    // Respawn trash in the range of the checkpoint background
    private void respawnTrashForCheckpoint(int checkpoint) {
        trashList.clear();

        // Calculate checkpoint horizontal range in world coordinates
        int startX = 0;
        for (int i = 0; i < checkpoint; i++) {
            if (i < backgroundList.size()) {
                startX += backgroundList.get(i).getWidth();
            }
        }
        int endX = startX;
        if (checkpoint < backgroundList.size()) {
            endX += backgroundList.get(checkpoint).getWidth();
        }

        int trashCount = trashRequirements[checkpoint];
        int maxAttempts = 200;
        int spawned = 0;

        while (spawned < trashCount) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < maxAttempts) {
                attempts++;
                int newX = (int) (Math.random() * (endX - startX - 80)) + startX + 40;
                int newY = (int) (Math.random() * 200 + 300);

                if (!isTooCloseInRange(newX, newY)) {
                    trashList.add(new GoldenTrash(newX, newY));
                    placed = true;
                }
            }
            if (!placed) break;
            spawned++;
        }
    }

    // Check collisions between Kirby and trash
    private void checkTrashCollision(int kirbyWorldX) {
        for (int i = 0; i < trashList.size(); i++) {
            GoldenTrash t = trashList.get(i);
            int dx = Math.abs(kirbyWorldX - t.getWorldX());
            int dy = Math.abs(kirby.getY() - t.getY());

            if (dx < 50 && dy < 50 && kirby.getAnimationState().equals("eat")) {
                if (t.isExplosive()) {
                    t.triggerExplosion(); // Start flash effect
                    if (!kirby.vacuum.resistExplosion()) {
                        kirby.takeDamage(30);
                    }
                    // Remove after flash completes
                    if (t.getExplosionFrames() <= 0) {
                        trashList.remove(i);
                        i--;
                    }
                } else {
                    kirby.collectTrash(false);
                    trashCollectedForCheckpoint++;
                    trashList.remove(i);
                    i--;
                    respawnOneTrashAtCheckpoint();

                    if (trashCollectedForCheckpoint >= trashRequirements[currentCheckpoint]) {
                        unlockNextCheckpoint();
                    }
                }
                break;
            }
        }
    }

    private void unlockNextCheckpoint() {
        trashCollectedForCheckpoint = 0;
        currentCheckpoint = (currentCheckpoint + 1) % 5;
        checkpointsUnlocked[currentCheckpoint] = true;

        // Set visual message
        checkpointMessage = "Area " + (currentCheckpoint+1) + " Unlocked! Collect " +
                trashRequirements[currentCheckpoint] + " trash";
        messageTimer = 180; // Show for 3 seconds (60 frames/sec * 3)
    }

    // Respawn a single trash in the current checkpoint background range
    private void respawnOneTrashAtCheckpoint() {
        int startX = 0;
        for (int j = 0; j < currentCheckpoint; j++) {
            startX += backgroundList.get(j).getWidth();
        }
        int endX = startX;
        if (currentCheckpoint < backgroundList.size()) {
            endX += backgroundList.get(currentCheckpoint).getWidth();
        }

        int maxAttempts = 100;
        boolean placed = false;
        while (!placed && maxAttempts > 0) {
            maxAttempts--;
            int newX = (int) (Math.random() * (endX - startX - 80)) + startX + 40;
            int newY = (int) (Math.random() * 200 + 300);

            if (!isTooCloseInRange(newX, newY)) {
                trashList.add(new GoldenTrash(newX, newY));
                placed = true;
            }
        }
    }

    private int getTotalWorldWidth() {
        int totalWidth = 0;
        for (BufferedImage bg : backgroundList) {
            totalWidth += bg.getWidth();
        }
        return totalWidth;
    }

    private int getCheckpointBoundary() {
        int boundary = 0;
        for (int i = 0; i <= currentCheckpoint; i++) {
            boundary += backgroundList.get(i % backgroundList.size()).getWidth();
        }
        return boundary;
    }

    // Check if new trash position is too close to existing trash
    private boolean isTooCloseInRange(int x, int y) {
        int minDistance = 50;
        for (GoldenTrash existing : trashList) {
            int ex = existing.getWorldX() + 20;
            int ey = existing.getY() + 20;

            int distX = ex - (x + 20);
            int distY = ey - (y + 20);
            double distance = Math.sqrt(distX * distX + distY * distY);
            if (distance < minDistance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState.equals("HOME")) {
            g.drawImage(homescreen, 0, 0, WIDTH, HEIGHT, null);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.setColor(Color.pink);
            g.drawString("Kirby the Janitor", 270, 200);

        } else if (gameState.equals("PLAYING")) {
            // Draw backgrounds horizontally offset by cameraX
            int totalWidth = 0;
            for (BufferedImage bg : backgroundList) {
                totalWidth += bg.getWidth();
            }
            // Calculate start position to draw backgrounds
            int drawX = -cameraX;
            int i = 0;
            while (drawX < WIDTH) {
                if (i >= backgroundList.size()) i = 0; // loop backgrounds if needed
                BufferedImage bg = backgroundList.get(i);
                g.drawImage(bg, drawX, 0, null);
                drawX += bg.getWidth();
                i++;
            }

            // Draw trash relative to cameraX
            for (GoldenTrash t : trashList) {
                t.draw(g, cameraX);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Area " + (currentCheckpoint+1) + ": " +
                    trashCollectedForCheckpoint + "/" +
                    trashRequirements[currentCheckpoint], 20, 30);

            // Checkpoint unlock message (center screen)
            if (messageTimer > 0) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                int msgWidth = g.getFontMetrics().stringWidth(checkpointMessage);
                g.drawString(checkpointMessage, getWidth()/2 - msgWidth/2, getHeight()/2);
            }

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
        currentCheckpoint = 0;
        kirby.setPosition(200, 500);
        kirby.resetHealth();
        kirby.resetScore();
        kirby.resetState();
        kirby.resetJump();
        kirby.resetAnimation();

        trashList.clear();
        respawnTrashForCheckpoint(0);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // no-op
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState.equals("PLAYING")) {
            if (key == KeyEvent.VK_W) {
                if (kirby.getJumpCount() < kirby.getMaxJumps()) {
                    kirby.jump();
                }
            } else if (key == KeyEvent.VK_A) {
                leftPressed = true;    // Set flag here
            } else if (key == KeyEvent.VK_D) {
                rightPressed = true;   // Set flag here
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

        if (key == KeyEvent.VK_A) {
            leftPressed = false;  // Reset flag here
        } else if (key == KeyEvent.VK_D) {
            rightPressed = false; // Reset flag here
        }
    }

    @Override
    public void run() {
        // Required by Runnable, not used
    }
}
