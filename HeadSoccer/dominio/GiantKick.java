package dominio;

import java.awt.Color;

/**
 * habilidad especial de patada gigante que empuja con gran fuerza el balon (aura color verde-dorado eléctrico)
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class GiantKick extends Ability {
    private static final long serialVersionUID = 1L;

    /** Color característico del aura de patada gigante */
    public static final Color AURA_COLOR = new Color(50, 235, 100);

    /** multiplicador de poder de impacto de la patada gigante */
    private double powerMultiplier;

    /**
     * constructor de la patada gigante con 12s de cooldown, 2s de duracion y aura verde esmeralda/dorada
     */
    public GiantKick() {
        super(12, 2, AURA_COLOR);
        this.powerMultiplier = 21.0;
    }

    /**
     * dispara el balon con fuerza masiva horizontal y vertical y hace saltar al jugador
     * 
     * @param caster el personaje que patea
     * @param ball el balon a impactar
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
        ball.setVelocityY(-11.0);
        caster.jump();
    }

    /**
     * obtiene el multiplicador de potencia
     * 
     * @return valor del multiplicador
     */
    public double getPowerMultiplier() {
        return powerMultiplier;
    }
}
