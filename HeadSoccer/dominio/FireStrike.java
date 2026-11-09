package dominio;

/**
 * Habilidad especial que envuelve el balón en fuego y lo dispara a gran velocidad.
 */
public class FireStrike extends Ability {
    private double powerMultiplier;

    public FireStrike() {
        super(10, 3);
        this.powerMultiplier = 18.5;
    }

    @Override
    public void activate(Character caster, Ball ball) {
        if (caster == null || ball == null) return;
        this.isActive = true;
        ball.setSuperShot(true);
        double dir = caster.isFacingRight() ? 1.0 : -1.0;
        ball.setVelocityX(dir * powerMultiplier);
        ball.setVelocityY(-6.5);
    }
}