import java.io.Serializable;

public class Hive implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private boolean isHealthy;
    private boolean isNeedsAttention;
    private boolean isQueenless;
    private double honeyLevel;
    
    public Hive(int id, boolean isHealthy, boolean isNeedsAttention, boolean isQueenless, double honeyLevel) {
        this.id = id;
        this.isHealthy = isHealthy;
        this.isNeedsAttention = isNeedsAttention;
        this.isQueenless = isQueenless;
        this.honeyLevel = honeyLevel;
    }
    
    public int getId() { return id; }
    public boolean isHealthy() { return isHealthy; }
    public boolean isNeedsAttention() { return isNeedsAttention; }
    public boolean isQueenless() { return isQueenless; }
    public double getHoneyLevel() { return honeyLevel; }
    
    public void setHealthy(boolean isHealthy) { this.isHealthy = isHealthy; }
    public void setNeedsAttention(boolean isNeedsAttention) { this.isNeedsAttention = isNeedsAttention; }
    public void setQueenless(boolean isQueenless) { this.isQueenless = isQueenless; }
    public void setHoneyLevel(double honeyLevel) { this.honeyLevel = honeyLevel; }
}