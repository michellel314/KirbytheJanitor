import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Panel extends JPanel implements Runnable, KeyListener, MouseListener {
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
    private boolean isLoadingCheckpoint = false;

    private Shop shop;
    private boolean inShop = false;

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

    public void openShop(){
        inShop = true;
        shop = new Shop(kirby, kirby.getVacuum(), kirby.getScore());
    }

    public void update() {
        if (!gameState.equals("PLAYING")) {
            return;
        }

        // Countdown checkpoint message timer
        if (messageTimer > 0) {
            messageTimer--;
            if (messageTimer == 0) checkpointMessage = "";
        }

        for (GoldenTrash t : trashList) {
            t.update();
        }

        for (int i = 0; i < trashList.size(); i++) {
            GoldenTrash t = trashList.get(i);
            if (t.wasEaten() && !t.isShowingPuff()) {
                trashList.remove(i);
                i--;
                respawnOneTrashAtCheckpoint();
            }
        }

        kirby.updateDamageCooldown();

        int dx = 0;
        if (leftPressed) {
            dx = -4;
        } else if (rightPressed) {
            dx = 4;
        }

        // Calculate total width of all backgrounds combined
        int totalBackgroundWidth = getTotalWorldWidth();

        // Calculate Kirby world X before moving
        int kirbyWorldX = cameraX + kirby.getX();

        // Predict next Kirby worldX after movement
        int nextKirbyWorldX = kirbyWorldX + dx;

        // Calculate current checkpoint boundary X position
        int checkpointBoundaryX = getCheckpointBoundary(currentCheckpoint);

        // Prevent Kirby from passing beyond the next checkpoint if not unlocked
        if (dx > 0) { // moving right
            if (currentCheckpoint < checkpointsUnlocked.length - 1) {
                // If next checkpoint is NOT unlocked and Kirby tries to go beyond current boundary
                if (!checkpointsUnlocked[currentCheckpoint + 1] && nextKirbyWorldX + kirby.getWidth() > checkpointBoundaryX) {
                    dx = 0;
                    nextKirbyWorldX = kirbyWorldX; // no movement
                }
            }
        }

        // Clamp Kirby worldX within world bounds
        nextKirbyWorldX = Math.max(0, Math.min(totalBackgroundWidth - kirby.getWidth(), nextKirbyWorldX));

        // Update Kirby position horizontally based on allowed dx
        kirby.move(dx, 0);

        // Calculate Kirby screen X relative to camera
        int kirbyScreenX = nextKirbyWorldX - cameraX;

        // Handle camera scrolling and clamping
        if (!isLoadingCheckpoint) {
            // No scrolling yet: Kirby stays fully inside screen bounds
            kirbyScreenX = Math.max(0, Math.min(WIDTH - kirby.getWidth(), kirbyScreenX));
            cameraX = currentCheckpoint * 800;
            // Update Kirby position on screen
            kirby.setPosition(kirbyScreenX, kirby.getY());
        } else {
            // Scroll background if past first checkpoint
            kirbyScreenX = Math.max(KIRBY_SCREEN_X_MIN, Math.min(KIRBY_SCREEN_X_MAX - kirby.getWidth(), kirbyScreenX));

            // Update cameraX so Kirby screen position is maintained
            cameraX = nextKirbyWorldX - kirbyScreenX;

            // Clamp cameraX to not scroll beyond next checkpoint boundary if next checkpoint is locked
            int maxCameraX;
            if (currentCheckpoint < checkpointsUnlocked.length - 1 && !checkpointsUnlocked[currentCheckpoint + 1]) {
                maxCameraX = checkpointBoundaryX - WIDTH;
                if (maxCameraX < 0) maxCameraX = 0; // prevent negative clamp
            } else {
                maxCameraX = totalBackgroundWidth - WIDTH;
            }

            cameraX = Math.max(0, Math.min(cameraX, maxCameraX));

            if (cameraX < (currentCheckpoint - 1) * 800) {
                cameraX = (currentCheckpoint - 1) * 800;
            }

            // Update Kirby position on screen
            kirby.setPosition(kirbyScreenX, kirby.getY());
        }

        if (cameraX >= currentCheckpoint * 800) {
            cameraX = currentCheckpoint * 800;
            isLoadingCheckpoint = false;
        }

        kirby.updateVerticalMovement();
        kirby.updateAnimation();

        // Check collisions with trash using Kirby world X
        checkTrashCollision(nextKirbyWorldX);

        // Check if trash collected reached requirement for current checkpoint
        if (trashCollectedForCheckpoint >= trashRequirements[currentCheckpoint]) {
            unlockNextCheckpoint();
        }

        // Game over condition
        if (kirby.getHealth() <= 0) {
            if (kirby.getScore() > kirby.getHighScore()) {
                kirby.setHighScore(kirby.getScore());
                kirby.saveHighScore();
            }
            gameState = "GAME_OVER";
        }

    }

    // Respawn trash in the range of the checkpoint background
    private void respawnTrashForCheckpoint(int checkpoint) {
        trashList.clear();

        // Calculate checkpoint horizontal range in world coordinates
        int startX = currentCheckpoint * WIDTH + 75;
        int endX = (currentCheckpoint + 1) * WIDTH - 75;

        int trashCount = trashRequirements[checkpoint];
        int maxAttempts = 200;
        int spawned = 0;

        while (spawned < trashCount) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < maxAttempts) {
                attempts++;
                int newX = (int) (Math.random() * (endX - startX)) + (currentCheckpoint * WIDTH);
                int newY = (int) (Math.random() * 200 + 300);

                if (!isTooCloseInRange(newX, newY)) {
                    trashList.add(new GoldenTrash(newX, newY));
                    placed = true;
                }
            }
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
                if (t.isExplosive() && !t.wasEaten()) {
                    if(!kirby.vacuum.resistExplosion()){
                        kirby.takeDamage(10);
                    }

                    kirby.collectTrash(true);
                    t.setWasEaten(true);
                    t.startSmoke();
                } else if (!t.isExplosive()){
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
        if (currentCheckpoint < checkpointsUnlocked.length - 1) {
            isLoadingCheckpoint = true;
            checkpointsUnlocked[currentCheckpoint + 1] = true;
            currentCheckpoint++;  // Move to next checkpoint
            trashCollectedForCheckpoint = 0;

            checkpointMessage = "Area " + (currentCheckpoint + 1) + " Unlocked! Collect " +
                    trashRequirements[currentCheckpoint] + " trash";
            messageTimer = 180; // 3 seconds message

            respawnTrashForCheckpoint(currentCheckpoint);
        }
    }
    // Respawn a single trash in the current checkpoint background range
    private void respawnOneTrashAtCheckpoint() {

        int startX = currentCheckpoint * WIDTH + 75;
        int endX = (currentCheckpoint + 1) * WIDTH - 75;

        int maxAttempts = 100;
        boolean placed = false;
        while (!placed && maxAttempts > 0) {
            maxAttempts--;
            int newX = (int) (Math.random() * (endX - startX))+ (currentCheckpoint * WIDTH);
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

    private int getCheckpointBoundary(int checkpoint) {
        int bgWidth = backgroundList.get(0).getWidth();
        return (checkpoint + 1) * bgWidth;
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

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("High Score: " + kirby.getHighScore(), 290, 50);

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
            g.drawString("Health: " + kirby.getHealth(), 20, 50);
            g.drawString("Score: " + kirby.getScore(), 20, 70);
            // Checkpoint unlock message (center screen)
            if (messageTimer > 0) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                int msgWidth = g.getFontMetrics().stringWidth(checkpointMessage);
                g.drawString(checkpointMessage, getWidth()/2 - msgWidth/2, getHeight()/2);
            }

            kirby.draw(g);
            if(inShop){
                shop.render(g);
            }

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
        trashCollectedForCheckpoint = 0;
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
                leftPressed = true;
            } else if (key == KeyEvent.VK_D) {
                rightPressed = true;
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

    @Override
    public void mouseClicked(MouseEvent e) {
        if(inShop){
            shop.handleClick(e.getX(), e.getY());
        } else {

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
