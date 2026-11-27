package dominio;

/**
 * Representa al oponente controlado por el computador (IA).
 * Utiliza el patrón Strategy junto con BotStrategyFactory para soportar los 4 perfiles
 * solicitados: Torpe, Ofensiva, Defensiva y Experta, siendo sencillo de extender.
 */
public class BotAI extends Character {
    private static final long serialVersionUID = 1L;

    public static final int LEVEL_TORPE = 1;
    public static final int LEVEL_OFENSIVA = 2;
    public static final int LEVEL_DEFENSIVA = 3;
    public static final int LEVEL_EXPERTA = 4;

    // Constantes equivalentes para mantener compatibilidad con pruebas previas
    public static final int DIFFICULTY_EASY = LEVEL_TORPE;
    public static final int DIFFICULTY_NORMAL = LEVEL_OFENSIVA;
    public static final int DIFFICULTY_HARD = LEVEL_EXPERTA;

    private BotStrategy strategy;

    public BotAI(double x, double y, int difficultyLevel) {
        this(x, y, difficultyLevel, new Teleport(), Team.alemania());
    }

    public BotAI(double x, double y, int difficultyLevel, Ability specialAbility) {
        this(x, y, difficultyLevel, specialAbility, Team.alemania());
    }

    public BotAI(double x, double y, int difficultyLevel, Ability specialAbility, Team team) {
        super(x, y, 72, 92, specialAbility != null ? specialAbility : new Teleport(), false, team != null ? team : Team.alemania());
        setDifficultyLevel(difficultyLevel);
    }

    public BotAI(double x, double y, String strategyName, Ability specialAbility, Team team) throws HeadSoccerException {
        super(x, y, 72, 92, specialAbility != null ? specialAbility : new Teleport(), false, team != null ? team : Team.alemania());
        setStrategyByName(strategyName);
    }

    /**
     * Evalúa y ejecuta las decisiones tácticas del bot delegando en su estrategia actual.
     *
     * @param ball El balón en juego.
     */
    public void calculateMove(Ball ball) {
        if (ball == null) {
            return;
        }
        if (strategy == null) {
            strategy = new OfensivaStrategy();
        }
        strategy.decideMove(this, ball);
    }

    public BotStrategy getStrategy() {
        if (strategy == null) {
            strategy = new OfensivaStrategy();
        }
        return strategy;
    }

    public String getStrategyName() {
        return getStrategy().getName();
    }

    public void setStrategy(BotStrategy strategy) throws HeadSoccerException {
        if (strategy == null) {
            throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_INVALIDA);
        }
        this.strategy = strategy;
    }

    public void setStrategyByName(String strategyName) throws HeadSoccerException {
        this.strategy = BotStrategyFactory.create(strategyName);
    }

    public int getDifficultyLevel() {
        return getStrategy().getLevelCode();
    }

    public void setDifficultyLevel(int difficultyLevel) {
        try {
            this.strategy = BotStrategyFactory.createByLevel(difficultyLevel);
        } catch (HeadSoccerException e) {
            this.strategy = new OfensivaStrategy();
        }
    }

    public void setDifficultyLevelStrict(int difficultyLevel) throws HeadSoccerException {
        this.strategy = BotStrategyFactory.createByLevel(difficultyLevel);
    }
}
