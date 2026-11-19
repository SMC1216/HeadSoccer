package dominio;

import java.io.Serializable;

/**
 * clase base para todas las habilidades especiales de los personajes
 * 
 * @author gaby-mathias
 * @version 2026
 */
public abstract class Ability implements Serializable {
    private static final long serialVersionUID = 1L;

    /** tiempo total de enfriamiento en segundos */
    protected int cooldown;

    /** duracion del efecto en segundos */
    protected int duration;

    /** segundos que faltan para poder volver a usar la habilidad */
    protected int cooldownRemaining;

    /** segundos que le quedan al efecto activo */
    protected int durationRemaining;

    /** indica si la habilidad se encuentra activa en este momento */
    protected boolean isActive;

    /**
     * constructor para inicializar los tiempos de la habilidad
     * 
     * @param cooldown tiempo de recarga en segundos
     * @param duration duracion del efecto en segundos
     */
    public Ability(int cooldown, int duration) {
        this.cooldown = cooldown;
        this.duration = duration;
        this.cooldownRemaining = 0;
        this.durationRemaining = 0;
        this.isActive = false;
    }

    /**
     * ejecuta la habilidad si ya no esta en tiempo de recarga
     * 
     * @param caster el personaje que usa el poder
     * @param ball el balon de juego
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
     * metodo abstracto que cada habilidad especifica debe programar
     * 
     * @param caster el personaje que lanza la habilidad
     * @param ball el balon de juego
     */
    protected abstract void activate(Character caster, Ball ball);

    /**
     * resta un segundo a la duracion y al cooldown cada segundo de partido
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
     * verifica si la habilidad esta lista para ser usada
     * 
     * @return true si el cooldown llego a cero, false si aun recarga
     */
    public boolean isReady() {
        return cooldownRemaining <= 0;
    }

    /**
     * consulta si la habilidad esta activa
     * 
     * @return true si esta activa
     */
    public boolean isActive() { 
        return isActive; 
    }
    
    /**
     * cambia el estado activo de la habilidad
     * 
     * @param active nuevo estado
     */
    public void setActive(boolean active) { 
        this.isActive = active; 
    }
    
    /**
     * obtiene el tiempo de cooldown total
     * 
     * @return segundos de cooldown
     */
    public int getCooldown() { 
        return cooldown; 
    }
    
    /**
     * cambia el tiempo de cooldown total
     * 
     * @param cooldown nuevos segundos
     */
    public void setCooldown(int cooldown) { 
        this.cooldown = cooldown; 
    }
    
    /**
     * obtiene la duracion del efecto
     * 
     * @return segundos de duracion
     */
    public int getDuration() { 
        return duration; 
    }
    
    /**
     * cambia la duracion del efecto
     * 
     * @param duration nuevos segundos
     */
    public void setDuration(int duration) { 
        this.duration = duration; 
    }
    
    /**
     * obtiene los segundos restantes de cooldown
     * 
     * @return segundos que faltan
     */
    public int getCooldownRemaining() { 
        return cooldownRemaining; 
    }
    
    /**
     * modifica los segundos restantes de cooldown
     * 
     * @param cooldownRemaining nuevo valor
     */
    public void setCooldownRemaining(int cooldownRemaining) { 
        this.cooldownRemaining = cooldownRemaining; 
    }
    
    /**
     * obtiene los segundos restantes de efecto
     * 
     * @return segundos activos
     */
    public int getDurationRemaining() { 
        return durationRemaining; 
    }
}
