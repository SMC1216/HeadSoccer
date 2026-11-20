package dominio;

/**
 * Representa a un jugador controlado por un usuario mediante el teclado (Jugador 1 o Jugador 2).
 */
public class Player extends Character {
    private static final long serialVersionUID = 1L;

    private String playerName;

    /**
     * Constructor por defecto para el Jugador 1 (mira a la derecha con FireStrike).
     * @param playerName Nombre del jugador.
     * @param x Posición horizontal inicial.
     * @param y Posición vertical inicial.
     */
    public Player(String playerName, double x, double y) {
        this(playerName, x, y, new FireStrike(), true);
    }

    /**
     * Constructor parametrizado para configurar Jugador 1 o Jugador 2 con habilidad y orientación.
     * @param playerName Nombre del jugador.
     * @param x Posición horizontal inicial.
     * @param y Posición vertical inicial.
     * @param specialAbility Habilidad especial asignada al jugador.
     * @param facingRight true si mira hacia la derecha, false si mira hacia la izquierda.
     */
    public Player(String playerName, double x, double y, Ability specialAbility, boolean facingRight) {
        super(x, y, 72, 92, specialAbility != null ? specialAbility : new FireStrike(), facingRight);
        this.playerName = (playerName != null && !playerName.trim().isEmpty()) ? playerName.trim() : "Jugador";
    }

    /**
     * Procesa una acción de control del jugador sin lanzar excepción verificada.
     * @param action Comando de acción ("LEFT", "RIGHT", "STOP", "JUMP", "KICK", "SPECIAL").
     * @param ball El balón del partido.
     */
    public void handleInput(String action, Ball ball) {
        if ("LEFT".equals(action)) move(-1);
        else if ("RIGHT".equals(action)) move(1);
        else if ("STOP".equals(action)) move(0);
        else if ("JUMP".equals(action)) jump();
        else if ("KICK".equals(action)) kick(ball);
        else if ("SPECIAL".equals(action)) useSpecial(ball);
    }

    /**
     * Valida y ejecuta una acción del jugador, lanzando excepción de dominio si el comando es inválido.
     * @param action Comando de acción ("LEFT", "RIGHT", "STOP", "JUMP", "KICK", "SPECIAL").
     * @param ball El balón del partido.
     * @throws HeadSoccerException Si la acción es nula o desconocida.
     */
    public void executeAction(String action, Ball ball) throws HeadSoccerException {
        if (action == null) {
            throw new HeadSoccerException(HeadSoccerException.ACCION_INVALIDA);
        }
        switch (action) {
            case "LEFT":
            case "RIGHT":
            case "STOP":
            case "JUMP":
            case "KICK":
            case "SPECIAL":
                handleInput(action, ball);
                break;
            default:
                throw new HeadSoccerException(HeadSoccerException.ACCION_INVALIDA);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) throws HeadSoccerException {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new HeadSoccerException(HeadSoccerException.PERSONAJE_INVALIDO);
        }
        this.playerName = playerName.trim();
    }
}