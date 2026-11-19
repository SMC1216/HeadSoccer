package dominio;

/**
 * habilidad de tiro de fuego
 * envia el balon a gran velocidad y con efecto hacia el arco
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class FireStrike extends Ability {
    private static final long serialVersionUID = 1L;

    private double powerMultiplier;

    public FireStrike() {
        super(10, 3);
        this.powerMultiplier = 18.5;
    }

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
