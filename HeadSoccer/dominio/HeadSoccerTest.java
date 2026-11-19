package dominio;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * pruebas unitarias con junit para comprobar que la logica del juego funciona bien
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class HeadSoccerTest {

    /** instancia del administrador del partido usada en cada prueba */
    private MatchManager match;

    /**
     * inicializa un partido nuevo antes de ejecutar cada prueba unitaria
     */
    @Before
    public void setUp() {
        match = new MatchManager();
    }

    /**
     * comprueba que el partido inicie con marcador cero a cero
     */
    @Test
    public void deberiaIniciarMarcadorEnCero() {
        assertEquals(0, match.getScorePlayer1());
        assertEquals(0, match.getScorePlayer2());
    }

    /**
     * comprueba que el tiempo reglamentario inicial sea noventa segundos
     */
    @Test
    public void deberiaIniciarTiempoEnNoventaSegundos() {
        assertEquals(90, match.getTimeRemaining());
    }

    /**
     * comprueba que el estado inicial del partido sea jugando
     */
    @Test
    public void deberiaIniciarEstadoEnJugando() {
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState());
    }

    /**
     * prueba pausar, reanudar y alternar el estado de pausa del partido
     */
    @Test
    public void deberiaPausarYReanudarPartido() {
        match.pause();
        assertEquals(MatchManager.STATE_PAUSED, match.getMatchState());
        match.resume();
        assertEquals(MatchManager.STATE_PLAYING, match.getMatchState());
        match.togglePause();
        assertEquals(MatchManager.STATE_PAUSED, match.getMatchState());
    }

    /**
     * comprueba que se creen los jugadores, el balon y las porterias al inicio
     */
    @Test
    public void deberiaInstanciarTodasLasEntidadesDelPartido() {
        assertNotNull(match.getPlayer1());
        assertNotNull(match.getPlayer2());
        assertNotNull(match.getBall());
        assertNotNull(match.getLeftGoal());
        assertNotNull(match.getRightGoal());
    }

    /**
     * comprueba el avance del jugador hacia la derecha al recibir movimiento
     */
    @Test
    public void deberiaMoverJugadorALaDerecha() {
        Player p1 = match.getPlayer1();
        double xInicial = p1.getX();
        p1.move(1);
        p1.updatePosition();
        assertTrue(p1.getX() > xInicial);
    }

    /**
     * comprueba el retroceso del jugador hacia la izquierda al recibir movimiento
     */
    @Test
    public void deberiaMoverJugadorALaIzquierda() {
        Player p1 = match.getPlayer1();
        p1.setPosition(300, Ball.GROUND_Y - 92);
        double xInicial = p1.getX();
        p1.move(-1);
        p1.updatePosition();
        assertTrue(p1.getX() < xInicial);
    }

    /**
     * comprueba que la velocidad horizontal se anule al soltar la tecla
     */
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

    /**
     * comprueba que el personaje gire visualmente segun la direccion donde corre
     */
    @Test
    public void deberiaCambiarOrientacionSegunDireccion() {
        Player p1 = match.getPlayer1();
        p1.move(1);
        assertTrue(p1.isFacingRight());
        p1.move(-1);
        assertFalse(p1.isFacingRight());
    }

    /**
     * comprueba que el salto aplique impulso hacia arriba restando valor en y
     */
    @Test
    public void deberiaEjecutarSaltoDisminuyendoCoordenadaY() {
        Player p1 = match.getPlayer1();
        double ySuelo = p1.getY();
        p1.jump();
        p1.updatePosition();
        assertTrue(p1.getY() < ySuelo);
    }

    /**
     * comprueba que el jugador no pueda salirse por la pared izquierda
     */
    @Test
    public void deberiaRespetarParedIzquierda() {
        Player p1 = match.getPlayer1();
        p1.setPosition(Ball.LEFT_WALL - 50, Ball.GROUND_Y - 92);
        p1.updatePosition();
        assertTrue(p1.getX() >= Ball.LEFT_WALL + 50);
    }

    /**
     * comprueba que el jugador no pueda salirse por la pared derecha
     */
    @Test
    public void deberiaRespetarParedDerecha() {
        Player p1 = match.getPlayer1();
        p1.setPosition(Ball.RIGHT_WALL + 50, Ball.GROUND_Y - 92);
        p1.updatePosition();
        assertTrue(p1.getX() + p1.getWidth() <= Ball.RIGHT_WALL);
    }

    /**
     * comprueba que la gravedad jale el balon hacia abajo cuando esta flotando
     */
    @Test
    public void deberiaAplicarGravedadAlBalonEnElAire() {
        Ball b = match.getBall();
        b.setPosition(400, 100);
        b.setVelocityY(0.0);
        b.updatePosition();
        assertTrue(b.getVelocityY() > 0);
        assertTrue(b.getY() > 100);
    }

    /**
     * comprueba que el balon invierta su velocidad vertical al pegar en el piso
     */
    @Test
    public void deberiaRebotarBalonEnElSuelo() {
        Ball b = match.getBall();
        b.setPosition(400, Ball.GROUND_Y - b.getHeight() - 1);
        b.setVelocityY(10.0);
        b.updatePosition();
        assertTrue(b.getVelocityY() < 0);
    }

    /**
     * comprueba que cuando la velocidad vertical sea casi nula el balon quede quieto en el suelo
     */
    @Test
    public void deberiaDetenerReboteEnSueloSiVelocidadEsMinima() {
        Ball b = match.getBall();
        b.setPosition(400, Ball.GROUND_Y - b.getHeight());
        b.setVelocityY(0.5);
        b.updatePosition();
        assertEquals(0.0, b.getVelocityY(), 0.001);
    }

    /**
     * comprueba que el balon rebote e invierta su sentido al chocar la pared izquierda
     */
    @Test
    public void deberiaRebotarBalonEnParedIzquierda() {
        Ball b = match.getBall();
        b.setPosition(Ball.LEFT_WALL - 5, 200);
        b.setVelocityX(-8.0);
        b.updatePosition();
        assertTrue(b.getVelocityX() > 0);
    }

    /**
     * comprueba que el balon rebote e invierta su sentido al chocar la pared derecha
     */
    @Test
    public void deberiaRebotarBalonEnParedDerecha() {
        Ball b = match.getBall();
        b.setPosition(Ball.RIGHT_WALL + 5, 200);
        b.setVelocityX(8.0);
        b.updatePosition();
        assertTrue(b.getVelocityX() < 0);
    }

    /**
     * comprueba que el balon rebote hacia abajo al tocar el limite superior del campo
     */
    @Test
    public void deberiaRebotarBalonEnElTecho() {
        Ball b = match.getBall();
        b.setPosition(400, Ball.CEILING_Y - 5);
        b.setVelocityY(-9.0);
        b.updatePosition();
        assertTrue(b.getVelocityY() > 0);
    }

    /**
     * comprueba que no se sobrepase la velocidad maxima configurada del balon
     */
    @Test
    public void deberiaLimitarVelocidadMaximaDelBalon() {
        Ball b = match.getBall();
        b.addForce(100.0, -100.0);
        assertTrue(b.getVelocityX() <= 22.0);
        assertTrue(b.getVelocityY() >= -22.0);
    }

    /**
     * comprueba que una patada mirando a la derecha impulse el balon hacia adelante
     */
    @Test
    public void deberiaPatearBalonHaciaLaDerecha() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX() + p1.getWidth() + 5, p1.getY() + 20);
        p1.setFacingRight(true);
        p1.kick(b);
        assertTrue(b.getVelocityX() > 0);
    }

    /**
     * comprueba que una patada mirando a la izquierda impulse el balon hacia atras
     */
    @Test
    public void deberiaPatearBalonHaciaLaIzquierda() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX() - 25, p1.getY() + 20);
        p1.setFacingRight(false);
        p1.kick(b);
        assertTrue(b.getVelocityX() < 0);
    }

    /**
     * comprueba que no se afecte el balon si el jugador patea estando muy lejos
     */
    @Test
    public void deberiaIgnorarPatadaSiBalonEstaFueraDeRango() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX() + 300, p1.getY());
        b.setVelocityX(0);
        p1.kick(b);
        assertEquals(0.0, b.getVelocityX(), 0.001);
    }

    /**
     * comprueba que al chocar cuerpo a cuerpo con el jugador el balon reciba impulso
     */
    @Test
    public void deberiaModificarVelocidadAlColisionarPersonajeYBalon() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        b.setPosition(p1.getX(), p1.getY());
        b.onCollision(p1);
        assertNotEquals(0.0, b.getVelocityY(), 0.001);
    }

    /**
     * comprueba que el personaje gane energia especial cada vez que toca el balon
     */
    @Test
    public void deberiaIncrementarEnergiaAlColisionarConBalon() {
        Player p1 = match.getPlayer1();
        p1.setSpecialMeter(20);
        p1.onCollision(match.getBall());
        assertTrue(p1.getSpecialMeter() > 20);
    }

    /**
     * comprueba que se sume un punto al jugador 1 al meter el balon en el arco derecho
     */
    @Test
    public void deberiaAnotarGolEnArcoDerecho() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();
        b.setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(1, match.getScorePlayer1());
    }

    /**
     * comprueba que se sume un punto al jugador 2 al entrar el balon en el arco izquierdo
     */
    @Test
    public void deberiaAnotarGolEnArcoIzquierdo() {
        Ball b = match.getBall();
        Goal leftGoal = match.getLeftGoal();
        b.setPosition(leftGoal.getX() + 5, leftGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(1, match.getScorePlayer2());
    }

    /**
     * comprueba que tras un gol el balon y los jugadores vuelvan al centro
     */
    @Test
    public void deberiaReiniciarPosicionesDespuesDeUnGol() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();
        b.setPosition(rightGoal.getX() + 15, rightGoal.getY() + 40);
        match.checkCollisions();
        assertEquals(430.0, b.getX(), 1.0);
        assertEquals(180.0, b.getY(), 1.0);
    }

    /**
     * comprueba el rebote en el travesano superior sin conceder gol
     */
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

    /**
     * comprueba que no cuente gol si el balon pasa por arriba del travesano
     */
    @Test
    public void deberiaIgnorarGolSiBalonPasaPorEncimaDelTravesano() {
        Ball b = match.getBall();
        Goal rightGoal = match.getRightGoal();
        b.setPosition(rightGoal.getX() + 10, rightGoal.getY() - 30);
        assertFalse(rightGoal.checkGoal(b));
    }

    /**
     * comprueba que la barra especial sume puntos y no sobrepase el tope de cien
     */
    @Test
    public void deberiaAcumularPoderHastaMaximoCien() {
        Player p1 = match.getPlayer1();
        p1.setSpecialMeter(50);
        p1.gainSpecial(30);
        assertEquals(80, p1.getSpecialMeter());
        p1.gainSpecial(50);
        assertEquals(100, p1.getSpecialMeter());
    }

    /**
     * comprueba que al disparar el poder especial la barra se vacie por completo
     */
    @Test
    public void deberiaConsumirPoderAlUsarHabilidad() {
        Player p1 = match.getPlayer1();
        p1.setSpecialMeter(100);
        p1.useSpecial(match.getBall());
        assertEquals(0, p1.getSpecialMeter());
    }

    /**
     * comprueba que firestrike active el efecto super tiro y lance el balon a gran velocidad
     */
    @Test
    public void deberiaActivarSuperTiroConFireStrike() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        p1.setSpecialMeter(100);
        p1.useSpecial(b);
        assertTrue(b.isSuperShot());
        assertTrue(Math.abs(b.getVelocityX()) >= 15.0);
    }

    /**
     * comprueba que teleport coloque al personaje inmediatamente cerca del balon
     */
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

    /**
     * comprueba que giant kick lance el balon con fuerte arco ascendente
     */
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

    /**
     * comprueba que no se pueda volver a tirar la habilidad hasta terminar el tiempo de cooldown
     */
    @Test
    public void deberiaBloquearUsoDeHabilidadDuranteCooldown() {
        Player p1 = match.getPlayer1();
        Ball b = match.getBall();
        p1.setSpecialMeter(100);
        p1.useSpecial(b);

        p1.setSpecialMeter(100);
        boolean reuso = p1.useSpecial(b);
        assertFalse(reuso);

        Ability ab = p1.getSpecialAbility();
        for (int i = 0; i < ab.getCooldown(); i++) {
            ab.updateTick();
        }
        assertTrue(ab.isReady());
        assertTrue(p1.useSpecial(b));
    }

    /**
     * comprueba la inicializacion y reaccion del bot en modo facil
     */
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

    /**
     * comprueba la inicializacion y calculo rapido del bot en modo dificil
     */
    @Test
    public void deberiaConfigurarDificultadDificil() {
        BotAI bot = new BotAI(660, Ball.GROUND_Y - 92, BotAI.DIFFICULTY_HARD);
        assertEquals(BotAI.DIFFICULTY_HARD, bot.getDifficultyLevel());
        Ball b = new Ball(550, Ball.GROUND_Y - 50, 36);
        b.setVelocityX(-4.0);
        bot.calculateMove(b);
        assertNotNull(bot);
    }

    /**
     * comprueba que un nivel fuera de rango vuelva al valor por defecto normal
     */
    @Test
    public void deberiaRestablecerADificultadNormalSiValorEsInvalido() {
        BotAI bot = match.getPlayer2();
        bot.setDifficultyLevel(99);
        assertEquals(BotAI.DIFFICULTY_NORMAL, bot.getDifficultyLevel());
    }

    /**
     * comprueba que la serializacion en disco guarde y recupere el partido con exactitud
     * 
     * @throws IOException si falla la creacion del archivo temporal
     * @throws HeadSoccerException si ocurre error durante el proceso de guardado o lectura
     */
    @Test
    public void deberiaSalvarYCargarPartidoEnArchivo() throws IOException, HeadSoccerException {
        File tempFile = File.createTempFile("partida_test_40", ".dat");
        tempFile.deleteOnExit();

        match.decrementTimer();
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

    /**
     * comprueba que al intentar abrir un archivo que no existe se lance headsoccerexception
     * 
     * @throws HeadSoccerException excepcion esperada por archivo inexistente
     */
    @Test(expected = HeadSoccerException.class)
    public void deberiaLanzarExcepcionAlCargarArchivoInexistente() throws HeadSoccerException {
        File archivoInexistente = new File("archivo_inexistente_99999.dat");
        MatchManager.abrir(archivoInexistente);
    }

    /**
     * comprueba que el cronometro reste exactamente un segundo por tick
     */
    @Test
    public void deberiaDecrementarRelojSegundoASegundo() {
        int tiempoInicial = match.getTimeRemaining();
        match.decrementTimer();
        assertEquals(tiempoInicial - 1, match.getTimeRemaining());
    }

    /**
     * comprueba que al llegar a cero segundos el juego se detenga y calcule el resultado
     */
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
