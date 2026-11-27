package dominio;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fábrica de equipos (Patrón Factory).
 * Centraliza la creación y el registro de selecciones nacionales para que
 * agregar un nuevo equipo sea sencillo sin cambiar la lógica del juego.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class TeamFactory {

    private static final List<Team> REGISTERED_TEAMS = new ArrayList<>();

    static {
        register(new Team(
            Team.TEAM_COLOMBIA,
            "COL",
            new Color(255, 205, 0),
            new Color(0, 56, 147),
            new Color(206, 17, 38)
        ));
        register(new Team(
            Team.TEAM_ALEMANIA,
            "GER",
            new Color(240, 240, 240),
            new Color(25, 25, 25),
            new Color(221, 0, 0)
        ));
        register(new Team(
            Team.TEAM_USA,
            "USA",
            new Color(20, 45, 115),
            new Color(195, 30, 45),
            new Color(245, 245, 245)
        ));
    }

    /**
     * Registra un nuevo equipo en la fábrica.
     *
     * @param team Instancia del equipo a registrar.
     */
    public static void register(Team team) {
        if (team == null) {
            return;
        }
        for (int i = 0; i < REGISTERED_TEAMS.size(); i++) {
            if (REGISTERED_TEAMS.get(i).getName().equalsIgnoreCase(team.getName())) {
                REGISTERED_TEAMS.set(i, team);
                return;
            }
        }
        REGISTERED_TEAMS.add(team);
    }

    /**
     * Obtiene o crea un equipo a partir de su nombre o código.
     *
     * @param name Nombre o código del equipo (ej. "Colombia", "Alemania", "USA").
     * @return Equipo correspondiente.
     * @throws HeadSoccerException Si el equipo no existe.
     */
    public static Team create(String name) throws HeadSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new HeadSoccerException(HeadSoccerException.EQUIPO_INVALIDO);
        }
        String clean = name.trim();
        for (Team t : REGISTERED_TEAMS) {
            if (t.getName().equalsIgnoreCase(clean) || t.getCode().equalsIgnoreCase(clean)) {
                return new Team(
                    t.getName(),
                    t.getCode(),
                    t.getPrimaryShirtColor(),
                    t.getSecondaryColor(),
                    t.getAccentColor()
                );
            }
        }
        throw new HeadSoccerException(HeadSoccerException.EQUIPO_INVALIDO);
    }

    /**
     * Devuelve la lista de equipos disponibles en la fábrica.
     *
     * @return Lista inmodificable de equipos.
     */
    public static List<Team> getAvailableTeams() {
        return Collections.unmodifiableList(REGISTERED_TEAMS);
    }

    /**
     * Devuelve los nombres de todos los equipos disponibles.
     *
     * @return Lista de nombres de equipos.
     */
    public static List<String> getAvailableNames() {
        List<String> names = new ArrayList<>();
        for (Team t : REGISTERED_TEAMS) {
            names.add(t.getName());
        }
        return Collections.unmodifiableList(names);
    }
}
