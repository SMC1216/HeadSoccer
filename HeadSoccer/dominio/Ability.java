package dominio;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Clase abstracta para las habilidades especiales de los personajes.
 * Implementa el patrón Strategy y administra el ciclo de enfriamiento (cooldown),
 * la duración activa de los efectos y el color del aura que emite el personaje al activarla.
 */
public abstract class Ability implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ABILITY_FIRE_STRIKE = "FireStrike";
    public static final String ABILITY_GIANT_KICK = "GiantKick";
    public static final String ABILITY_TELEPORT = "Teleport";

    protected int cooldown; // Tiempo total de enfriamiento en segundos
    protected int duration; // Duración total del efecto activo en segundos
    protected int cooldownRemaining; // Segundos restantes de enfriamiento
    protected int durationRemaining; // Segundos restantes del efecto
    protected boolean isActive;
    protected Color auraColor; // Color del aura del personaje al activar este poder

    /**
     * Constructor de la habilidad con color de aura por defecto (dorado).
     * @param cooldown Tiempo de enfriamiento en segundos tras su activación.
     * @param duration Duración del efecto en segundos.
     */
    public Ability(int cooldown, int duration) {
        this(cooldown, duration, new Color(255, 215, 0));
    }

    /**
     * Constructor de la habilidad con su color de aura característico.
     * @param cooldown Tiempo de enfriamiento en segundos tras su activación.
     * @param duration Duración del efecto en segundos.
     * @param auraColor Color del aura que rodea al jugador cuando activa este poder.
     */
    public Ability(int cooldown, int duration, Color auraColor) {
        this.cooldown = cooldown;
        this.duration = duration;
        this.cooldownRemaining = 0;
        this.durationRemaining = 0;
        this.isActive = false;
        this.auraColor = auraColor != null ? auraColor : new Color(255, 215, 0);
    }

    /**
     * Devuelve la lista de nombres de poderes disponibles para seleccionar en el menú.
     * Facilita la extensibilidad bajo el principio Abierto/Cerrado (OCP).
     * @return Lista de identificadores de poderes disponibles.
     */
    public static List<String> getAvailableAbilityNames() {
        return AbilityFactory.getAvailableNames();
    }

    /**
     * Fábrica extensible para crear una instancia fresca de una habilidad a partir de su nombre.
     * @param name Nombre de la habilidad ("FireStrike", "GiantKick", "Teleport").
     * @return Nueva instancia de la habilidad solicitada.
     * @throws HeadSoccerException Si el nombre es nulo o no corresponde a un poder válido.
     */
    public static Ability createByName(String name) throws HeadSoccerException {
        return AbilityFactory.create(name);
    }

    /**
     * Activa el efecto de la habilidad sobre el lanzador y/o el balón,
     * encendiendo también el aura del color del poder en el personaje lanzador.
     * @param caster El personaje que invoca la habilidad.
     * @param ball El balón sobre el cual se puede aplicar la fuerza o efecto.
     */
    public void execute(Character caster, Ball ball) {
        if (isReady()) {
            this.isActive = true;
            this.durationRemaining = this.duration;
            this.cooldownRemaining = this.cooldown;
            activate(caster, ball);
            if (caster != null) {
                caster.activatePowerAura(this.auraColor);
            }
        }
    }

    /**
     * Lógica específica de la habilidad que define cada subclase concreta.
     */
    protected abstract void activate(Character caster, Ball ball);

    /**
     * Actualiza el temporizador de enfriamiento y duración (llamado cada segundo de partido).
     */
    public void updateTick() {
        if (durationRemaining > 0) {
            durationRemaining--;
            if (durationRemaining == 0) {
                isActive = false;
            }
        }
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
        }
    }

    /**
     * Indica si la habilidad está disponible para ser usada (sin tiempo de enfriamiento).
     * @return true si el enfriamiento ha terminado, false si aún está recargando.
     */
    public boolean isReady() {
        return cooldownRemaining <= 0;
    }

    /**
     * Reinicia el estado de enfriamiento y duración de la habilidad.
     */
    public void reset() {
        this.cooldownRemaining = 0;
        this.durationRemaining = 0;
        this.isActive = false;
    }

    /**
     * Devuelve el nombre identificador de la habilidad.
     * @return Nombre simple de la habilidad.
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    // Getters y Setters
    public Color getAuraColor() { return auraColor; }
    public void setAuraColor(Color auraColor) {
        if (auraColor != null) {
            this.auraColor = auraColor;
        }
    }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public int getCooldown() { return cooldown; }
    public void setCooldown(int cooldown) { this.cooldown = cooldown; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getCooldownRemaining() { return cooldownRemaining; }
    public void setCooldownRemaining(int cooldownRemaining) { this.cooldownRemaining = cooldownRemaining; }
    public int getDurationRemaining() { return durationRemaining; }
}
