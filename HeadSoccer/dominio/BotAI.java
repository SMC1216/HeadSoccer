package dominio;

/**
 * Representa al oponente controlado automáticamente por el computador (IA).
 * Implementa tres niveles reales de dificultad (Fácil, Normal y Difícil) basados
 * en reglas de decisión, predicción vectorial de trayectorias y tiempos de reacción.
 */
public class BotAI extends Character {
    private static final long serialVersionUID = 1L;

    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_NORMAL = 2;
    public static final int DIFFICULTY_HARD = 3;

    private int difficultyLevel;
    private int reactionDelayCounter;
    private int currentDirection;

    /**
     * Constructor del Bot oponente.
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     * @param difficultyLevel Nivel de dificultad (1=Fácil, 2=Normal, 3=Difícil).
     */
    public BotAI(double x, double y, int difficultyLevel) {
        super(x, y, 72, 92, new Teleport(), false);
        this.difficultyLevel = validateDifficulty(difficultyLevel);
        this.reactionDelayCounter = 0;
        this.currentDirection = 0;
    }

    private int validateDifficulty(int level) {
        if (level < DIFFICULTY_EASY || level > DIFFICULTY_HARD) {
            return DIFFICULTY_NORMAL;
        }
        return level;
    }

    /**
     * Evalúa y ejecuta las decisiones tácticas del bot según su nivel de dificultad.
     * @param ball El balón en juego.
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

        // El bot siempre mira prioritariamente hacia la izquierda (portería rival)
        this.setFacingRight(false);
    }

    /**
     * Nivel Fácil: Decisiones lentas con retardo de reacción, menor rango de impacto
     * y sin anticipación ni uso de poderes.
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
            // Regresa lentamente a su arco
            if (myCenter < 690) currentDirection = 1;
            else if (myCenter > 720) currentDirection = -1;
            else currentDirection = 0;
        }
        move(currentDirection);

        // Salto con menor precisión
        if (Math.abs(ballCenter - myCenter) < 65 && ball.getY() < getY()) {
            jump();
        }

        // Patea con rango reducido
        if (Math.abs(ballCenter - myCenter) < 65) {
            kick(ball);
        }
    }

    /**
     * Nivel Normal: Equilibrio de ataque y defensa con buena cobertura de portería.
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
                if (ballCenter < myCenter - 20) currentDirection = -1;
                else if (ballCenter > myCenter + 20) currentDirection = 1;
                else currentDirection = 0;
            } else {
                if (myCenter < 670) currentDirection = 1;
                else if (myCenter > 710) currentDirection = -1;
                else currentDirection = 0;
            }
            move(currentDirection);
        }

        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        // Salta si el balón viene elevado
        if (Math.abs(ballCenter - myCenter) < 90 && ball.getY() < getY() + 10) {
            jump();
        }

        // Patea en rango estándar
        if (Math.abs(ballCenter - myCenter) < 85) {
            kick(ball);
        }

        // Uso defensivo del poder especial
        if (getSpecialMeter() >= 100 && getSpecialAbility() != null && getSpecialAbility().isReady()) {
            boolean peligroso = ball.getVelocityX() > 3.5 && ballCenter > 560;
            if (peligroso) {
                useSpecial(ball);
                kick(ball);
            }
        }
    }

    /**
     * Nivel Difícil: Reacción instantánea, predicción de trayectoria balística del balón,
     * saltos de anticipación y uso quirúrgico de habilidades.
     */
    private void calculateMoveHard(Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        // Predicción balística: proyectar dónde estará el balón en los próximos fotogramas
        double projectedBallX = ballCenter + (ball.getVelocityX() * 8.0);

        // Posicionarse detrás del balón proyectado para impulsarlo hacia la izquierda
        double targetX = projectedBallX + 35.0; // Se coloca a la derecha del balón para patear a la izquierda

        if (targetX < 440) {
            targetX = 440; // No pasar demasiado al área rival para no regalar su arco
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

        // Salto de intercepción o remate aéreo anticipado
        if (Math.abs(projectedBallX - myCenter) < 110 && ball.getY() < getY() + 25) {
            jump();
        }

        // Patea con máximo timing
        if (Math.abs(ballCenter - myCenter) < 95) {
            kick(ball);
        }

        // Uso inteligente de la habilidad Teleport (ataque o emergencia)
        if (getSpecialMeter() >= 100 && getSpecialAbility() != null && getSpecialAbility().isReady()) {
            boolean peligro = ball.getVelocityX() > 2.0 && ballCenter > 500;
            boolean contragolpe = ballCenter > 380 && ballCenter < 600 && ball.getY() < Ball.GROUND_Y - 40;
            if (peligro || contragolpe) {
                useSpecial(ball);
                kick(ball);
            }
        }
    }

    // Getters y Setters
    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = validateDifficulty(difficultyLevel);
    }
}
