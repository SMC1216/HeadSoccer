package dominio;

/**
 * Clase abstracta para las habilidades especiales de los personajes.
 */
public abstract class Ability {
    protected int cooldown;
    protected int duration;
    protected boolean isActive;

    public Ability(int cooldown, int duration) {
        this.cooldown = cooldown;
        this.duration = duration;
        this.isActive = false;
    }

    public abstract void activate(Character caster, Ball ball);

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}