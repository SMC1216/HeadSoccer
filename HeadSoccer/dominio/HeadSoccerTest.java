package dominio;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Suite de pruebas unitarias con JUnit 4 para la lógica de dominio de Head Soccer.
 * Evalúa inicialización, físicas, colisiones, goles, habilidades y persistencia.
 */
public class HeadSoccerTest {

    private MatchManager match;

    @Before
    public void setUp() {
        match = new MatchManager();
    }

    @Test
    public void testInicializacionPartido() {
        assertEquals("El marcador del jugador 1 debe iniciar en 0", 0, match.getScorePlayer1());
        assertEquals("El marcador del jugador 2 debe iniciar en 0", 0, match.getScorePlayer2());
        assertEquals("El tiempo inicial debe ser 90 segundos", 90, match.getTimeRemaining());
        assertEquals("El estado inicial debe ser PLAYING", "PLAYING", match.getMatchState());
        assertNotNull("El jugador 1 debe estar instanciado", match.getPlayer1());
        assertNotNull("El jugador 2 (Bot) debe estar instanciado", match.getPlayer2());
        assertNotNull("El balón debe estar instanciado", match.getBall());
        assertNotNull("La portería izquierda debe estar instanciada", match.getLeftGoal());
        assertNotNull("La portería derecha debe estar instanciada", match.getRightGoal());
    }

    @Test
    public void testFisicasYGravedadBalon() {
        Ball ball = match.getBall();
        ball.setPosition(400, 100);
        ball.setVelocityY(0.0);
        ball.setVelocityX(0.0);

        ball.updatePosition();

        assertTrue("La gravedad debe acelerar verticalmente hacia abajo el balón", ball.getVelocityY() > 0);
        assertTrue("La posición Y debe haber descendido", ball.getY() > 100);
    }

    @Test
    public void testMovimientoYSaltoJugador() {
        Player player = match.getPlayer1();
        double xInicial = player.getX();

        player.handleInput("RIGHT", match.getBall());
        player.updatePosition();
        assertTrue("Mover hacia la derecha debe incrementar la coordenada X", player.getX() > xInicial);

        player.handleInput("STOP", match.getBall());
        player.handleInput("JUMP", match.getBall());
        player.updatePosition();
        assertTrue("El salto debe desplazar al jugador hacia arriba", player.getY() < Ball.GROUND_Y - player.getHeight());
    }

    @Test
    public void testAcumulacionYConsumoDePoder() {
        Player player = match.getPlayer1();
        player.setSpecialMeter(50);
        player.gainSpecial(30);
        assertEquals("El poder acumulado debe ser 80%", 80, player.getSpecialMeter());

        player.gainSpecial(50);
        assertEquals("El poder no debe sobrepasar el 100%", 100, player.getSpecialMeter());

        player.handleInput("SPECIAL", match.getBall());
        assertEquals("Al usar el poder especial, el medidor debe reiniciarse a 0%", 0, player.getSpecialMeter());
    }

    @Test
    public void testHabilidadFireStrike() {
        Player player = match.getPlayer1();
        Ball ball = match.getBall();

        player.setSpecialMeter(100);
        player.handleInput("SPECIAL", ball);

        assertTrue("FireStrike debe activar superShot en el balón", ball.isSuperShot());
        assertTrue("FireStrike debe conferir alta velocidad horizontal al balón", Math.abs(ball.getVelocityX()) > 10);
    }

    @Test
    public void testHabilidadTeleport() {
        BotAI bot = match.getPlayer2();
        Ball ball = match.getBall();
        ball.setPosition(300, 250);

        bot.setSpecialMeter(100);
        bot.useSpecial(ball);

        assertEquals("El medidor del bot debe reiniciarse tras usar Teleport", 0, bot.getSpecialMeter());
        assertTrue("El bot debe reubicarse cerca de la posición del balón",
            Math.abs(bot.getX() - ball.getX()) <= 150);
    }

    @Test
    public void testHabilidadGiantKick() {
        Player player = match.getPlayer1();
        Ball ball = match.getBall();
        GiantKick giantKick = new GiantKick();

        giantKick.activate(player, ball);

        assertTrue("GiantKick debe activar superShot en el balón", ball.isSuperShot());
        assertTrue("GiantKick debe aplicar velocidad horizontal al balón", Math.abs(ball.getVelocityX()) >= 20.0);
        assertTrue("GiantKick debe enviar el balón hacia arriba con velocidad vertical negativa", ball.getVelocityY() < 0);
    }

    @Test
    public void testDeteccionGolYMarcador() {
        Ball ball = match.getBall();
        Goal rightGoal = match.getRightGoal();

        // Posicionar balón dentro del arco derecho del Bot
        ball.setPosition(rightGoal.getX() + 10, rightGoal.getY() + 30);
        match.checkCollisions();

        assertEquals("Jugador 1 debe tener 1 gol tras ingresar al arco derecho", 1, match.getScorePlayer1());
        assertEquals("El balón debe regresar al centro de la cancha", 430.0, ball.getX(), 1.0);
    }

    @Test
    public void testPausaYReanudacion() {
        assertEquals("PLAYING", match.getMatchState());
        match.togglePause();
        assertEquals("PAUSED", match.getMatchState());
        match.togglePause();
        assertEquals("PLAYING", match.getMatchState());
    }

    @Test
    public void testFinDePartidoYGanador() {
        for (int i = 0; i < 90; i++) {
            match.decrementTimer();
        }
        assertEquals("El tiempo restante debe ser 0", 0, match.getTimeRemaining());
        assertEquals("El estado debe ser FINISHED", "FINISHED", match.getMatchState());
        assertEquals("Debe reportar empate al estar 0-0", "¡EMPATE!", match.getWinnerMessage());
    }

    @Test
    public void testPersistenciaSalvarYCargar() throws IOException, HeadSoccerException {
        File tempFile = File.createTempFile("partida_test", ".dat");
        tempFile.deleteOnExit();

        match.decrementTimer(); // Tiempo pasa a 89
        match.getPlayer1().gainSpecial(20);

        match.salvar(tempFile);
        assertTrue("El archivo de persistencia debe haberse creado", tempFile.exists());

        MatchManager cargado = MatchManager.abrir(tempFile);
        assertNotNull("El objeto cargado no debe ser nulo", cargado);
        assertEquals("El tiempo cargado debe coincidir", 89, cargado.getTimeRemaining());
        assertEquals("El marcador de P1 debe coincidir", match.getScorePlayer1(), cargado.getScorePlayer1());
        assertEquals("El poder especial de P1 debe coincidir",
            match.getPlayer1().getSpecialMeter(), cargado.getPlayer1().getSpecialMeter());
    }

    @Test(expected = HeadSoccerException.class)
    public void testCargarArchivoInexistenteLanzaExcepcion() throws HeadSoccerException {
        File inexistente = new File("archivo_que_no_existe_12345.dat");
        MatchManager.abrir(inexistente);
    }
}
