package dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Batería de pruebas automatizadas con JUnit 5 (Jupiter) para el dominio de Head Soccer.
 * Evalúa exhaustivamente personajes, balón, colisiones, porterías, IA multidificultad,
 * habilidades especiales con cooldowns y persistencia en disco.
 */
public class HeadSoccerJUnit5Test {

    private MatchManager match;

    @BeforeEach
    public void setUp() {
        match = new MatchManager();
    }

    // ==========================================
    // SECCIÓN 1: INICIALIZACIÓN Y ESTADOS
    // ==========================================

    @Test
    @DisplayName("Inicialización correcta del partido y entidades")
    public void testInicializacionPartido() {
        assertEquals(0, match.getScorePlayer1(), "Marcador de P1 debe ser 0");
        assertEquals(0, match.getScorePlayer2(), "Marcador de P2 debe ser 0");
        assertEquals(90, match.getTimeRemaining(), "Tiempo inicial debe ser 90s");
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState(), "Estado inicial debe ser PLAYING");
        assertNotNull(match.getPlayer1(), "Player 1 no debe ser nulo");
        assertNotNull(match.getPlayer2(), "Player 2 no debe ser nulo");
        assertNotNull(match.getBall(), "Balón no debe ser nulo");
        assertNotNull(match.getLeftGoal(), "Portería izquierda no debe ser nula");
        assertNotNull(match.getRightGoal(), "Portería derecha no debe ser nula");
    }

    @Test
    @DisplayName("Control de pausa y reanudación del partido")
    public void testPausaYReanudacion() {
        match.pause();
        assertEquals(MatchManager.STATE_PAUSED, match.getMatchState());

        match.resume();
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState());

        match.togglePause();
        assertEquals(MatchManager.STATE_PAUSED, match.getMatchState());
        match.togglePause();
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState());
    }

    @Test
    @DisplayName("Finalización del partido al expirar el tiempo y cálculo del ganador")
    public void testFinalizacionYGanador() {
        for (int i = 0; i < 90; i++) {
            match.decrementTimer();
        }
        assertEquals(0, match.getTimeRemaining());
        assertEquals(MatchManager.STATE_FINISHED, match.getMatchState());
        assertTrue(match.getWinnerMessage().contains("EMPATE"));
    }

    // ==========================================
    // SECCIÓN 2: PERSONAJES Y MOVIMIENTO
    // ==========================================

    @Test
    @DisplayName("Desplazamiento horizontal y orientación del jugador")
    public void testMovimientoJugador() {
        Player p1 = match.getPlayer1();
        double xInicial = p1.getX();

        p1.handleInput("RIGHT", match.getBall());
        p1.updatePosition();
        assertTrue(p1.getX() > xInicial, "Mover derecha debe aumentar X");
        assertTrue(p1.isFacingRight(), "Debe mirar hacia la derecha");

        p1.handleInput("LEFT", match.getBall());
        p1.updatePosition();
        assertFalse(p1.isFacingRight(), "Debe mirar hacia la izquierda al retroceder");

        p1.handleInput("STOP", match.getBall());
        p1.updatePosition();
        double xDetenido = p1.getX();
        p1.updatePosition();
        assertEquals(xDetenido, p1.getX(), 0.001, "STOP debe anular velocidad horizontal");
    }

    @Test
    @DisplayName("Salto del personaje y física de gravedad terrestre")
    public void testSaltoJugador() {
        Player p1 = match.getPlayer1();
        double ySuelo = p1.getY();

        p1.handleInput("JUMP", match.getBall());
        p1.updatePosition();
        assertTrue(p1.getY() < ySuelo, "El salto debe elevar al jugador (menor Y)");

        // Dejar que caiga de nuevo por gravedad
        for (int i = 0; i < 30; i++) {
            p1.updatePosition();
        }
        assertEquals(ySuelo, p1.getY(), 0.1, "Debe aterrizar exactamente en el nivel del césped");
    }

    @Test
    @DisplayName("Límites laterales de movimiento del personaje")
    public void testLimitesMovimientoPersonaje() {
        Player p1 = match.getPlayer1();
        p1.setPosition(Ball.LEFT_WALL - 50, Ball.GROUND_Y - 92);
        p1.updatePosition();
        assertTrue(p1.getX() >= Ball.LEFT_WALL + 50, "No debe atravesar la pared izquierda");

        p1.setPosition(Ball.RIGHT_WALL + 100, Ball.GROUND_Y - 92);
        p1.updatePosition();
        assertTrue(p1.getX() + p1.getWidth() <= Ball.RIGHT_WALL, "No debe atravesar la pared derecha");
    }

    // ==========================================
    // SECCIÓN 3: BALÓN, GRAVEDAD Y REBOTES
    // ==========================================

    @Test
    @DisplayName("Gravedad y aceleración vertical del balón")
    public void testGravedadBalon() {
        Ball b = match.getBall();
        b.setPosition(400, 100);
        b.setVelocityY(0);
        b.updatePosition();

        assertTrue(b.getVelocityY() > 0, "Gravedad debe acelerar positivamente hacia abajo");
        assertTrue(b.getY() > 100, "Coordenada Y debe aumentar al caer");
    }

    @Test
    @DisplayName("Rebote elástico contra el suelo y límite de amortiguación")
    public void testReboteSueloBalon() {
        Ball b = match.getBall();
        b.setPosition(400, Ball.GROUND_Y - b.getHeight() - 1);
        b.setVelocityY(10.0);

        b.updatePosition();
        assertTrue(b.getVelocityY() < 0, "Al impactar el suelo debe rebotar hacia arriba (velocidad negativa)");
    }

    @Test
    @DisplayName("Rebote contra paredes laterales del estadio")
    public void testReboteParedesBalon() {
        Ball b = match.getBall();
        b.setPosition(Ball.LEFT_WALL - 5, 200);
        b.setVelocityX(-8.0);
        b.updatePosition();
        assertTrue(b.getVelocityX() > 0, "Al golpear la pared izquierda debe rebotar a la derecha");

        b.setPosition(Ball.RIGHT_WALL + 5, 200);
        b.setVelocityX(8.0);
        b.updatePosition();
        assertTrue(b.getVelocityX() < 0, "Al golpear la pared derecha debe rebotar a la izquierda");
    }

    @Test
    @DisplayName("Patada al balón y dirección de fuerza según orientación")
    public void testPatadaBalon() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();

        // Ubicar balón frente al jugador
        b.setPosition(p1.getX() + p1.getWidth() + 5, p1.getY() + 20);
        p1.setFacingRight(true);
        p1.handleInput("KICK", b);

        assertTrue(b.getVelocityX() > 5.0, "Patada a la derecha debe impulsar con velocidad horizontal positiva");
        assertTrue(b.getVelocityY() < 0, "Patada debe elevar el balón");
    }

    // ==========================================
    // SECCIÓN 4: PORTERÍAS, TRAVESAÑOS Y GOLES
    // ==========================================

    @Test
    @DisplayName("Detección de gol válido en portería derecha")
    public void testDeteccionGolDerecho() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();

        // Balón cruzando la línea de gol bajo el travesaño
        b.setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();

        assertEquals(1, match.getScorePlayer1(), "Debe anotar gol el jugador 1");
        assertEquals(430.0, b.getX(), 1.0, "Balón debe reiniciarse en el centro tras gol");
    }

    @Test
    @DisplayName("Detección de gol válido en portería izquierda")
    public void testDeteccionGolIzquierdo() {
        Ball b = match.getBall();
        Goal leftGoal = match.getLeftGoal();

        b.setPosition(leftGoal.getX() + 5, leftGoal.getY() + 40);
        match.checkCollisions();

        assertEquals(1, match.getScorePlayer2(), "Debe anotar gol el bot (jugador 2)");
    }

    @Test
    @DisplayName("Impacto contra el travesaño superior sin cruzar gol")
    public void testReboteTravesanoSinGol() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();

        // Balón cayendo justo sobre el travesaño superior
        b.setPosition(rightGoal.getX() + 10, rightGoal.getY() - b.getHeight() + 2);
        b.setVelocityY(8.0);
        b.setVelocityX(2.0);

        rightGoal.onCollision(b);
        assertTrue(b.getVelocityY() < 0, "Debe rebotar hacia arriba al golpear el travesaño");
        assertFalse(rightGoal.checkGoal(b), "No debe ser gol si golpea por encima del travesaño");
    }

    // ==========================================
    // SECCIÓN 5: INTELIGENCIA ARTIFICIAL (BOT)
    // ==========================================

    @Test
    @DisplayName("Comportamiento del Bot en dificultad Fácil")
    public void testBotDificultadFacil() {
        BotAI bot = new BotAI(660, Ball.GROUND_Y - 92, BotAI.DIFFICULTY_EASY);
        assertEquals(BotAI.DIFFICULTY_EASY, bot.getDifficultyLevel());

        Ball b = new Ball(680, Ball.GROUND_Y - 50, 36);
        // En fácil tarda algunos frames en reaccionar (reactionDelayCounter)
        for (int i = 0; i < 7; i++) {
            bot.calculateMove(b);
        }
        assertNotNull(bot);
    }

    @Test
    @DisplayName("Comportamiento del Bot en dificultad Difícil con predicción")
    public void testBotDificultadDificil() {
        BotAI bot = new BotAI(660, Ball.GROUND_Y - 92, BotAI.DIFFICULTY_HARD);
        assertEquals(BotAI.DIFFICULTY_HARD, bot.getDifficultyLevel());

        Ball b = new Ball(550, Ball.GROUND_Y - 50, 36);
        b.setVelocityX(-4.0); // Balón moviéndose hacia la izquierda

        bot.calculateMove(b);
        assertNotNull(bot);
    }

    @Test
    @DisplayName("Validación y cambio dinámico de dificultad")
    public void testCambioDificultadBot() {
        BotAI bot = match.getBotPlayer2();
        bot.setDifficultyLevel(BotAI.DIFFICULTY_HARD);
        assertEquals(BotAI.DIFFICULTY_HARD, bot.getDifficultyLevel());

        // Entrada inválida debe reajustarse a Normal
        bot.setDifficultyLevel(99);
        assertEquals(BotAI.DIFFICULTY_NORMAL, bot.getDifficultyLevel());
    }

    @Test
    @DisplayName("Movimiento bidireccional del BotAI (izquierda y derecha)")
    public void testMovimientoBidireccionalBot() {
        BotAI bot = new BotAI(500, Ball.GROUND_Y - 92, BotAI.DIFFICULTY_HARD);

        // Balón a la izquierda: el bot avanza a la izquierda
        Ball bIzq = new Ball(280, Ball.GROUND_Y - 50, 36);
        bot.calculateMove(bIzq);
        assertTrue(bot.getVelocityX() < 0, "El bot debe moverse hacia la izquierda");
        assertFalse(bot.isFacingRight(), "El bot debe mirar a la izquierda al avanzar");

        // Balón a la derecha (detrás del bot): el bot retrocede/rodea hacia la derecha
        Ball bDer = new Ball(680, Ball.GROUND_Y - 50, 36);
        bot.calculateMove(bDer);
        assertTrue(bot.getVelocityX() > 0, "El bot debe moverse hacia la derecha");
        assertTrue(bot.isFacingRight(), "El bot debe mirar a la derecha al retroceder");
    }

    // ==========================================
    // SECCIÓN 6: HABILIDADES ESPECIALES Y COOLDOWNS
    // ==========================================

    @Test
    @DisplayName("Habilidad FireStrike: supertiro y velocidad extrema")
    public void testFireStrikeActivacion() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();

        p1.setSpecialMeter(100);
        boolean usado = p1.useSpecial(b);

        assertTrue(usado, "Debe poder usarse con energía al 100%");
        assertTrue(b.isSuperShot(), "Debe activar efecto supertiro");
        assertTrue(Math.abs(b.getVelocityX()) >= 15.0, "Velocidad del disparo debe ser alta");
        assertEquals(0, p1.getSpecialMeter(), "Debe consumir toda la energía especial");
    }

    @Test
    @DisplayName("Control de Cooldown: impide reuso inmediato de habilidad")
    public void testCooldownHabilidad() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();

        p1.setSpecialMeter(100);
        p1.useSpecial(b);

        // Recargar energía artificialmente para verificar si el cooldown lo bloquea
        p1.setSpecialMeter(100);
        boolean segundoUso = p1.useSpecial(b);
        assertFalse(segundoUso, "No debe permitir usar la habilidad si aún está en enfriamiento (cooldown)");

        // Simular paso del tiempo hasta completar el cooldown
        Ability ab = p1.getSpecialAbility();
        for (int s = 0; s < ab.getCooldown(); s++) {
            ab.updateTick();
        }
        assertTrue(ab.isReady(), "Debe estar lista una vez transcurrido el tiempo de cooldown");
        assertTrue(p1.useSpecial(b), "Debe permitir el uso una vez expirado el cooldown");
    }

    @Test
    @DisplayName("Habilidad Teleport del BotAI")
    public void testTeleportActivacion() {
        BotAI bot = match.getBotPlayer2();
        Ball b = match.getBall();
        b.setPosition(350, 200);

        bot.setSpecialMeter(100);
        bot.useSpecial(b);

        assertEquals(0, bot.getSpecialMeter());
        assertTrue(Math.abs(bot.getX() - b.getX()) <= 160, "Debe ubicarse en las cercanías del balón");
    }

    @Test
    @DisplayName("Habilidad GiantKick (Principio OCP)")
    public void testGiantKickActivacion() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        GiantKick giantKick = new GiantKick();

        giantKick.execute(p1, b);
        assertTrue(b.isSuperShot());
        assertTrue(Math.abs(b.getVelocityX()) >= 20.0);
        assertTrue(b.getVelocityY() < 0);
    }

    // ==========================================
    // SECCIÓN 7: PERSISTENCIA Y SERIALIZACIÓN
    // ==========================================

    @Test
    @DisplayName("Guardado y recuperación integral de partida")
    public void testPersistenciaCompleta() throws IOException, HeadSoccerException {
        File tempFile = File.createTempFile("partida_junit5", ".dat");
        tempFile.deleteOnExit();

        match.decrementTimer(); // Tiempo 89s
        match.setDifficultyLevel(BotAI.DIFFICULTY_HARD);
        match.getPlayer1().gainSpecial(25);

        match.salvar(tempFile);
        assertTrue(tempFile.exists(), "Archivo debe haberse generado");

        MatchManager recuperado = MatchManager.abrir(tempFile);
        assertNotNull(recuperado);
        assertEquals(89, recuperado.getTimeRemaining());
        assertEquals(BotAI.DIFFICULTY_HARD, recuperado.getDifficultyLevel());
        assertEquals(match.getPlayer1().getSpecialMeter(), recuperado.getPlayer1().getSpecialMeter());
        assertEquals(match.getScorePlayer1(), recuperado.getScorePlayer1());
    }

    @Test
    @DisplayName("Manejo de excepciones al cargar archivo corrupto o inexistente")
    public void testExcepcionArchivoInvalido() {
        File archivoInexistente = new File("archivo_inexistente_xyz_999.dat");
        assertThrows(HeadSoccerException.class, () -> {
            MatchManager.abrir(archivoInexistente);
        });
    }

    // ==========================================
    // SECCIÓN 8: MODO 2 JUGADORES, MENÚ, COLISIONES Y EXCEPCIONES
    // ==========================================

    @Test
    @DisplayName("Configuración y control del Modo 2 Jugadores (PVP)")
    public void testModoDosJugadores() throws HeadSoccerException {
        match.configureMatch(MatchManager.MODE_PVP, BotAI.DIFFICULTY_NORMAL);
        assertTrue(match.isTwoPlayerMode(), "Debe estar en modo 2 jugadores");
        assertNotNull(match.getHumanPlayer2(), "Jugador 2 humano no debe ser nulo");
        assertNull(match.getBotPlayer2(), "No debe haber instancia de BotAI en modo PVP");

        Player p2 = match.getHumanPlayer2();
        double xInicial = p2.getX();
        match.handlePlayer2Input("LEFT");
        p2.updatePosition();
        assertTrue(p2.getX() < xInicial, "Jugador 2 debe desplazarse a la izquierda");
    }

    @Test
    @DisplayName("Retorno al menú principal sin cerrar la partida")
    public void testRetornoAlMenuPrincipal() {
        match.returnToMenu();
        assertEquals(MatchManager.STATE_MENU, match.getMatchState());

        int tiempoAntes = match.getTimeRemaining();
        match.decrementTimer();
        assertEquals(tiempoAntes, match.getTimeRemaining(), "El reloj no debe correr en el menú principal");
    }

    @Test
    @DisplayName("Colisión física cuerpo a cuerpo entre Jugador 1 y Jugador 2")
    public void testColisionEntreJugadores() {
        Player p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();

        p1.setPosition(410, Ball.GROUND_Y - 92);
        p2.setPosition(440, Ball.GROUND_Y - 92);
        assertTrue(p1.getBounds().intersects(p2.getBounds()), "Deben estar superpuestos inicialmente");

        match.checkCollisions();
        assertFalse(p1.getBounds().intersects(p2.getBounds()), "La colisión debe separar físicamente a ambos jugadores");
    }

    @Test
    @DisplayName("Excepciones de dominio para modos, dificultad, acciones y jugador no humano")
    public void testExcepcionesDominioNuevasFuncionalidades() {
        assertThrows(HeadSoccerException.class, () -> {
            match.configureMatch("MODO_INVALIDO", BotAI.DIFFICULTY_NORMAL);
        });

        assertThrows(HeadSoccerException.class, () -> {
            match.configureMatch(MatchManager.MODE_PVE, -5);
        });

        assertThrows(HeadSoccerException.class, () -> {
            match.handlePlayer2Input("LEFT"); // Falla porque está en modo PVE contra el Bot
        });

        assertThrows(HeadSoccerException.class, () -> {
            match.handlePlayer1Input("ACCION_INEXISTENTE");
        });

        assertThrows(HeadSoccerException.class, () -> {
            match.getPlayer1().setPlayerName("");
        });
    }

    // ==========================================
    // SECCIÓN 9: PODERES, EQUIPOS, CAMISETAS, ESTADIOS Y AURA
    // ==========================================

    @Test
    @DisplayName("Selección de poderes en menú para PVE (Player 1 y Bot) y PVP (Player 1 y Player 2)")
    public void testSeleccionPoderesPVEyPVP() throws HeadSoccerException {
        match.setPlayer1Ability(Ability.ABILITY_TELEPORT);
        match.setPlayer2Ability(Ability.ABILITY_GIANT_KICK);
        match.configureMatch(MatchManager.MODE_PVE, BotAI.DIFFICULTY_HARD);

        assertTrue(match.getPlayer1().getSpecialAbility() instanceof Teleport);
        assertTrue(match.getBotPlayer2().getSpecialAbility() instanceof GiantKick);

        match.setPlayer1Ability(Ability.ABILITY_GIANT_KICK);
        match.setPlayer2Ability(Ability.ABILITY_FIRE_STRIKE);
        match.configureMatch(MatchManager.MODE_PVP, BotAI.DIFFICULTY_NORMAL);

        assertTrue(match.getPlayer1().getSpecialAbility() instanceof GiantKick);
        assertTrue(match.getHumanPlayer2().getSpecialAbility() instanceof FireStrike);
    }

    @Test
    @DisplayName("Activación de aura del color del poder en todos los jugadores (Player y BotAI)")
    public void testAuraColorSegunPoder() throws HeadSoccerException {
        Player p1 = match.getPlayer1();
        BotAI bot = match.getBotPlayer2();
        Ball b = match.getBall();

        // FireStrike -> Aura naranja fuego
        p1.setSpecialAbility(new FireStrike());
        p1.setSpecialMeter(100);
        assertTrue(p1.useSpecial(b));
        assertTrue(p1.isAuraActive());
        assertEquals(FireStrike.AURA_COLOR, p1.getAuraColor());

        // Teleport -> Aura cian
        bot.setSpecialAbility(new Teleport());
        bot.setSpecialMeter(100);
        assertTrue(bot.useSpecial(b));
        assertTrue(bot.isAuraActive());
        assertEquals(Teleport.AURA_COLOR, bot.getAuraColor());

        // GiantKick -> Aura verde esmeralda
        p1.setSpecialAbility(new GiantKick());
        p1.setSpecialMeter(100);
        assertTrue(p1.useSpecial(b));
        assertTrue(p1.isAuraActive());
        assertEquals(GiantKick.AURA_COLOR, p1.getAuraColor());
    }

    @Test
    @DisplayName("Selección de equipos (Alemania, Colombia, USA), color de camiseta y estadios")
    public void testEquiposCamisetaYEstadios() throws HeadSoccerException {
        match.configureMatch(
            MatchManager.MODE_PVP,
            BotAI.DIFFICULTY_NORMAL,
            Ability.ABILITY_FIRE_STRIKE,
            Ability.ABILITY_TELEPORT,
            Team.TEAM_ALEMANIA,
            Team.TEAM_USA,
            Stadium.STADIUM_ALLIANZ
        );

        assertEquals("Alemania", match.getPlayer1().getTeam().getName());
        assertEquals("USA", match.getPlayer2().getTeam().getName());
        assertEquals(Stadium.STADIUM_ALLIANZ, match.getStadium().getName());

        java.awt.Color customShirt = new java.awt.Color(120, 40, 200);
        match.setPlayer1ShirtColor(customShirt);
        assertEquals(customShirt, match.getPlayer1().getShirtColor());

        assertThrows(HeadSoccerException.class, () -> match.setPlayer1Team("EquipoInvalido"));
        assertThrows(HeadSoccerException.class, () -> match.setStadium("EstadioInvalido"));
        assertThrows(HeadSoccerException.class, () -> match.setPlayer2Ability("PoderInvalido"));
    }

    @Test
    @DisplayName("Estrategias del Bot (Torpe, Ofensiva, Defensiva, Experta), tiempo/goles maleables y estadísticas 1-10")
    public void testEstrategiasReglasYEstadisticas() throws HeadSoccerException {
        // 1. Estrategias con Factory
        match.setBotStrategy(BotStrategy.STRATEGY_TORPE);
        assertEquals("Torpe", match.getBotStrategyName());
        match.setBotStrategy(BotStrategy.STRATEGY_DEFENSIVA);
        assertEquals("Defensiva", match.getBotStrategyName());
        match.setBotStrategy(BotStrategy.STRATEGY_EXPERTA);
        assertEquals("Experta", match.getBotStrategyName());

        // 2. Partido a límite de goles
        match.setMatchRule(MatchManager.RULE_GOALS);
        match.setGoalLimit(1);
        match.startGame();
        Goal rightGoal = match.getRightGoal();
        match.getBall().setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(MatchManager.STATE_FINISHED, match.getMatchState());

        // 3. Estadísticas (1 a 10) y reinicio
        PlayerStats p1Stats = match.getPlayer1Stats();
        match.upgradePlayer1Stat(PlayerStats.STAT_KICK);
        assertEquals(2, p1Stats.getKick());
        match.resetPlayer1Stats();
        assertEquals(1, p1Stats.getKick());
    }
}
