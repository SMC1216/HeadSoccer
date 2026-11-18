package dominio;

import java.io.Serializable;

/**
 * Clase abstracta para las habilidades especiales de los personajes.
 */
public abstract class Ability implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int cooldown;
    protected int duration;
    protected boolean isActive;

    public Ability(int cooldown, int duration) {
        this.cooldown = cooldown;
        this.duration = duration;
        this.isActive = false;
    }

    public abstract void activate(dominio.Character caster, Ball ball);

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}