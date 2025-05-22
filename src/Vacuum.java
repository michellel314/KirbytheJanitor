public class Vacuum {
    private int tier;

    public Vacuum(int tier){
        this.tier = tier;
    }

    public boolean resistExplosion(){
        if(tier == 1){
            return false;
        } else if (tier == 2){
            return Math.random() < 0.3; // 30% chance
        } else if (tier == 3){
            return Math.random() < 0.6; // 60% chance
        }
        return false;
    }
}
