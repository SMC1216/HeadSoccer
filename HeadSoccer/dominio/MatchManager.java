package dominio;

import java.io.File;
import java.io.Serializable;

/**
 * clase controladora principal que administra todo el flujo y estado del partido
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class MatchManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /** estado de partido activo jugando */
    public static final String STATE_PLAYING = "PLAYING";

    /** estado de partido en pausa */
    public static final String STATE_PAUSED = "PAUSED";

    /** estado de partido terminado por tiempo */
    public static final String STATE_FINISHED = "FINISHED";

    /** goles anotados por el jugador 1 */
    private int scorePlayer1;

    /** goles anotados por el jugador 2 bot */
    private int scorePlayer2;

    /** segundos restantes de juego */
    private int timeRemaining;

    /** estado actual del juego */
    private String matchState;

    /** objeto del jugador humano */
    private Player player1;

    /** objeto del rival bot */
    private BotAI player2;

    /** objeto del balon del partido */
    private Ball ball;

    /** porteria izquierda */
    private Goal leftGoal;

    /** porteria derecha */
    private Goal rightGoal;

    /**
     * constructor por defecto que inicia el juego en dificultad normal
     */
    public MatchManager() {
        this(BotAI.DIFFICULTY_NORMAL);
    }

    /**
     * constructor que permite elegir la dificultad del bot
     * 
     * @param difficultyLevel 1 facil, 2 normal, 3 dificil
     */
    public MatchManager(int difficultyLevel) {
        this.leftGoal = new Goal("LEFT", 15, Ball.GROUND_Y - 160, 65, 160);
        this.rightGoal = new Goal("RIGHT", 815, Ball.GROUND_Y - 160, 65, 160);
        this.player1 = new Player("Jugador 1", 160, Ball.GROUND_Y - 92);
        this.player2 = new BotAI(660, Ball.GROUND_Y - 92, difficultyLevel);
        this.ball = new Ball(430, 180, 36);
        startGame();
    }

    /**
     * inicia el partido restableciendo marcadores y tiempo a 90 segundos
     */
    public void startGame() {
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.timeRemaining = 90;
        this.matchState = STATE_PLAYING;
        resetPositions();
    }

    /**
     * ciclo de actualizacion de cada frame para mover personajes, bot, balon y choques
     */
    public void updateFrame() {
        if (!STATE_PLAYING.equals(matchState)) {
            return;
        }

        player2.calculateMove(ball);
        player1.updatePosition();
        player2.updatePosition();
        ball.updatePosition();

        checkCollisions();
    }

    /**
     * revisa las colisiones entre jugadores, balon y postes, y si hubo gol
     */
    public void checkCollisions() {
        if (ball.getBounds().intersects(player1.getBounds())) {
            ball.onCollision(player1);
            player1.onCollision(ball);
        }

        if (ball.getBounds().intersects(player2.getBounds())) {
            ball.onCollision(player2);
            player2.onCollision(ball);
        }

        leftGoal.onCollision(ball);
        rightGoal.onCollision(ball);

        if (leftGoal.checkGoal(ball)) {
            scorePlayer2++;
            resetPositions();
        } else if (rightGoal.checkGoal(ball)) {
            scorePlayer1++;
            resetPositions();
        }
    }

    /**
     * regresa a los jugadores y al balon a sus lugares de saque inicial
     */
    public void resetPositions() {
        player1.setPosition(160, Ball.GROUND_Y - 92);
        player1.move(0);
        player1.setFacingRight(true);

        player2.setPosition(660, Ball.GROUND_Y - 92);
        player2.move(0);
        player2.setFacingRight(false);

        ball.resetBall(430, 180);
    }

    /**
     * descuenta un segundo al cronometro y recarga cooldowns y energia pasiva
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

    /**
     * pausa el juego si se estaba jugando
     */
    public void pause() {
        if (STATE_PLAYING.equals(matchState)) {
            matchState = STATE_PAUSED;
        }
    }

    /**
     * reanuda el juego si estaba pausado
     */
    public void resume() {
        if (STATE_PAUSED.equals(matchState)) {
            matchState = STATE_PLAYING;
        }
    }

    /**
     * alterna entre pausa y juego segun el estado actual
     */
    public void togglePause() {
        if (STATE_PLAYING.equals(matchState)) {
            matchState = STATE_PAUSED;
        } else if (STATE_PAUSED.equals(matchState)) {
            matchState = STATE_PLAYING;
        }
    }

    /**
     * obtiene el nivel de dificultad actual del bot
     * 
     * @return nivel de 1 a 3
     */
    public int getDifficultyLevel() {
        return player2.getDifficultyLevel();
    }

    /**
     * cambia el nivel de dificultad del bot
     * 
     * @param difficultyLevel nuevo nivel de 1 a 3
     */
    public void setDifficultyLevel(int difficultyLevel) {
        player2.setDifficultyLevel(difficultyLevel);
    }

    /**
     * genera el mensaje del ganador al finalizar los 90 segundos
     * 
     * @return texto del resultado del partido
     */
    public String getWinnerMessage() {
        if (scorePlayer1 > scorePlayer2) {
            return "VICTORIA PARA " + player1.getPlayerName().toUpperCase();
        } else if (scorePlayer2 > scorePlayer1) {
            return "VICTORIA PARA EL BOT AI";
        } else {
            return "EMPATE";
        }
    }

    /**
     * guarda la partida en un archivo usando matchpersistence
     * 
     * @param file archivo destino
     * @throws HeadSoccerException si ocurre error al guardar
     */
    public void salvar(File file) throws HeadSoccerException {
        MatchPersistence.guardar(this, file);
    }

    /**
     * abre y recupera una partida guardada desde un archivo
     * 
     * @param file archivo origen
     * @return objeto matchmanager recuperado
     * @throws HeadSoccerException si ocurre error al abrir
     */
    public static MatchManager abrir(File file) throws HeadSoccerException {
        return MatchPersistence.cargar(file);
    }

    /**
     * obtiene el objeto del jugador 1
     * 
     * @return jugador 1
     */
    public Player getPlayer1() { 
        return player1; 
    }
    
    /**
     * obtiene el objeto del bot rival
     * 
     * @return bot ai
     */
    public BotAI getPlayer2() { 
        return player2; 
    }
    
    /**
     * obtiene el balon del partido
     * 
     * @return balon
     */
    public Ball getBall() { 
        return ball; 
    }
    
    /**
     * obtiene el arco izquierdo
     * 
     * @return porteria izquierda
     */
    public Goal getLeftGoal() { 
        return leftGoal; 
    }
    
    /**
     * obtiene el arco derecho
     * 
     * @return porteria derecha
     */
    public Goal getRightGoal() { 
        return rightGoal; 
    }
    
    /**
     * obtiene los goles del jugador 1
     * 
     * @return goles de p1
     */
    public int getScorePlayer1() { 
        return scorePlayer1; 
    }
    
    /**
     * obtiene los goles del bot
     * 
     * @return goles de bot
     */
    public int getScorePlayer2() { 
        return scorePlayer2; 
    }
    
    /**
     * obtiene el tiempo restante en segundos
     * 
     * @return segundos de partido
     */
    public int getTimeRemaining() { 
        return timeRemaining; 
    }
    
    /**
     * obtiene el estado del juego PLAYING, PAUSED o FINISHED
     * 
     * @return texto del estado
     */
    public String getMatchState() { 
        return matchState; 
    }
}
