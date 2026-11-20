package dominio;

/**
 * habilidad especial de tiro de fuego
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class FireStrike extends Ability {
    private static final long serialVersionUID = 1L;

    /** multiplicador de fuerza para el tiro */
    private double powerMultiplier;

    /**
     * constructor del tiro de fuego con 10s de cooldown y 3s de duracion
     */
    public FireStrike() {
        super(10, 3);
        this.powerMultiplier = 18.5;
    }

    /**
     * activa el tiro potente con fuego hacia el arco contrario
     * 
     * @param caster el personaje que lanza el poder
     * @param ball el balon a disparar
     */
    @Override
    public void activate(Character caster, Ball ball) {
        if (caster == null || ball == null) {
            return;
        }
        this.isActive = true;
        ball.setSuperShot(true);
        double dir = caster.isFacingRight() ? 1.0 : -1.0;
        ball.setVelocityX(dir * powerMultiplier);
        ball.setVelocityY(-6.5);
    }
}
