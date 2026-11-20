package dominio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * clase encargada de guardar y cargar partidas completas en archivos binarios
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class MatchPersistence {

    /**
     * guarda un partido en el archivo especificado
     * 
     * @param match el objeto matchmanager a guardar
     * @param file el archivo destino
     * @throws HeadSoccerException si ocurre error de escritura o parametros nulos
     */
    public static void guardar(MatchManager match, File file) throws HeadSoccerException {
        if (match == null) {
            throw new HeadSoccerException(HeadSoccerException.PARTIDO_NO_INICIADO);
        }
        if (file == null) {
            throw new HeadSoccerException("El archivo de destino no puede ser nulo.");
        }

        FileOutputStream fileOut = null;
        ObjectOutputStream objectOut = null;
        try {
            fileOut = new FileOutputStream(file);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(match);
            objectOut.flush();
        } catch (IOException e) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_GUARDAR, e);
        } finally {
            try {
                if (objectOut != null) {
                    objectOut.close();
                }
                if (fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * lee un archivo de partida y recupera el objeto matchmanager guardado
     * 
     * @param file el archivo a cargar
     * @return el objeto matchmanager reconstruido
     * @throws HeadSoccerException si el archivo no existe o no se puede leer
     */
    public static MatchManager cargar(File file) throws HeadSoccerException {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new HeadSoccerException(HeadSoccerException.ARCHIVO_NO_EXISTE);
        }

        FileInputStream fileIn = null;
        ObjectInputStream objectIn = null;
        try {
            fileIn = new FileInputStream(file);
            objectIn = new ObjectInputStream(fileIn);
            Object obj = objectIn.readObject();
            if (obj instanceof MatchManager) {
                return (MatchManager) obj;
            } else {
                throw new HeadSoccerException("El contenido del archivo no corresponde a una partida de Head Soccer.");
            }
        } catch (IOException e) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_ABRIR, e);
        } catch (ClassNotFoundException e) {
            throw new HeadSoccerException("Clase no encontrada al cargar la partida.", e);
        } finally {
            try {
                if (objectIn != null) {
                    objectIn.close();
                }
                if (fileIn != null) {
                    fileIn.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
