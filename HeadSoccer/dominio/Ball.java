package dominio;

/**
 * representa el balon de futbol en el partido
 * tiene gravedad, rebotes contra el suelo y paredes, y friccion
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Ball extends GameObject implements ICollidable {
    private static final long serialVersionUID = 1L;

    private double velocityX;
    private double velocityY;
    private double bounceFactor;
    private boolean superShot;

    public static final double GRAVITY = 0.65;
    public static final double FRICTION = 0.985;
    public static final double GROUND_Y = 420.0;
    public static final double LEFT_WALL = 20.0;
    public static final double RIGHT_WALL = 860.0;
    public static final double CEILING_Y = 20.0;
    private static final double MAX_SPEED = 22.0;

    public Ball(double x, double y, int size) {
        super(x, y, size, size);
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.bounceFactor = 0.78;
        this.superShot = false;
    }

    public void applyGravity() {
        this.velocityY += GRAVITY;
    }

    public void addForce(double forceX, double forceY) {
        this.velocityX += forceX;
        this.velocityY += forceY;

        if (this.velocityX > MAX_SPEED) {
            this.velocityX = MAX_SPEED;
        }
        if (this.velocityX < -MAX_SPEED) {
            this.velocityX = -MAX_SPEED;
        }
        if (this.velocityY > MAX_SPEED) {
            this.velocityY = MAX_SPEED;
        }
        if (this.velocityY < -MAX_SPEED) {
            this.velocityY = -MAX_SPEED;
        }
    }

    @Override
    public void updatePosition() {
        applyGravity();

        setX(getX() + velocityX);
        setY(getY() + velocityY);

        velocityX *= FRICTION;
        if (Math.abs(velocityX) < 0.08) {
            velocityX = 0.0;
            superShot = false;
        }

        if (getY() + getHeight() >= GROUND_Y) {
            setY(GROUND_Y - getHeight());
            velocityY = -velocityY * bounceFactor;

            if (Math.abs(velocityY) < 1.2) {
                velocityY = 0.0;
            }
        }

        if (getY() < CEILING_Y) {
            setY(CEILING_Y);
            velocityY = -velocityY * bounceFactor;
        }

        if (getX() < LEFT_WALL) {
            setX(LEFT_WALL);
            velocityX = -velocityX * bounceFactor;
        } else if (getX() + getWidth() > RIGHT_WALL) {
            setX(RIGHT_WALL - getWidth());
            velocityX = -velocityX * bounceFactor;
        }
    }

    public void resetBall(double startX, double startY) {
        setPosition(startX, startY);
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.superShot = false;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other == null) {
            return;
        }
        Bounds myBounds = this.getBounds();
        Bounds otherBounds = other.getBounds();

        if (myBounds.intersects(otherBounds)) {
            double dx = myBounds.getCenterX() - otherBounds.getCenterX();
            double dy = myBounds.getCenterY() - otherBounds.getCenterY();

            double pushX = (dx >= 0) ? 7.5 : -7.5;
            double pushY = (dy < 0) ? -8.5 : -4.0;

            this.velocityX = pushX + (dx * 0.12);
            this.velocityY = pushY;
        }
    }

    public double getVelocityX() { 
        return velocityX; 
    }
    
    public void setVelocityX(double velocityX) { 
        this.velocityX = velocityX; 
    }

    public double getVelocityY() { 
        return velocityY; 
    }
    
    public void setVelocityY(double velocityY) { 
        this.velocityY = velocityY; 
    }

    public double getBounceFactor() { 
        return bounceFactor; 
    }
    
    public void setBounceFactor(double bounceFactor) { 
        this.bounceFactor = bounceFactor; 
    }

    public boolean isSuperShot() { 
        return superShot; 
    }
    
    public void setSuperShot(boolean superShot) { 
        this.superShot = superShot; 
    }
}
