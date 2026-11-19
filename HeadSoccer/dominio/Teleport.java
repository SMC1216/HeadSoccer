package dominio;

/**
 * habilidad especial de teletransporte para colocarse detras del balon
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Teleport extends Ability {
    private static final long serialVersionUID = 1L;

    /** distancia en pixeles de separacion respecto al balon */
    private double offsetDistance;

    /**
     * constructor del teletransporte con 8s de cooldown y 1s de duracion
     */
    public Teleport() {
        super(8, 1);
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
