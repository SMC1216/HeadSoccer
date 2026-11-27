package dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fábrica de estrategias para el Bot (Patrón Factory + Strategy).
 * Permite crear cualquiera de los 4 niveles (Torpe, Ofensiva, Defensiva, Experta)
 * y registrar nuevos comportamientos sin romper el código existente.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class BotStrategyFactory {

    private static final Map<String, Supplier<BotStrategy>> CREATORS = new LinkedHashMap<>();

    static {
        register(BotStrategy.STRATEGY_TORPE, TorpeStrategy::new);
        register(BotStrategy.STRATEGY_OFENSIVA, OfensivaStrategy::new);
        register(BotStrategy.STRATEGY_DEFENSIVA, DefensivaStrategy::new);
        register(BotStrategy.STRATEGY_EXPERTA, ExpertaStrategy::new);
    }

    /**
     * Registra una nueva estrategia de IA en la fábrica.
     *
     * @param name Nombre del comportamiento.
     * @param creator Proveedor que instancia la estrategia.
     */
    public static void register(String name, Supplier<BotStrategy> creator) {
        if (name != null && !name.trim().isEmpty() && creator != null) {
            CREATORS.put(name.trim(), creator);
        }
    }

    /**
     * Crea una estrategia de Bot por su nombre ("Torpe", "Ofensiva", "Defensiva", "Experta").
     *
     * @param name Nombre de la estrategia.
     * @return Instancia de BotStrategy.
     * @throws HeadSoccerException Si el nombre no corresponde a ninguna estrategia registrada.
     */
    public static BotStrategy create(String name) throws HeadSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_INVALIDA);
        }
        String clean = name.trim();
        for (Map.Entry<String, Supplier<BotStrategy>> entry : CREATORS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(clean)) {
                return entry.getValue().get();
            }
        }
        throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_INVALIDA);
    }

    /**
     * Crea una estrategia a partir de su código numérico (1=Torpe, 2=Ofensiva, 3=Defensiva, 4=Experta).
     *
     * @param level Nivel numérico (1 a 4).
     * @return Instancia de BotStrategy correspondiente.
     * @throws HeadSoccerException Si el nivel está fuera del rango válido.
     */
    public static BotStrategy createByLevel(int level) throws HeadSoccerException {
        switch (level) {
            case BotAI.LEVEL_TORPE:
                return create(BotStrategy.STRATEGY_TORPE);
            case BotAI.LEVEL_OFENSIVA:
                return create(BotStrategy.STRATEGY_OFENSIVA);
            case BotAI.LEVEL_DEFENSIVA:
                return create(BotStrategy.STRATEGY_DEFENSIVA);
            case BotAI.LEVEL_EXPERTA:
                return create(BotStrategy.STRATEGY_EXPERTA);
            default:
                throw new HeadSoccerException(HeadSoccerException.DIFICULTAD_INVALIDA);
        }
    }

    /**
     * Devuelve la lista con los nombres de todas las estrategias registradas.
     *
     * @return Lista inmodificable de nombres de estrategias.
     */
    public static List<String> getAvailableNames() {
        return Collections.unmodifiableList(new ArrayList<>(CREATORS.keySet()));
    }
}
