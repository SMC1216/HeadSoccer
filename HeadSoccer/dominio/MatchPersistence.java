package dominio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * clase que se encarga de guardar y abrir partidas en archivos binarios
 * usamos objectoutputstream y objectinputstream con serializable
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class MatchPersistence {

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
