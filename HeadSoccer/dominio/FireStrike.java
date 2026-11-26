package dominio;

import java.awt.Color;

/**
 * habilidad especial de tiro de fuego (aura color rojo-naranja intenso)
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class FireStrike extends Ability {
    private static final long serialVersionUID = 1L;

    /** Color característico del aura de fuego */
    public static final Color AURA_COLOR = new Color(255, 69, 0);

    /** multiplicador de fuerza para el tiro */
    private double powerMultiplier;

    /**
     * constructor del tiro de fuego con 10s de cooldown, 3s de duracion y aura naranja-fuego
     */
    public FireStrike() {
        super(10, 3, AURA_COLOR);
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
