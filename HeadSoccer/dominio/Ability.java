package dominio;

import java.io.Serializable;

/**
 * clase base para los poderes especiales de los jugadores
 * maneja el tiempo de recarga y cuanto dura el efecto
 * 
 * @author gaby-mathias
 * @version 2026
 */
public abstract class Ability implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int cooldown;
    protected int duration;
    protected int cooldownRemaining;
    protected int durationRemaining;
    protected boolean isActive;

    public Ability(int cooldown, int duration) {
        this.cooldown = cooldown;
        this.duration = duration;
        this.cooldownRemaining = 0;
        this.durationRemaining = 0;
        this.isActive = false;
    }

    public void execute(Character caster, Ball ball) {
        if (isReady()) {
            this.isActive = true;
            this.durationRemaining = this.duration;
            this.cooldownRemaining = this.cooldown;
            activate(caster, ball);
        }
    }

    protected abstract void activate(Character caster, Ball ball);

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

    public boolean isReady() {
        return cooldownRemaining <= 0;
    }

    public boolean isActive() { 
        return isActive; 
    }
    
    public void setActive(boolean active) { 
        this.isActive = active; 
    }
    
    public int getCooldown() { 
        return cooldown; 
    }
    
    public void setCooldown(int cooldown) { 
        this.cooldown = cooldown; 
    }
    
    public int getDuration() { 
        return duration; 
    }
    
    public void setDuration(int duration) { 
        this.duration = duration; 
    }
    
    public int getCooldownRemaining() { 
        return cooldownRemaining; 
    }
    
    public void setCooldownRemaining(int cooldownRemaining) { 
        this.cooldownRemaining = cooldownRemaining; 
    }
    
    public int getDurationRemaining() { 
        return durationRemaining; 
    }
}
