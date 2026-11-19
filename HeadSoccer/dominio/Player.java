package dominio;

/**
 * representa al jugador humano que se controla mediante teclado
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Player extends Character {
    private static final long serialVersionUID = 1L;

    /** nombre que identifica al jugador */
    private String playerName;

    /**
     * constructor para crear el jugador humano
     * 
     * @param playerName nombre del jugador
     * @param x posicion inicial en x
     * @param y posicion inicial en y
     */
    public Player(String playerName, double x, double y) {
        super(x, y, 72, 92, new FireStrike(), true);
        this.playerName = playerName;
    }

    /**
     * recibe y ejecuta los comandos que manda la ventana por teclado
     * 
     * @param action texto de la accion como LEFT, RIGHT, JUMP, KICK o SPECIAL
     * @param ball el balon del partido
     */
    public void handleInput(String action, Ball ball) {
        if ("LEFT".equals(action)) {
            move(-1);
        } else if ("RIGHT".equals(action)) {
            move(1);
        } else if ("STOP".equals(action)) {
            move(0);
        } else if ("JUMP".equals(action)) {
            jump();
        } else if ("KICK".equals(action)) {
            kick(ball);
        } else if ("SPECIAL".equals(action)) {
            useSpecial(ball);
        }
    }

    /**
     * obtiene el nombre del jugador
     * 
     * @return texto con el nombre
     */
    public String getPlayerName() { 
        return playerName; 
    }
}
