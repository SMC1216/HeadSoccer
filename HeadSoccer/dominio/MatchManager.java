package dominio;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;

/**
 * Clase coordinadora del dominio que administra el estado del partido,
 * físicas, colisiones, cronómetro o límite de goles, marcador, estrategias del Bot,
 * selección de poderes, equipos, colores de camiseta, estadio y estadísticas (1-10).
 */
public class MatchManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATE_MENU = "MENU";
    public static final String STATE_PLAYING = "PLAYING";
    public static final String STATE_PAUSED = "PAUSED";
    public static final String STATE_FINISHED = "FINISHED";

    public static final String MODE_PVE = "PVE";
    public static final String MODE_PVP = "PVP";

    public static final String RULE_TIME = "TIME";
    public static final String RULE_GOALS = "GOALS";

    private int scorePlayer1;
    private int scorePlayer2;
    private int timeRemaining;
    private int elapsedTime;
    private String matchState;
    private String gameMode;
    private int difficultyLevel;

    // Configuración de tipo de juego (tiempo maleable o límite de goles)
    private String matchRule;
    private int configuredDuration;
    private int goalLimit;

    // Estadísticas persistentes de cada jugador (se aplican a todos sus personajes)
    private PlayerStats player1Stats;
    private PlayerStats player2Stats;

    private Player player1;
    private Character player2;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;
    private Stadium stadium;

    private String selectedP1AbilityName;
    private String selectedP2AbilityName;
    private boolean customP2AbilitySelected;

    /**
     * Constructor por defecto: inicia en modo contra el Bot en estrategia Ofensiva (2) y por tiempo (90s).
     */
    public MatchManager() {
        this(BotAI.LEVEL_OFENSIVA);
    }

    /**
     * Constructor con selección de nivel de la IA (modo PVE).
     * @param difficultyLevel Nivel del Bot (1=Torpe, 2=Ofensiva, 3=Defensiva, 4=Experta).
     */
    public MatchManager(int difficultyLevel) {
        this.gameMode = MODE_PVE;
        this.matchRule = RULE_TIME;
        this.configuredDuration = 90;
        this.goalLimit = 5;
        this.player1Stats = new PlayerStats();
        this.player2Stats = new PlayerStats();

        this.stadium = StadiumFactory.getDefault();
        this.selectedP1AbilityName = Ability.ABILITY_FIRE_STRIKE;
        this.selectedP2AbilityName = Ability.ABILITY_TELEPORT;
        this.customP2AbilitySelected = false;

        this.leftGoal = new Goal("LEFT", 15, Ball.GROUND_Y - 160, 65, 160);
        this.rightGoal = new Goal("RIGHT", 815, Ball.GROUND_Y - 160, 65, 160);
        this.player1 = new Player("Jugador 1", 160, Ball.GROUND_Y - 92, new FireStrike(), true, Team.colombia());
        this.player1.setStats(this.player1Stats);

        BotAI bot = new BotAI(660, Ball.GROUND_Y - 92, difficultyLevel, new Teleport(), Team.alemania());
        bot.setStats(this.player2Stats);
        this.difficultyLevel = bot.getDifficultyLevel();
        this.player2 = bot;
        this.ball = new Ball(430, 180, 36);
        startGame();
    }

    public MatchManager(String gameMode) throws HeadSoccerException {
        this(gameMode, BotAI.LEVEL_OFENSIVA);
    }

    public MatchManager(String gameMode, int difficultyLevel) throws HeadSoccerException {
        this.matchRule = RULE_TIME;
        this.configuredDuration = 90;
        this.goalLimit = 5;
        this.player1Stats = new PlayerStats();
        this.player2Stats = new PlayerStats();

        this.stadium = StadiumFactory.getDefault();
        this.selectedP1AbilityName = Ability.ABILITY_FIRE_STRIKE;
        this.selectedP2AbilityName = MODE_PVP.equals(gameMode) ? Ability.ABILITY_GIANT_KICK : Ability.ABILITY_TELEPORT;
        this.customP2AbilitySelected = false;

        this.leftGoal = new Goal("LEFT", 15, Ball.GROUND_Y - 160, 65, 160);
        this.rightGoal = new Goal("RIGHT", 815, Ball.GROUND_Y - 160, 65, 160);
        this.ball = new Ball(430, 180, 36);
        configureMatch(gameMode, difficultyLevel);
    }

    /**
     * Configura e inicia un nuevo partido en el modo especificado (contra Bot o 2 Jugadores),
     * aplicando las fábricas y conservando poderes, equipos, camisetas, estadio y estadísticas.
     *
     * @param gameMode Modo de juego (MODE_PVE o MODE_PVP).
     * @param difficultyLevel Nivel de estrategia de la IA (1=Torpe, 2=Ofensiva, 3=Defensiva, 4=Experta).
     * @throws HeadSoccerException Si el modo de juego o el nivel son inválidos.
     */
    public void configureMatch(String gameMode, int difficultyLevel) throws HeadSoccerException {
        if (!MODE_PVE.equals(gameMode) && !MODE_PVP.equals(gameMode)) {
            throw new HeadSoccerException(HeadSoccerException.MODO_JUEGO_INVALIDO);
        }
        if (difficultyLevel < BotAI.LEVEL_TORPE || difficultyLevel > BotAI.LEVEL_EXPERTA) {
            throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_INVALIDA);
        }

        if (player1Stats == null) player1Stats = new PlayerStats();
        if (player2Stats == null) player2Stats = new PlayerStats();

        Team p1Team = (player1 != null) ? player1.getTeam() : Team.colombia();
        Color p1Color = (player1 != null) ? player1.getShirtColor() : p1Team.getPrimaryShirtColor();
        String p1Name = (player1 != null) ? player1.getPlayerName() : "Jugador 1";

        Team p2Team = (player2 != null) ? player2.getTeam() : (MODE_PVP.equals(gameMode) ? Team.usa() : Team.alemania());
        Color p2Color = (player2 != null) ? player2.getShirtColor() : p2Team.getPrimaryShirtColor();

        String p1AbName = (selectedP1AbilityName != null) ? selectedP1AbilityName : Ability.ABILITY_FIRE_STRIKE;
        String p2AbName;
        if (customP2AbilitySelected && selectedP2AbilityName != null) {
            p2AbName = selectedP2AbilityName;
        } else {
            p2AbName = MODE_PVP.equals(gameMode) ? Ability.ABILITY_GIANT_KICK : Ability.ABILITY_TELEPORT;
            this.selectedP2AbilityName = p2AbName;
        }

        Ability p1Ability = AbilityFactory.create(p1AbName);
        Ability p2Ability = AbilityFactory.create(p2AbName);

        this.gameMode = gameMode;
        this.difficultyLevel = difficultyLevel;

        this.player1 = new Player(p1Name, 160, Ball.GROUND_Y - 92, p1Ability, true, p1Team);
        this.player1.setShirtColor(p1Color);
        this.player1.setStats(this.player1Stats);

        if (MODE_PVP.equals(gameMode)) {
            this.player2 = new Player("Jugador 2", 660, Ball.GROUND_Y - 92, p2Ability, false, p2Team);
        } else {
            this.player2 = new BotAI(660, Ball.GROUND_Y - 92, difficultyLevel, p2Ability, p2Team);
        }
        this.player2.setShirtColor(p2Color);
        this.player2.setStats(this.player2Stats);

        if (this.stadium == null) {
            this.stadium = StadiumFactory.getDefault();
        }

        startGame();
    }

    public void configureMatch(
        String gameMode,
        int difficultyLevel,
        String p1AbilityName,
        String p2AbilityName,
        String p1TeamName,
        String p2TeamName,
        String stadiumName
    ) throws HeadSoccerException {
        setPlayer1Ability(p1AbilityName);
        setPlayer2Ability(p2AbilityName);
        setPlayer1Team(p1TeamName);
        setPlayer2Team(p2TeamName);
        setStadium(stadiumName);
        configureMatch(gameMode, difficultyLevel);
    }

    public void setPlayer1Ability(String abilityName) throws HeadSoccerException {
        Ability ability = AbilityFactory.create(abilityName);
        this.selectedP1AbilityName = ability.getName();
        if (this.player1 != null) {
            this.player1.setSpecialAbility(ability);
        }
    }

    public void setPlayer2Ability(String abilityName) throws HeadSoccerException {
        Ability ability = AbilityFactory.create(abilityName);
        this.selectedP2AbilityName = ability.getName();
        this.customP2AbilitySelected = true;
        if (this.player2 != null) {
            this.player2.setSpecialAbility(ability);
        }
    }

    public void setPlayer1Team(String teamName) throws HeadSoccerException {
        Team team = TeamFactory.create(teamName);
        if (this.player1 != null) {
            this.player1.setTeam(team);
        }
    }

    public void setPlayer2Team(String teamName) throws HeadSoccerException {
        Team team = TeamFactory.create(teamName);
        if (this.player2 != null) {
            this.player2.setTeam(team);
        }
    }

    public void setPlayer1ShirtColor(Color color) throws HeadSoccerException {
        if (this.player1 == null) {
            throw new HeadSoccerException(HeadSoccerException.PERSONAJE_INVALIDO);
        }
        this.player1.setShirtColor(color);
    }

    public void setPlayer2ShirtColor(Color color) throws HeadSoccerException {
        if (this.player2 == null) {
            throw new HeadSoccerException(HeadSoccerException.PERSONAJE_INVALIDO);
        }
        this.player2.setShirtColor(color);
    }

    public void setStadium(String stadiumName) throws HeadSoccerException {
        this.stadium = StadiumFactory.create(stadiumName);
    }

    public void setStadium(Stadium stadium) throws HeadSoccerException {
        if (stadium == null) {
            throw new HeadSoccerException(HeadSoccerException.ESTADIO_INVALIDO);
        }
        this.stadium = stadium;
    }

    // =========================================================================
    // CONFIGURACIÓN DE TIPO DE PARTIDO (TIEMPO MALEABLE O LÍMITE DE GOLES)
    // =========================================================================

    /**
     * Define la regla de victoria del partido (RULE_TIME o RULE_GOALS).
     *
     * @param matchRule Tipo de partido ("TIME" o "GOALS").
     * @throws HeadSoccerException Si la regla no es válida.
     */
    public void setMatchRule(String matchRule) throws HeadSoccerException {
        if (!RULE_TIME.equalsIgnoreCase(matchRule) && !RULE_GOALS.equalsIgnoreCase(matchRule)) {
            throw new HeadSoccerException(HeadSoccerException.REGLA_JUEGO_INVALIDA);
        }
        this.matchRule = matchRule.toUpperCase();
    }

    public String getMatchRule() {
        return matchRule != null ? matchRule : RULE_TIME;
    }

    public boolean isGoalLimitMode() {
        return RULE_GOALS.equalsIgnoreCase(getMatchRule());
    }

    /**
     * Configura la duración en segundos para partidos por tiempo.
     *
     * @param seconds Duración en segundos (mayor a 0).
     * @throws HeadSoccerException Si el tiempo es menor o igual a 0.
     */
    public void setConfiguredDuration(int seconds) throws HeadSoccerException {
        if (seconds <= 0) {
            throw new HeadSoccerException(HeadSoccerException.TIEMPO_INVALIDO);
        }
        this.configuredDuration = seconds;
        this.timeRemaining = seconds;
    }

    public int getConfiguredDuration() {
        return configuredDuration > 0 ? configuredDuration : 90;
    }

    /**
     * Configura la cantidad de goles necesarios para ganar en modo por goles.
     *
     * @param goals Meta de goles (al menos 1).
     * @throws HeadSoccerException Si el límite es menor a 1.
     */
    public void setGoalLimit(int goals) throws HeadSoccerException {
        if (goals < 1) {
            throw new HeadSoccerException(HeadSoccerException.LIMITE_GOLES_INVALIDO);
        }
        this.goalLimit = goals;
    }

    public int getGoalLimit() {
        return goalLimit > 0 ? goalLimit : 5;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    // =========================================================================
    // ESTADÍSTICAS Y MEJORAS DEL JUGADOR (1 A 10 PUNTOS)
    // =========================================================================

    public PlayerStats getPlayer1Stats() {
        if (player1Stats == null) {
            player1Stats = new PlayerStats();
            if (player1 != null) player1.setStats(player1Stats);
        }
        return player1Stats;
    }

    public PlayerStats getPlayer2Stats() {
        if (player2Stats == null) {
            player2Stats = new PlayerStats();
            if (player2 != null) player2.setStats(player2Stats);
        }
        return player2Stats;
    }

    public void upgradePlayer1Stat(String statType) throws HeadSoccerException {
        getPlayer1Stats().upgradeStat(statType);
        if (player1 != null) player1.setStats(player1Stats);
    }

    public void upgradePlayer2Stat(String statType) throws HeadSoccerException {
        getPlayer2Stats().upgradeStat(statType);
        if (player2 != null) player2.setStats(player2Stats);
    }

    public void resetPlayer1Stats() {
        getPlayer1Stats().resetPoints();
        if (player1 != null) player1.setStats(player1Stats);
    }

    public void resetPlayer2Stats() {
        getPlayer2Stats().resetPoints();
        if (player2 != null) player2.setStats(player2Stats);
    }

    private void finishMatch() {
        if (STATE_FINISHED.equals(this.matchState)) {
            return;
        }
        this.matchState = STATE_FINISHED;
        if (scorePlayer1 > scorePlayer2) {
            getPlayer1Stats().addUpgradePoints(3);
            getPlayer2Stats().addUpgradePoints(1);
        } else if (scorePlayer2 > scorePlayer1) {
            getPlayer1Stats().addUpgradePoints(1);
            getPlayer2Stats().addUpgradePoints(3);
        } else {
            getPlayer1Stats().addUpgradePoints(2);
            getPlayer2Stats().addUpgradePoints(2);
        }
    }

    /**
     * Inicia o reinicia el partido a su estado inicial conservando modo, reglas y estadísticas.
     */
    public void startGame() {
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.timeRemaining = getConfiguredDuration();
        this.elapsedTime = 0;
        this.matchState = STATE_PLAYING;
        if (player1 != null) {
            player1.setStats(getPlayer1Stats());
            player1.deactivatePowerAura();
            if (player1.getSpecialAbility() != null) {
                player1.getSpecialAbility().reset();
            }
            player1.setSpecialMeter(80);
        }
        if (player2 != null) {
            player2.setStats(getPlayer2Stats());
            player2.deactivatePowerAura();
            if (player2.getSpecialAbility() != null) {
                player2.getSpecialAbility().reset();
            }
            player2.setSpecialMeter(80);
        }
        resetPositions();
    }

    public void returnToMenu() {
        this.matchState = STATE_MENU;
        resetPositions();
    }

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

    public void checkCollisions() {
        if (player1.getBounds().intersects(player2.getBounds())) {
            player1.onCollision(player2);
            player2.onCollision(player1);
        }

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
            if (isGoalLimitMode() && scorePlayer2 >= getGoalLimit()) {
                finishMatch();
            }
        } else if (rightGoal.checkGoal(ball)) {
            scorePlayer1++;
            resetPositions();
            if (isGoalLimitMode() && scorePlayer1 >= getGoalLimit()) {
                finishMatch();
            }
        }
    }

    public void resetPositions() {
        player1.resetState(160, Ball.GROUND_Y - 92, true);
        player2.resetState(660, Ball.GROUND_Y - 92, false);
        ball.resetBall(430, 180);
    }

    public void handlePlayer1Input(String action) throws HeadSoccerException {
        player1.executeAction(action, ball);
    }

    public void handlePlayer2Input(String action) throws HeadSoccerException {
        if (!(player2 instanceof Player)) {
            throw new HeadSoccerException(HeadSoccerException.JUGADOR_NO_HUMANO);
        }
        ((Player) player2).executeAction(action, ball);
    }

    /**
     * Avanza un segundo del partido, recargando energía, actualizando cooldowns/auras
     * y verificando el fin por tiempo cuando la regla activa es RULE_TIME.
     */
    public void decrementTimer() {
        if (!STATE_PLAYING.equals(matchState)) {
            return;
        }

        elapsedTime++;
        player1.gainSpecial(3 + (player1.getStats().getEffectiveSpecialBonus() / 3));
        player2.gainSpecial(3 + (player2.getStats().getEffectiveSpecialBonus() / 3));

        player1.updateCooldown();
        player2.updateCooldown();

        if (!isGoalLimitMode()) {
            if (timeRemaining > 0) {
                timeRemaining--;
                if (timeRemaining == 0) {
                    finishMatch();
                }
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
            if (difficultyLevel < BotAI.LEVEL_TORPE || difficultyLevel > BotAI.LEVEL_EXPERTA) {
                this.difficultyLevel = BotAI.LEVEL_OFENSIVA;
            } else {
                this.difficultyLevel = difficultyLevel;
            }
        }
    }

    public void setBotStrategy(String strategyName) throws HeadSoccerException {
        BotStrategy strategy = BotStrategyFactory.create(strategyName);
        this.difficultyLevel = strategy.getLevelCode();
        if (player2 instanceof BotAI) {
            ((BotAI) player2).setStrategy(strategy);
        }
    }

    public String getBotStrategyName() {
        if (player2 instanceof BotAI) {
            return ((BotAI) player2).getStrategyName();
        }
        try {
            return BotStrategyFactory.createByLevel(difficultyLevel).getName();
        } catch (HeadSoccerException e) {
            return BotStrategy.STRATEGY_OFENSIVA;
        }
    }

    public String getWinnerMessage() {
        if (scorePlayer1 > scorePlayer2) {
            return "¡VICTORIA PARA " + player1.getPlayerName().toUpperCase() + " (" + player1.getTeam().getName().toUpperCase() + ")!";
        } else if (scorePlayer2 > scorePlayer1) {
            if (player2 instanceof Player) {
                return "¡VICTORIA PARA " + ((Player) player2).getPlayerName().toUpperCase() + " (" + player2.getTeam().getName().toUpperCase() + ")!";
            }
            return "¡VICTORIA PARA EL BOT AI (" + player2.getTeam().getName().toUpperCase() + ")!";
        } else {
            return "¡EMPATE!";
        }
    }

    public void salvar(File file) throws HeadSoccerException {
        MatchPersistence.guardar(this, file);
    }

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
    public Stadium getStadium() { return stadium != null ? stadium : StadiumFactory.getDefault(); }
    public int getScorePlayer1() { return scorePlayer1; }
    public int getScorePlayer2() { return scorePlayer2; }
    public int getTimeRemaining() { return timeRemaining; }
    public String getMatchState() { return matchState; }
}
