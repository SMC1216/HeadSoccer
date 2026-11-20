package dominio;

/**
 * Clase abstracta que representa a un personaje (muñeco) dentro de la cancha.
 */
public abstract class Character extends GameObject implements ICollidable {
    private static final long serialVersionUID = 1L;

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

    /**
     * Reinicia el estado cinemático y la posición del personaje.
     * @param startX Coordenada X inicial.
     * @param startY Coordenada Y inicial.
     * @param faceRight Orientación inicial (true = derecha, false = izquierda).
     */
    public void resetState(double startX, double startY, boolean faceRight) {
        setPosition(startX, startY);
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.isGrounded = true;
        this.facingRight = faceRight;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other == null || other == this) {
            return;
        }
        if (other instanceof Ball) {
            gainSpecial(5);
        } else if (other instanceof Character) {
            resolveCharacterCollision((Character) other);
        }
    }

    /**
     * Resuelve la colisión física cuerpo a cuerpo entre dos personajes,
     * evitando que se atraviesen y respetando los límites de la cancha.
     * @param other El otro personaje con el que colisiona.
     */
    private void resolveCharacterCollision(Character other) {
        Bounds myBounds = this.getBounds();
        Bounds otherBounds = other.getBounds();

        if (!myBounds.intersects(otherBounds)) {
            return;
        }

        double myCenterX = myBounds.getCenterX();
        double otherCenterX = otherBounds.getCenterX();
        double myCenterY = myBounds.getCenterY();
        double otherCenterY = otherBounds.getCenterY();

        // Si un personaje cae encima de la cabeza del otro, rebota ligeramente y se desliza
        if (Math.abs(myCenterY - otherCenterY) > 35 && Math.abs(myCenterX - otherCenterX) < 55) {
            if (myCenterY < otherCenterY) {
                this.velocityY = -jumpForce * 0.55;
                this.isGrounded = false;
            } else {
                other.velocityY = -other.jumpForce * 0.55;
                other.isGrounded = false;
            }
        }

        double minDistanceX = (this.getWidth() + other.getWidth()) / 2.0;
        double currentDistX = Math.abs(myCenterX - otherCenterX);
        double overlapX = minDistanceX - currentDistX;

        if (overlapX > 0) {
            boolean iAmLeft = (myCenterX < otherCenterX) || (myCenterX == otherCenterX && this.getX() <= other.getX());
            double push = (overlapX / 2.0) + 1.0;

            double minX = Ball.LEFT_WALL + 55;
            double maxThisX = Ball.RIGHT_WALL - 55 - this.getWidth();
            double maxOtherX = Ball.RIGHT_WALL - 55 - other.getWidth();

            if (iAmLeft) {
                double newThisX = Math.max(minX, this.getX() - push);
                double newOtherX = Math.min(maxOtherX, other.getX() + push);

                // Si uno quedó contra la pared izquierda, desplazar al otro el resto
                if (newThisX + this.getWidth() > newOtherX) {
                    newOtherX = Math.min(maxOtherX, newThisX + this.getWidth() + 1.0);
                }
                // Si el otro quedó contra la pared derecha, desplazar a este hacia la izquierda
                if (newThisX + this.getWidth() > newOtherX) {
                    newThisX = Math.max(minX, newOtherX - this.getWidth() - 1.0);
                }

                this.setX(newThisX);
                other.setX(newOtherX);

                if (this.velocityX > 0) this.velocityX = 0;
                if (other.velocityX < 0) other.velocityX = 0;
            } else {
                double newOtherX = Math.max(minX, other.getX() - push);
                double newThisX = Math.min(maxThisX, this.getX() + push);

                if (newOtherX + other.getWidth() > newThisX) {
                    newThisX = Math.min(maxThisX, newOtherX + other.getWidth() + 1.0);
                }
                if (newOtherX + other.getWidth() > newThisX) {
                    newOtherX = Math.max(minX, newThisX - other.getWidth() - 1.0);
                }

                other.setX(newOtherX);
                this.setX(newThisX);

                if (this.velocityX < 0) this.velocityX = 0;
                if (other.velocityX > 0) other.velocityX = 0;
            }
        }
    }

    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public int getSpecialMeter() { return specialMeter; }
    public void setSpecialMeter(int specialMeter) { this.specialMeter = specialMeter; }
    public Ability getSpecialAbility() { return specialAbility; }
    public void setSpecialAbility(Ability specialAbility) { this.specialAbility = specialAbility; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public boolean isGrounded() { return isGrounded; }
}