package dominio;

import java.io.File;
import java.io.Serializable;

/**
 * Clase coordinadora del dominio que administra el estado del partido,
 * las físicas, las colisiones, el cronómetro, el marcador y las dificultades.
 */
public class MatchManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATE_MENU = "MENU";
    public static final String STATE_PLAYING = "PLAYING";
    public static final String STATE_PAUSED = "PAUSED";
    public static final String STATE_FINISHED = "FINISHED";

    public static final String MODE_PVE = "PVE";
    public static final String MODE_PVP = "PVP";

    private int scorePlayer1;
    private int scorePlayer2;
    private int timeRemaining;
    private String matchState;
    private String gameMode;
    private int difficultyLevel;

    private Player player1;
    private Character player2;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;

    /**
     * Constructor por defecto: inicia en modo contra el Bot con dificultad normal (2).
     */
    public MatchManager() {
        this(BotAI.DIFFICULTY_NORMAL);
    }

    /**
     * Constructor con selección de dificultad de la IA (modo PVE).
     * @param difficultyLevel Nivel de dificultad (1=Fácil, 2=Normal, 3=Difícil).
     */
    public MatchManager(int difficultyLevel) {
        this.gameMode = MODE_PVE;
        this.leftGoal = new Goal("LEFT", 15, Ball.GROUND_Y - 160, 65, 160);
        this.rightGoal = new Goal("RIGHT", 815, Ball.GROUND_Y - 160, 65, 160);
        this.player1 = new Player("Jugador 1", 160, Ball.GROUND_Y - 92);
        BotAI bot = new BotAI(660, Ball.GROUND_Y - 92, difficultyLevel);
        this.difficultyLevel = bot.getDifficultyLevel();
        this.player2 = bot;
        this.ball = new Ball(430, 180, 36);
        startGame();
    }

    /**
     * Constructor con validación estricta de modo de juego (PVE o PVP).
     * @param gameMode Modo de juego (MODE_PVE o MODE_PVP).
     * @throws HeadSoccerException Si el modo de juego es inválido.
     */
    public MatchManager(String gameMode) throws HeadSoccerException {
        this(gameMode, BotAI.DIFFICULTY_NORMAL);
    }

    /**
     * Constructor completo con validación estricta de modo de juego y dificultad.
     * @param gameMode Modo de juego (MODE_PVE o MODE_PVP).
     * @param difficultyLevel Nivel de dificultad para el Bot (1, 2 o 3).
     * @throws HeadSoccerException Si el modo o la dificultad no son válidos.
     */
    public MatchManager(String gameMode, int difficultyLevel) throws HeadSoccerException {
        this.leftGoal = new Goal("LEFT", 15, Ball.GROUND_Y - 160, 65, 160);
        this.rightGoal = new Goal("RIGHT", 815, Ball.GROUND_Y - 160, 65, 160);
        this.ball = new Ball(430, 180, 36);
        configureMatch(gameMode, difficultyLevel);
    }

    /**
     * Configura e inicia un nuevo partido en el modo especificado (contra Bot o 2 Jugadores).
     * @param gameMode Modo de juego (MODE_PVE o MODE_PVP).
     * @param difficultyLevel Nivel de dificultad de la IA (1=Fácil, 2=Normal, 3=Difícil).
     * @throws HeadSoccerException Si el modo de juego o la dificultad son inválidos.
     */
    public void configureMatch(String gameMode, int difficultyLevel) throws HeadSoccerException {
        if (!MODE_PVE.equals(gameMode) && !MODE_PVP.equals(gameMode)) {
            throw new HeadSoccerException(HeadSoccerException.MODO_JUEGO_INVALIDO);
        }
        if (difficultyLevel < BotAI.DIFFICULTY_EASY || difficultyLevel > BotAI.DIFFICULTY_HARD) {
            throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_INVALIDA);
        }

        this.gameMode = gameMode;
        this.difficultyLevel = difficultyLevel;
        this.player1 = new Player("Jugador 1", 160, Ball.GROUND_Y - 92, new FireStrike(), true);

        if (MODE_PVP.equals(gameMode)) {
            this.player2 = new Player("Jugador 2", 660, Ball.GROUND_Y - 92, new GiantKick(), false);
        } else {
            this.player2 = new BotAI(660, Ball.GROUND_Y - 92, difficultyLevel);
        }

        startGame();
    }

    /**
     * Inicia o reinicia el partido a su estado inicial conservando el modo actual.
     */
    public void startGame() {
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.timeRemaining = 90;
        this.matchState = STATE_PLAYING;
        if (player1 != null && player1.getSpecialAbility() != null) {
            player1.getSpecialAbility().reset();
            player1.setSpecialMeter(80);
        }
        if (player2 != null && player2.getSpecialAbility() != null) {
            player2.getSpecialAbility().reset();
            player2.setSpecialMeter(80);
        }
        resetPositions();
    }

    /**
     * Devuelve el estado del partido al menú principal sin cerrar el juego.
     */
    public void returnToMenu() {
        this.matchState = STATE_MENU;
        resetPositions();
    }

    /**
     * Ciclo principal de actualización de físicas y lógica de juego por fotograma.
     */
    public void updateFrame() {
        if (!STATE_PLAYING.equals(matchState)) {
            return;
        }

        if (player2 instanceof BotAI) {
            ((BotAI) player2).calculateMove(ball);
        }
        player1.updatePosition();
        player2.updatePosition();
        ball.updatePosition();

        checkCollisions();
    }

    /**
     * Evalúa las colisiones entre jugadores, balón y porterías, y verifica goles.
     */
    public void checkCollisions() {
        // Colisión física entre Jugador 1 y Jugador 2 (o BotAI)
        if (player1.getBounds().intersects(player2.getBounds())) {
            player1.onCollision(player2);
            player2.onCollision(player1);
        }

        // Colisión entre Jugador 1 y el balón
        if (ball.getBounds().intersects(player1.getBounds())) {
            ball.onCollision(player1);
            player1.onCollision(ball);
        }

        // Colisión entre Jugador 2 / BotAI y el balón
        if (ball.getBounds().intersects(player2.getBounds())) {
            ball.onCollision(player2);
            player2.onCollision(ball);
        }

        // Rebotes en travesaños y postes
        leftGoal.onCollision(ball);
        rightGoal.onCollision(ball);

        // Verificación reglamentaria de goles
        if (leftGoal.checkGoal(ball)) {
            scorePlayer2++;
            resetPositions();
        } else if (rightGoal.checkGoal(ball)) {
            scorePlayer1++;
            resetPositions();
        }
    }

    /**
     * Restablece las posiciones de ambos personajes y del balón en el centro de la cancha.
     */
    public void resetPositions() {
        player1.resetState(160, Ball.GROUND_Y - 92, true);
        player2.resetState(660, Ball.GROUND_Y - 92, false);
        ball.resetBall(430, 180);
    }

    /**
     * Procesa una acción de control para el Jugador 1 con validación de dominio.
     * @param action Acción solicitada ("LEFT", "RIGHT", "STOP", "JUMP", "KICK", "SPECIAL").
     * @throws HeadSoccerException Si la acción es inválida.
     */
    public void handlePlayer1Input(String action) throws HeadSoccerException {
        player1.executeAction(action, ball);
    }

    /**
     * Procesa una acción de control para el Jugador 2 en modo 2 Jugadores (PVP).
     * @param action Acción solicitada ("LEFT", "RIGHT", "STOP", "JUMP", "KICK", "SPECIAL").
     * @throws HeadSoccerException Si el partido está en modo contra el Bot o la acción es inválida.
     */
    public void handlePlayer2Input(String action) throws HeadSoccerException {
        if (!(player2 instanceof Player)) {
            throw new HeadSoccerException(HeadSoccerException.JUGADOR_NO_HUMANO);
        }
        ((Player) player2).executeAction(action, ball);
    }

    /**
     * Decrementa el tiempo del partido y actualiza los cooldowns de habilidades cada segundo.
     */
    public void decrementTimer() {
        if (STATE_PLAYING.equals(matchState) && timeRemaining > 0) {
            timeRemaining--;
            player1.gainSpecial(3);
            player2.gainSpecial(3);

            player1.updateCooldown();
            player2.updateCooldown();

            if (timeRemaining == 0) {
                matchState = STATE_FINISHED;
            }
        }
    }

    public void pause() {
        if (STATE_PLAYING.equals(matchState)) {
            matchState = STATE_PAUSED;
        }
    }

    public void resume() {
        if (STATE_PAUSED.equals(matchState)) {
            matchState = STATE_PLAYING;
        }
    }

    public void togglePause() {
        if (STATE_PLAYING.equals(matchState)) {
            matchState = STATE_PAUSED;
        } else if (STATE_PAUSED.equals(matchState)) {
            matchState = STATE_PLAYING;
        }
    }

    public int getDifficultyLevel() {
        if (player2 instanceof BotAI) {
            return ((BotAI) player2).getDifficultyLevel();
        }
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        if (player2 instanceof BotAI) {
            ((BotAI) player2).setDifficultyLevel(difficultyLevel);
            this.difficultyLevel = ((BotAI) player2).getDifficultyLevel();
        } else {
            if (difficultyLevel < BotAI.DIFFICULTY_EASY || difficultyLevel > BotAI.DIFFICULTY_HARD) {
                this.difficultyLevel = BotAI.DIFFICULTY_NORMAL;
            } else {
                this.difficultyLevel = difficultyLevel;
            }
        }
    }

    /**
     * Determina el mensaje con el ganador del partido al finalizar el tiempo.
     * @return Texto descriptivo del resultado del partido.
     */
    public String getWinnerMessage() {
        if (scorePlayer1 > scorePlayer2) {
            return "¡VICTORIA PARA " + player1.getPlayerName().toUpperCase() + "!";
        } else if (scorePlayer2 > scorePlayer1) {
            if (player2 instanceof Player) {
                return "¡VICTORIA PARA " + ((Player) player2).getPlayerName().toUpperCase() + "!";
            }
            return "¡VICTORIA PARA EL BOT AI!";
        } else {
            return "¡EMPATE!";
        }
    }

    /**
     * Guarda el estado actual del partido en un archivo usando MatchPersistence.
     * @param file Archivo destino donde se almacenará el partido.
     * @throws HeadSoccerException Si ocurre un error de entrada/salida.
     */
    public void salvar(File file) throws HeadSoccerException {
        MatchPersistence.guardar(this, file);
    }

    /**
     * Carga un partido guardado desde un archivo usando MatchPersistence.
     * @param file Archivo origen que contiene la partida guardada.
     * @return Objeto MatchManager restaurado.
     * @throws HeadSoccerException Si ocurre un error de lectura o el archivo no existe.
     */
    public static MatchManager abrir(File file) throws HeadSoccerException {
        return MatchPersistence.cargar(file);
    }

    // Getters
    public Player getPlayer1() { return player1; }
    public Character getPlayer2() { return player2; }
    public BotAI getBotPlayer2() { return (player2 instanceof BotAI) ? (BotAI) player2 : null; }
    public Player getHumanPlayer2() { return (player2 instanceof Player) ? (Player) player2 : null; }
    public boolean isTwoPlayerMode() { return MODE_PVP.equals(gameMode) || (player2 instanceof Player); }
    public String getGameMode() { return gameMode != null ? gameMode : MODE_PVE; }
    public Ball getBall() { return ball; }
    public Goal getLeftGoal() { return leftGoal; }
    public Goal getRightGoal() { return rightGoal; }
    public int getScorePlayer1() { return scorePlayer1; }
    public int getScorePlayer2() { return scorePlayer2; }
    public int getTimeRemaining() { return timeRemaining; }
    public String getMatchState() { return matchState; }
}
