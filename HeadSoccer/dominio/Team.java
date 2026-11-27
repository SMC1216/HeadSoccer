package dominio;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

/**
 * Representa un equipo o selección nacional que un jugador o bot puede elegir.
 * Se asocia por composición a Character y su creación está delegada a TeamFactory.
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

    public static void registerTeam(Team team) {
        TeamFactory.register(team);
    }

    public static List<Team> getAvailableTeams() {
        return TeamFactory.getAvailableTeams();
    }

    public static Team getByName(String name) throws HeadSoccerException {
        return TeamFactory.create(name);
    }

    public static Team colombia() {
        try {
            return TeamFactory.create(TEAM_COLOMBIA);
        } catch (HeadSoccerException e) {
            return TeamFactory.getAvailableTeams().get(0);
        }
    }

    public static Team alemania() {
        try {
            return TeamFactory.create(TEAM_ALEMANIA);
        } catch (HeadSoccerException e) {
            return TeamFactory.getAvailableTeams().get(1);
        }
    }

    public static Team usa() {
        try {
            return TeamFactory.create(TEAM_USA);
        } catch (HeadSoccerException e) {
            return TeamFactory.getAvailableTeams().get(2);
        }
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public Color getPrimaryShirtColor() { return primaryShirtColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public Color getAccentColor() { return accentColor; }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
