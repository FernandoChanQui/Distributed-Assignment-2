import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int score;

    public Player(String name) {
        this.name = name;
        this.score = 0;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public void addScore(int points) { score += points; }
    public void setScore(int score) { this.score = score; }
    public void resetScore() { score = 0; }
    
}