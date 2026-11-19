package dominio;

import java.io.File;
import java.io.Serializable;

/**
 * clase principal del dominio que gestiona el partido completo
 * controla el marcador, el tiempo, el estado de juego y los objetos de la cancha
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class MatchManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATE_PLAYING = "PLAYING";
    public static final String STATE_PAUSED = "PAUSED";
    public static final String STATE_FINISHED = "FINISHED";

    private int scorePlayer1;
    private int scorePlayer2;
    private int timeRemaining;
    private String matchState;

    private Player player1;
    private BotAI player2;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;

    public MatchManager() {
        this(BotAI.DIFFICULTY_NORMAL);
    }

    public MatchManager(int difficultyLevel) {
        this.leftGoal = new Goal("LEFT", 15, Ball.GROUND_Y - 160, 65, 160);
        this.rightGoal = new Goal("RIGHT", 815, Ball.GROUND_Y - 160, 65, 160);
        this.player1 = new Player("Jugador 1", 160, Ball.GROUND_Y - 92);
        this.player2 = new BotAI(660, Ball.GROUND_Y - 92, difficultyLevel);
        this.ball = new Ball(430, 180, 36);
        startGame();
    }

    public void startGame() {
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.timeRemaining = 90;
        this.matchState = STATE_PLAYING;
        resetPositions();
    }

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

    public void resetPositions() {
        player1.setPosition(160, Ball.GROUND_Y - 92);
        player1.move(0);
        player1.setFacingRight(true);

        player2.setPosition(660, Ball.GROUND_Y - 92);
        player2.move(0);
        player2.setFacingRight(false);

        ball.resetBall(430, 180);
    }

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
        return player2.getDifficultyLevel();
    }

    public void setDifficultyLevel(int difficultyLevel) {
        player2.setDifficultyLevel(difficultyLevel);
    }

    public String getWinnerMessage() {
        if (scorePlayer1 > scorePlayer2) {
            return "VICTORIA PARA " + player1.getPlayerName().toUpperCase();
        } else if (scorePlayer2 > scorePlayer1) {
            return "VICTORIA PARA EL BOT AI";
        } else {
            return "EMPATE";
        }
    }

    public void salvar(File file) throws HeadSoccerException {
        MatchPersistence.guardar(this, file);
    }

    public static MatchManager abrir(File file) throws HeadSoccerException {
        return MatchPersistence.cargar(file);
    }

    public Player getPlayer1() { 
        return player1; 
    }
    
    public BotAI getPlayer2() { 
        return player2; 
    }
    
    public Ball getBall() { 
        return ball; 
    }
    
    public Goal getLeftGoal() { 
        return leftGoal; 
    }
    
    public Goal getRightGoal() { 
        return rightGoal; 
    }
    
    public int getScorePlayer1() { 
        return scorePlayer1; 
    }
    
    public int getScorePlayer2() { 
        return scorePlayer2; 
    }
    
    public int getTimeRemaining() { 
        return timeRemaining; 
    }
    
    public String getMatchState() { 
        return matchState; 
    }
}
