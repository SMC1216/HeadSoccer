package dominio;

/**
 * Habilidad especial que teletransporta al personaje frente al balón.
 */
public class Teleport extends Ability {
    private double offsetDistance;

    public Teleport() {
        super(8, 1);
        this.offsetDistance = 55.0;
    }

    @Override
    public void activate(Character caster, Ball ball) {
        if (caster == null || ball == null) return;
        this.isActive = true;
        double offset = caster.isFacingRight() ? -offsetDistance : offsetDistance;
        double targetX = ball.getX() + offset;

        if (targetX < Ball.LEFT_WALL + 60) targetX = Ball.LEFT_WALL + 60;
        if (targetX > Ball.RIGHT_WALL - 130) targetX = Ball.RIGHT_WALL - 130;

        caster.setX(targetX);
    }
}