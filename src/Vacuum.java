public class Vacuum {
    private int tier;

    public Vacuum(int tier){
        this.tier = tier;
    }

    public boolean resistExplosion(){
        if(tier == 1){
            return false;
        } else if (tier == 2){
            return Math.random() < 0.2; // 20% chance
        } else if (tier == 3){
            return Math.random() < 0.4; // 40% chance
        } else if (tier == 4){
            return Math.random() < 0.6; // 60% chance
        } else if (tier == 5){
            return Math.random() < 0.8; // 80% chance
        }
        return false;
    }

    public int getTier(){
        return tier;
    }

    public void setTier(int newTier){
        tier = newTier;
    }



}
