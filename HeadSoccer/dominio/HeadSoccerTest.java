package dominio;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Suite de pruebas unitarias exhaustiva (40+ pruebas) para la lógica de dominio de Head Soccer.
 * Evalúa inicialización, personajes, físicas del balón, rebotes, porterías, colisiones,
 * goles reglamentarios, habilidades, cooldowns, IA multidificultad y persistencia.
 */
public class HeadSoccerTest {

    private MatchManager match;

    @Before
    public void setUp() {
        match = new MatchManager();
    }

    // ==========================================================
    // 1. INICIALIZACIÓN Y ESTADOS DEL PARTIDO (5 pruebas)
    // ==========================================================

    @Test
    public void deberiaIniciarMarcadorEnCero() {
        assertEquals(0, match.getScorePlayer1());
        assertEquals(0, match.getScorePlayer2());
    }

    @Test
    public void deberiaIniciarTiempoEnNoventaSegundos() {
        assertEquals(90, match.getTimeRemaining());
    }

    @Test
    public void deberiaIniciarEstadoEnJugando() {
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState());
    }

    @Test
    public void deberiaPausarYReanudarPartido() {
        match.pause();
        assertEquals(MatchManager.STATE_PAUSED, match.getMatchState());
        match.resume();
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState());
        match.togglePause();
        assertEquals(MatchManager.STATE_PAUSED, match.getMatchState());
    }

    @Test
    public void deberiaInstanciarTodasLasEntidadesDelPartido() {
        assertNotNull(match.getPlayer1());
        assertNotNull(match.getPlayer2());
        assertNotNull(match.getBall());
        assertNotNull(match.getLeftGoal());
        assertNotNull(match.getRightGoal());
    }

    // ==========================================================
    // 2. PERSONAJES Y MOVIMIENTO (7 pruebas)
    // ==========================================================

    @Test
    public void deberiaMoverJugadorALaDerecha() {
        Player p1 = match.getPlayer1();
        double xInicial = p1.getX();
        p1.move(1);
        p1.updatePosition();
        assertTrue(p1.getX() > xInicial);
    }

    @Test
    public void deberiaMoverJugadorALaIzquierda() {
        Player p1 = match.getPlayer1();
        p1.setPosition(300, Ball.GROUND_Y - 92);
        double xInicial = p1.getX();
        p1.move(-1);
        p1.updatePosition();
        assertTrue(p1.getX() < xInicial);
    }

    @Test
    public void deberiaDetenerJugadorConComandoStop() {
        Player p1 = match.getPlayer1();
        p1.handleInput("RIGHT", match.getBall());
        p1.updatePosition();
        p1.handleInput("STOP", match.getBall());
        p1.updatePosition();
        double xDetenido = p1.getX();
        p1.updatePosition();
        assertEquals(xDetenido, p1.getX(), 0.001);
    }

    @Test
    public void deberiaCambiarOrientacionSegunDireccion() {
        Player p1 = match.getPlayer1();
        p1.move(1);
        assertTrue(p1.isFacingRight());
        p1.move(-1);
        assertFalse(p1.isFacingRight());
    }

    @Test
    public void deberiaEjecutarSaltoDisminuyendoCoordenadaY() {
        Player p1 = match.getPlayer1();
        double ySuelo = p1.getY();
        p1.jump();
        p1.updatePosition();
        assertTrue(p1.getY() < ySuelo);
    }

    @Test
    public void deberiaRespetarParedIzquierda() {
        Player p1 = match.getPlayer1();
        p1.setPosition(Ball.LEFT_WALL - 50, Ball.GROUND_Y - 92);
        p1.updatePosition();
        assertTrue(p1.getX() >= Ball.LEFT_WALL + 50);
    }

    @Test
    public void deberiaRespetarParedDerecha() {
        Player p1 = match.getPlayer1();
        p1.setPosition(Ball.RIGHT_WALL + 50, Ball.GROUND_Y - 92);
        p1.updatePosition();
        assertTrue(p1.getX() + p1.getWidth() <= Ball.RIGHT_WALL);
    }

    // ==========================================================
    // 3. BALÓN Y FÍSICAS (7 pruebas)
    // ==========================================================

    @Test
    public void deberiaAplicarGravedadAlBalonEnElAire() {
        Ball b = match.getBall();
        b.setPosition(400, 100);
        b.setVelocityY(0.0);
        b.updatePosition();
        assertTrue(b.getVelocityY() > 0);
        assertTrue(b.getY() > 100);
    }

    @Test
    public void deberiaRebotarBalonEnElSuelo() {
        Ball b = match.getBall();
        b.setPosition(400, Ball.GROUND_Y - b.getHeight() - 1);
        b.setVelocityY(10.0);
        b.updatePosition();
        assertTrue(b.getVelocityY() < 0);
    }

    @Test
    public void deberiaDetenerReboteEnSueloSiVelocidadEsMinima() {
        Ball b = match.getBall();
        b.setPosition(400, Ball.GROUND_Y - b.getHeight());
        b.setVelocityY(0.5); // Muy pequeña velocidad vertical
        b.updatePosition();
        assertEquals(0.0, b.getVelocityY(), 0.001);
    }

    @Test
    public void deberiaRebotarBalonEnParedIzquierda() {
        Ball b = match.getBall();
        b.setPosition(Ball.LEFT_WALL - 5, 200);
        b.setVelocityX(-8.0);
        b.updatePosition();
        assertTrue(b.getVelocityX() > 0);
    }

    @Test
    public void deberiaRebotarBalonEnParedDerecha() {
        Ball b = match.getBall();
        b.setPosition(Ball.RIGHT_WALL + 5, 200);
        b.setVelocityX(8.0);
        b.updatePosition();
        assertTrue(b.getVelocityX() < 0);
    }

    @Test
    public void deberiaRebotarBalonEnElTecho() {
        Ball b = match.getBall();
        b.setPosition(400, Ball.CEILING_Y - 5);
        b.setVelocityY(-9.0);
        b.updatePosition();
        assertTrue(b.getVelocityY() > 0);
    }

    @Test
    public void deberiaLimitarVelocidadMaximaDelBalon() {
        Ball b = match.getBall();
        b.addForce(100.0, -100.0);
        assertTrue(b.getVelocityX() <= 22.0);
        assertTrue(b.getVelocityY() >= -22.0);
    }

    // ==========================================================
    // 4. INTERACCIÓN Y PATADA (5 pruebas)
    // ==========================================================

    @Test
    public void deberiaPatearBalonHaciaLaDerecha() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX() + p1.getWidth() + 5, p1.getY() + 20);
        p1.setFacingRight(true);
        p1.kick(b);
        assertTrue(b.getVelocityX() > 0);
    }

    @Test
    public void deberiaPatearBalonHaciaLaIzquierda() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX() - 25, p1.getY() + 20);
        p1.setFacingRight(false);
        p1.kick(b);
        assertTrue(b.getVelocityX() < 0);
    }

    @Test
    public void deberiaIgnorarPatadaSiBalonEstaFueraDeRango() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX() + 300, p1.getY()); // Muy lejos
        b.setVelocityX(0);
        p1.kick(b);
        assertEquals(0.0, b.getVelocityX(), 0.001);
    }

    @Test
    public void deberiaModificarVelocidadAlColisionarPersonajeYBalon() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX(), p1.getY());
        b.onCollision(p1);
        assertNotEquals(0.0, b.getVelocityY(), 0.001);
    }

    @Test
    public void deberiaIncrementarEnergiaAlColisionarConBalon() {
        Player p1 = match.getPlayer1();
        p1.setSpecialMeter(20);
        p1.onCollision(match.getBall());
        assertTrue(p1.getSpecialMeter() > 20);
    }

    // ==========================================================
    // 5. PORTERÍAS, TRAVESAÑOS Y GOLES (5 pruebas)
    // ==========================================================

    @Test
    public void deberiaAnotarGolEnArcoDerecho() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();
        b.setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(1, match.getScorePlayer1());
    }

    @Test
    public void deberiaAnotarGolEnArcoIzquierdo() {
        Ball b = match.getBall();
        Goal leftGoal = match.getLeftGoal();
        b.setPosition(leftGoal.getX() + 5, leftGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(1, match.getScorePlayer2());
    }

    @Test
    public void deberiaReiniciarPosicionesDespuesDeUnGol() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();
        b.setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(430.0, b.getX(), 1.0);
        assertEquals(180.0, b.getY(), 1.0);
    }

    @Test
    public void deberiaRebotarEnTravesanoSuperiorSinMarcarGol() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();
        b.setPosition(rightGoal.getX() + 10, rightGoal.getY() - b.getHeight() + 2);
        b.setVelocityY(6.0);
        rightGoal.onCollision(b);
        assertTrue(b.getVelocityY() < 0);
        assertFalse(rightGoal.checkGoal(b));
    }

    @Test
    public void deberiaIgnorarGolSiBalonPasaPorEncimaDelTravesano() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();
        b.setPosition(rightGoal.getX() + 10, rightGoal.getY() - 30); // Por encima del travesaño
        assertFalse(rightGoal.checkGoal(b));
    }

    // ==========================================================
    // 6. HABILIDADES ESPECIALES Y COOLDOWNS (6 pruebas)
    // ==========================================================

    @Test
    public void deberiaAcumularPoderHastaMaximoCien() {
        Player p1 = match.getPlayer1();
        p1.setSpecialMeter(50);
        p1.gainSpecial(30);
        assertEquals(80, p1.getSpecialMeter());
        p1.gainSpecial(50);
        assertEquals(100, p1.getSpecialMeter());
    }

    @Test
    public void deberiaConsumirPoderAlUsarHabilidad() {
        Player p1 = match.getPlayer1();
        p1.setSpecialMeter(100);
        p1.useSpecial(match.getBall());
        assertEquals(0, p1.getSpecialMeter());
    }

    @Test
    public void deberiaActivarSuperTiroConFireStrike() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        p1.setSpecialMeter(100);
        p1.useSpecial(b);
        assertTrue(b.isSuperShot());
        assertTrue(Math.abs(b.getVelocityX()) >= 15.0);
    }

    @Test
    public void deberiaTeletransportarPersonajeCercaDelBalonConTeleport() {
        BotAI bot = match.getBotPlayer2();
        Ball b = match.getBall();
        b.setPosition(350, 200);
        bot.setSpecialMeter(100);
        bot.useSpecial(b);
        assertEquals(0, bot.getSpecialMeter());
        assertTrue(Math.abs(bot.getX() - b.getX()) <= 160);
    }

    @Test
    public void deberiaImpulsarBalonHaciaArribaConGiantKick() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        GiantKick giantKick = new GiantKick();
        giantKick.execute(p1, b);
        assertTrue(b.isSuperShot());
        assertTrue(Math.abs(b.getVelocityX()) >= 20.0);
        assertTrue(b.getVelocityY() < 0);
    }

    @Test
    public void deberiaBloquearUsoDeHabilidadDuranteCooldown() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        p1.setSpecialMeter(100);
        p1.useSpecial(b);

        // Intento de reuso con medidor recargado pero cooldown activo
        p1.setSpecialMeter(100);
        boolean reuso = p1.useSpecial(b);
        assertFalse(reuso);

        // Cumplir cooldown
        Ability ab = p1.getSpecialAbility();
        for (int i = 0; i < ab.getCooldown(); i++) {
            ab.updateTick();
        }
        assertTrue(ab.isReady());
        assertTrue(p1.useSpecial(b));
    }

    // ==========================================================
    // 7. INTELIGENCIA ARTIFICIAL (BotAI) (3 pruebas)
    // ==========================================================

    @Test
    public void deberiaConfigurarDificultadFacil() {
        BotAI bot = new BotAI(660, Ball.GROUND_Y - 92, BotAI.DIFFICULTY_EASY);
        assertEquals(BotAI.DIFFICULTY_EASY, bot.getDifficultyLevel());
        Ball b = new Ball(680, Ball.GROUND_Y - 50, 36);
        for (int i = 0; i < 7; i++) {
            bot.calculateMove(b);
        }
        assertNotNull(bot);
    }

    @Test
    public void deberiaConfigurarDificultadDificil() {
        BotAI bot = new BotAI(660, Ball.GROUND_Y - 92, BotAI.DIFFICULTY_HARD);
        assertEquals(BotAI.DIFFICULTY_HARD, bot.getDifficultyLevel());
        Ball b = new Ball(550, Ball.GROUND_Y - 50, 36);
        b.setVelocityX(-4.0);
        bot.calculateMove(b);
        assertNotNull(bot);
    }

    @Test
    public void deberiaRestablecerADificultadNormalSiValorEsInvalido() {
        BotAI bot = match.getBotPlayer2();
        bot.setDifficultyLevel(99);
        assertEquals(BotAI.DIFFICULTY_NORMAL, bot.getDifficultyLevel());
    }

    @Test
    public void deberiaMoverBotEnAmbasDireccionesSegunPosicionDelBalon() {
        BotAI bot = new BotAI(500, Ball.GROUND_Y - 92, BotAI.DIFFICULTY_HARD);

        // Balón a la izquierda: el bot debe moverse a la izquierda y mirar a la izquierda
        Ball balonIzquierda = new Ball(300, Ball.GROUND_Y - 50, 36);
        bot.calculateMove(balonIzquierda);
        assertTrue(bot.getVelocityX() < 0);
        assertFalse(bot.isFacingRight());

        // Balón a la derecha (detrás del bot): el bot debe moverse a la derecha y mirar a la derecha
        Ball balonDerecha = new Ball(680, Ball.GROUND_Y - 50, 36);
        bot.calculateMove(balonDerecha);
        assertTrue(bot.getVelocityX() > 0);
        assertTrue(bot.isFacingRight());
    }

    // ==========================================================
    // 8. PERSISTENCIA, EXCEPCIONES Y FIN DE JUEGO (4 pruebas)
    // ==========================================================

    @Test
    public void deberiaSalvarYCargarPartidoEnArchivo() throws IOException, HeadSoccerException {
        File tempFile = File.createTempFile("partida_test_40", ".dat");
        tempFile.deleteOnExit();

        match.decrementTimer(); // Tiempo 89
        match.setDifficultyLevel(BotAI.DIFFICULTY_HARD);
        match.getPlayer1().gainSpecial(25);

        match.salvar(tempFile);
        assertTrue(tempFile.exists());

        MatchManager recuperado = MatchManager.abrir(tempFile);
        assertNotNull(recuperado);
        assertEquals(89, recuperado.getTimeRemaining());
        assertEquals(BotAI.DIFFICULTY_HARD, recuperado.getDifficultyLevel());
        assertEquals(match.getPlayer1().getSpecialMeter(), recuperado.getPlayer1().getSpecialMeter());
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionAlCargarArchivoInexistente() throws HeadSoccerException {
        File archivoInexistente = new File("archivo_inexistente_99999.dat");
        MatchManager.abrir(archivoInexistente);
    }

    @Test
    public void deberiaDecrementarRelojSegundoASegundo() {
        int tiempoInicial = match.getTimeRemaining();
        match.decrementTimer();
        assertEquals(tiempoInicial - 1, match.getTimeRemaining());
    }

    @Test
    public void deberiaFinalizarPartidoYReportarGanadorOEmpate() {
        for (int i = 0; i < 90; i++) {
            match.decrementTimer();
        }
        assertEquals(0, match.getTimeRemaining());
        assertEquals(MatchManager.STATE_FINISHED, match.getMatchState());
        assertTrue(match.getWinnerMessage().contains("EMPATE"));
    }

    // ==========================================================
    // 9. MODO DOS JUGADORES, MENÚ, COLISIÓN ENTRE JUGADORES Y EXCEPCIONES
    // ==========================================================

    @Test
    public void deberiaConfigurarModoDosJugadoresCorrectamente() throws HeadSoccerException {
        match.configureMatch(MatchManager.MODE_PVP, BotAI.DIFFICULTY_NORMAL);
        assertTrue(match.isTwoPlayerMode());
        assertEquals(MatchManager.MODE_PVP, match.getGameMode());
        assertNotNull(match.getHumanPlayer2());
        assertNull(match.getBotPlayer2());
        assertEquals("Jugador 2", match.getHumanPlayer2().getPlayerName());
    }

    @Test
    public void deberiaControlarJugador2EnModoDosJugadores() throws HeadSoccerException {
        match.configureMatch(MatchManager.MODE_PVP, BotAI.DIFFICULTY_NORMAL);
        Player p2 = match.getHumanPlayer2();
        double xInicial = p2.getX();

        match.handlePlayer2Input("LEFT");
        p2.updatePosition();
        assertTrue(p2.getX() < xInicial);
        assertFalse(p2.isFacingRight());

        match.handlePlayer2Input("RIGHT");
        p2.updatePosition();
        assertTrue(p2.isFacingRight());

        double ySuelo = p2.getY();
        match.handlePlayer2Input("JUMP");
        p2.updatePosition();
        assertTrue(p2.getY() < ySuelo);
    }

    @Test
    public void deberiaVolverAlMenuPrincipalSinCerrarJuego() {
        match.returnToMenu();
        assertEquals(MatchManager.STATE_MENU, match.getMatchState());

        // En estado MENU el reloj no debe avanzar
        int tiempo = match.getTimeRemaining();
        match.decrementTimer();
        assertEquals(tiempo, match.getTimeRemaining());
    }

    @Test
    public void deberiaSepararJugadoresAlColisionarEntreEllos() {
        Player p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();

        // Superponer ambos jugadores en el centro de la cancha
        p1.setPosition(400, Ball.GROUND_Y - 92);
        p2.setPosition(430, Ball.GROUND_Y - 92);
        assertTrue(p1.getBounds().intersects(p2.getBounds()));

        match.checkCollisions();
        assertFalse(p1.getBounds().intersects(p2.getBounds()));
        assertTrue(p1.getX() < p2.getX());
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionConModoDeJuegoInvalido() throws HeadSoccerException {
        match.configureMatch("MODO_INEXISTENTE", BotAI.DIFFICULTY_NORMAL);
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionConDificultadInvalidaEnConfiguracion() throws HeadSoccerException {
        match.configureMatch(MatchManager.MODE_PVE, 99);
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionAlControlarJugador2EnModoContraBot() throws HeadSoccerException {
        // Por defecto inicia en MODE_PVE contra el Bot
        match.handlePlayer2Input("LEFT");
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionConAccionDeJugadorInvalida() throws HeadSoccerException {
        match.handlePlayer1Input("VOLAR");
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionConNombreDeJugadorVacio() throws HeadSoccerException {
        match.getPlayer1().setPlayerName("   ");
    }

    // ==========================================================
    // 10. SELECCIÓN DE PODERES, EQUIPOS, CAMISETAS, ESTADIOS Y AURA
    // ==========================================================

    @Test
    public void deberiaSeleccionarPoderesEnModoPVEYModoPVP() throws HeadSoccerException {
        // Modo PVE: cambiar poder de Player 1 y del Bot
        match.setPlayer1Ability(Ability.ABILITY_GIANT_KICK);
        match.setPlayer2Ability(Ability.ABILITY_FIRE_STRIKE);
        match.configureMatch(MatchManager.MODE_PVE, BotAI.DIFFICULTY_NORMAL);

        assertTrue(match.getPlayer1().getSpecialAbility() instanceof GiantKick);
        assertTrue(match.getBotPlayer2().getSpecialAbility() instanceof FireStrike);

        // Modo PVP: cambiar poder de Player 1 y Player 2
        match.setPlayer1Ability(Ability.ABILITY_TELEPORT);
        match.setPlayer2Ability(Ability.ABILITY_GIANT_KICK);
        match.configureMatch(MatchManager.MODE_PVP, BotAI.DIFFICULTY_NORMAL);

        assertTrue(match.getPlayer1().getSpecialAbility() instanceof Teleport);
        assertTrue(match.getHumanPlayer2().getSpecialAbility() instanceof GiantKick);
    }

    @Test
    public void deberiaActivarAuraDelColorDelPoderEnTodosLosJugadores() throws HeadSoccerException {
        Player p1 = match.getPlayer1();
        BotAI bot = match.getBotPlayer2();
        Ball b = match.getBall();

        // Player 1 con FireStrike -> Aura Naranja Fuego
        p1.setSpecialAbility(new FireStrike());
        p1.setSpecialMeter(100);
        assertFalse(p1.isAuraActive());
        assertTrue(p1.useSpecial(b));
        assertTrue(p1.isAuraActive());
        assertEquals(FireStrike.AURA_COLOR, p1.getAuraColor());

        // BotAI con Teleport -> Aura Cian
        bot.setSpecialAbility(new Teleport());
        bot.setSpecialMeter(100);
        assertTrue(bot.useSpecial(b));
        assertTrue(bot.isAuraActive());
        assertEquals(Teleport.AURA_COLOR, bot.getAuraColor());

        // Player 2 con GiantKick -> Aura Verde Esmeralda
        match.configureMatch(MatchManager.MODE_PVP, BotAI.DIFFICULTY_NORMAL);
        Player p2 = match.getHumanPlayer2();
        p2.setSpecialAbility(new GiantKick());
        p2.setSpecialMeter(100);
        assertTrue(p2.useSpecial(match.getBall()));
        assertTrue(p2.isAuraActive());
        assertEquals(GiantKick.AURA_COLOR, p2.getAuraColor());

        // Al expirar la duración de la habilidad, el aura se apaga
        int dur = p2.getSpecialAbility().getDuration();
        for (int i = 0; i < dur; i++) {
            p2.updateCooldown();
        }
        assertFalse(p2.isAuraActive());
    }

    @Test
    public void deberiaConfigurarEquiposAlemaniaColombiaUSAYColorCamiseta() throws HeadSoccerException {
        match.setPlayer1Team(Team.TEAM_COLOMBIA);
        assertEquals("Colombia", match.getPlayer1().getTeam().getName());
        assertEquals("COL", match.getPlayer1().getTeam().getCode());
        assertEquals(Team.colombia().getPrimaryShirtColor(), match.getPlayer1().getShirtColor());

        match.setPlayer2Team(Team.TEAM_ALEMANIA);
        assertEquals("Alemania", match.getPlayer2().getTeam().getName());
        assertEquals("GER", match.getPlayer2().getTeam().getCode());

        match.setPlayer1Team(Team.TEAM_USA);
        assertEquals("USA", match.getPlayer1().getTeam().getName());
        assertEquals("USA", match.getPlayer1().getTeam().getCode());

        // Editar el color de la camiseta libremente
        java.awt.Color customMagenta = new java.awt.Color(200, 20, 140);
        match.setPlayer1ShirtColor(customMagenta);
        assertEquals(customMagenta, match.getPlayer1().getShirtColor());
    }

    @Test
    public void deberiaSeleccionarDiferentesEstadios() throws HeadSoccerException {
        match.setStadium(Stadium.STADIUM_METROPOLITANO);
        assertEquals(Stadium.STADIUM_METROPOLITANO, match.getStadium().getName());

        match.setStadium(Stadium.STADIUM_ALLIANZ);
        assertEquals(Stadium.STADIUM_ALLIANZ, match.getStadium().getName());

        match.setStadium(Stadium.STADIUM_ROSE_BOWL);
        assertEquals(Stadium.STADIUM_ROSE_BOWL, match.getStadium().getName());
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionConEquipoInexistente() throws HeadSoccerException {
        match.setPlayer1Team("Atlantida FC");
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionConEstadioInexistente() throws HeadSoccerException {
        match.setStadium("Estadio Lunar");
    }

    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionConPoderInexistente() throws HeadSoccerException {
        match.setPlayer1Ability("SuperRayoLaser");
    }

    // ==========================================================
    // 11. ESTRATEGIAS DEL BOT, FÁBRICAS, REGLAS MALEABLES Y ESTADÍSTICAS
    // ==========================================================

    @Test
    public void deberiaConfigurarLasCuatroEstrategiasDelBotMedianteFactory() throws HeadSoccerException {
        String[] estrategias = new String[] {
            BotStrategy.STRATEGY_TORPE,
            BotStrategy.STRATEGY_OFENSIVA,
            BotStrategy.STRATEGY_DEFENSIVA,
            BotStrategy.STRATEGY_EXPERTA
        };

        for (String nombre : estrategias) {
            match.setBotStrategy(nombre);
            assertEquals(nombre, match.getBotStrategyName());
            match.getBotPlayer2().calculateMove(match.getBall());
        }
    }

    @Test
    public void deberiaPermitirTiempoDeJuegoMaleable() throws HeadSoccerException {
        match.setMatchRule(MatchManager.RULE_TIME);
        match.setConfiguredDuration(30);
        match.startGame();

        assertEquals(30, match.getTimeRemaining());
        for (int i = 0; i < 30; i++) {
            match.decrementTimer();
        }
        assertEquals(0, match.getTimeRemaining());
        assertEquals(MatchManager.STATE_FINISHED, match.getMatchState());
    }

    @Test
    public void deberiaFinalizarPartidoAlAlcanzarLimiteDeGoles() throws HeadSoccerException {
        match.setMatchRule(MatchManager.RULE_GOALS);
        match.setGoalLimit(2);
        match.startGame();

        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();

        // Gol 1
        b.setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(1, match.getScorePlayer1());
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState());

        // Gol 2 -> Alcanza el límite de 2 goles y termina
        b.setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(2, match.getScorePlayer1());
        assertEquals(MatchManager.STATE_FINISHED, match.getMatchState());
    }

    @Test
    public void deberiaMejorarYReiniciarEstadisticasAplicadasATodosLosPersonajes() throws HeadSoccerException {
        PlayerStats stats = match.getPlayer1Stats();
        assertEquals(1, stats.getSpeed());
        assertEquals(1, stats.getJump());
        assertEquals(1, stats.getKick());
        assertEquals(1, stats.getPower());

        double velBase = stats.getEffectiveSpeed();
        match.upgradePlayer1Stat(PlayerStats.STAT_SPEED);
        match.upgradePlayer1Stat(PlayerStats.STAT_JUMP);

        assertEquals(2, stats.getSpeed());
        assertEquals(2, stats.getJump());
        assertTrue(stats.getEffectiveSpeed() > velBase);

        // Cambiar de personaje/equipo y reconfigurar partido: las estadísticas se mantienen
        match.setPlayer1Team(Team.TEAM_USA);
        match.configureMatch(MatchManager.MODE_PVE, BotAI.LEVEL_EXPERTA);
        assertEquals(2, match.getPlayer1().getStats().getSpeed());
        assertEquals(2, match.getPlayer1().getStats().getJump());

        // Reiniciar puntos devuelve todas las estadísticas a 1
        int totalEarned = stats.getTotalEarnedPoints();
        match.resetPlayer1Stats();
        assertEquals(1, stats.getSpeed());
        assertEquals(1, stats.getJump());
        assertEquals(totalEarned, stats.getAvailablePoints());
    }
}
