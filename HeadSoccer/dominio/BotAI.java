package dominio;

/**
 * representa al jugador controlado por el computador con tres niveles de dificultad
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class BotAI extends Character {
    private static final long serialVersionUID = 1L;

    /** nivel facil del bot */
    public static final int DIFFICULTY_EASY = 1;

    /** nivel normal del bot */
    public static final int DIFFICULTY_NORMAL = 2;

    /** nivel dificil del bot */
    public static final int DIFFICULTY_HARD = 3;

    /** nivel de dificultad actual del bot */
    private int difficultyLevel;

    /** contador para que el bot demore su reaccion en niveles bajos */
    private int reactionDelayCounter;

    /** direccion de movimiento que lleva el bot actualmente */
    private int currentDirection;

    /**
     * constructor para crear el bot con su dificultad
     * 
     * @param x coordenada inicial en x
     * @param y coordenada inicial en y
     * @param difficultyLevel nivel de 1 a 3
     */
    public BotAI(double x, double y, int difficultyLevel) {
        super(x, y, 72, 92, new Teleport(), false);
        this.difficultyLevel = validateDifficulty(difficultyLevel);
        this.reactionDelayCounter = 0;
        this.currentDirection = 0;
    }

    /**
     * metodo privado para asegurarse de que la dificultad este entre 1 y 3
     * 
     * @param level numero a comprobar
     * @return nivel valido
     */
    private int validateDifficulty(int level) {
        if (level < DIFFICULTY_EASY || level > DIFFICULTY_HARD) {
            return DIFFICULTY_NORMAL;
        }
        return level;
    }

    /**
     * decide y ejecuta el movimiento del bot segun el nivel configurado
     * 
     * @param ball el balon del juego
     */
    public void calculateMove(Ball ball) {
        if (ball == null) {
            return;
        }

        switch (difficultyLevel) {
            case DIFFICULTY_EASY:
                calculateMoveEasy(ball);
                break;
            case DIFFICULTY_HARD:
                calculateMoveHard(ball);
                break;
            case DIFFICULTY_NORMAL:
            default:
                calculateMoveNormal(ball);
                break;
        }

        this.setFacingRight(false);
    }

    /**
     * logica del bot en modo facil con retraso de reaccion
     * 
     * @param ball el balon en juego
     */
    private void calculateMoveEasy(Ball ball) {
        reactionDelayCounter++;
        if (reactionDelayCounter < 6) {
            move(currentDirection);
            return;
        }
        reactionDelayCounter = 0;

        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        if (ballCenter > 520) {
            if (ballCenter < myCenter - 30) {
                currentDirection = -1;
            } else if (ballCenter > myCenter + 30) {
                currentDirection = 1;
            } else {
                currentDirection = 0;
            }
        } else {
            if (myCenter < 690) {
                currentDirection = 1;
            } else if (myCenter > 720) {
                currentDirection = -1;
            } else {
                currentDirection = 0;
            }
        }
        move(currentDirection);

        if (Math.abs(ballCenter - myCenter) < 65 && ball.getY() < getY()) {
            jump();
        }

        if (Math.abs(ballCenter - myCenter) < 65) {
            kick(ball);
        }
    }

    /**
     * logica del bot en modo normal con buen balance entre ataque y defensa
     * 
     * @param ball el balon en juego
     */
    private void calculateMoveNormal(Ball ball) {
        reactionDelayCounter++;
        if (reactionDelayCounter < 2) {
            move(currentDirection);
        } else {
            reactionDelayCounter = 0;
            double ballCenter = ball.getBounds().getCenterX();
            double myCenter = this.getBounds().getCenterX();

            if (ballCenter > 420) {
                if (ballCenter < myCenter - 20) {
                    currentDirection = -1;
                } else if (ballCenter > myCenter + 20) {
                    currentDirection = 1;
                } else {
                    currentDirection = 0;
                }
            } else {
                if (myCenter < 670) {
                    currentDirection = 1;
                } else if (myCenter > 710) {
                    currentDirection = -1;
                } else {
                    currentDirection = 0;
                }
            }
            move(currentDirection);
        }

        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        if (Math.abs(ballCenter - myCenter) < 90 && ball.getY() < getY() + 10) {
            jump();
        }

        if (Math.abs(ballCenter - myCenter) < 85) {
            kick(ball);
        }

        if (getSpecialMeter() >= 100 && getSpecialAbility() != null && getSpecialAbility().isReady()) {
            boolean peligroso = ball.getVelocityX() > 3.5 && ballCenter > 560;
            if (peligroso) {
                useSpecial(ball);
                kick(ball);
            }
        }
    }

    /**
     * logica del bot en modo dificil que calcula la trayectoria futura del balon
     * 
     * @param ball el balon en juego
     */
    private void calculateMoveHard(Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        double projectedBallX = ballCenter + (ball.getVelocityX() * 8.0);
        double targetX = projectedBallX + 35.0;

        if (targetX < 440) {
            targetX = 440;
        }
        if (targetX > 740) {
            targetX = 740;
        }

        if (myCenter < targetX - 10) {
            move(1);
        } else if (myCenter > targetX + 10) {
            move(-1);
        } else {
            move(0);
        }

        if (Math.abs(projectedBallX - myCenter) < 110 && ball.getY() < getY() + 25) {
            jump();
        }

        if (Math.abs(ballCenter - myCenter) < 95) {
            kick(ball);
        }

        if (getSpecialMeter() >= 100 && getSpecialAbility() != null && getSpecialAbility().isReady()) {
            boolean peligro = ball.getVelocityX() > 2.0 && ballCenter > 500;
            boolean contragolpe = ballCenter > 380 && ballCenter < 600 && ball.getY() < Ball.GROUND_Y - 40;
            if (peligro || contragolpe) {
                useSpecial(ball);
                kick(ball);
            }
        }
    }

    /**
     * obtiene la dificultad actual
     * 
     * @return numero del 1 al 3
     */
    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    /**
     * cambia la dificultad del bot
     * 
     * @param difficultyLevel nuevo nivel
     */
    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = validateDifficulty(difficultyLevel);
    }
}
