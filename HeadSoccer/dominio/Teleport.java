package dominio;

import java.awt.Color;

/**
 * habilidad especial de teletransporte para colocarse detras del balon (aura color cian eléctrico)
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Teleport extends Ability {
    private static final long serialVersionUID = 1L;

    /** Color característico del aura de teletransporte */
    public static final Color AURA_COLOR = new Color(0, 220, 255);

    /** distancia en pixeles de separacion respecto al balon */
    private double offsetDistance;

    /**
     * constructor del teletransporte con 8s de cooldown, 2s de duracion de aura y aura cian
     */
    public Teleport() {
        super(8, 2, AURA_COLOR);
        this.offsetDistance = 55.0;
    }

    /**
     * cambia instantaneamente la posicion del jugador para posicionarlo junto al balon
     * 
     * @param caster el personaje que se teletransporta
     * @param ball el balon de referencia
     */
    @Override
    public void activate(Character caster, Ball ball) {
        if (caster == null || ball == null) {
            return;
        }
        this.isActive = true;
        double offset = caster.isFacingRight() ? -offsetDistance : offsetDistance;
        double targetX = ball.getX() + offset;

        if (targetX < Ball.LEFT_WALL + 60) {
            targetX = Ball.LEFT_WALL + 60;
        }
        if (targetX > Ball.RIGHT_WALL - 130) {
            targetX = Ball.RIGHT_WALL - 130;
        }

        caster.setX(targetX);
    }
}
