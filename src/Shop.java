import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
public class Shop {
    private int points;
    private Kirby kirby;
    private Vacuum vacuum;

    private BufferedImage shopBackground;
    private BufferedImage vacuumImage;
    private BufferedImage cakeImage;

    private int vacuumTier;
    private int vacuumUpgradeCost;
    private int cakeCost = 1200;
    private int cakePurchasedCount = 0;
    private final int maxCakePurchases = 3;


    public Shop(Kirby kirby, Vacuum vacuum, int points){
        this.kirby = kirby;
        this.vacuum = vacuum;
        this.points = points;
        this.vacuumTier = vacuum.getTier();
        this.vacuumUpgradeCost = 300;  // Starting cost for upgrade

        try {
            shopBackground = ImageIO.read(new File("src/Shop/SHOP_BACKGROUND.jpg"));
            vacuumImage = ImageIO.read(new File("src/Shop/Vacuum.png"));
            cakeImage = ImageIO.read(new File("src/Shop/StrawberryCake.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPoints(){
        return points;
    }

    public void render (Graphics g){
        g.drawImage(shopBackground, 0, 0, null);
        g.drawImage(vacuumImage,100, 200, null);
        g.setColor(Color.WHITE);
        g.drawString("Vacuum Tier: " + vacuumTier, 150, 250);
        if (vacuumTier < 4) {
            g.drawString("Upgrade Cost: " + vacuumUpgradeCost, 150, 300);
        } else {
            g.drawString("Max Tier Reached", 150, 300);
        }

        // Draw cake with cost and purchased count
        g.drawImage(cakeImage, 300, 200, null);
        g.drawString("Cake Cost: " + cakeCost, 300, 250);
        g.drawString("Purchased: " + cakePurchasedCount + "/" + maxCakePurchases, 300, 300);

        // Draw player points
        g.drawString("Points: " + points, 10, 20);

    }

    public void updatePoints (int newPoints){
        this.points = newPoints;
    }

    public void handleClick(int mouseX, int mouseY){

    }


    private void buyOrUpgradeVacuum(){
        if(vacuumTier >= 4){
            return;
        }

        if(points >= vacuumUpgradeCost){
            points -= vacuumUpgradeCost;
            vacuumTier++;
            vacuum = new Vacuum(vacuumTier);
            kirby.setVacuum(vacuum);
            vacuumUpgradeCost *= 2;
        }
    }

    private void buyCake(){
        if(cakePurchasedCount >= maxCakePurchases){
            if(points >= cakeCost){
                points -= cakeCost;
                cakePurchasedCount++;
                kirby.restoreHealth(50);
            }
        }
    }
}
