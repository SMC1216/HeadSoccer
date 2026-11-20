package dominio;

import java.io.Serializable;

/**
 * Clase abstracta para las habilidades especiales de los personajes.
 * Implementa el patrón Strategy y administra el ciclo de enfriamiento (cooldown)
 * y la duración activa de los efectos.
 */
public abstract class Ability implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int cooldown; // Tiempo total de enfriamiento en segundos
    protected int duration; // Duración total del efecto activo en segundos
    protected int cooldownRemaining; // Segundos restantes de enfriamiento
    protected int durationRemaining; // Segundos restantes del efecto
    protected boolean isActive;

    /**
     * Constructor de la habilidad.
     * @param cooldown Tiempo de enfriamiento en segundos tras su activación.
     * @param duration Duración del efecto en segundos.
     */
    public Ability(int cooldown, int duration) {
        this.cooldown = cooldown;
        this.duration = duration;
        this.cooldownRemaining = 0;
        this.durationRemaining = 0;
        this.isActive = false;
    }

    /**
     * Activa el efecto de la habilidad sobre el lanzador y/o el balón.
     * @param caster El personaje que invoca la habilidad.
     * @param ball El balón sobre el cual se puede aplicar la fuerza o efecto.
     */
    public void execute(Character caster, Ball ball) {
        if (isReady()) {
            this.isActive = true;
            this.durationRemaining = this.duration;
            this.cooldownRemaining = this.cooldown;
            activate(caster, ball);
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

    // Getters y Setters
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
