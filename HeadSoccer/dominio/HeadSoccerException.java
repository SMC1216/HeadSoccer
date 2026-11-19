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
