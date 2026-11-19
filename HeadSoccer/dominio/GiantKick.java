package dominio;

/**
 * habilidad de patada gigante
 * patea con mucha fuerza al balon y hace que el personaje salte
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class GiantKick extends Ability {
    private static final long serialVersionUID = 1L;

    private double powerMultiplier;

    public GiantKick() {
        super(12, 2);
        this.powerMultiplier = 21.0;
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
        ball.setVelocityY(-11.0);
        caster.jump();
    }

    public double getPowerMultiplier() {
        return powerMultiplier;
    }
}
