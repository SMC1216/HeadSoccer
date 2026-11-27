package presentacion;

import dominio.Ability;
import dominio.Ball;
import dominio.BotAI;
import dominio.BotStrategy;
import dominio.Character;
import dominio.Goal;
import dominio.HeadSoccerException;
import dominio.MatchManager;
import dominio.PlayerStats;
import dominio.Stadium;
import dominio.Team;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana principal (Vista-Controlador) del juego Head Soccer.
 * Soporta redimensionamiento dinámico (maximizar y reducir ventana),
 * selección de estrategias del Bot (Torpe, Ofensiva, Defensiva, Experta),
 * tipo de partido por tiempo maleable o límite de goles,
 * mejoras de estadísticas del jugador (1 a 10 puntos),
 * equipos, colores de camiseta, poderes, auras y estadios.
 */
public class GameWindow extends JFrame {

    private MatchManager match;

    // Elementos de la barra de menú superior
    private JMenuItem itemNuevoBot, itemNuevoPvP, itemAbrir, itemSalvar, itemSalir, itemColor;
    private JRadioButtonMenuItem itemBotTorpe, itemBotOfensiva, itemBotDefensiva, itemBotExperta;

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

    // Timers para físicas (~50 FPS) y reloj de 1 segundo
    private Timer gameTimer;
    private Timer clockTimer;

    private final Set<Integer> pressedKeys = new HashSet<>();

    private Color customGrassColor = null;

    public GameWindow() {
        match = new MatchManager();
        match.returnToMenu();
        prepareElements();
        prepareActions();
    }

    /**
     * Inicializa los componentes visuales de la ventana permitiendo redimensionarla.
     */
    private void prepareElements() {
        setTitle("Head Soccer");
        setSize(920, 650);
        setMinimumSize(new Dimension(740, 520));
        setResizable(true);
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

        JMenu menuDificultad = new JMenu("Comportamiento Bot IA");
        ButtonGroup bgDif = new ButtonGroup();
        itemBotTorpe = new JRadioButtonMenuItem("Torpe");
        itemBotOfensiva = new JRadioButtonMenuItem("Ofensiva", true);
        itemBotDefensiva = new JRadioButtonMenuItem("Defensiva");
        itemBotExperta = new JRadioButtonMenuItem("Experta");
        bgDif.add(itemBotTorpe);
        bgDif.add(itemBotOfensiva);
        bgDif.add(itemBotDefensiva);
        bgDif.add(itemBotExperta);
        menuDificultad.add(itemBotTorpe);
        menuDificultad.add(itemBotOfensiva);
        menuDificultad.add(itemBotDefensiva);
        menuDificultad.add(itemBotExperta);
        menuEdicion.add(menuDificultad);

        menuBar.add(menuArchivo);
        menuBar.add(menuEdicion);
        setJMenuBar(menuBar);
    }

    private void prepareElementsScore() {
        hudPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        hudPanel.setBackground(new Color(20, 25, 40));
        hudPanel.setBorder(new EmptyBorder(8, 20, 8, 20));

        JPanel p1Box = new JPanel(new GridLayout(2, 1, 0, 4));
        p1Box.setOpaque(false);
        lblP1 = new JLabel("JUGADOR 1 [COL] (FireStrike)", JLabel.LEFT);
        lblP1.setForeground(new Color(255, 190, 90));
        lblP1.setFont(new Font("Arial", Font.BOLD, 13));
        specialBarP1 = new JProgressBar(0, 100);
        specialBarP1.setStringPainted(true);
        specialBarP1.setForeground(new Color(255, 140, 0));
        p1Box.add(lblP1);
        p1Box.add(specialBarP1);

        JPanel centerBox = new JPanel(new GridLayout(2, 1));
        centerBox.setOpaque(false);
        scoreLabel = new JLabel("0  -  0", JLabel.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 26));

        JPanel subCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        subCenter.setOpaque(false);
        timerLabel = new JLabel("Tiempo: 90s");
        timerLabel.setForeground(Color.YELLOW);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        stateLabel = new JLabel("[ MENÚ ]");
        stateLabel.setForeground(new Color(100, 200, 255));
        stateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        difficultyLabel = new JLabel("(Ofensiva)");
        difficultyLabel.setForeground(new Color(180, 200, 220));
        difficultyLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        subCenter.add(timerLabel);
        subCenter.add(stateLabel);
        subCenter.add(difficultyLabel);

        centerBox.add(scoreLabel);
        centerBox.add(subCenter);

        JPanel p2Box = new JPanel(new GridLayout(2, 1, 0, 4));
        p2Box.setOpaque(false);
        lblP2 = new JLabel("BOT AI [GER] (Teleport)", JLabel.RIGHT);
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
        if (match.isGoalLimitMode()) {
            timerLabel.setText("Meta: " + match.getGoalLimit() + " Goles (" + match.getElapsedTime() + "s)");
        } else {
            timerLabel.setText("Tiempo: " + match.getTimeRemaining() + "s");
        }

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
        if (dif == BotAI.LEVEL_TORPE) {
            itemBotTorpe.setSelected(true);
        } else if (dif == BotAI.LEVEL_DEFENSIVA) {
            itemBotDefensiva.setSelected(true);
        } else if (dif == BotAI.LEVEL_EXPERTA) {
            itemBotExperta.setSelected(true);
        } else {
            itemBotOfensiva.setSelected(true);
        }

        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();
        String p1AbName = (p1.getSpecialAbility() != null) ? p1.getSpecialAbility().getName() : "FireStrike";
        String p2AbName = (p2.getSpecialAbility() != null) ? p2.getSpecialAbility().getName() : "Teleport";
        String p1TeamCode = p1.getTeam().getCode();
        String p2TeamCode = p2.getTeam().getCode();

        lblP1.setText("JUGADOR 1 [" + p1TeamCode + "] (" + p1AbName + ")");
        if (p1.getSpecialAbility() != null) {
            specialBarP1.setForeground(p1.getSpecialAbility().getAuraColor());
        }

        String stadName = match.getStadium().getName();
        if (dosJugadores) {
            difficultyLabel.setText("(2J | " + stadName + ")");
            lblP2.setText("JUGADOR 2 [" + p2TeamCode + "] (" + p2AbName + ")");
            infoLabel.setText(
                "J1: [A/D] Mover [W] Saltar [ESPACIO] Patear [E] Poder   ||   " +
                "J2: [←/→] Mover [↑] Saltar [ENTER/↓] Patear [M/SHIFT] Poder   ||   [ESC] Pausa"
            );
        } else {
            String botStrat = match.getBotStrategyName();
            difficultyLabel.setText("(Bot " + botStrat + " | " + stadName + ")");
            lblP2.setText("BOT AI [" + p2TeamCode + "] (" + p2AbName + ")");
            infoLabel.setText(
                "[A / D] o [← / →] Mover  |  [W / ↑] Saltar  |  [ESPACIO] Patear  |  [E] Poder Especial  |  [ESC] Menú de Pausa"
            );
        }
        if (p2.getSpecialAbility() != null) {
            specialBarP2.setForeground(p2.getSpecialAbility().getAuraColor());
        }

        int sp1 = p1.getSpecialMeter();
        specialBarP1.setValue(sp1);
        boolean p1Ready = p1.getSpecialAbility() != null && p1.getSpecialAbility().isReady();
        if (p1.isAuraActive()) {
            specialBarP1.setString("¡AURA " + p1AbName.toUpperCase() + " ACTIVA!");
        } else if (sp1 >= 100 && p1Ready) {
            specialBarP1.setString("¡LISTO! (Presiona E)");
        } else if (!p1Ready && p1.getSpecialAbility() != null) {
            specialBarP1.setString("Enfriando: " + p1.getSpecialAbility().getCooldownRemaining() + "s");
        } else {
            specialBarP1.setString("Poder: " + sp1 + "%");
        }

        int sp2 = p2.getSpecialMeter();
        specialBarP2.setValue(sp2);
        boolean p2Ready = p2.getSpecialAbility() != null && p2.getSpecialAbility().isReady();
        if (p2.isAuraActive()) {
            specialBarP2.setString("¡AURA " + p2AbName.toUpperCase() + " ACTIVA!");
        } else if (dosJugadores) {
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

        itemBotTorpe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.setDifficultyLevel(BotAI.LEVEL_TORPE);
                refresh();
            }
        });
        itemBotOfensiva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.setDifficultyLevel(BotAI.LEVEL_OFENSIVA);
                refresh();
            }
        });
        itemBotDefensiva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.setDifficultyLevel(BotAI.LEVEL_DEFENSIVA);
                refresh();
            }
        });
        itemBotExperta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.setDifficultyLevel(BotAI.LEVEL_EXPERTA);
                refresh();
            }
        });
    }

    private void actualizarMovimientoContinuo() {
        if (!MatchManager.STATE_PLAYING.equals(match.getMatchState())) {
            return;
        }

        boolean dosJugadores = match.isTwoPlayerMode();

        if (dosJugadores) {
            boolean p1Left = pressedKeys.contains(KeyEvent.VK_A);
            boolean p1Right = pressedKeys.contains(KeyEvent.VK_D);
            if (p1Left && !p1Right) {
                match.getPlayer1().handleInput("LEFT", match.getBall());
            } else if (p1Right && !p1Left) {
                match.getPlayer1().handleInput("RIGHT", match.getBall());
            } else {
                match.getPlayer1().handleInput("STOP", match.getBall());
            }

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
                // Ignorar en modo contra Bot
            }
        } else {
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

                if (MatchManager.STATE_MENU.equals(match.getMatchState())) {
                    if (code == KeyEvent.VK_1) iniciarModoJuego(MatchManager.MODE_PVE);
                    else if (code == KeyEvent.VK_2) iniciarModoJuego(MatchManager.MODE_PVP);
                    else if (code == KeyEvent.VK_3) abrirJuego();
                    return;
                }

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
                    if (code == KeyEvent.VK_W) {
                        match.getPlayer1().handleInput("JUMP", match.getBall());
                    } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_S) {
                        match.getPlayer1().handleInput("KICK", match.getBall());
                    } else if (code == KeyEvent.VK_E) {
                        match.getPlayer1().handleInput("SPECIAL", match.getBall());
                    }

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

        gameTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarMovimientoContinuo();
                match.updateFrame();
                refresh();
            }
        });
        gameTimer.start();

        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                match.decrementTimer();
                refresh();
            }
        });
        clockTimer.start();
    }

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
        Color actual = customGrassColor != null ? customGrassColor : match.getStadium().getGrassColor();
        Color nuevoColor = JColorChooser.showDialog(this, "Selecciona el color del césped", actual);
        if (nuevoColor != null) {
            customGrassColor = nuevoColor;
            refresh();
        }
    }

    private void elegirColorCamisetaPersonalizado(boolean esJugador1) {
        Color actual = esJugador1 ? match.getPlayer1().getShirtColor() : match.getPlayer2().getShirtColor();
        String titulo = esJugador1 ? "Color de Camiseta - Jugador 1" : "Color de Camiseta - Jugador 2 / Bot";
        Color elegido = JColorChooser.showDialog(this, titulo, actual);
        if (elegido != null) {
            try {
                if (esJugador1) {
                    match.setPlayer1ShirtColor(elegido);
                } else {
                    match.setPlayer2ShirtColor(elegido);
                }
                refresh();
            } catch (HeadSoccerException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        boardPanel.requestFocusInWindow();
    }

    /**
     * Abre una ventana sencilla para consultar y mejorar las estadísticas del jugador (1 a 10)
     * o reiniciar sus puntos de mejora.
     */
    private void abrirVentanaEstadisticas(final boolean esJugador1) {
        final PlayerStats stats = esJugador1 ? match.getPlayer1Stats() : match.getPlayer2Stats();
        String nombre = esJugador1 ? "Jugador 1" : "Jugador 2 / Bot";

        final JDialog dialog = new JDialog(this, "Estadísticas y Mejoras - " + nombre, true);
        dialog.setSize(420, 310);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        final JLabel lblPuntos = new JLabel("Puntos disponibles para mejorar: " + stats.getAvailablePoints(), JLabel.CENTER);
        lblPuntos.setFont(new Font("Arial", Font.BOLD, 14));
        lblPuntos.setBorder(new EmptyBorder(10, 10, 0, 10));
        dialog.add(lblPuntos, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 3, 8, 10));
        centerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        final JLabel lblVel = new JLabel();
        final JLabel lblSal = new JLabel();
        final JLabel lblPat = new JLabel();
        final JLabel lblPod = new JLabel();

        final JProgressBar barVel = new JProgressBar(PlayerStats.MIN_STAT, PlayerStats.MAX_STAT);
        final JProgressBar barSal = new JProgressBar(PlayerStats.MIN_STAT, PlayerStats.MAX_STAT);
        final JProgressBar barPat = new JProgressBar(PlayerStats.MIN_STAT, PlayerStats.MAX_STAT);
        final JProgressBar barPod = new JProgressBar(PlayerStats.MIN_STAT, PlayerStats.MAX_STAT);

        final JButton btnMasVel = new JButton("+ Mejorar");
        final JButton btnMasSal = new JButton("+ Mejorar");
        final JButton btnMasPat = new JButton("+ Mejorar");
        final JButton btnMasPod = new JButton("+ Mejorar");

        final Runnable actualizarVista = new Runnable() {
            @Override
            public void run() {
                lblPuntos.setText("Puntos disponibles para mejorar: " + stats.getAvailablePoints());
                lblVel.setText("Velocidad: " + stats.getSpeed() + " / 10");
                lblSal.setText("Salto: " + stats.getJump() + " / 10");
                lblPat.setText("Patada: " + stats.getKick() + " / 10");
                lblPod.setText("Poder: " + stats.getPower() + " / 10");

                barVel.setValue(stats.getSpeed());
                barSal.setValue(stats.getJump());
                barPat.setValue(stats.getKick());
                barPod.setValue(stats.getPower());

                boolean hayPuntos = stats.getAvailablePoints() > 0;
                btnMasVel.setEnabled(hayPuntos && stats.getSpeed() < PlayerStats.MAX_STAT);
                btnMasSal.setEnabled(hayPuntos && stats.getJump() < PlayerStats.MAX_STAT);
                btnMasPat.setEnabled(hayPuntos && stats.getKick() < PlayerStats.MAX_STAT);
                btnMasPod.setEnabled(hayPuntos && stats.getPower() < PlayerStats.MAX_STAT);
                refresh();
            }
        };

        btnMasVel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (esJugador1) match.upgradePlayer1Stat(PlayerStats.STAT_SPEED);
                    else match.upgradePlayer2Stat(PlayerStats.STAT_SPEED);
                    actualizarVista.run();
                } catch (HeadSoccerException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage());
                }
            }
        });
        btnMasSal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (esJugador1) match.upgradePlayer1Stat(PlayerStats.STAT_JUMP);
                    else match.upgradePlayer2Stat(PlayerStats.STAT_JUMP);
                    actualizarVista.run();
                } catch (HeadSoccerException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage());
                }
            }
        });
        btnMasPat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (esJugador1) match.upgradePlayer1Stat(PlayerStats.STAT_KICK);
                    else match.upgradePlayer2Stat(PlayerStats.STAT_KICK);
                    actualizarVista.run();
                } catch (HeadSoccerException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage());
                }
            }
        });
        btnMasPod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (esJugador1) match.upgradePlayer1Stat(PlayerStats.STAT_POWER);
                    else match.upgradePlayer2Stat(PlayerStats.STAT_POWER);
                    actualizarVista.run();
                } catch (HeadSoccerException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage());
                }
            }
        });

        centerPanel.add(lblVel); centerPanel.add(barVel); centerPanel.add(btnMasVel);
        centerPanel.add(lblSal); centerPanel.add(barSal); centerPanel.add(btnMasSal);
        centerPanel.add(lblPat); centerPanel.add(barPat); centerPanel.add(btnMasPat);
        centerPanel.add(lblPod); centerPanel.add(barPod); centerPanel.add(btnMasPod);
        dialog.add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnReiniciar = new JButton("Reiniciar Puntos");
        JButton btnCerrar = new JButton("Aceptar");

        btnReiniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (esJugador1) match.resetPlayer1Stats();
                else match.resetPlayer2Stats();
                actualizarVista.run();
            }
        });
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        southPanel.add(btnReiniciar);
        southPanel.add(btnCerrar);
        dialog.add(southPanel, BorderLayout.SOUTH);

        actualizarVista.run();
        dialog.setVisible(true);
        boardPanel.requestFocusInWindow();
    }

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

    public void salir() {
        volverAlMenuPrincipal();
    }

    /**
     * Lienzo interactivo que escala automáticamente todo el campo y los menús
     * cuando la ventana se maximiza o se reduce.
     */
    private class FieldCanvas extends JPanel {
        private static final int BASE_WIDTH = 900;
        private static final int BASE_HEIGHT = 510;

        // Columna Central: Botones principales de modo de juego
        private final Rectangle btnModeBotRect = new Rectangle(327, 94, 246, 36);
        private final Rectangle btnModePvPRect = new Rectangle(327, 135, 246, 36);
        private final Rectangle btnModeLoadRect = new Rectangle(327, 176, 246, 32);

        // Columna Central: Estrategias del Bot (Torpe, Ofensiva, Defensiva, Experta)
        private final Rectangle btnBotTorpeRect = new Rectangle(325, 232, 120, 25);
        private final Rectangle btnBotOfensivaRect = new Rectangle(453, 232, 120, 25);
        private final Rectangle btnBotDefensivaRect = new Rectangle(325, 261, 120, 25);
        private final Rectangle btnBotExpertaRect = new Rectangle(453, 261, 120, 25);

        // Columna Central: Tipo de Partido (Por Tiempo o Por Goles) y ajuste maleable
        private final Rectangle btnRuleTimeRect = new Rectangle(325, 306, 120, 24);
        private final Rectangle btnRuleGoalsRect = new Rectangle(453, 306, 120, 24);
        private final Rectangle btnValueMinusRect = new Rectangle(335, 334, 42, 24);
        private final Rectangle btnValuePlusRect = new Rectangle(523, 334, 42, 24);

        // Columna Izquierda (Jugador 1): Equipo, Camiseta, Poder y Estadísticas
        private final Rectangle p1TeamColRect = new Rectangle(58, 122, 74, 25);
        private final Rectangle p1TeamGerRect = new Rectangle(136, 122, 74, 25);
        private final Rectangle p1TeamUsaRect = new Rectangle(214, 122, 74, 25);

        private final Rectangle[] p1ColorSwatches = new Rectangle[] {
            new Rectangle(58, 168, 25, 22),
            new Rectangle(87, 168, 25, 22),
            new Rectangle(116, 168, 25, 22),
            new Rectangle(145, 168, 25, 22),
            new Rectangle(174, 168, 25, 22)
        };
        private final Rectangle p1CustomColorRect = new Rectangle(205, 168, 83, 22);

        private final Rectangle p1AbFireRect = new Rectangle(58, 210, 230, 27);
        private final Rectangle p1AbGiantRect = new Rectangle(58, 241, 230, 27);
        private final Rectangle p1AbTeleRect = new Rectangle(58, 272, 230, 27);
        private final Rectangle p1StatsBtnRect = new Rectangle(58, 326, 230, 28);

        // Columna Derecha (Jugador 2 / Bot AI): Equipo, Camiseta, Poder y Estadísticas
        private final Rectangle p2TeamColRect = new Rectangle(612, 122, 74, 25);
        private final Rectangle p2TeamGerRect = new Rectangle(690, 122, 74, 25);
        private final Rectangle p2TeamUsaRect = new Rectangle(768, 122, 74, 25);

        private final Rectangle[] p2ColorSwatches = new Rectangle[] {
            new Rectangle(612, 168, 25, 22),
            new Rectangle(641, 168, 25, 22),
            new Rectangle(670, 168, 25, 22),
            new Rectangle(699, 168, 25, 22),
            new Rectangle(728, 168, 25, 22)
        };
        private final Rectangle p2CustomColorRect = new Rectangle(759, 168, 83, 22);

        private final Rectangle p2AbFireRect = new Rectangle(612, 210, 230, 27);
        private final Rectangle p2AbGiantRect = new Rectangle(612, 241, 230, 27);
        private final Rectangle p2AbTeleRect = new Rectangle(612, 272, 230, 27);
        private final Rectangle p2StatsBtnRect = new Rectangle(612, 326, 230, 28);

        // Fila Inferior: Selección de Estadio
        private final Rectangle btnStadMetroRect = new Rectangle(65, 405, 240, 60);
        private final Rectangle btnStadAllianzRect = new Rectangle(330, 405, 240, 60);
        private final Rectangle btnStadRoseRect = new Rectangle(595, 405, 240, 60);

        private final Color[] presetShirtColors = new Color[] {
            new Color(255, 205, 0),
            new Color(240, 240, 240),
            new Color(20, 45, 115),
            new Color(220, 35, 45),
            new Color(30, 180, 85)
        };

        // Botones del menú de pausa
        private final Rectangle btnResumeRect = new Rectangle(330, 175, 240, 42);
        private final Rectangle btnSaveRect = new Rectangle(330, 230, 240, 42);
        private final Rectangle btnLoadRect = new Rectangle(330, 285, 240, 42);
        private final Rectangle btnRestartRect = new Rectangle(330, 340, 240, 42);
        private final Rectangle btnExitRect = new Rectangle(330, 395, 240, 42);

        // Botones de fin de partido
        private final Rectangle btnPlayAgainRect = new Rectangle(255, 340, 185, 46);
        private final Rectangle btnQuitRect = new Rectangle(460, 340, 195, 46);

        private Point mousePos = new Point(-1, -1);

        public FieldCanvas() {
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePos = toLogicalPoint(e.getPoint());
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    Point p = toLogicalPoint(e.getPoint());

                    if (MatchManager.STATE_MENU.equals(match.getMatchState())) {
                        handleMainMenuClick(p);
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

        /**
         * Convierte las coordenadas reales del ratón a coordenadas lógicas (900x510)
         * para que todos los botones funcionen al maximizar o reducir la ventana.
         */
        private Point toLogicalPoint(Point raw) {
            int cw = Math.max(1, getWidth());
            int ch = Math.max(1, getHeight());
            int lx = (int) (raw.x * (BASE_WIDTH / (double) cw));
            int ly = (int) (raw.y * (BASE_HEIGHT / (double) ch));
            return new Point(lx, ly);
        }

        private void handleMainMenuClick(Point p) {
            try {
                if (btnModeBotRect.contains(p)) {
                    iniciarModoJuego(MatchManager.MODE_PVE);
                    return;
                } else if (btnModePvPRect.contains(p)) {
                    iniciarModoJuego(MatchManager.MODE_PVP);
                    return;
                } else if (btnModeLoadRect.contains(p)) {
                    abrirJuego();
                    return;
                }

                // Estrategias del Bot (Torpe, Ofensiva, Defensiva, Experta)
                if (btnBotTorpeRect.contains(p)) {
                    match.setBotStrategy(BotStrategy.STRATEGY_TORPE);
                } else if (btnBotOfensivaRect.contains(p)) {
                    match.setBotStrategy(BotStrategy.STRATEGY_OFENSIVA);
                } else if (btnBotDefensivaRect.contains(p)) {
                    match.setBotStrategy(BotStrategy.STRATEGY_DEFENSIVA);
                } else if (btnBotExpertaRect.contains(p)) {
                    match.setBotStrategy(BotStrategy.STRATEGY_EXPERTA);
                }

                // Tipo de partido (Por Tiempo o Por Goles) y botones - / +
                if (btnRuleTimeRect.contains(p)) {
                    match.setMatchRule(MatchManager.RULE_TIME);
                } else if (btnRuleGoalsRect.contains(p)) {
                    match.setMatchRule(MatchManager.RULE_GOALS);
                } else if (btnValueMinusRect.contains(p)) {
                    if (match.isGoalLimitMode()) {
                        match.setGoalLimit(Math.max(1, match.getGoalLimit() - 1));
                    } else {
                        match.setConfiguredDuration(Math.max(15, match.getConfiguredDuration() - 15));
                    }
                } else if (btnValuePlusRect.contains(p)) {
                    if (match.isGoalLimitMode()) {
                        match.setGoalLimit(Math.min(25, match.getGoalLimit() + 1));
                    } else {
                        match.setConfiguredDuration(Math.min(300, match.getConfiguredDuration() + 15));
                    }
                }

                // Jugador 1: Equipo, color, poder y mejoras de estadísticas
                if (p1TeamColRect.contains(p)) {
                    match.setPlayer1Team(Team.TEAM_COLOMBIA);
                } else if (p1TeamGerRect.contains(p)) {
                    match.setPlayer1Team(Team.TEAM_ALEMANIA);
                } else if (p1TeamUsaRect.contains(p)) {
                    match.setPlayer1Team(Team.TEAM_USA);
                }

                for (int i = 0; i < p1ColorSwatches.length; i++) {
                    if (p1ColorSwatches[i].contains(p)) {
                        match.setPlayer1ShirtColor(presetShirtColors[i]);
                    }
                }
                if (p1CustomColorRect.contains(p)) {
                    elegirColorCamisetaPersonalizado(true);
                    return;
                }

                if (p1AbFireRect.contains(p)) {
                    match.setPlayer1Ability(Ability.ABILITY_FIRE_STRIKE);
                } else if (p1AbGiantRect.contains(p)) {
                    match.setPlayer1Ability(Ability.ABILITY_GIANT_KICK);
                } else if (p1AbTeleRect.contains(p)) {
                    match.setPlayer1Ability(Ability.ABILITY_TELEPORT);
                } else if (p1StatsBtnRect.contains(p)) {
                    abrirVentanaEstadisticas(true);
                    return;
                }

                // Jugador 2 / Bot: Equipo, color, poder y mejoras de estadísticas
                if (p2TeamColRect.contains(p)) {
                    match.setPlayer2Team(Team.TEAM_COLOMBIA);
                } else if (p2TeamGerRect.contains(p)) {
                    match.setPlayer2Team(Team.TEAM_ALEMANIA);
                } else if (p2TeamUsaRect.contains(p)) {
                    match.setPlayer2Team(Team.TEAM_USA);
                }

                for (int i = 0; i < p2ColorSwatches.length; i++) {
                    if (p2ColorSwatches[i].contains(p)) {
                        match.setPlayer2ShirtColor(presetShirtColors[i]);
                    }
                }
                if (p2CustomColorRect.contains(p)) {
                    elegirColorCamisetaPersonalizado(false);
                    return;
                }

                if (p2AbFireRect.contains(p)) {
                    match.setPlayer2Ability(Ability.ABILITY_FIRE_STRIKE);
                } else if (p2AbGiantRect.contains(p)) {
                    match.setPlayer2Ability(Ability.ABILITY_GIANT_KICK);
                } else if (p2AbTeleRect.contains(p)) {
                    match.setPlayer2Ability(Ability.ABILITY_TELEPORT);
                } else if (p2StatsBtnRect.contains(p)) {
                    abrirVentanaEstadisticas(false);
                    return;
                }

                // Selección de Estadio
                if (btnStadMetroRect.contains(p)) {
                    match.setStadium(Stadium.STADIUM_METROPOLITANO);
                    customGrassColor = null;
                } else if (btnStadAllianzRect.contains(p)) {
                    match.setStadium(Stadium.STADIUM_ALLIANZ);
                    customGrassColor = null;
                } else if (btnStadRoseRect.contains(p)) {
                    match.setStadium(Stadium.STADIUM_ROSE_BOWL);
                    customGrassColor = null;
                }

                refresh();
            } catch (HeadSoccerException ex) {
                JOptionPane.showMessageDialog(GameWindow.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Escalar proporcionalmente todo el lienzo al tamaño actual de la ventana
            double scaleX = getWidth() / (double) BASE_WIDTH;
            double scaleY = getHeight() / (double) BASE_HEIGHT;
            g2.scale(scaleX, scaleY);

            int w = BASE_WIDTH;
            int h = BASE_HEIGHT;
            int groundY = (int) Ball.GROUND_Y;
            Stadium stadium = match.getStadium();

            // 1. Cielo del estadio
            GradientPaint sky = new GradientPaint(0, 0, stadium.getSkyTopColor(), 0, groundY, stadium.getSkyBottomColor());
            g2.setPaint(sky);
            g2.fillRect(0, 0, w, groundY);

            g2.setColor(new Color(255, 255, 220, 45));
            g2.fillPolygon(new int[]{80, 0, 260}, new int[]{0, groundY - 110, groundY - 110}, 3);
            g2.fillPolygon(new int[]{w - 80, w - 260, w}, new int[]{0, groundY - 110, groundY - 110}, 3);

            g2.setColor(stadium.getStandsColor());
            g2.fillRect(0, groundY - 110, w, 110);
            for (int row = 0; row < 4; row++) {
                for (int col = 20; col < w - 20; col += 24) {
                    g2.setColor((col + row) % 2 == 0 ? stadium.getCrowdPrimaryColor() : stadium.getCrowdSecondaryColor());
                    g2.fillOval(col, groundY - 96 + (row * 20), 12, 12);
                }
            }

            g2.setColor(stadium.getBannerColor());
            g2.fillRect(0, groundY - 24, w, 24);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String banner = "HEAD SOCCER  -  " + stadium.getBannerText();
            FontMetrics fmBanner = g2.getFontMetrics();
            g2.drawString(banner, (w - fmBanner.stringWidth(banner)) / 2, groundY - 7);

            // 2. Césped
            Color baseGrass = customGrassColor != null ? customGrassColor : stadium.getGrassColor();
            Color stripeBright = baseGrass.brighter();
            int stripeWidth = Math.max(1, w / 10);
            for (int i = 0; i < 11; i++) {
                g2.setColor(i % 2 == 0 ? baseGrass : stripeBright);
                g2.fillRect(i * stripeWidth, groundY, stripeWidth, h - groundY);
            }

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(0, groundY, w, groundY);
            g2.drawLine(w / 2, groundY, w / 2, h);
            g2.drawOval(w / 2 - 50, groundY + 10, 100, 45);

            // 3. Porterías
            drawGoal(g2, match.getLeftGoal(), true);
            drawGoal(g2, match.getRightGoal(), false);

            // 4. Personajes
            String p1Tag = "J1 [" + match.getPlayer1().getTeam().getCode() + "]";
            String p2Prefix = match.isTwoPlayerMode() ? "J2" : "BOT";
            String p2Tag = p2Prefix + " [" + match.getPlayer2().getTeam().getCode() + "]";
            drawCharacter(g2, match.getPlayer1(), p1Tag);
            drawCharacter(g2, match.getPlayer2(), p2Tag);

            // 5. Balón
            drawBall(g2, match.getBall(), groundY);

            // 6. Overlays de estado
            if (MatchManager.STATE_MENU.equals(match.getMatchState())) {
                drawMainMenu(g2, w, h);
            }
            if (MatchManager.STATE_PAUSED.equals(match.getMatchState())) {
                drawPauseMenu(g2, w, h);
            }
            if (MatchManager.STATE_FINISHED.equals(match.getMatchState())) {
                drawResultsScreen(g2, w, h);
            }

            g2.dispose();
        }

        private void drawMainMenu(Graphics2D g2, int w, int h) {
            g2.setColor(new Color(8, 12, 22, 220));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(18, 26, 44));
            g2.fillRoundRect(30, 14, 840, 476, 22, 22);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(30, 14, 840, 476, 22, 22);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            String title = "HEAD SOCCER 2D  -  MENÚ DE INICIO";
            FontMetrics fmTitle = g2.getFontMetrics();
            g2.drawString(title, (w - fmTitle.stringWidth(title)) / 2, 42);

            g2.setColor(new Color(190, 210, 235));
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            String sub = "Configura poderes, equipos, camisetas, mejoras (1-10), tipo de juego y estadio";
            FontMetrics fmSub = g2.getFontMetrics();
            g2.drawString(sub, (w - fmSub.stringWidth(sub)) / 2, 61);

            // Columna Izquierda: Jugador 1
            drawPlayerConfigPanel(
                g2,
                46,
                72,
                254,
                290,
                "JUGADOR 1 (Izquierda)",
                match.getPlayer1(),
                match.getPlayer1Stats(),
                p1TeamColRect,
                p1TeamGerRect,
                p1TeamUsaRect,
                p1ColorSwatches,
                p1CustomColorRect,
                p1AbFireRect,
                p1AbGiantRect,
                p1AbTeleRect,
                p1StatsBtnRect
            );

            // Columna Central: Modo, Estrategia del Bot y Tipo de Partido
            g2.setColor(new Color(26, 38, 62));
            g2.fillRoundRect(312, 72, 276, 290, 16, 16);
            g2.setColor(new Color(90, 130, 195));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(312, 72, 276, 290, 16, 16);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.drawString("INICIAR PARTIDA", 394, 88);

            drawMenuButton(g2, btnModeBotRect, "1. Jugar vs Bot (Modo PVE)");
            drawMenuButton(g2, btnModePvPRect, "2. Jugar 2 Jugadores (PVP)");
            drawMenuButton(g2, btnModeLoadRect, "3. Cargar Partida Guardada");

            g2.setColor(new Color(210, 225, 245));
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("Comportamiento del Bot (PVE):", 362, 226);

            String botStrat = match.getBotStrategyName();
            drawSelectablePill(g2, btnBotTorpeRect, "Torpe", BotStrategy.STRATEGY_TORPE.equalsIgnoreCase(botStrat), new Color(45, 155, 75));
            drawSelectablePill(g2, btnBotOfensivaRect, "Ofensiva", BotStrategy.STRATEGY_OFENSIVA.equalsIgnoreCase(botStrat), new Color(45, 155, 75));
            drawSelectablePill(g2, btnBotDefensivaRect, "Defensiva", BotStrategy.STRATEGY_DEFENSIVA.equalsIgnoreCase(botStrat), new Color(45, 155, 75));
            drawSelectablePill(g2, btnBotExpertaRect, "Experta", BotStrategy.STRATEGY_EXPERTA.equalsIgnoreCase(botStrat), new Color(45, 155, 75));

            g2.setColor(new Color(210, 225, 245));
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("Tipo de Juego (Tiempo / Goles):", 360, 300);

            boolean porGoles = match.isGoalLimitMode();
            drawSelectablePill(g2, btnRuleTimeRect, "Por Tiempo", !porGoles, new Color(45, 125, 195));
            drawSelectablePill(g2, btnRuleGoalsRect, "Por Goles", porGoles, new Color(45, 125, 195));

            drawSelectablePill(g2, btnValueMinusRect, "-", false, new Color(60, 80, 120));
            drawSelectablePill(g2, btnValuePlusRect, "+", false, new Color(60, 80, 120));

            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String ruleVal = porGoles ? ("Meta: " + match.getGoalLimit() + " goles") : ("Tiempo: " + match.getConfiguredDuration() + "s");
            FontMetrics fmRule = g2.getFontMetrics();
            g2.drawString(ruleVal, 450 - (fmRule.stringWidth(ruleVal) / 2), 350);

            // Columna Derecha: Jugador 2 / Bot AI
            drawPlayerConfigPanel(
                g2,
                600,
                72,
                254,
                290,
                "JUGADOR 2 / BOT AI (Derecha)",
                match.getPlayer2(),
                match.getPlayer2Stats(),
                p2TeamColRect,
                p2TeamGerRect,
                p2TeamUsaRect,
                p2ColorSwatches,
                p2CustomColorRect,
                p2AbFireRect,
                p2AbGiantRect,
                p2AbTeleRect,
                p2StatsBtnRect
            );

            // Fila Inferior: Selección de Estadio
            g2.setColor(new Color(26, 38, 62));
            g2.fillRoundRect(46, 368, 808, 108, 16, 16);
            g2.setColor(new Color(90, 130, 195));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(46, 368, 808, 108, 16, 16);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.drawString("SELECCIONA EL ESTADIO DEL PARTIDO:", 320, 392);

            String currentStad = match.getStadium().getName();
            drawStadiumCard(
                g2,
                btnStadMetroRect,
                "Metropolitano",
                "Barranquilla (COL) - Día Soleado",
                new Color(34, 155, 45),
                new Color(255, 205, 0),
                Stadium.STADIUM_METROPOLITANO.equalsIgnoreCase(currentStad)
            );
            drawStadiumCard(
                g2,
                btnStadAllianzRect,
                "Allianz Arena",
                "Múnich (GER) - Noche Europea",
                new Color(26, 125, 40),
                new Color(220, 40, 50),
                Stadium.STADIUM_ALLIANZ.equalsIgnoreCase(currentStad)
            );
            drawStadiumCard(
                g2,
                btnStadRoseRect,
                "Rose Bowl",
                "Pasadena (USA) - Atardecer",
                new Color(42, 142, 52),
                new Color(215, 105, 75),
                Stadium.STADIUM_ROSE_BOWL.equalsIgnoreCase(currentStad)
            );
        }

        private void drawPlayerConfigPanel(
            Graphics2D g2,
            int x,
            int y,
            int pw,
            int ph,
            String headerTitle,
            Character ch,
            PlayerStats stats,
            Rectangle teamColRect,
            Rectangle teamGerRect,
            Rectangle teamUsaRect,
            Rectangle[] colorSwatches,
            Rectangle customColorRect,
            Rectangle abFireRect,
            Rectangle abGiantRect,
            Rectangle abTeleRect,
            Rectangle statsBtnRect
        ) {
            g2.setColor(new Color(26, 38, 62));
            g2.fillRoundRect(x, y, pw, ph, 16, 16);
            g2.setColor(new Color(90, 130, 195));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, pw, ph, 16, 16);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(headerTitle, x + 14, y + 20);

            Color shirt = ch.getShirtColor();
            g2.setColor(shirt);
            g2.fillRoundRect(x + pw - 42, y + 7, 28, 18, 6, 6);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x + pw - 42, y + 7, 28, 18, 6, 6);

            String teamName = ch.getTeam().getName();
            g2.setColor(new Color(210, 225, 245));
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("Equipo: " + teamName + " [" + ch.getTeam().getCode() + "]", x + 14, y + 42);

            drawSelectablePill(g2, teamColRect, "Colombia", Team.TEAM_COLOMBIA.equalsIgnoreCase(teamName), new Color(215, 165, 0));
            drawSelectablePill(g2, teamGerRect, "Alemania", Team.TEAM_ALEMANIA.equalsIgnoreCase(teamName), new Color(80, 90, 110));
            drawSelectablePill(g2, teamUsaRect, "USA", Team.TEAM_USA.equalsIgnoreCase(teamName), new Color(35, 75, 165));

            g2.setColor(new Color(210, 225, 245));
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("Color de Camiseta:", x + 14, y + 90);

            for (int i = 0; i < colorSwatches.length; i++) {
                Rectangle sr = colorSwatches[i];
                Color c = presetShirtColors[i];
                g2.setColor(c);
                g2.fillRoundRect(sr.x, sr.y, sr.width, sr.height, 6, 6);
                boolean sameColor = c.equals(shirt);
                boolean hover = sr.contains(mousePos);
                g2.setColor(sameColor ? Color.YELLOW : (hover ? Color.WHITE : Color.DARK_GRAY));
                g2.setStroke(new BasicStroke(sameColor ? 2.5f : 1.2f));
                g2.drawRoundRect(sr.x, sr.y, sr.width, sr.height, 6, 6);
            }
            drawSelectablePill(g2, customColorRect, "Editar Color", false, new Color(60, 85, 130));

            String currentAb = ch.getSpecialAbility() != null ? ch.getSpecialAbility().getName() : Ability.ABILITY_FIRE_STRIKE;
            g2.setColor(new Color(210, 225, 245));
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("Poder Especial y Aura:", x + 14, y + 132);

            drawAbilityOption(
                g2,
                abFireRect,
                "FireStrike (Tiro de Fuego)",
                new Color(255, 69, 0),
                Ability.ABILITY_FIRE_STRIKE.equalsIgnoreCase(currentAb)
            );
            drawAbilityOption(
                g2,
                abGiantRect,
                "GiantKick (Patada Gigante)",
                new Color(50, 235, 100),
                Ability.ABILITY_GIANT_KICK.equalsIgnoreCase(currentAb)
            );
            drawAbilityOption(
                g2,
                abTeleRect,
                "Teleport (Teletransporte)",
                new Color(0, 220, 255),
                Ability.ABILITY_TELEPORT.equalsIgnoreCase(currentAb)
            );

            // Resumen y botón de mejoras de estadísticas (1 a 10)
            g2.setColor(new Color(185, 220, 255));
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            String statSummary = "VEL:" + stats.getSpeed() + "  SAL:" + stats.getJump() +
                                 "  PAT:" + stats.getKick() + "  POD:" + stats.getPower();
            g2.drawString(statSummary, x + 16, y + 245);

            drawSelectablePill(
                g2,
                statsBtnRect,
                "Mejorar Estadísticas (Pts: " + stats.getAvailablePoints() + ")",
                stats.getAvailablePoints() > 0,
                new Color(140, 85, 20)
            );
        }

        private void drawAbilityOption(Graphics2D g2, Rectangle r, String label, Color auraDotColor, boolean selected) {
            boolean hover = r.contains(mousePos);
            if (selected) {
                g2.setColor(new Color(45, 75, 125));
            } else {
                g2.setColor(hover ? new Color(40, 60, 98) : new Color(20, 30, 50));
            }
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);

            g2.setColor(selected ? auraDotColor : (hover ? Color.WHITE : new Color(110, 130, 165)));
            g2.setStroke(new BasicStroke(selected ? 2.2f : 1.0f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);

            g2.setColor(new Color(auraDotColor.getRed(), auraDotColor.getGreen(), auraDotColor.getBlue(), 95));
            g2.fillOval(r.x + 8, r.y + 4, 20, 20);
            g2.setColor(auraDotColor);
            g2.fillOval(r.x + 12, r.y + 8, 12, 12);

            g2.setColor(selected ? Color.YELLOW : Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString(label, r.x + 34, r.y + 18);
        }

        private void drawStadiumCard(
            Graphics2D g2,
            Rectangle r,
            String name,
            String subtitle,
            Color grassPreview,
            Color accentPreview,
            boolean selected
        ) {
            boolean hover = r.contains(mousePos);
            if (selected) {
                g2.setColor(new Color(40, 72, 118));
            } else {
                g2.setColor(hover ? new Color(36, 56, 92) : new Color(20, 30, 50));
            }
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);

            g2.setColor(accentPreview);
            g2.fillRoundRect(r.x + 10, r.y + 10, 34, 20, 4, 4);
            g2.setColor(grassPreview);
            g2.fillRoundRect(r.x + 10, r.y + 28, 34, 22, 4, 4);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(r.x + 10, r.y + 10, 34, 40, 4, 4);

            g2.setColor(selected ? Color.YELLOW : (hover ? Color.WHITE : new Color(130, 150, 185)));
            g2.setStroke(new BasicStroke(selected ? 2.5f : 1.2f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

            g2.setColor(selected ? Color.YELLOW : Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(name, r.x + 54, r.y + 26);

            g2.setColor(new Color(195, 210, 230));
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.drawString(subtitle, r.x + 54, r.y + 45);
        }

        private void drawSelectablePill(Graphics2D g2, Rectangle r, String text, boolean selected, Color activeBg) {
            boolean hover = r.contains(mousePos);
            if (selected) {
                g2.setColor(activeBg);
            } else {
                g2.setColor(hover ? new Color(55, 85, 135) : new Color(32, 45, 70));
            }
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            g2.setColor(selected ? Color.YELLOW : (hover ? Color.WHITE : new Color(145, 165, 195)));
            g2.setStroke(new BasicStroke(selected ? 2.0f : 1.0f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
            int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);
        }

        private void drawPauseMenu(Graphics2D g2, int w, int h) {
            g2.setColor(new Color(10, 15, 25, 210));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(25, 35, 55));
            g2.fillRoundRect(290, 80, 320, 380, 20, 20);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(290, 80, 320, 380, 20, 20);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 26));
            g2.drawString("PARTIDO EN PAUSA", 325, 130);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(new Color(180, 195, 215));
            g2.drawString("Presiona ESC para continuar", 370, 152);

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
            g2.fillRoundRect(200, 90, 500, 320, 20, 20);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(200, 90, 500, 320, 20, 20);

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.drawString("¡FIN DEL PARTIDO!", 325, 145);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 38));
            String score = match.getScorePlayer1() + "   -   " + match.getScorePlayer2();
            g2.drawString(score, 390, 205);

            g2.setFont(new Font("Arial", Font.BOLD, 20));
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
            int gx = 200 + (500 - fm.stringWidth(ganador)) / 2;
            g2.drawString(ganador, gx, 250);

            g2.setColor(new Color(200, 220, 255));
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            String bonusMsg = "¡Has ganado puntos de mejora para tus estadísticas!";
            FontMetrics fmBonus = g2.getFontMetrics();
            g2.drawString(bonusMsg, 200 + (500 - fmBonus.stringWidth(bonusMsg)) / 2, 285);

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

            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, r.x + 16, ty);
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

            g2.setColor(new Color(235, 235, 235, 130));
            g2.setStroke(new BasicStroke(1));
            for (int gx = x; gx <= x + gw; gx += 12) {
                g2.drawLine(gx, y, gx, y + gh);
            }
            for (int gy = y; gy <= y + gh; gy += 12) {
                g2.drawLine(x, gy, x + gw, gy);
            }

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(5));
            g2.drawLine(x, y, x + gw, y);
            if (isLeft) {
                g2.drawLine(x + gw, y, x + gw, y + gh);
            } else {
                g2.drawLine(x, y, x, y + gh);
            }
        }

        private void drawCharacter(Graphics2D g2, Character ch, String tag) {
            int x = (int) ch.getX();
            int y = (int) ch.getY();
            boolean right = ch.isFacingRight();
            Color shirtColor = ch.getShirtColor();
            Team team = ch.getTeam();

            if (ch.isAuraActive()) {
                Color aura = ch.getAuraColor();
                int pulse = (int) (Math.sin(ch.getAuraTickCounter() * 0.35) * 6);

                g2.setColor(new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 75));
                g2.fillOval(x - 22 - pulse, y - 20 - pulse, 114 + (pulse * 2), 124 + (pulse * 2));

                g2.setColor(new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 135));
                g2.fillOval(x - 12, y - 10, 94, 106);

                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x - 14 - (pulse / 2), y - 12 - (pulse / 2), 98 + pulse, 110 + pulse);

                g2.setColor(aura.brighter());
                int offset = (ch.getAuraTickCounter() * 4) % 24;
                g2.fillOval(x - 16, y + 60 - offset, 10, 18);
                g2.fillOval(x + 76, y + 55 - offset, 10, 18);
                g2.fillOval(x + 30, y - 18 - (offset / 2), 12, 16);
            }

            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillOval(x + 6, (int) Ball.GROUND_Y - 6, 60, 14);

            g2.setColor(shirtColor);
            g2.fillRoundRect(x + 20, y + 54, 32, 24, 8, 8);
            g2.setColor(team.getSecondaryColor());
            g2.fillRect(x + 22, y + 68, 28, 8);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x + 20, y + 54, 32, 24, 8, 8);

            int shoeX = right ? x + 40 : x + 4;
            g2.setColor(new Color(35, 35, 35));
            g2.fillRoundRect(shoeX, y + 74, 28, 16, 8, 8);
            g2.setColor(team.getAccentColor());
            g2.drawLine(shoeX + 5, y + 82, shoeX + 20, y + 82);

            g2.setColor(new Color(255, 218, 185));
            g2.fillOval(x, y, 70, 60);
            g2.setColor(Color.BLACK);
            g2.drawOval(x, y, 70, 60);

            g2.setColor(shirtColor);
            g2.fillRect(x + 4, y + 12, 62, 10);
            g2.setColor(team.getAccentColor());
            g2.fillRect(x + 4, y + 19, 62, 3);
            g2.setColor(Color.BLACK);
            g2.drawRect(x + 4, y + 12, 62, 10);

            int eyeX = right ? x + 45 : x + 12;
            g2.setColor(Color.WHITE);
            g2.fillOval(eyeX, y + 25, 13, 13);
            g2.setColor(Color.BLACK);
            g2.fillOval(right ? eyeX + 5 : eyeX + 2, y + 28, 6, 6);
            g2.drawOval(eyeX, y + 25, 13, 13);

            g2.setColor(ch.isAuraActive() ? ch.getAuraColor().brighter() : Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (70 - fm.stringWidth(tag)) / 2;
            g2.drawString(tag, tx, y - 6);
        }

        private void drawBall(Graphics2D g2, Ball ball, int groundY) {
            int bx = (int) ball.getX();
            int by = (int) ball.getY();
            int size = ball.getWidth();

            g2.setColor(new Color(0, 0, 0, 75));
            g2.fillOval(bx + 2, groundY - 4, size - 4, 10);

            if (ball.isSuperShot()) {
                g2.setColor(new Color(255, 90, 0, 190));
                g2.fillOval(bx - 10, by - 10, size + 20, size + 20);
                g2.setColor(new Color(255, 215, 0, 200));
                g2.fillOval(bx - 5, by - 5, size + 10, size + 10);
            }

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
