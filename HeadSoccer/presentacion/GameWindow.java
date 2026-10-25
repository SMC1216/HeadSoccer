package presentacion;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Clase principal para la interfaz gráfica del juego Head Soccer.
 * Pertenece al paquete de presentación y maneja la ventana base.
 */
public class GameWindow extends JFrame {

    // Paneles para estructurar la vista
    private JPanel scorePanel;
    private JPanel fieldPanel;
    private JPanel controlPanel;

    // Etiquetas de información
    private JLabel scorePlayer1;
    private JLabel scorePlayer2;
    private JLabel timerLabel;

    // Controles
    private JButton pauseButton;

    /**
     * Constructor de la ventana. Inicializa y distribuye los componentes gráficos.
     */
    public GameWindow() {
        // Configuración básica de la ventana[cite: 3]
        setTitle("Head Soccer");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // 1. Zona Superior: Marcador y Temporizador[cite: 7]
        scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(1, 3)); // Divide el panel superior en 3 columnas iguales[cite: 3]
        scorePanel.setBackground(Color.LIGHT_GRAY);

        scorePlayer1 = new JLabel("Jugador 1: 0", SwingConstants.CENTER);
        timerLabel = new JLabel("Tiempo: 01:30", SwingConstants.CENTER);
        scorePlayer2 = new JLabel("Jugador 2: 0", SwingConstants.CENTER);

        scorePanel.add(scorePlayer1);
        scorePanel.add(timerLabel);
        scorePanel.add(scorePlayer2);
        add(scorePanel, BorderLayout.NORTH);

        // 2. Zona Central: Cancha de Juego[cite: 7]
        fieldPanel = new JPanel();
        fieldPanel.setBackground(new Color(34, 139, 34)); // Verde para simular el césped de la cancha
        fieldPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
        // En entregas futuras, aquí se sobreescribirá paintComponent(Graphics g) para dibujar a los personajes y el balón[cite: 7].
        add(fieldPanel, BorderLayout.CENTER);

        // 3. Zona Inferior: Controles o estado del juego[cite: 7]
        controlPanel = new JPanel();
        pauseButton = new JButton("Pausar Partido");
        controlPanel.add(pauseButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Centrar la ventana dinámicamente según el tamaño de la pantalla del monitor[cite: 3]
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        // Interceptar el evento de cierre de ventana usando un WindowAdapter[cite: 3]
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    /**
     * Muestra una ventana de diálogo (JOptionPane) para confirmar si el usuario desea salir[cite: 3].
     */
    private void confirmExit() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que deseas salir del partido?",
            "Confirmar salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            System.exit(0); // Cierra la ejecución del programa de forma segura[cite: 3]
        }
    }

    /**
     * Andamio de pruebas (Test Scaffold) para previsualizar la interfaz en BlueJ[cite: 3].
     * Haz clic derecho sobre la clase compilada y ejecuta main(String[] args) para verla.
     */
    public static void main(String[] args) {
        GameWindow window = new GameWindow();
        window.setVisible(true); // Hace que la ventana aparezca en pantalla[cite: 3]
    }
}