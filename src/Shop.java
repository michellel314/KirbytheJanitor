import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Shop {
    private Kirby kirby;

    private BufferedImage shopBackground;
    private BufferedImage vacuumImage;
    private BufferedImage cakeImage;

    private int vacuumTier;
    private int vacuumUpgradeCost;
    private int cakeCost = 1200;
    private int cakePurchasedCount = 0;
    private final int maxCakePurchases = 3;

    // Positions and sizes
    private final int vacuumX = 100, vacuumY = 150;
    private final int cakeX = 400, cakeY = 150;
    private final int itemSize = 200;
    private final int buttonWidth = 150, buttonHeight = 40;

    // Feedback message
    private String message = "";
    private long messageTime = 0;

    public Shop(Kirby kirby, Vacuum vacuum) {
        this.kirby = kirby;
        this.vacuumTier = vacuum.getTier();
        this.vacuumUpgradeCost = 400;

        try {
            shopBackground = ImageIO.read(new File("src/Shop/SHOP_BACKGROUND.jpg"));
            vacuumImage = ImageIO.read(new File("src/Shop/Vacuum.png"));
            cakeImage = ImageIO.read(new File("src/Shop/StrawberryCake.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(Graphics g) {
        g.drawImage(shopBackground, 0, 0, null);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Points: " + kirby.getScore(), 20, 30);

        // --- VACUUM ---
        g.drawImage(vacuumImage, vacuumX, vacuumY, itemSize, itemSize, null);

        // Draw Vacuum Buy Button
        g.setColor(Color.DARK_GRAY);
        g.fillRect(vacuumX, vacuumY + itemSize + 10, buttonWidth, buttonHeight);
        g.setColor(Color.WHITE);
        g.drawString("Upgrade Vacuum", vacuumX + 10, vacuumY + itemSize + 35);

        // Draw Vacuum Cost/Status
        String vacuumStatus = (vacuumTier <= 5) ?
                "Upgrade Cost: " + vacuumUpgradeCost : "Max Tier Reached";
        g.drawString("Vacuum Tier: " + (vacuumTier + 1), vacuumX, vacuumY + itemSize + 70);
        g.drawString(vacuumStatus, vacuumX, vacuumY + itemSize + 90);

        // --- CAKE ---
        g.drawImage(cakeImage, cakeX, cakeY, itemSize, itemSize, null);

        // Draw Cake Buy Button
        g.setColor(Color.DARK_GRAY);
        g.fillRect(cakeX + 30, cakeY + itemSize + 10, buttonWidth, buttonHeight);
        g.setColor(Color.WHITE);
        g.drawString("Buy Cake", cakeX + 30, cakeY + itemSize + 35);

        // Draw Cake Cost/Status
        g.drawString("Cake Cost: " + cakeCost, cakeX + 30, cakeY + itemSize + 70);
        g.drawString("Purchased: " + cakePurchasedCount + "/" + maxCakePurchases, cakeX + 30, cakeY + itemSize + 90);

        // Draw message if recent
        if (System.currentTimeMillis() - messageTime < 2000 && !message.isEmpty()) {
            g.setColor(Color.YELLOW);
            g.drawString(message, 270, 460);
        }
    }

    public void handleClick(int mouseX, int mouseY) {
        // Vacuum Button
        if (mouseX >= vacuumX && mouseX <= vacuumX + buttonWidth &&
                mouseY >= vacuumY + itemSize + 10 && mouseY <= vacuumY + itemSize + 10 + buttonHeight) {
            buyOrUpgradeVacuum();
        }

        // Cake Button
        if (mouseX >= cakeX && mouseX <= cakeX + buttonWidth &&
                mouseY >= cakeY + itemSize + 10 && mouseY <= cakeY + itemSize + 10 + buttonHeight) {
            buyCake();
        }
    }

    private void buyOrUpgradeVacuum() {
        int currentScore = kirby.getScore();

        if (vacuumTier + 1 > 5) {
            message = "Max vacuum tier reached!";
        } else if (currentScore >= vacuumUpgradeCost) {
            kirby.setScore(currentScore - vacuumUpgradeCost);  // Deduct points from Kirby
            vacuumTier++;
            kirby.getVacuum().setTier(vacuumTier);
            vacuumUpgradeCost *= 2;
            message = "Vacuum upgraded!";
        } else {
            message = "Not enough points!";
        }
        messageTime = System.currentTimeMillis();
    }


    private void buyCake() {
        int currentScore = kirby.getScore();

        if (cakePurchasedCount >= maxCakePurchases) {
            message = "Max cake limit reached!";
        } else if (currentScore >= cakeCost) {
            kirby.setScore(currentScore - cakeCost);  // Deduct points from Kirby
            cakePurchasedCount++;
            kirby.restoreHealth(50);
            message = "Kirby healed!";
        } else {
            message = "Not enough points!";
        }
        messageTime = System.currentTimeMillis();
    }

    public void resetCosts(){
        vacuumUpgradeCost = 400;
        cakeCost = 1200;
        cakePurchasedCount = 0;
        vacuumTier = 0;
    }
}
