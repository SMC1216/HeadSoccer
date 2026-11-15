package dominio;

/**
 * Clase coordinadora del dominio que administra el estado del partido,
 * las físicas, las colisiones y el marcador.
 */
public class MatchManager {
    private int scorePlayer1;
    private int scorePlayer2;
    private int timeRemaining;
    private String matchState; // "PLAYING", "PAUSED", "FINISHED"

    private Player player1;
    private BotAI player2;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;

    public MatchManager() {
        this.leftGoal = new Goal("LEFT", 15, Ball.GROUND_Y - 160, 65, 160);
        this.rightGoal = new Goal("RIGHT", 815, Ball.GROUND_Y - 160, 65, 160);
        this.player1 = new Player("Jugador 1", 160, Ball.GROUND_Y - 92);
        this.player2 = new BotAI(660, Ball.GROUND_Y - 92, 1);
        this.ball = new Ball(430, 180, 36);
        startGame();
    }

    public void startGame() {
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.timeRemaining = 90;
        this.matchState = "PLAYING";
        resetPositions();
    }

    public void updateFrame() {
        if (!"PLAYING".equals(matchState)) return;

        player2.calculateMove(ball);
        player1.updatePosition();
        player2.updatePosition();
        ball.updatePosition();

        checkCollisions();
    }

    public void checkCollisions() {
        // Colisión entre Jugador 1 y el balón
        if (ball.getBounds().intersects(player1.getBounds())) {
            ball.onCollision(player1);
            player1.onCollision(ball);
        }

        // Colisión entre BotAI y el balón
        if (ball.getBounds().intersects(player2.getBounds())) {
            ball.onCollision(player2);
            player2.onCollision(ball);
        }

        // Rebote en travesaños
        leftGoal.onCollision(ball);
        rightGoal.onCollision(ball);

        // Verificación de goles
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
        if ("PLAYING".equals(matchState) && timeRemaining > 0) {
            timeRemaining--;
            player1.gainSpecial(4);
            player2.gainSpecial(4);
            if (timeRemaining == 0) {
                matchState = "FINISHED";
            }
        }
    }

    public void togglePause() {
        if ("PLAYING".equals(matchState)) matchState = "PAUSED";
        else if ("PAUSED".equals(matchState)) matchState = "PLAYING";
    }

    // Getters para que la capa de presentación lea el estado
    public Player getPlayer1() { return player1; }
    public BotAI getPlayer2() { return player2; }
    public Ball getBall() { return ball; }
    public Goal getLeftGoal() { return leftGoal; }
    public Goal getRightGoal() { return rightGoal; }
    public int getScorePlayer1() { return scorePlayer1; }
    public int getScorePlayer2() { return scorePlayer2; }
    public int getTimeRemaining() { return timeRemaining; }
    public String getMatchState() { return matchState; }
}