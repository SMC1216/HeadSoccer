package dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fábrica de poderes especiales (Patrón Factory).
 * Permite crear instancias nuevas de cualquier poder registrado y agregar
 * nuevos poderes en el futuro sin modificar las clases existentes.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class AbilityFactory {

    private static final Map<String, Supplier<Ability>> CREATORS = new LinkedHashMap<>();

    static {
        register(Ability.ABILITY_FIRE_STRIKE, FireStrike::new);
        register(Ability.ABILITY_GIANT_KICK, GiantKick::new);
        register(Ability.ABILITY_TELEPORT, Teleport::new);
    }

    /**
     * Registra un nuevo tipo de poder en la fábrica.
     *
     * @param name Nombre identificador del poder.
     * @param creator Constructor o proveedor que instancia el poder.
     */
    public static void register(String name, Supplier<Ability> creator) {
        if (name != null && !name.trim().isEmpty() && creator != null) {
            CREATORS.put(name.trim(), creator);
        }
    }

    /**
     * Crea una nueva instancia del poder solicitado por su nombre.
     *
     * @param name Nombre del poder ("FireStrike", "GiantKick", "Teleport").
     * @return Nueva instancia de la habilidad.
     * @throws HeadSoccerException Si el nombre es nulo o no está registrado.
     */
    public static Ability create(String name) throws HeadSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new HeadSoccerException(HeadSoccerException.PODER_INVALIDO);
        }
        String buscado = name.trim();
        for (Map.Entry<String, Supplier<Ability>> entry : CREATORS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(buscado)) {
                return entry.getValue().get();
            }
        }
        throw new HeadSoccerException(HeadSoccerException.PODER_INVALIDO);
    }

    /**
     * Devuelve la lista con los nombres de todos los poderes registrados.
     *
     * @return Lista inmodificable de nombres de poderes.
     */
    public static List<String> getAvailableNames() {
        return Collections.unmodifiableList(new ArrayList<>(CREATORS.keySet()));
    }
}
