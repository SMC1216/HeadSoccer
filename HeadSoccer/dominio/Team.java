package dominio;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un equipo o selección nacional que un jugador o bot puede elegir.
 * No necesita heredar de GameObject porque no es un cuerpo físico dentro de la cancha,
 * sino una entidad de configuración e identidad deportiva asociada por composición a Character.
 * Implementa Serializable para soportar la persistencia completa del partido.
 *
 * Diseño extensible (OCP): permite registrar o instanciar nuevos equipos en cualquier momento
 * sin modificar la lógica de los personajes ni del coordinador de partido.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class Team implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TEAM_COLOMBIA = "Colombia";
    public static final String TEAM_ALEMANIA = "Alemania";
    public static final String TEAM_USA = "USA";

    private final String name;
    private final String code;
    private final Color primaryShirtColor;
    private final Color secondaryColor;
    private final Color accentColor;

    /** Registro extensible de equipos disponibles en el juego */
    private static final List<Team> REGISTERED_TEAMS = new ArrayList<>();

    static {
        // Equipos oficiales solicitados: Alemania, Colombia y USA
        registerTeam(new Team(
            TEAM_COLOMBIA,
            "COL",
            new Color(255, 205, 0),   // Amarillo oro
            new Color(0, 56, 147),    // Azul bandera
            new Color(206, 17, 38)    // Rojo bandera
        ));
        registerTeam(new Team(
            TEAM_ALEMANIA,
            "GER",
            new Color(240, 240, 240), // Blanco clásico
            new Color(25, 25, 25),    // Negro
            new Color(221, 0, 0)      // Rojo/Dorado detalle
        ));
        registerTeam(new Team(
            TEAM_USA,
            "USA",
            new Color(20, 45, 115),   // Azul marino
            new Color(195, 30, 45),   // Rojo
            new Color(245, 245, 245)  // Blanco
        ));
    }

    /**
     * Constructor para crear un equipo con su identidad visual.
     *
     * @param name Nombre del equipo (ej. "Alemania", "Colombia", "USA").
     * @param code Código corto de 3 letras para el marcador (ej. "GER", "COL", "USA").
     * @param primaryShirtColor Color principal por defecto de la camiseta.
     * @param secondaryColor Color secundario (pantaloneta/cinta/bandera).
     * @param accentColor Color de acento o detalle.
     */
    public Team(String name, String code, Color primaryShirtColor, Color secondaryColor, Color accentColor) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : TEAM_COLOMBIA;
        this.code = (code != null && !code.trim().isEmpty()) ? code.trim().toUpperCase() : "COL";
        this.primaryShirtColor = primaryShirtColor != null ? primaryShirtColor : new Color(255, 205, 0);
        this.secondaryColor = secondaryColor != null ? secondaryColor : new Color(0, 56, 147);
        this.accentColor = accentColor != null ? accentColor : new Color(206, 17, 38);
    }

    /**
     * Registra un nuevo equipo en el catálogo global para extender el juego fácilmente.
     *
     * @param team Equipo a registrar.
     */
    public static void registerTeam(Team team) {
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
     * Devuelve la lista inmodificable de todos los equipos registrados.
     *
     * @return Lista de equipos disponibles.
     */
    public static List<Team> getAvailableTeams() {
        return Collections.unmodifiableList(REGISTERED_TEAMS);
    }

    /**
     * Obtiene un equipo registrado por su nombre (ej. "Alemania", "Colombia", "USA").
     *
     * @param name Nombre del equipo a buscar.
     * @return Instancia del equipo correspondiente.
     * @throws HeadSoccerException Si el nombre es nulo o no corresponde a un equipo registrado.
     */
    public static Team getByName(String name) throws HeadSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new HeadSoccerException(HeadSoccerException.EQUIPO_INVALIDO);
        }
        for (Team team : REGISTERED_TEAMS) {
            if (team.getName().equalsIgnoreCase(name.trim()) || team.getCode().equalsIgnoreCase(name.trim())) {
                return team;
            }
        }
        throw new HeadSoccerException(HeadSoccerException.EQUIPO_INVALIDO);
    }

    /**
     * Fábrica rápida del equipo Colombia.
     * @return Equipo Colombia.
     */
    public static Team colombia() {
        try {
            return getByName(TEAM_COLOMBIA);
        } catch (HeadSoccerException e) {
            return REGISTERED_TEAMS.get(0);
        }
    }

    /**
     * Fábrica rápida del equipo Alemania.
     * @return Equipo Alemania.
     */
    public static Team alemania() {
        try {
            return getByName(TEAM_ALEMANIA);
        } catch (HeadSoccerException e) {
            return REGISTERED_TEAMS.get(1);
        }
    }

    /**
     * Fábrica rápida del equipo USA.
     * @return Equipo USA.
     */
    public static Team usa() {
        try {
            return getByName(TEAM_USA);
        } catch (HeadSoccerException e) {
            return REGISTERED_TEAMS.get(2);
        }
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Color getPrimaryShirtColor() {
        return primaryShirtColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
