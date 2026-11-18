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
        BotAI bot = match.getPlayer2();
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
        BotAI bot = match.getPlayer2();
        bot.setDifficultyLevel(99);
        assertEquals(BotAI.DIFFICULTY_NORMAL, bot.getDifficultyLevel());
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
}
