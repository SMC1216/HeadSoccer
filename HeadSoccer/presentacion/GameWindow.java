package presentacion;

import dominio.Ball;
import dominio.Character;
import dominio.Goal;
import dominio.MatchManager;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana principal (Vista-Controlador) del juego Head Soccer.
 * Se comunica con MatchManager en el paquete dominio y dibuja la cancha y los personajes.
 */
public class GameWindow extends JFrame {

    // Referencia al modelo del juego (Dominio)
    private MatchManager match;

    // Elementos de la barra de menú
    private JMenuItem itemNuevo, itemAbrir, itemSalvar, itemSalir, itemColor;

    // Paneles y componentes de la vista
    private JPanel hudPanel;
    private FieldCanvas boardPanel;
    private JPanel controlPanel;

    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JLabel stateLabel;
    private JProgressBar specialBarP1;
    private JProgressBar specialBarP2;

    private JButton btnPause;
    private JButton btnSpecial;
    private JButton btnReset;

    // Timers para el ciclo de juego (60 FPS aprox.) y el reloj de 1 segundo
    private Timer gameTimer;
    private Timer clockTimer;

    // Color del césped configurable con JColorChooser
    private Color colorCesped = new Color(34, 139, 34);

    public GameWindow() {
        match = new MatchManager();
        setTitle("Head Soccer - DOPO");
        prepareView();
        prepareController();
    }

    /**
     * Inicializa el diseño general de la ventana (BorderLayout) y sus paneles.
     */
    private void prepareView() {
        setSize(915, 640);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        prepareViewMenu();
        prepareViewScoreboard();
        prepareViewBoard();
        prepareViewControls();
    }

    /**
     * Construye el menú superior con Archivo y Edición.
     */
    private void prepareViewMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        itemNuevo = new JMenuItem("Nuevo Partido");
        itemAbrir = new JMenuItem("Abrir");
        itemSalvar = new JMenuItem("Salvar");
        itemSalir = new JMenuItem("Salir");

        menuArchivo.add(itemNuevo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemSalvar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        JMenu menuEdicion = new JMenu("Edición");
        itemColor = new JMenuItem("Cambiar Color Cancha");
        menuEdicion.add(itemColor);

        menuBar.add(menuArchivo);
        menuBar.add(menuEdicion);
        setJMenuBar(menuBar);
    }

    /**
     * Construye el marcador superior (NORTH) con puntaje, reloj y barras de poder.
     */
    private void prepareViewScoreboard() {
        hudPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        hudPanel.setBackground(new Color(25, 30, 45));
        hudPanel.setBorder(new EmptyBorder(8, 20, 8, 20));

        // Panel Jugador 1
        JPanel p1Box = new JPanel(new GridLayout(2, 1, 0, 4));
        p1Box.setOpaque(false);
        JLabel lblP1 = new JLabel("JUGADOR 1 (FireStrike)", JLabel.LEFT);
        lblP1.setForeground(new Color(255, 110, 110));
        lblP1.setFont(new Font("Arial", Font.BOLD, 13));
        specialBarP1 = new JProgressBar(0, 100);
        specialBarP1.setStringPainted(true);
        specialBarP1.setForeground(new Color(255, 140, 0));
        p1Box.add(lblP1);
        p1Box.add(specialBarP1);

        // Panel Central (Marcador y Reloj)
        JPanel centerBox = new JPanel(new GridLayout(2, 1));
        centerBox.setOpaque(false);
        scoreLabel = new JLabel("0  -  0", JLabel.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel subCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        subCenter.setOpaque(false);
        timerLabel = new JLabel("Tiempo: 90s");
        timerLabel.setForeground(Color.YELLOW);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        stateLabel = new JLabel("[ JUGANDO ]");
        stateLabel.setForeground(new Color(100, 255, 100));
        stateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        subCenter.add(timerLabel);
        subCenter.add(stateLabel);

        centerBox.add(scoreLabel);
        centerBox.add(subCenter);

        // Panel Bot AI
        JPanel p2Box = new JPanel(new GridLayout(2, 1, 0, 4));
        p2Box.setOpaque(false);
        JLabel lblP2 = new JLabel("BOT AI (Teleport)", JLabel.RIGHT);
        lblP2.setForeground(new Color(110, 180, 255));
        lblP2.setFont(new Font("Arial", Font.BOLD, 13));
        specialBarP2 = new JProgressBar(0, 100);
        specialBarP2.setStringPainted(true);
        specialBarP2.setForeground(new Color(0, 191, 255));
        p2Box.add(lblP2);
        p2Box.add(specialBarP2);

        hudPanel.add(p1Box);
        hudPanel.add(centerBox);
        hudPanel.add(p2Box);

        add(hudPanel, BorderLayout.NORTH);
    }

    /**
     * Inicializa el lienzo central donde se dibujan la cancha, arcos, muñecos y balón.
     */
    private void prepareViewBoard() {
        boardPanel = new FieldCanvas();
        boardPanel.setPreferredSize(new Dimension(900, 480));
        boardPanel.setFocusable(true);
        add(boardPanel, BorderLayout.CENTER);
        refresh();
    }

    /**
     * Construye el panel inferior (SOUTH) con botones y guía de teclado.
     */
    private void prepareViewControls() {
        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(new Color(230, 230, 230));
        controlPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
        btnPanel.setOpaque(false);

        btnPause = new JButton("Pausar Partido");
        btnSpecial = new JButton("Usar Poder (Tecla E)");
        btnReset = new JButton("Reiniciar Partido");

        btnPanel.add(btnPause);
        btnPanel.add(btnSpecial);
        btnPanel.add(btnReset);

        JLabel infoLabel = new JLabel(
            "Controles Jugador 1:  [A / D] Mover   |   [W] Saltar   |   [ESPACIO] Patear   |   [E] Poder Especial",
            JLabel.CENTER
        );
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));

        controlPanel.add(btnPanel, BorderLayout.NORTH);
        controlPanel.add(infoLabel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Actualiza los textos del marcador y repinta el lienzo de la cancha.
     */
    private void refresh() {
        scoreLabel.setText(match.getScorePlayer1() + "  -  " + match.getScorePlayer2());
        timerLabel.setText("Tiempo: " + match.getTimeRemaining() + "s");

        int sp1 = match.getPlayer1().getSpecialMeter();
        specialBarP1.setValue(sp1);
        specialBarP1.setString(sp1 >= 100 ? "¡LISTO! (Presiona E)" : "Poder: " + sp1 + "%");

        int sp2 = match.getPlayer2().getSpecialMeter();
        specialBarP2.setValue(sp2);
        specialBarP2.setString("Poder: " + sp2 + "%");

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    /**
     * Configura todos los oyentes de ventana, menú, botones, teclado y temporizador.
     */
    private void prepareController() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });

        prepareControllerMenu();
        prepareGameController();
    }

    private void prepareControllerMenu() {
        itemSalir.addActionListener(e -> salir());
        itemNuevo.addActionListener(e -> reiniciarJuego());
        itemAbrir.addActionListener(e -> abrirJuego());
        itemSalvar.addActionListener(e -> salvarJuego());
        itemColor.addActionListener(e -> cambiarColorCancha());

        btnPause.addActionListener(e -> {
            match.togglePause();
            if ("PAUSED".equals(match.getMatchState())) {
                btnPause.setText("Reanudar Partido");
                stateLabel.setText("[ PAUSADO ]");
                stateLabel.setForeground(Color.ORANGE);
            } else {
                btnPause.setText("Pausar Partido");
                stateLabel.setText("[ JUGANDO ]");
                stateLabel.setForeground(new Color(100, 255, 100));
            }
            boardPanel.requestFocusInWindow();
            refresh();
        });

        btnSpecial.addActionListener(e -> {
            match.getPlayer1().setSpecialMeter(100);
            match.getPlayer1().handleInput("SPECIAL", match.getBall());
            boardPanel.requestFocusInWindow();
            refresh();
        });

        btnReset.addActionListener(e -> {
            reiniciarJuego();
            boardPanel.requestFocusInWindow();
        });
    }

    /**
     * Conecta el KeyListener para mover al Jugador 1 y el Timer que actualiza las físicas.
     */
    private void prepareGameController() {
        KeyAdapter keyHandler = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                    match.getPlayer1().handleInput("LEFT", match.getBall());
                } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                    match.getPlayer1().handleInput("RIGHT", match.getBall());
                } else if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                    match.getPlayer1().handleInput("JUMP", match.getBall());
                } else if (code == KeyEvent.VK_SPACE) {
                    match.getPlayer1().handleInput("KICK", match.getBall());
                } else if (code == KeyEvent.VK_E) {
                    match.getPlayer1().handleInput("SPECIAL", match.getBall());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_D ||
                    code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
                    match.getPlayer1().handleInput("STOP", match.getBall());
                }
            }
        };

        boardPanel.addKeyListener(keyHandler);
        this.addKeyListener(keyHandler);

        // Timer de físicas y renderizado (cada 20 ms)
        gameTimer = new Timer(20, e -> {
            match.updateFrame();
            refresh();
        });
        gameTimer.start();

        // Timer del reloj del partido (cada 1000 ms = 1 segundo)
        clockTimer = new Timer(1000, e -> {
            match.decrementTimer();
            refresh();
        });
        clockTimer.start();
    }

    private void reiniciarJuego() {
        match.startGame();
        btnPause.setText("Pausar Partido");
        stateLabel.setText("[ JUGANDO ]");
        stateLabel.setForeground(new Color(100, 255, 100));
        refresh();
    }

    private void abrirJuego() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                "Función ABRIR en construcción.\nArchivo: " + archivo.getName(),
                "En construcción", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void salvarJuego() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                "Función SALVAR en construcción.\nArchivo: " + archivo.getName(),
                "En construcción", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cambiarColorCancha() {
        Color nuevoColor = JColorChooser.showDialog(this, "Selecciona el color del césped", colorCesped);
        if (nuevoColor != null) {
            colorCesped = nuevoColor;
            refresh();
        }
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que deseas salir de Head Soccer?",
            "Confirmar salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (respuesta == JOptionPane.YES_OPTION) {
            gameTimer.stop();
            clockTimer.stop();
            System.exit(0);
        }
    }

    /**
     * Lienzo personalizado que dibuja la cancha, las porterías, los personajes y el balón
     * leyendo las coordenadas reales de los objetos del paquete dominio.
     */
    private class FieldCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int groundY = (int) Ball.GROUND_Y;

            // 1. Cielo nocturno del estadio y tribunas
            GradientPaint sky = new GradientPaint(0, 0, new Color(18, 30, 62), 0, groundY, new Color(55, 100, 160));
            g2.setPaint(sky);
            g2.fillRect(0, 0, w, groundY);

            // Gradas con público simulado
            g2.setColor(new Color(35, 45, 70));
            g2.fillRect(0, groundY - 105, w, 105);
            for (int row = 0; row < 4; row++) {
                for (int col = 20; col < w - 20; col += 24) {
                    g2.setColor((col + row) % 2 == 0 ? new Color(215, 70, 70) : new Color(70, 155, 235));
                    g2.fillOval(col, groundY - 92 + (row * 20), 12, 12);
                }
            }

            // Valla publicitaria
            g2.setColor(new Color(18, 75, 145));
            g2.fillRect(0, groundY - 22, w, 22);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("HEAD SOCCER  -  ARENA DOPO", w / 2 - 90, groundY - 6);

            // 2. Césped con franjas deportivas
            Color stripeBright = colorCesped.brighter();
            int stripeWidth = Math.max(1, w / 10);
            for (int i = 0; i < 11; i++) {
                g2.setColor(i % 2 == 0 ? colorCesped : stripeBright);
                g2.fillRect(i * stripeWidth, groundY, stripeWidth, h - groundY);
            }

            // Líneas reglamentarias de la cancha
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(0, groundY, w, groundY);
            g2.drawLine(w / 2, groundY, w / 2, h);
            g2.drawOval(w / 2 - 45, groundY + 10, 90, 40);

            // 3. Dibujar las dos Porterías desde el dominio
            drawGoal(g2, match.getLeftGoal(), true);
            drawGoal(g2, match.getRightGoal(), false);

            // 4. Dibujar a los dos Personajes desde el dominio
            drawCharacter(g2, match.getPlayer1(), new Color(220, 45, 45), "J1");
            drawCharacter(g2, match.getPlayer2(), new Color(40, 120, 225), "BOT");

            // 5. Dibujar el Balón desde el dominio
            drawBall(g2, match.getBall(), groundY);

            // 6. Mensaje si está en pausa o finalizado
            if ("PAUSED".equals(match.getMatchState())) {
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, w, h);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 34));
                g2.drawString("PARTIDO EN PAUSA", w / 2 - 165, h / 2);
            }
        }

        private void drawGoal(Graphics2D g2, Goal goal, boolean isLeft) {
            int x = (int) goal.getX();
            int y = (int) goal.getY();
            int gw = goal.getWidth();
            int gh = goal.getHeight();

            // Red cuadriculada
            g2.setColor(new Color(235, 235, 235, 130));
            g2.setStroke(new BasicStroke(1));
            for (int gx = x; gx <= x + gw; gx += 12) {
                g2.drawLine(gx, y, gx, y + gh);
            }
            for (int gy = y; gy <= y + gh; gy += 12) {
                g2.drawLine(x, gy, x + gw, gy);
            }

            // Postes y travesaño
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(5));
            g2.drawLine(x, y, x + gw, y);
            if (isLeft) {
                g2.drawLine(x, y, x, y + gh);
            } else {
                g2.drawLine(x + gw, y, x + gw, y + gh);
            }
        }

        private void drawCharacter(Graphics2D g2, Character ch, Color teamColor, String tag) {
            int x = (int) ch.getX();
            int y = (int) ch.getY();
            boolean right = ch.isFacingRight();

            // Sombra en el césped
            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillOval(x + 6, (int) Ball.GROUND_Y - 6, 60, 14);

            // Camiseta pequeña
            g2.setColor(teamColor);
            g2.fillRoundRect(x + 20, y + 54, 32, 24, 8, 8);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x + 20, y + 54, 32, 24, 8, 8);

            // Botín / Guayo
            int shoeX = right ? x + 40 : x + 4;
            g2.setColor(new Color(35, 35, 35));
            g2.fillRoundRect(shoeX, y + 74, 28, 16, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawLine(shoeX + 5, y + 82, shoeX + 20, y + 82);

            // Cabeza grande estilo Head Soccer
            g2.setColor(new Color(255, 218, 185));
            g2.fillOval(x, y, 70, 60);
            g2.setColor(Color.BLACK);
            g2.drawOval(x, y, 70, 60);

            // Cinta deportiva en la frente
            g2.setColor(teamColor);
            g2.fillRect(x + 4, y + 12, 62, 10);
            g2.setColor(Color.BLACK);
            g2.drawRect(x + 4, y + 12, 62, 10);

            // Ojo mirando hacia la dirección activa
            int eyeX = right ? x + 45 : x + 12;
            g2.setColor(Color.WHITE);
            g2.fillOval(eyeX, y + 25, 13, 13);
            g2.setColor(Color.BLACK);
            g2.fillOval(right ? eyeX + 5 : eyeX + 2, y + 28, 6, 6);
            g2.drawOval(eyeX, y + 25, 13, 13);

            // Etiqueta sobre la cabeza
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(tag, x + 26, y - 6);
        }

        private void drawBall(Graphics2D g2, Ball ball, int groundY) {
            int bx = (int) ball.getX();
            int by = (int) ball.getY();
            int size = ball.getWidth();

            // Sombra del balón en el césped
            g2.setColor(new Color(0, 0, 0, 75));
            g2.fillOval(bx + 2, groundY - 4, size - 4, 10);

            // Efecto de fuego cuando se activa FireStrike
            if (ball.isSuperShot()) {
                g2.setColor(new Color(255, 90, 0, 190));
                g2.fillOval(bx - 10, by - 10, size + 20, size + 20);
                g2.setColor(new Color(255, 215, 0, 200));
                g2.fillOval(bx - 5, by - 5, size + 10, size + 10);
            }

            // Esfera del balón
            g2.setColor(Color.WHITE);
            g2.fillOval(bx, by, size, size);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(bx, by, size, size);

            // Pentágono central y costuras
            g2.fillOval(bx + 12, by + 12, 12, 12);
            g2.drawLine(bx + 18, by + 12, bx + 18, by + 2);
            g2.drawLine(bx + 12, by + 18, bx + 2, by + 14);
            g2.drawLine(bx + 24, by + 18, bx + 34, by + 14);
        }
    }

    public static void main(String[] args) {
        GameWindow window = new GameWindow();
        window.setVisible(true);
    }
}