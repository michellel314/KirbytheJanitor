import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
public class Panel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {
    private ArrayList<GoldenTrash> trashList = new ArrayList<>();
    private ArrayList<BufferedImage> backgroundList = new ArrayList<>();
    private BufferedImage shopIcon;
    private BufferedImage vacuum;
    private BufferedImage homescreen;
    private BufferedImage w;
    private BufferedImage a;
    private BufferedImage s;
    private BufferedImage d;
    private BufferedImage e;
    private BufferedImage j;
    private BufferedImage r;
    private int cameraX = 0;
    private int currentCheckpoint = 0;
    private int trashCollectedForCheckpoint = 0;
    private int shopIconX = 700, shopIconY = 10;
    private int shopIconWidth = 80;
    private int shopIconHeight = 80;
    private int messageTimer = 0;
    private int inventoryX = 100, inventoryY = 100, slotSize = 64, slotSpacing = 10;
    private String gameState = "HOME";
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean hoveringVacuumSlot = false;
    private boolean isLoadingCheckpoint = false;
    private boolean inShop = false;
    private boolean showInventory = false;
    private boolean musicStarted;
    private boolean musicStopped;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    // Constants for checkpoint size and Kirby screen X clamp
    private final int CHECKPOINT_WIDTH = 0; // Will calculate dynamically from backgrounds
    private final int KIRBY_SCREEN_X_MIN = 100;
    private final int KIRBY_SCREEN_X_MAX = WIDTH - 100;
    private Kirby kirby;
    private JButton start;
    private boolean[] checkpointsUnlocked = new boolean[10]; // Tracks which backgrounds are accessible
    private final int[] trashRequirements = {5, 10, 15, 20, 25, 25, 25, 25, 25, 25};
    private String checkpointMessage = "";
    private Shop shop;
    private Rectangle[] inventoryBoxes = new Rectangle[3];
    private Music backgroundMusic;
    public Panel() {
        kirby = new Kirby(200, 300);
        kirby.loadWalkingFrames("src\\Visuals", 4);
        kirby.loadEatingFrames("src\\Eating_Animation", 5);
        kirby.loadJumpingFrames("src\\Jumping_Animation", 5);
        checkpointsUnlocked[0] = true;
        checkpointMessage = "Area 1 Unlocked! Collect 5 trash";
        shop = new Shop(kirby, kirby.getVacuum());
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setLayout(null);

        start = new JButton("Start Game");
        start.setBounds(315, 275, 150, 50);
        add(start);

        start.addActionListener(e -> {
            remove(start);
            gameState = "PLAYING";
            resetGame();
            requestFocusInWindow();
            if (backgroundMusic != null && !musicStarted) {
                backgroundMusic.playLoop();
                musicStarted = true;
            }
        });

        try {
            homescreen = ImageIO.read(new File("src\\Visuals\\HOMESCREEN.png"));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Dreamscape.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\campfire.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Forest.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Sky.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Sky_2.jpg")));
            // loop over background for levels 5-10
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Dreamscape.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\campfire.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Forest.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Sky.jpg")));
            backgroundList.add(ImageIO.read(new File("src\\Visuals\\Sky_2.jpg")));
            shopIcon = ImageIO.read(new File("src\\Shop\\ShopIcon.png"));
            vacuum = ImageIO.read(new File("src\\Shop\\Vacuum.png"));

            w = ImageIO.read(new File("src\\Visuals\\wKey.png"));
            a = ImageIO.read(new File("src\\Visuals\\aKey.png"));
            s = ImageIO.read(new File("src\\Visuals\\sKey.png"));
            d = ImageIO.read(new File("src\\Visuals\\dKey.png"));
            e = ImageIO.read(new File("src\\Visuals\\eKey.png"));
            j = ImageIO.read(new File("src\\Visuals\\jKey.png"));
            r = ImageIO.read(new File("src\\Visuals\\rKey.png"));

            backgroundMusic = new Music("src\\Music\\KirbySong.wav");

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
        if (!gameState.equals("PLAYING")) {
            return;
        }

        if (inShop) {
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

        inventoryX = kirbyScreenX - 74;
        inventoryY = kirby.getY() - 75;

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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState.equals("HOME")) {
            g.drawImage(homescreen, 0, 0, WIDTH, HEIGHT, null);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.setColor(Color.pink);
            g.drawString("Kirby the Janitor", 265, 200);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("High Score: " + kirby.getHighScore(), 290, 50);
            // Instruction box background (optional for clarity)
            g.setColor(new Color(0, 0, 0, 150)); // semi-transparent black
            g.fillRoundRect(30, 230, 280, 220, 15, 15); // Left
            g.fillRoundRect(470, 240, 290, 200, 15, 15); // Right

            g.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g.setColor(Color.WHITE);

            int iconSize = 50;
            int spacingY = 50;

            // LEFT COLUMN (W, A, D, E)
            int leftX = 50;
            int leftY = 240;

            if (w != null) {
                g.drawImage(w, leftX, leftY, iconSize, iconSize, null);
                g.drawString("Jump", leftX + iconSize + 10, leftY + 24);
                leftY += spacingY;
            }
            if (a != null) {
                g.drawImage(a, leftX, leftY, iconSize, iconSize, null);
                g.drawString("Move Left", leftX + iconSize + 10, leftY + 24);
                leftY += spacingY;
            }
            if (d != null) {
                g.drawImage(d, leftX, leftY, iconSize, iconSize, null);
                g.drawString("Move Right", leftX + iconSize + 10, leftY + 24);
                leftY += spacingY;
            }
            if (e != null) {
                g.drawImage(e, leftX, leftY, iconSize, iconSize, null);
                g.drawString("Eat Trash", leftX + iconSize + 10, leftY + 24);
            }

            // RIGHT COLUMN (J, S, R)
            int rightX = 490;
            int rightY = 260;

            if (j != null) {
                g.drawImage(j, rightX, rightY, iconSize, iconSize, null);
                g.drawString("Toggle Inventory", rightX + iconSize + 10, rightY + 24);
                rightY += spacingY;
            }
            if (s != null) {
                g.drawImage(s, rightX, rightY, iconSize, iconSize, null);
                g.drawString("Exit Shop", rightX + iconSize + 10, rightY + 24);
                rightY += spacingY;
            }
            if (r != null) {
                g.drawImage(r, rightX, rightY, iconSize, iconSize, null);
                g.drawString("Restart (Game Over)", rightX + iconSize + 10, rightY + 24);
            }
            backgroundMusic.stop();
            musicStarted = false;
            musicStopped = false;
        } else if (gameState.equals("PLAYING")) {
            if (!musicStarted) {
                backgroundMusic.playLoop();
                musicStarted = true;
                musicStopped = false;
            }
            if(!inShop){
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
                g.drawImage(shopIcon, shopIconX, shopIconY, shopIconWidth, shopIconHeight, null);
            } else {
                shop.render(g);
            }
            if (showInventory) {
                for (int i = 0; i < inventoryBoxes.length; i++) {
                    int x = inventoryX + i * (slotSize + slotSpacing);
                    inventoryBoxes[i] = new Rectangle(x, inventoryY, slotSize, slotSize);

                    g.setColor(Color.WHITE);
                    g.fillRect(x, inventoryY, slotSize, slotSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, inventoryY, slotSize, slotSize);

                    // Draw vacuum in first slot
                    if (i == 0) {
                        if (kirby.getVacuum().getTier() > 0){
                            g.drawImage(vacuum, x + 5, inventoryY + 5, slotSize - 10, slotSize - 10, null);
                        }
                    }
                }

                // Tooltip for vacuum
                if (hoveringVacuumSlot) {
                    String tierText = "Vacuum Tier: " + kirby.getVacuum().getTier();
                    int tooltipX = inventoryBoxes[0].x;
                    int tooltipY = inventoryBoxes[0].y - 25;
                    g.setColor(new Color(0, 0, 0, 180));
                    g.fillRoundRect(tooltipX, tooltipY, 130, 20, 10, 10);
                    g.setColor(Color.WHITE);
                    g.drawString(tierText, tooltipX + 5, tooltipY + 15);
                }
            }

        } else if (gameState.equals("GAME_OVER")) {
            if (!musicStopped) {
                backgroundMusic.stop();
                musicStopped = true;
                musicStarted = false;
            }
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
        inShop = false;
        kirby.setPosition(200, 500);
        kirby.resetHealth();
        kirby.resetScore();
        kirby.resetState();
        kirby.resetJump();
        kirby.resetAnimation();
        kirby.getVacuum().setTier(0);
        shop.resetCosts();
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
        if (inShop) {
            if(key == KeyEvent.VK_S){
                inShop = false;
                return;

            }
            return;  // Disable controls in shop
        }

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
            } else if (key == KeyEvent.VK_J){
                showInventory = !showInventory;
            }
        } else if (gameState.equals("GAME_OVER")) {
            if (key == KeyEvent.VK_R) {
                if (kirby.getScore() > kirby.getHighScore()) {
                    kirby.setHighScore(kirby.getScore());
                    kirby.saveHighScore();
                }

                musicStarted = false;
                musicStopped = false;

                if (backgroundMusic != null) {
                    backgroundMusic.stop();
                    musicStarted = false; // allow replaying next time
                }

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
    public void run() {// Required by Runnable, not used
    }
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (!inShop && gameState.equals("PLAYING")) {
            // If the player clicks on the shop icon
            if (mouseX >= shopIconX && mouseX <= shopIconX + shopIconWidth &&
                    mouseY >= shopIconY && mouseY <= shopIconY + shopIconHeight) {
                inShop = true;
                showInventory = false;
            }
        } else if (inShop) {
            // Pass click to Shop class
            shop.handleClick(mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {
        if (showInventory && inventoryBoxes[0] != null) {
            hoveringVacuumSlot = inventoryBoxes[0].contains(e.getPoint());
        } else {
            hoveringVacuumSlot = false;
        }
        repaint();
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
                int newX = startX + (int)(Math.random() * (endX - startX));
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
                    boolean resisted = kirby.resistExplosion();
                    if(!resisted){
                        kirby.takeDamage(10);
                        kirby.collectTrash(true);
                    } else {
                        kirby.collectTrash(false);
                    }

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
        } else {
            gameState = "GAME_OVER";
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
            int newX = startX + (int)(Math.random() * (endX - startX));
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
        int trashWidth = 75;
        int trashHeight = 69;
        Rectangle newBounds = new Rectangle(x, y, trashWidth, trashHeight);
        for (GoldenTrash existing : trashList) {
            Rectangle existingBounds = new Rectangle(existing.getWorldX(), existing.getY(), trashWidth, trashHeight);
            int ex = existing.getWorldX() + 20;
            int ey = existing.getY() + 20;

            int distX = ex - (x + 20);
            int distY = ey - (y + 20);
            double distance = Math.sqrt(distX * distX + distY * distY);
            if (newBounds.intersects(existingBounds)) {
                return true;
            }
        }
        return false;
    }
}