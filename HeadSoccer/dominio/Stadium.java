package dominio;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un estadio donde se disputa el partido de Head Soccer.
 * Define la ambientación visual (cielo, césped, gradas, valla publicitaria)
 * y está diseñado bajo un esquema extensible (OCP) para agregar nuevos escenarios fácilmente.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class Stadium implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STADIUM_METROPOLITANO = "Metropolitano";
    public static final String STADIUM_ALLIANZ = "Allianz Arena";
    public static final String STADIUM_ROSE_BOWL = "Rose Bowl";

    private final String name;
    private final String bannerText;
    private final Color skyTopColor;
    private final Color skyBottomColor;
    private final Color grassColor;
    private final Color standsColor;
    private final Color bannerColor;
    private final Color crowdPrimaryColor;
    private final Color crowdSecondaryColor;

    /** Registro extensible de estadios disponibles */
    private static final List<Stadium> REGISTERED_STADIUMS = new ArrayList<>();

    static {
        // 1. Estadio Metropolitano (Colombia) - Día soleado tropical y césped esmeralda
        registerStadium(new Stadium(
            STADIUM_METROPOLITANO,
            "ESTADIO METROPOLITANO  -  BARRANQUILLA",
            new Color(25, 115, 210),
            new Color(135, 200, 250),
            new Color(34, 155, 45),
            new Color(45, 55, 75),
            new Color(180, 130, 0),
            new Color(255, 210, 0),
            new Color(0, 70, 170)
        ));

        // 2. Allianz Arena (Alemania) - Noche de Champions iluminada
        registerStadium(new Stadium(
            STADIUM_ALLIANZ,
            "ALLIANZ ARENA  -  MÚNICH",
            new Color(12, 18, 45),
            new Color(38, 68, 125),
            new Color(26, 125, 40),
            new Color(28, 32, 48),
            new Color(165, 20, 35),
            new Color(230, 45, 45),
            new Color(240, 240, 245)
        ));

        // 3. Rose Bowl (USA) - Atardecer californiano
        registerStadium(new Stadium(
            STADIUM_ROSE_BOWL,
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
     * Constructor completo para definir un estadio personalizado.
     *
     * @param name Nombre corto del estadio.
     * @param bannerText Texto que se muestra en la valla publicitaria central.
     * @param skyTopColor Color superior del gradiente del cielo.
     * @param skyBottomColor Color inferior del gradiente del cielo.
     * @param grassColor Color base del césped.
     * @param standsColor Color de la estructura de las tribunas.
     * @param bannerColor Color de fondo de la valla publicitaria.
     * @param crowdPrimaryColor Color principal de los aficionados en la tribuna.
     * @param crowdSecondaryColor Color secundario de los aficionados en la tribuna.
     */
    public Stadium(
        String name,
        String bannerText,
        Color skyTopColor,
        Color skyBottomColor,
        Color grassColor,
        Color standsColor,
        Color bannerColor,
        Color crowdPrimaryColor,
        Color crowdSecondaryColor
    ) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : STADIUM_METROPOLITANO;
        this.bannerText = (bannerText != null && !bannerText.trim().isEmpty()) ? bannerText.trim() : "HEAD SOCCER ARENA";
        this.skyTopColor = skyTopColor != null ? skyTopColor : new Color(15, 25, 55);
        this.skyBottomColor = skyBottomColor != null ? skyBottomColor : new Color(45, 85, 145);
        this.grassColor = grassColor != null ? grassColor : new Color(34, 139, 34);
        this.standsColor = standsColor != null ? standsColor : new Color(30, 40, 65);
        this.bannerColor = bannerColor != null ? bannerColor : new Color(15, 65, 130);
        this.crowdPrimaryColor = crowdPrimaryColor != null ? crowdPrimaryColor : new Color(220, 65, 65);
        this.crowdSecondaryColor = crowdSecondaryColor != null ? crowdSecondaryColor : new Color(65, 150, 230);
    }

    /**
     * Registra un nuevo estadio en la lista global disponible para el menú.
     *
     * @param stadium Estadio a registrar.
     */
    public static void registerStadium(Stadium stadium) {
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
     * Devuelve la lista inmodificable de estadios registrados.
     *
     * @return Lista de estadios disponibles.
     */
    public static List<Stadium> getAvailableStadiums() {
        return Collections.unmodifiableList(REGISTERED_STADIUMS);
    }

    /**
     * Busca un estadio por su nombre.
     *
     * @param name Nombre del estadio.
     * @return Estadio encontrado.
     * @throws HeadSoccerException Si el nombre no corresponde a ningún estadio registrado.
     */
    public static Stadium getByName(String name) throws HeadSoccerException {
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

    /**
     * Obtiene el estadio por defecto (Metropolitano).
     *
     * @return Estadio por defecto.
     */
    public static Stadium getDefault() {
        return REGISTERED_STADIUMS.get(0);
    }

    public String getName() {
        return name;
    }

    public String getBannerText() {
        return bannerText;
    }

    public Color getSkyTopColor() {
        return skyTopColor;
    }

    public Color getSkyBottomColor() {
        return skyBottomColor;
    }

    public Color getGrassColor() {
        return grassColor;
    }

    public Color getStandsColor() {
        return standsColor;
    }

    public Color getBannerColor() {
        return bannerColor;
    }

    public Color getCrowdPrimaryColor() {
        return crowdPrimaryColor;
    }

    public Color getCrowdSecondaryColor() {
        return crowdSecondaryColor;
    }

    @Override
    public String toString() {
        return name;
    }
}
