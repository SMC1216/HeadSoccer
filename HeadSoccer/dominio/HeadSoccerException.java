package dominio;

/**
 * excepcion personalizada del juego para reportar errores de archivo y de partida
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class HeadSoccerException extends Exception {
    private static final long serialVersionUID = 1L;

    /** mensaje cuando falla el guardado */
    public static final String ERROR_GUARDAR = "Error al intentar guardar el estado del partido.";

    /** mensaje cuando falla la carga */
    public static final String ERROR_ABRIR = "Error al intentar cargar el archivo del partido.";

    /** mensaje cuando el archivo buscado no existe */
    public static final String ARCHIVO_NO_EXISTE = "El archivo especificado no existe o no se puede leer.";

    /** mensaje cuando se intenta una accion sin haber iniciado el juego */
    public static final String PARTIDO_NO_INICIADO = "El partido no ha sido iniciado correctamente.";
    public static final String MODO_JUEGO_INVALIDO = "El modo de juego seleccionado no es válido.";
    public static final String JUGADOR_NO_HUMANO = "El Jugador 2 no es controlado por un usuario en el modo contra el Bot.";
    public static final String ACCION_INVALIDA = "La acción solicitada para el jugador no es válida.";
    public static final String DIFICULTAD_INVALIDA = "El nivel de dificultad especificado no es válido.";
    public static final String PERSONAJE_INVALIDO = "Los datos del personaje son inválidos o nulos.";
    public static final String EQUIPO_INVALIDO = "El equipo seleccionado no es válido o no existe.";
    public static final String ESTADIO_INVALIDO = "El estadio seleccionado no es válido o no existe.";
    public static final String PODER_INVALIDO = "El poder especial seleccionado no es válido o no existe.";
    public static final String COLOR_INVALIDO = "El color de camiseta especificado no puede ser nulo.";
    public static final String REGLA_JUEGO_INVALIDA = "El tipo de partido seleccionado no es válido (debe ser TIME o GOALS).";
    public static final String TIEMPO_INVALIDO = "El tiempo configurado para el partido debe ser mayor a cero.";
    public static final String LIMITE_GOLES_INVALIDO = "El límite de goles para el partido debe ser al menos 1.";
    public static final String ESTADISTICA_INVALIDA = "No es posible mejorar la estadística (límite máximo 10 alcanzado o sin puntos disponibles).";

    /**
     * constructor simple con mensaje descriptivo
     * 
     * @param message texto del error
     */
    public HeadSoccerException(String message) {
        super(message);
    }

    /**
     * constructor con mensaje y la excepcion original que causo el fallo
     * 
     * @param message texto del error
     * @param cause excepcion original de java
     */
    public HeadSoccerException(String message, Throwable cause) {
        super(message, cause);
    }
}
