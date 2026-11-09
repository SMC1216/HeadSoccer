package dominio;

/**
 * Representa al jugador controlado mediante el teclado.
 */
public class Player extends Character {
    private String playerName;

    public Player(String playerName, double x, double y) {
        super(x, y, 72, 92, new FireStrike(), true);
        this.playerName = playerName;
    }

    public void handleInput(String action, Ball ball) {
        if ("LEFT".equals(action)) move(-1);
        else if ("RIGHT".equals(action)) move(1);
        else if ("STOP".equals(action)) move(0);
        else if ("JUMP".equals(action)) jump();
        else if ("KICK".equals(action)) kick(ball);
        else if ("SPECIAL".equals(action)) useSpecial(ball);
    }

    public String getPlayerName() { return playerName; }
}