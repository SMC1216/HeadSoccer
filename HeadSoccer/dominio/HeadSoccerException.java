package dominio;

/**
 * excepcion propia del juego para reportar errores
 * se usa principalmente al guardar y cargar archivos de partida
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class HeadSoccerException extends Exception {
    private static final long serialVersionUID = 1L;

    public static final String ERROR_GUARDAR = "Error al intentar guardar el estado del partido.";
    public static final String ERROR_ABRIR = "Error al intentar cargar el archivo del partido.";
    public static final String ARCHIVO_NO_EXISTE = "El archivo especificado no existe o no se puede leer.";
    public static final String PARTIDO_NO_INICIADO = "El partido no ha sido iniciado correctamente.";

    public HeadSoccerException(String message) {
        super(message);
    }

    public HeadSoccerException(String message, Throwable cause) {
        super(message, cause);
    }
}
