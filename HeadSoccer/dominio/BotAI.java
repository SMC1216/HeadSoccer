package dominio;

/**
 * Representa al oponente controlado automáticamente por el computador (IA).
 * Implementa tres niveles reales de dificultad (Fácil, Normal y Difícil) basados
 * en reglas de decisión, predicción vectorial de trayectorias y tiempos de reacción,
 * soportando cualquier poder especial y equipo configurado en el menú de inicio.
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
     * Constructor del Bot oponente con habilidad por defecto (Teleport) y equipo Alemania.
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     * @param difficultyLevel Nivel de dificultad (1=Fácil, 2=Normal, 3=Difícil).
     */
    public BotAI(double x, double y, int difficultyLevel) {
        this(x, y, difficultyLevel, new Teleport(), Team.alemania());
    }

    /**
     * Constructor del Bot oponente con habilidad personalizada.
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     * @param difficultyLevel Nivel de dificultad (1=Fácil, 2=Normal, 3=Difícil).
     * @param specialAbility Habilidad especial asignada al Bot.
     */
    public BotAI(double x, double y, int difficultyLevel, Ability specialAbility) {
        this(x, y, difficultyLevel, specialAbility, Team.alemania());
    }

    /**
     * Constructor completo del Bot oponente con habilidad y equipo configurables.
     * @param x Coordenada X inicial.
     * @param y Coordenada Y inicial.
     * @param difficultyLevel Nivel de dificultad (1=Fácil, 2=Normal, 3=Difícil).
     * @param specialAbility Habilidad especial asignada al Bot.
     * @param team Equipo nacional asignado al Bot.
     */
    public BotAI(double x, double y, int difficultyLevel, Ability specialAbility, Team team) {
        super(x, y, 72, 92, specialAbility != null ? specialAbility : new Teleport(), false, team != null ? team : Team.alemania());
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
     * Permite desplazamiento y orientación natural en ambas direcciones (izquierda y derecha).
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
    }

    /**
     * Ejecuta una patada orientada hacia la portería rival (izquierda) únicamente
     * si el bot está posicionado a la derecha del balón, evitando autogoles.
     * @param ball El balón a patear.
     */
    private void kickTowardsOpponentGoal(Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        if (myCenter >= ballCenter - 12) {
            this.setFacingRight(false);
            kick(ball);
        }
    }

    /**
     * Nivel Fácil: Decisiones con retardo de reacción, movimiento en ambas direcciones,
     * menor rango de impacto y sin uso de poderes.
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

        if (ballCenter > 360) {
            double targetX = ballCenter + 30.0;
            if (targetX > 745) targetX = 745;

            if (myCenter > targetX + 25) {
                currentDirection = -1; // Avanza hacia la izquierda
            } else if (myCenter < targetX - 25) {
                currentDirection = 1;  // Retrocede o rodea hacia la derecha
            } else {
                currentDirection = 0;
            }
        } else {
            // Repliegue hacia su portería moviéndose a la derecha o ajustando a la izquierda
            if (myCenter < 680) currentDirection = 1;
            else if (myCenter > 720) currentDirection = -1;
            else currentDirection = 0;
        }
        move(currentDirection);
        if (currentDirection == 0) {
            this.setFacingRight(false);
        }

        // Salto con menor precisión
        if (Math.abs(ballCenter - myCenter) < 65 && ball.getY() < getY()) {
            jump();
        }

        // Patea hacia el arco rival si está detrás del balón
        if (Math.abs(ballCenter - myCenter) < 65) {
            kickTowardsOpponentGoal(ball);
        }
    }

    /**
     * Nivel Normal: Equilibrio de ataque y defensa en toda la cancha,
     * moviéndose hacia izquierda y derecha con reposicionamiento detrás del balón.
     */
    private void calculateMoveNormal(Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        reactionDelayCounter++;
        if (reactionDelayCounter < 2) {
            move(currentDirection);
        } else {
            reactionDelayCounter = 0;

            if (ballCenter > 240) {
                double targetX = ballCenter + 32.0;
                if (targetX > 745) targetX = 745;

                if (myCenter > targetX + 18) {
                    currentDirection = -1;
                } else if (myCenter < targetX - 18) {
                    currentDirection = 1;
                } else {
                    currentDirection = 0;
                }
            } else {
                // Regresa a cubrir su portería
                if (myCenter < 670) currentDirection = 1;
                else if (myCenter > 710) currentDirection = -1;
                else currentDirection = 0;
            }
            move(currentDirection);
            if (currentDirection == 0) {
                this.setFacingRight(false);
            }
        }

        // Si el balón quedó detrás del bot (a su derecha), salta para superarlo sin empujarlo a su arco
        if (ballCenter > myCenter && (ballCenter - myCenter) < 75 && ball.getY() > getY() - 10) {
            jump();
        } else if (Math.abs(ballCenter - myCenter) < 90 && ball.getY() < getY() + 10) {
            jump();
        }

        // Patea en rango estándar hacia el arco izquierdo
        if (Math.abs(ballCenter - myCenter) < 85) {
            kickTowardsOpponentGoal(ball);
        }

        // Uso táctico del poder especial asignado
        if (getSpecialMeter() >= 100 && getSpecialAbility() != null && getSpecialAbility().isReady()) {
            boolean peligroso = ball.getVelocityX() > 3.5 && ballCenter > 560;
            boolean enRangoTiro = Math.abs(ballCenter - myCenter) < 120 && myCenter >= ballCenter;
            if (peligroso || enRangoTiro) {
                this.setFacingRight(false);
                useSpecial(ball);
                kickTowardsOpponentGoal(ball);
            }
        }
    }

    /**
     * Nivel Difícil: Reacción instantánea en ambas direcciones, predicción de trayectoria
     * balística del balón, presión ofensiva y repliegue defensivo inteligente.
     */
    private void calculateMoveHard(Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        // Predicción balística: proyectar dónde estará el balón en los próximos fotogramas
        double projectedBallX = ballCenter + (ball.getVelocityX() * 8.0);

        // Posicionarse a la derecha del balón proyectado para rematar hacia la izquierda
        double targetX = projectedBallX + 35.0;

        // Si el balón está muy profundo en el arco rival y subiendo, mantener cobertura media
        if (targetX < 150) {
            targetX = 150;
        }
        if (targetX > 745) {
            targetX = 745;
        }

        if (myCenter < targetX - 10) {
            currentDirection = 1;
            move(1);
        } else if (myCenter > targetX + 10) {
            currentDirection = -1;
            move(-1);
        } else {
            currentDirection = 0;
            move(0);
            this.setFacingRight(false);
        }

        // Si el balón pasa a la derecha del bot, saltar para rodearlo por el aire sin autogol
        if (ballCenter > myCenter && (ballCenter - myCenter) < 80) {
            jump();
        } else if (Math.abs(projectedBallX - myCenter) < 110 && ball.getY() < getY() + 25) {
            jump();
        }

        // Patea con máximo timing cuando está detrás del balón
        if (Math.abs(ballCenter - myCenter) < 95) {
            kickTowardsOpponentGoal(ball);
        }

        // Uso inteligente del poder especial equipado (ataque o emergencia)
        if (getSpecialMeter() >= 100 && getSpecialAbility() != null && getSpecialAbility().isReady()) {
            boolean peligro = ball.getVelocityX() > 2.0 && ballCenter > 500;
            boolean contragolpe = ballCenter > 320 && ballCenter < 620 && ball.getY() < Ball.GROUND_Y - 40;
            if (peligro || contragolpe) {
                this.setFacingRight(false);
                useSpecial(ball);
                kickTowardsOpponentGoal(ball);
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

    /**
     * Configura la dificultad validando estrictamente el rango permitido.
     * @param difficultyLevel Nivel de dificultad (1=Fácil, 2=Normal, 3=Difícil).
     * @throws HeadSoccerException Si el nivel está fuera del rango válido.
     */
    public void setDifficultyLevelStrict(int difficultyLevel) throws HeadSoccerException {
        if (difficultyLevel < DIFFICULTY_EASY || difficultyLevel > DIFFICULTY_HARD) {
            throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_INVALIDA);
        }
        this.difficultyLevel = difficultyLevel;
    }
}
