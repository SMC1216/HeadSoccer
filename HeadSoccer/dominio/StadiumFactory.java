package dominio;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fábrica de estadios (Patrón Factory).
 * Administra la creación y catálogo de escenarios deportivos disponibles.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class StadiumFactory {

    private static final List<Stadium> REGISTERED_STADIUMS = new ArrayList<>();

    static {
        register(new Stadium(
            Stadium.STADIUM_METROPOLITANO,
            "ESTADIO METROPOLITANO  -  BARRANQUILLA",
            new Color(25, 115, 210),
            new Color(135, 200, 250),
            new Color(34, 155, 45),
            new Color(45, 55, 75),
            new Color(180, 130, 0),
            new Color(255, 210, 0),
            new Color(0, 70, 170)
        ));
        register(new Stadium(
            Stadium.STADIUM_ALLIANZ,
            "ALLIANZ ARENA  -  MÚNICH",
            new Color(12, 18, 45),
            new Color(38, 68, 125),
            new Color(26, 125, 40),
            new Color(28, 32, 48),
            new Color(165, 20, 35),
            new Color(230, 45, 45),
            new Color(240, 240, 245)
        ));
        register(new Stadium(
            Stadium.STADIUM_ROSE_BOWL,
            "ROSE BOWL STADIUM  -  PASADENA USA",
            new Color(45, 25, 85),
            new Color(215, 105, 75),
            new Color(42, 142, 52),
            new Color(35, 42, 65),
            new Color(20, 50, 125),
            new Color(35, 85, 205),
            new Color(215, 45, 55)
        ));
    }

    /**
     * Registra un estadio en el catálogo de la fábrica.
     *
     * @param stadium Estadio a registrar.
     */
    public static void register(Stadium stadium) {
        if (stadium == null) {
            return;
        }
        for (int i = 0; i < REGISTERED_STADIUMS.size(); i++) {
            if (REGISTERED_STADIUMS.get(i).getName().equalsIgnoreCase(stadium.getName())) {
                REGISTERED_STADIUMS.set(i, stadium);
                return;
            }
        }
        REGISTERED_STADIUMS.add(stadium);
    }

    /**
     * Crea u obtiene el estadio solicitado por su nombre.
     *
     * @param name Nombre del estadio ("Metropolitano", "Allianz Arena", "Rose Bowl").
     * @return Estadio encontrado.
     * @throws HeadSoccerException Si el nombre no corresponde a un estadio válido.
     */
    public static Stadium create(String name) throws HeadSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new HeadSoccerException(HeadSoccerException.ESTADIO_INVALIDO);
        }
        for (Stadium s : REGISTERED_STADIUMS) {
            if (s.getName().equalsIgnoreCase(name.trim())) {
                return s;
            }
        }
        throw new HeadSoccerException(HeadSoccerException.ESTADIO_INVALIDO);
    }

    public static List<Stadium> getAvailableStadiums() {
        return Collections.unmodifiableList(REGISTERED_STADIUMS);
    }

    public static Stadium getDefault() {
        return REGISTERED_STADIUMS.get(0);
    }
}
