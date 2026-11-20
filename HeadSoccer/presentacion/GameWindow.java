package presentacion;

import dominio.Ball;
import dominio.BotAI;
import dominio.Character;
import dominio.Goal;
import dominio.HeadSoccerException;
import dominio.MatchManager;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana principal (Vista-Controlador) del juego Head Soccer.
 * Presenta el menú inicial de selección de modo (vs Bot, 2 Jugadores o Cargar Partida),
 * la cancha a pantalla completa, el marcador superior con HUD de poder,
 * menú de pausa interactivo con tecla ESC y pantalla de resultados al finalizar.
 */
public class GameWindow extends JFrame {

    private MatchManager match;

    // Elementos de la barra de menú superior
    private JMenuItem itemNuevoBot, itemNuevoPvP, itemAbrir, itemSalvar, itemSalir, itemColor;
    private JRadioButtonMenuItem itemDifFacil, itemDifNormal, itemDifDificil;

    // Componentes del HUD
    private JPanel hudPanel;
    private FieldCanvas boardPanel;
    private JPanel footerPanel;

    private JLabel lblP1;
    private JLabel lblP2;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JLabel stateLabel;
    private JLabel difficultyLabel;
    private JLabel infoLabel;
    private JProgressBar specialBarP1;
    private JProgressBar specialBarP2;

    // Timers para el ciclo de físicas (~50 FPS) y cronómetro de 1 segundo
    private Timer gameTimer;
    private Timer clockTimer;

    // Conjunto de teclas presionadas para movimiento fluido simultáneo de 1 o 2 jugadores
    private final Set<Integer> pressedKeys = new HashSet<>();

    // Color del césped configurable
    private Color colorCesped = new Color(34, 139, 34);

    public GameWindow() {
        match = new MatchManager();
        match.returnToMenu();
        prepareElements();
        prepareActions();
    }

    /**
     * Inicializa los componentes visuales de la ventana (Semana 12 - GUI).
     */
    private void prepareElements() {
        setTitle("Head Soccer");
        setSize(920, 650);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        prepareElementsMenu();
        prepareElementsScore();
        prepareElementsBoard();
        prepareElementsFooter();
        refresh();
    }

    private void prepareElementsMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        itemNuevoBot = new JMenuItem("Nuevo Partido (1J vs Bot)");
        itemNuevoPvP = new JMenuItem("Nuevo Partido (2 Jugadores)");
        itemAbrir = new JMenuItem("Abrir Partida");
        itemSalvar = new JMenuItem("Salvar Partida");
        itemSalir = new JMenuItem("Salir al Menú Principal");

        menuArchivo.add(itemNuevoBot);
        menuArchivo.add(itemNuevoPvP);
        menuArchivo.addSeparator();
        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemSalvar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        JMenu menuEdicion = new JMenu("Opciones");
        itemColor = new JMenuItem("Cambiar Color Césped");
        menuEdicion.add(itemColor);
        menuEdicion.addSeparator();

        JMenu menuDificultad = new JMenu("Dificultad IA");
        ButtonGroup bgDif = new ButtonGroup();
        itemDifFacil = new JRadioButtonMenuItem("Fácil");
        itemDifNormal = new JRadioButtonMenuItem("Normal", true);
        itemDifDificil = new JRadioButtonMenuItem("Difícil");
        bgDif.add(itemDifFacil);
        bgDif.add(itemDifNormal);
        bgDif.add(itemDifDificil);
        menuDificultad.add(itemDifFacil);
        menuDificultad.add(itemDifNormal);
        menuDificultad.add(itemDifDificil);
        menuEdicion.add(menuDificultad);

        menuBar.add(menuArchivo);
        menuBar.add(menuEdicion);
        setJMenuBar(menuBar);
    }

    private void prepareElementsScore() {
        hudPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        hudPanel.setBackground(new Color(20, 25, 40));
        hudPanel.setBorder(new EmptyBorder(8, 20, 8, 20));

        // Panel Jugador 1
        JPanel p1Box = new JPanel(new GridLayout(2, 1, 0, 4));
        p1Box.setOpaque(false);
        lblP1 = new JLabel("JUGADOR 1 (FireStrike)", JLabel.LEFT);
        lblP1.setForeground(new Color(255, 110, 110));
        lblP1.setFont(new Font("Arial", Font.BOLD, 13));
        specialBarP1 = new JProgressBar(0, 100);
        specialBarP1.setStringPainted(true);
        specialBarP1.setForeground(new Color(255, 140, 0));
        p1Box.add(lblP1);
        p1Box.add(specialBarP1);

        // Panel Central (Marcador, Reloj, Estado y Dificultad/Modo)
        JPanel centerBox = new JPanel(new GridLayout(2, 1));
        centerBox.setOpaque(false);
        scoreLabel = new JLabel("0  -  0", JLabel.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 26));

        JPanel subCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        subCenter.setOpaque(false);
        timerLabel = new JLabel("Tiempo: 90s");
        timerLabel.setForeground(Color.YELLOW);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        stateLabel = new JLabel("[ MENÚ ]");
        stateLabel.setForeground(new Color(100, 200, 255));
        stateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        difficultyLabel = new JLabel("(Normal)");
        difficultyLabel.setForeground(new Color(180, 200, 220));
        difficultyLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        subCenter.add(timerLabel);
        subCenter.add(stateLabel);
        subCenter.add(difficultyLabel);

        centerBox.add(scoreLabel);
        centerBox.add(subCenter);

        // Panel Jugador 2 / Bot AI
        JPanel p2Box = new JPanel(new GridLayout(2, 1, 0, 4));
        p2Box.setOpaque(false);
        lblP2 = new JLabel("BOT AI (Teleport)", JLabel.RIGHT);
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

    private void prepareElementsBoard() {
        boardPanel = new FieldCanvas();
        boardPanel.setPreferredSize(new Dimension(900, 510));
        boardPanel.setFocusable(true);
        add(boardPanel, BorderLayout.CENTER);
    }

    private void prepareElementsFooter() {
        footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(15, 20, 30));
        footerPanel.setBorder(new EmptyBorder(3, 10, 3, 10));

        infoLabel = new JLabel(
            "[A / D] Mover  |  [W] Saltar  |  [ESPACIO] Patear  |  [E] Poder Especial  |  [ESC] Menú de Pausa",
            JLabel.CENTER
        );
        infoLabel.setForeground(new Color(200, 210, 225));
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        footerPanel.add(infoLabel);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void refresh() {
        scoreLabel.setText(match.getScorePlayer1() + "  -  " + match.getScorePlayer2());
        timerLabel.setText("Tiempo: " + match.getTimeRemaining() + "s");

        boolean enMenu = MatchManager.STATE_MENU.equals(match.getMatchState());
        boolean terminado = MatchManager.STATE_FINISHED.equals(match.getMatchState());
        boolean pausado = MatchManager.STATE_PAUSED.equals(match.getMatchState());
        boolean dosJugadores = match.isTwoPlayerMode();

        if (enMenu) {
            stateLabel.setText("[ MENÚ PRINCIPAL ]");
            stateLabel.setForeground(new Color(100, 200, 255));
        } else if (terminado) {
            stateLabel.setText("[ FINALIZADO ]");
            stateLabel.setForeground(Color.RED);
        } else if (pausado) {
            stateLabel.setText("[ PAUSADO ]");
            stateLabel.setForeground(Color.ORANGE);
        } else {
            stateLabel.setText("[ JUGANDO ]");
            stateLabel.setForeground(new Color(100, 255, 100));
        }

        int dif = match.getDifficultyLevel();
        if (dif == BotAI.DIFFICULTY_EASY) {
            itemDifFacil.setSelected(true);
        } else if (dif == BotAI.DIFFICULTY_HARD) {
            itemDifDificil.setSelected(true);
        } else {
            itemDifNormal.setSelected(true);
        }

        if (dosJugadores) {
            difficultyLabel.setText("(2 Jugadores)");
            lblP2.setText("JUGADOR 2 (GiantKick)");
            infoLabel.setText(
                "J1: [A/D] Mover [W] Saltar [ESPACIO] Patear [E] Poder   ||   " +
                "J2: [←/→] Mover [↑] Saltar [ENTER/↓] Patear [M/SHIFT] Poder   ||   [ESC] Pausa"
            );
        } else {
            if (dif == BotAI.DIFFICULTY_EASY) difficultyLabel.setText("(Bot: Fácil)");
            else if (dif == BotAI.DIFFICULTY_HARD) difficultyLabel.setText("(Bot: Difícil)");
            else difficultyLabel.setText("(Bot: Normal)");

            lblP2.setText("BOT AI (Teleport)");
            infoLabel.setText(
                "[A / D] o [← / →] Mover  |  [W / ↑] Saltar  |  [ESPACIO] Patear  |  [E] Poder Especial  |  [ESC] Menú de Pausa"
            );
        }

        // Barra Jugador 1
        int sp1 = match.getPlayer1().getSpecialMeter();
        specialBarP1.setValue(sp1);
        boolean p1Ready = match.getPlayer1().getSpecialAbility() != null && match.getPlayer1().getSpecialAbility().isReady();
        if (sp1 >= 100 && p1Ready) {
            specialBarP1.setString("¡LISTO! (Presiona E)");
        } else if (!p1Ready && match.getPlayer1().getSpecialAbility() != null) {
            specialBarP1.setString("Enfriando: " + match.getPlayer1().getSpecialAbility().getCooldownRemaining() + "s");
        } else {
            specialBarP1.setString("Poder: " + sp1 + "%");
        }

        // Barra Jugador 2 / Bot AI
        Character p2 = match.getPlayer2();
        int sp2 = p2.getSpecialMeter();
        specialBarP2.setValue(sp2);
        boolean p2Ready = p2.getSpecialAbility() != null && p2.getSpecialAbility().isReady();
        if (dosJugadores) {
            if (sp2 >= 100 && p2Ready) {
                specialBarP2.setString("¡LISTO! (Presiona M / Shift)");
            } else if (!p2Ready && p2.getSpecialAbility() != null) {
                specialBarP2.setString("Enfriando: " + p2.getSpecialAbility().getCooldownRemaining() + "s");
            } else {
                specialBarP2.setString("Poder: " + sp2 + "%");
            }
        } else {
            if (!p2Ready && p2.getSpecialAbility() != null) {
                specialBarP2.setString("Enfriando: " + p2.getSpecialAbility().getCooldownRemaining() + "s");
            } else {
                specialBarP2.setString("Poder: " + sp2 + "%");
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void prepareActions() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion();
            }
        });

        prepareActionsMenu();
        prepareActionsKeyboard();
    }

    private void prepareActionsMenu() {
        itemSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                volverAlMenuPrincipal();
            }
        });
        itemNuevoBot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarModoJuego(MatchManager.MODE_PVE);
            }
        });
        itemNuevoPvP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarModoJuego(MatchManager.MODE_PVP);
            }
        });
        itemAbrir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirJuego();
            }
        });
        itemSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarJuego();
            }
        });
        itemColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarColorCancha();
            }
        });

        itemDifFacil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.setDifficultyLevel(BotAI.DIFFICULTY_EASY);
                refresh();
            }
        });
        itemDifNormal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.setDifficultyLevel(BotAI.DIFFICULTY_NORMAL);
                refresh();
            }
        });
        itemDifDificil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.setDifficultyLevel(BotAI.DIFFICULTY_HARD);
                refresh();
            }
        });
    }

    /**
     * Sincroniza el movimiento horizontal de Jugador 1 y Jugador 2 según las teclas mantenidas.
     */
    private void actualizarMovimientoContinuo() {
        if (!MatchManager.STATE_PLAYING.equals(match.getMatchState())) {
            return;
        }

        boolean dosJugadores = match.isTwoPlayerMode();

        if (dosJugadores) {
            // Jugador 1: A / D
            boolean p1Left = pressedKeys.contains(KeyEvent.VK_A);
            boolean p1Right = pressedKeys.contains(KeyEvent.VK_D);
            if (p1Left && !p1Right) {
                match.getPlayer1().handleInput("LEFT", match.getBall());
            } else if (p1Right && !p1Left) {
                match.getPlayer1().handleInput("RIGHT", match.getBall());
            } else {
                match.getPlayer1().handleInput("STOP", match.getBall());
            }

            // Jugador 2: Flecha Izquierda / Flecha Derecha
            boolean p2Left = pressedKeys.contains(KeyEvent.VK_LEFT);
            boolean p2Right = pressedKeys.contains(KeyEvent.VK_RIGHT);
            try {
                if (p2Left && !p2Right) {
                    match.handlePlayer2Input("LEFT");
                } else if (p2Right && !p2Left) {
                    match.handlePlayer2Input("RIGHT");
                } else {
                    match.handlePlayer2Input("STOP");
                }
            } catch (HeadSoccerException ex) {
                // Ignorar si no es modo 2 jugadores
            }
        } else {
            // Modo 1 Jugador vs Bot: acepta tanto A/D como Flechas Izquierda/Derecha
            boolean p1Left = pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT);
            boolean p1Right = pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT);
            if (p1Left && !p1Right) {
                match.getPlayer1().handleInput("LEFT", match.getBall());
            } else if (p1Right && !p1Left) {
                match.getPlayer1().handleInput("RIGHT", match.getBall());
            } else {
                match.getPlayer1().handleInput("STOP", match.getBall());
            }
        }
    }

    private void prepareActionsKeyboard() {
        KeyAdapter keyHandler = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                pressedKeys.add(code);

                // Si estamos en el menú principal, atajos 1, 2 y 3
                if (MatchManager.STATE_MENU.equals(match.getMatchState())) {
                    if (code == KeyEvent.VK_1) iniciarModoJuego(MatchManager.MODE_PVE);
                    else if (code == KeyEvent.VK_2) iniciarModoJuego(MatchManager.MODE_PVP);
                    else if (code == KeyEvent.VK_3) abrirJuego();
                    return;
                }

                // Tecla ESC para pausar o reanudar durante un partido
                if (code == KeyEvent.VK_ESCAPE) {
                    if (MatchManager.STATE_PLAYING.equals(match.getMatchState())) {
                        pressedKeys.clear();
                        match.pause();
                    } else if (MatchManager.STATE_PAUSED.equals(match.getMatchState())) {
                        match.resume();
                    } else if (MatchManager.STATE_FINISHED.equals(match.getMatchState())) {
                        volverAlMenuPrincipal();
                    }
                    refresh();
                    return;
                }

                // Si el partido está pausado o finalizado, procesar atajos de menú
                if (!MatchManager.STATE_PLAYING.equals(match.getMatchState())) {
                    if (MatchManager.STATE_PAUSED.equals(match.getMatchState())) {
                        if (code == KeyEvent.VK_1) match.resume();
                        else if (code == KeyEvent.VK_2) salvarJuego();
                        else if (code == KeyEvent.VK_3) abrirJuego();
                        else if (code == KeyEvent.VK_4) reiniciarJuego();
                        else if (code == KeyEvent.VK_5) volverAlMenuPrincipal();
                        refresh();
                    } else if (MatchManager.STATE_FINISHED.equals(match.getMatchState())) {
                        if (code == KeyEvent.VK_R || code == KeyEvent.VK_ENTER) reiniciarJuego();
                        else if (code == KeyEvent.VK_Q) volverAlMenuPrincipal();
                    }
                    return;
                }

                boolean dosJugadores = match.isTwoPlayerMode();

                if (dosJugadores) {
                    // Acciones instantáneas Jugador 1 (W, ESPACIO/S, E)
                    if (code == KeyEvent.VK_W) {
                        match.getPlayer1().handleInput("JUMP", match.getBall());
                    } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_S) {
                        match.getPlayer1().handleInput("KICK", match.getBall());
                    } else if (code == KeyEvent.VK_E) {
                        match.getPlayer1().handleInput("SPECIAL", match.getBall());
                    }

                    // Acciones instantáneas Jugador 2 (Flecha Arriba, ENTER/Flecha Abajo/L, M/SHIFT)
                    try {
                        if (code == KeyEvent.VK_UP) {
                            match.handlePlayer2Input("JUMP");
                        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_L) {
                            match.handlePlayer2Input("KICK");
                        } else if (code == KeyEvent.VK_M || code == KeyEvent.VK_SHIFT) {
                            match.handlePlayer2Input("SPECIAL");
                        }
                    } catch (HeadSoccerException ex) {
                        // Controlado por dominio
                    }
                } else {
                    // Modo 1 Jugador vs Bot
                    if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                        match.getPlayer1().handleInput("JUMP", match.getBall());
                    } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_S) {
                        match.getPlayer1().handleInput("KICK", match.getBall());
                    } else if (code == KeyEvent.VK_E) {
                        match.getPlayer1().handleInput("SPECIAL", match.getBall());
                    }
                }

                actualizarMovimientoContinuo();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                pressedKeys.remove(code);
                actualizarMovimientoContinuo();
            }
        };

        boardPanel.addKeyListener(keyHandler);
        this.addKeyListener(keyHandler);

        // Timer de físicas a 50 FPS (cada 20 ms)
        gameTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarMovimientoContinuo();
                match.updateFrame();
                refresh();
            }
        });
        gameTimer.start();

        // Timer del reloj (cada 1000 ms)
        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.decrementTimer();
                refresh();
            }
        });
        clockTimer.start();
    }

    /**
     * Inicia un nuevo partido en el modo indicado (MODE_PVE contra el Bot o MODE_PVP para 2 jugadores).
     * @param modo Modo de juego a iniciar.
     */
    public void iniciarModoJuego(String modo) {
        pressedKeys.clear();
        int dif = match.getDifficultyLevel();
        try {
            match.configureMatch(modo, dif);
        } catch (HeadSoccerException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de Configuración", JOptionPane.ERROR_MESSAGE);
        }
        boardPanel.requestFocusInWindow();
        refresh();
    }

    public void reiniciarJuego() {
        pressedKeys.clear();
        int dif = match.getDifficultyLevel();
        match.startGame();
        match.setDifficultyLevel(dif);
        boardPanel.requestFocusInWindow();
        refresh();
    }

    /**
     * Devuelve al jugador al menú principal de selección de tipo de partida sin cerrar la ventana.
     */
    public void volverAlMenuPrincipal() {
        pressedKeys.clear();
        match.returnToMenu();
        boardPanel.requestFocusInWindow();
        refresh();
    }

    public void abrirJuego() {
        pressedKeys.clear();
        String estadoPrevio = match.getMatchState();
        boolean estabaJugando = MatchManager.STATE_PLAYING.equals(estadoPrevio);
        if (estabaJugando) {
            match.pause();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar partida de Head Soccer");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try {
                MatchManager cargado = MatchManager.abrir(archivo);
                this.match = cargado;
                if (MatchManager.STATE_MENU.equals(this.match.getMatchState())) {
                    this.match.resume();
                }
                refresh();
                JOptionPane.showMessageDialog(this,
                    "Partida cargada exitosamente desde:\n" + archivo.getName(),
                    "Carga Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (HeadSoccerException e) {
                JOptionPane.showMessageDialog(this,
                    "No se pudo cargar la partida: " + e.getMessage(),
                    "Error al Cargar", JOptionPane.ERROR_MESSAGE);
                if (estabaJugando) match.resume();
            }
        } else {
            if (estabaJugando) match.resume();
        }
        boardPanel.requestFocusInWindow();
        refresh();
    }

    public void salvarJuego() {
        pressedKeys.clear();
        if (MatchManager.STATE_MENU.equals(match.getMatchState())) {
            JOptionPane.showMessageDialog(this,
                "Inicia o carga una partida antes de guardar.",
                "Menú Principal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean estabaJugando = MatchManager.STATE_PLAYING.equals(match.getMatchState());
        if (estabaJugando) {
            match.pause();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar partida de Head Soccer");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try {
                match.salvar(archivo);
                JOptionPane.showMessageDialog(this,
                    "Partida guardada exitosamente en:\n" + archivo.getName(),
                    "Guardado Exitoso", JOptionPane.INFORMATION_MESSAGE);
            } catch (HeadSoccerException e) {
                JOptionPane.showMessageDialog(this,
                    "No se pudo guardar la partida: " + e.getMessage(),
                    "Error al Guardar", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (estabaJugando) {
            match.resume();
        }
        boardPanel.requestFocusInWindow();
        refresh();
    }

    public void cambiarColorCancha() {
        Color nuevoColor = JColorChooser.showDialog(this, "Selecciona el color del césped", colorCesped);
        if (nuevoColor != null) {
            colorCesped = nuevoColor;
            refresh();
        }
    }

    /**
     * Cierra la ventana únicamente cuando el usuario pulsa el botón X de la ventana del sistema.
     */
    private void cerrarAplicacion() {
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Deseas cerrar completamente la aplicación Head Soccer?",
            "Cerrar aplicación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (respuesta == JOptionPane.YES_OPTION) {
            if (gameTimer != null) gameTimer.stop();
            if (clockTimer != null) clockTimer.stop();
            System.exit(0);
        }
    }

    /**
     * Cuando se elige Salir desde los menús del juego, regresa al menú principal de selección de partida
     * sin cerrar la ventana.
     */
    public void salir() {
        volverAlMenuPrincipal();
    }

    /**
     * Lienzo interactivo donde se dibujan la cancha, arcos, personajes, balón,
     * menú principal de selección de modo, menú de pausa flotante y pantalla final de resultados.
     */
    private class FieldCanvas extends JPanel {
        // Coordenadas de botones del menú principal (inicio)
        private final Rectangle btnModeBotRect = new Rectangle(290, 210, 320, 48);
        private final Rectangle btnModePvPRect = new Rectangle(290, 272, 320, 48);
        private final Rectangle btnModeLoadRect = new Rectangle(290, 334, 320, 48);

        // Selector rápido de dificultad en el menú principal
        private final Rectangle btnDifEasyRect = new Rectangle(290, 412, 100, 32);
        private final Rectangle btnDifNormalRect = new Rectangle(400, 412, 100, 32);
        private final Rectangle btnDifHardRect = new Rectangle(510, 412, 100, 32);

        // Coordenadas de botones del menú de pausa
        private final Rectangle btnResumeRect = new Rectangle(330, 175, 240, 42);
        private final Rectangle btnSaveRect = new Rectangle(330, 230, 240, 42);
        private final Rectangle btnLoadRect = new Rectangle(330, 285, 240, 42);
        private final Rectangle btnRestartRect = new Rectangle(330, 340, 240, 42);
        private final Rectangle btnExitRect = new Rectangle(330, 395, 240, 42);

        // Coordenadas de botones de la pantalla de fin de partido
        private final Rectangle btnPlayAgainRect = new Rectangle(255, 340, 185, 46);
        private final Rectangle btnQuitRect = new Rectangle(460, 340, 195, 46);

        private Point mousePos = new Point(-1, -1);

        public FieldCanvas() {
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePos = e.getPoint();
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    Point p = e.getPoint();

                    if (MatchManager.STATE_MENU.equals(match.getMatchState())) {
                        if (btnModeBotRect.contains(p)) {
                            iniciarModoJuego(MatchManager.MODE_PVE);
                        } else if (btnModePvPRect.contains(p)) {
                            iniciarModoJuego(MatchManager.MODE_PVP);
                        } else if (btnModeLoadRect.contains(p)) {
                            abrirJuego();
                        } else if (btnDifEasyRect.contains(p)) {
                            match.setDifficultyLevel(BotAI.DIFFICULTY_EASY);
                            refresh();
                        } else if (btnDifNormalRect.contains(p)) {
                            match.setDifficultyLevel(BotAI.DIFFICULTY_NORMAL);
                            refresh();
                        } else if (btnDifHardRect.contains(p)) {
                            match.setDifficultyLevel(BotAI.DIFFICULTY_HARD);
                            refresh();
                        }
                    } else if (MatchManager.STATE_PAUSED.equals(match.getMatchState())) {
                        if (btnResumeRect.contains(p)) {
                            match.resume();
                        } else if (btnSaveRect.contains(p)) {
                            salvarJuego();
                        } else if (btnLoadRect.contains(p)) {
                            abrirJuego();
                        } else if (btnRestartRect.contains(p)) {
                            reiniciarJuego();
                        } else if (btnExitRect.contains(p)) {
                            volverAlMenuPrincipal();
                        }
                        refresh();
                    } else if (MatchManager.STATE_FINISHED.equals(match.getMatchState())) {
                        if (btnPlayAgainRect.contains(p)) {
                            reiniciarJuego();
                        } else if (btnQuitRect.contains(p)) {
                            volverAlMenuPrincipal();
                        }
                    }
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int groundY = (int) Ball.GROUND_Y;

            // 1. Cielo nocturno del estadio y tribunas
            GradientPaint sky = new GradientPaint(0, 0, new Color(15, 25, 55), 0, groundY, new Color(45, 85, 145));
            g2.setPaint(sky);
            g2.fillRect(0, 0, w, groundY);

            // Gradas con público
            g2.setColor(new Color(30, 40, 65));
            g2.fillRect(0, groundY - 110, w, 110);
            for (int row = 0; row < 4; row++) {
                for (int col = 20; col < w - 20; col += 24) {
                    g2.setColor((col + row) % 2 == 0 ? new Color(220, 65, 65) : new Color(65, 150, 230));
                    g2.fillOval(col, groundY - 96 + (row * 20), 12, 12);
                }
            }

            // Valla publicitaria
            g2.setColor(new Color(15, 65, 130));
            g2.fillRect(0, groundY - 24, w, 24);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("HEAD SOCCER  -  ARENA DOPO", w / 2 - 95, groundY - 7);

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
            g2.drawOval(w / 2 - 50, groundY + 10, 100, 45);

            // 3. Dibujar las Porterías
            drawGoal(g2, match.getLeftGoal(), true);
            drawGoal(g2, match.getRightGoal(), false);

            // 4. Dibujar Personajes
            String p2Tag = match.isTwoPlayerMode() ? "J2" : "BOT";
            drawCharacter(g2, match.getPlayer1(), new Color(225, 45, 45), "J1");
            drawCharacter(g2, match.getPlayer2(), new Color(40, 120, 225), p2Tag);

            // 5. Dibujar Balón
            drawBall(g2, match.getBall(), groundY);

            // 6. Overlay del Menú Principal (Selección de modo al iniciar o al salir de partida)
            if (MatchManager.STATE_MENU.equals(match.getMatchState())) {
                drawMainMenu(g2, w, h);
            }

            // 7. Overlay del Menú de Pausa (tecla ESC)
            if (MatchManager.STATE_PAUSED.equals(match.getMatchState())) {
                drawPauseMenu(g2, w, h);
            }

            // 8. Overlay de Fin de Partido (Resultados)
            if (MatchManager.STATE_FINISHED.equals(match.getMatchState())) {
                drawResultsScreen(g2, w, h);
            }
        }

        private void drawMainMenu(Graphics2D g2, int w, int h) {
            // Fondo translúcido
            g2.setColor(new Color(8, 12, 22, 215));
            g2.fillRect(0, 0, w, h);

            // Marco del panel central
            g2.setColor(new Color(22, 32, 52));
            g2.fillRoundRect(245, 55, 410, 415, 22, 22);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(245, 55, 410, 415, 22, 22);

            // Título y subtítulo
            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.drawString("HEAD SOCCER 2D", 315, 108);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.drawString("Selecciona el tipo de partida que deseas jugar:", 282, 145);

            g2.setColor(new Color(175, 195, 220));
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("(Haz clic en una opción o presiona las teclas 1, 2 o 3)", 302, 172);

            // Botones principales de modo de juego
            drawMenuButton(g2, btnModeBotRect, "1. Jugar contra el Bot (1J vs IA)");
            drawMenuButton(g2, btnModePvPRect, "2. Jugar 2 Jugadores (J1 vs J2)");
            drawMenuButton(g2, btnModeLoadRect, "3. Cargar Partida Guardada");

            // Selector de dificultad para el Bot
            g2.setColor(new Color(200, 215, 235));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("Dificultad del Bot AI:", 385, 402);

            int dif = match.getDifficultyLevel();
            drawDifficultyPill(g2, btnDifEasyRect, "Fácil", dif == BotAI.DIFFICULTY_EASY);
            drawDifficultyPill(g2, btnDifNormalRect, "Normal", dif == BotAI.DIFFICULTY_NORMAL);
            drawDifficultyPill(g2, btnDifHardRect, "Difícil", dif == BotAI.DIFFICULTY_HARD);
        }

        private void drawDifficultyPill(Graphics2D g2, Rectangle r, String text, boolean selected) {
            boolean hover = r.contains(mousePos);
            if (selected) {
                g2.setColor(new Color(45, 155, 75));
            } else {
                g2.setColor(hover ? new Color(55, 85, 135) : new Color(35, 48, 72));
            }
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.setColor(selected ? Color.YELLOW : (hover ? Color.WHITE : new Color(160, 175, 200)));
            g2.setStroke(new BasicStroke(selected ? 2 : 1));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
            int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);
        }

        private void drawPauseMenu(Graphics2D g2, int w, int h) {
            // Fondo translúcido
            g2.setColor(new Color(10, 15, 25, 210));
            g2.fillRect(0, 0, w, h);

            // Marco del panel modal
            g2.setColor(new Color(25, 35, 55));
            g2.fillRoundRect(290, 80, 320, 380, 20, 20);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(290, 80, 320, 380, 20, 20);

            // Título
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 26));
            g2.drawString("PARTIDO EN PAUSA", 325, 130);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(new Color(180, 195, 215));
            g2.drawString("Presiona ESC para continuar", 370, 152);

            // Botones interactivos
            drawMenuButton(g2, btnResumeRect, "1. Reanudar Partido");
            drawMenuButton(g2, btnSaveRect, "2. Guardar Partida");
            drawMenuButton(g2, btnLoadRect, "3. Cargar Partida");
            drawMenuButton(g2, btnRestartRect, "4. Reiniciar Partido");
            drawMenuButton(g2, btnExitRect, "5. Salir al Menú Principal");
        }

        private void drawResultsScreen(Graphics2D g2, int w, int h) {
            g2.setColor(new Color(10, 15, 25, 220));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(25, 35, 55));
            g2.fillRoundRect(220, 90, 460, 320, 20, 20);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(220, 90, 460, 320, 20, 20);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.drawString("¡FIN DEL PARTIDO!", 325, 145);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 38));
            String score = match.getScorePlayer1() + "   -   " + match.getScorePlayer2();
            g2.drawString(score, 390, 205);

            g2.setFont(new Font("Arial", Font.BOLD, 22));
            String ganador = match.getWinnerMessage();
            if (ganador.contains("VICTORIA PARA JUGADOR 1")) {
                g2.setColor(new Color(100, 255, 100));
            } else if (ganador.contains("VICTORIA PARA JUGADOR 2")) {
                g2.setColor(new Color(100, 195, 255));
            } else if (ganador.contains("VICTORIA PARA EL BOT")) {
                g2.setColor(new Color(255, 100, 100));
            } else {
                g2.setColor(new Color(255, 220, 100));
            }
            FontMetrics fm = g2.getFontMetrics();
            int gx = 220 + (460 - fm.stringWidth(ganador)) / 2;
            g2.drawString(ganador, gx, 255);

            drawActionButton(g2, btnPlayAgainRect, "Jugar de Nuevo [R]", new Color(40, 160, 60));
            drawActionButton(g2, btnQuitRect, "Menú Principal [Q]", new Color(180, 50, 50));
        }

        private void drawMenuButton(Graphics2D g2, Rectangle r, String text) {
            boolean hover = r.contains(mousePos);
            g2.setColor(hover ? new Color(50, 95, 160) : new Color(35, 50, 80));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.setColor(hover ? Color.YELLOW : Color.WHITE);
            g2.setStroke(new BasicStroke(hover ? 2 : 1));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(text, r.x + 25, r.y + 26);
        }

        private void drawActionButton(Graphics2D g2, Rectangle r, String text, Color baseColor) {
            boolean hover = r.contains(mousePos);
            g2.setColor(hover ? baseColor.brighter() : baseColor);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(hover ? 2 : 1));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(text, r.x + 15, r.y + 28);
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
                g2.drawLine(x + gw, y, x + gw, y + gh);
            } else {
                g2.drawLine(x, y, x, y + gh);
            }
        }

        private void drawCharacter(Graphics2D g2, Character ch, Color teamColor, String tag) {
            int x = (int) ch.getX();
            int y = (int) ch.getY();
            boolean right = ch.isFacingRight();

            // Sombra en el césped
            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillOval(x + 6, (int) Ball.GROUND_Y - 6, 60, 14);

            // Camiseta
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

            // Ojo
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

            // Sombra en césped
            g2.setColor(new Color(0, 0, 0, 75));
            g2.fillOval(bx + 2, groundY - 4, size - 4, 10);

            // Fuego si está activo el supertiro
            if (ball.isSuperShot()) {
                g2.setColor(new Color(255, 90, 0, 190));
                g2.fillOval(bx - 10, by - 10, size + 20, size + 20);
                g2.setColor(new Color(255, 215, 0, 200));
                g2.fillOval(bx - 5, by - 5, size + 10, size + 10);
            }

            // Balón
            g2.setColor(Color.WHITE);
            g2.fillOval(bx, by, size, size);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(bx, by, size, size);

            g2.fillOval(bx + 12, by + 12, 12, 12);
            g2.drawLine(bx + 18, by + 12, bx + 18, by + 2);
            g2.drawLine(bx + 12, by + 18, bx + 2, by + 14);
            g2.drawLine(bx + 24, by + 18, bx + 34, by + 14);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GameWindow window = new GameWindow();
                window.setVisible(true);
            }
        });
    }
}
