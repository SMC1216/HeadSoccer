package dominio;

/**
 * Clase abstracta que representa a un personaje (muñeco) dentro de la cancha.
 */
public abstract class Character extends GameObject implements ICollidable {
    private double speed;
    private double jumpForce;
    private double velocityX;
    private double velocityY;
    private boolean isGrounded;
    private int specialMeter;
    private Ability specialAbility;
    private boolean facingRight;

    public Character(double x, double y, int width, int height, Ability specialAbility, boolean facingRight) {
        super(x, y, width, height);
        this.speed = 6.0;
        this.jumpForce = 14.0;
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.isGrounded = true;
        this.specialMeter = 80; // Inicia al 80% para probar el poder rápidamente
        this.specialAbility = specialAbility;
        this.facingRight = facingRight;
    }

    public void move(int direction) {
        this.velocityX = direction * speed;
        if (direction > 0) this.facingRight = true;
        else if (direction < 0) this.facingRight = false;
    }

    public void jump() {
        if (isGrounded) {
            this.velocityY = -jumpForce;
            this.isGrounded = false;
        }
    }

    public void kick(Ball ball) {
        if (ball == null) return;
        Bounds kickArea = new Bounds(getX() - 18, getY() - 10, getWidth() + 36, getHeight() + 20);
        if (kickArea.intersects(ball.getBounds())) {
            double dir = facingRight ? 1.0 : -1.0;
            ball.addForce(dir * 13.0, -9.5);
            gainSpecial(20);
        }
    }

    public boolean useSpecial(Ball ball) {
        if (specialMeter >= 100 && specialAbility != null && specialAbility.isReady()) {
            specialAbility.execute(this, ball);
            specialMeter = 0;
            return true;
        }
        return false;
    }

    /**
     * Actualiza el enfriamiento de la habilidad especial (invocado periódicamente).
     */
    public void updateCooldown() {
        if (specialAbility != null) {
            specialAbility.updateTick();
        }
    }

    public void gainSpecial(int amount) {
        specialMeter = Math.min(100, specialMeter + amount);
    }

    @Override
    public void updatePosition() {
        if (!isGrounded) {
            velocityY += Ball.GRAVITY;
        }

        setX(getX() + velocityX);
        setY(getY() + velocityY);

        // Contacto con el césped
        if (getY() + getHeight() >= Ball.GROUND_Y) {
            setY(Ball.GROUND_Y - getHeight());
            velocityY = 0.0;
            isGrounded = true;
        }

        // Límites laterales de la cancha
        if (getX() < Ball.LEFT_WALL + 55) {
            setX(Ball.LEFT_WALL + 55);
        } else if (getX() + getWidth() > Ball.RIGHT_WALL - 55) {
            setX(Ball.RIGHT_WALL - 55 - getWidth());
        }
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Ball) {
            gainSpecial(5);
        }
    }

    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public int getSpecialMeter() { return specialMeter; }
    public void setSpecialMeter(int specialMeter) { this.specialMeter = specialMeter; }
    public Ability getSpecialAbility() { return specialAbility; }
}