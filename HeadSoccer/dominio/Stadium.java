package dominio;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

/**
 * Representa un estadio donde se disputa el partido de Head Soccer.
 * Define la ambientación visual y delega su catálogo a StadiumFactory.
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

    public static void registerStadium(Stadium stadium) {
        StadiumFactory.register(stadium);
    }

    public static List<Stadium> getAvailableStadiums() {
        return StadiumFactory.getAvailableStadiums();
    }

    public static Stadium getByName(String name) throws HeadSoccerException {
        return StadiumFactory.create(name);
    }

    public static Stadium getDefault() {
        return StadiumFactory.getDefault();
    }

    public String getName() { return name; }
    public String getBannerText() { return bannerText; }
    public Color getSkyTopColor() { return skyTopColor; }
    public Color getSkyBottomColor() { return skyBottomColor; }
    public Color getGrassColor() { return grassColor; }
    public Color getStandsColor() { return standsColor; }
    public Color getBannerColor() { return bannerColor; }
    public Color getCrowdPrimaryColor() { return crowdPrimaryColor; }
    public Color getCrowdSecondaryColor() { return crowdSecondaryColor; }

    @Override
    public String toString() {
        return name;
    }
}
