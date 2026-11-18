package dominio;

/**
 * Excepción propia del dominio para el juego Head Soccer.
 * Permite manejar situaciones anómalas como errores de persistencia,
 * archivos corruptos o estados inválidos del partido.
 */
public class HeadSoccerException extends Exception {

    public static final String ERROR_GUARDAR = "Error al intentar guardar el estado del partido.";
    public static final String ERROR_ABRIR = "Error al intentar cargar el archivo del partido.";
    public static final String ARCHIVO_NO_EXISTE = "El archivo especificado no existe o no se puede leer.";
    public static final String PARTIDO_NO_INICIADO = "El partido no ha sido iniciado correctamente.";

    /**
     * Constructor con mensaje de detalle.
     * @param message Descripción del error producido.
     */
    public HeadSoccerException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa subyacente.
     * @param message Descripción del error.
     * @param cause Causa original de la excepción.
     */
    public HeadSoccerException(String message, Throwable cause) {
        super(message, cause);
    }
}
