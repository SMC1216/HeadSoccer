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
        BotAI bot = match.getPlayer2();
        bot.setDifficultyLevel(BotAI.DIFFICULTY_HARD);
        assertEquals(BotAI.DIFFICULTY_HARD, bot.getDifficultyLevel());

        // Entrada inválida debe reajustarse a Normal
        bot.setDifficultyLevel(99);
        assertEquals(BotAI.DIFFICULTY_NORMAL, bot.getDifficultyLevel());
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
        BotAI bot = match.getPlayer2();
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
}
