package dominio;

/**
 * representa el balon de futbol con fisicas de gravedad, rebote y friccion
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Ball extends GameObject implements ICollidable {
    private static final long serialVersionUID = 1L;

    /** velocidad en el eje x */
    private double velocityX;

    /** velocidad en el eje y */
    private double velocityY;

    /** fraccion de energia conservada en cada rebote */
    private double bounceFactor;

    /** indica si el balon lleva activo un efecto de super tiro */
    private boolean superShot;

    /** constante de gravedad aplicada por frame */
    public static final double GRAVITY = 0.65;

    /** friccion del aire para frenar suavemente */
    public static final double FRICTION = 0.985;

    /** altura en y donde esta el suelo del campo */
    public static final double GROUND_Y = 420.0;

    /** limite izquierdo de la cancha */
    public static final double LEFT_WALL = 20.0;

    /** limite derecho de la cancha */
    public static final double RIGHT_WALL = 860.0;

    /** altura del techo de la cancha */
    public static final double CEILING_Y = 20.0;

    /** velocidad maxima permitida para evitar que atraviese objetos */
    private static final double MAX_SPEED = 22.0;

    /**
     * constructor para inicializar el balon con su posicion y diametro
     * 
     * @param x posicion inicial en x
     * @param y posicion inicial en y
     * @param size diametro del balon
     */
    public Ball(double x, double y, int size) {
        super(x, y, size, size);
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.bounceFactor = 0.78;
        this.superShot = false;
    }

    /**
     * suma la fuerza de la gravedad a la velocidad vertical
     */
    public void applyGravity() {
        this.velocityY += GRAVITY;
    }

    /**
     * le da un impulso de fuerza al balon
     * 
     * @param forceX fuerza horizontal
     * @param forceY fuerza vertical
     */
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

    /**
     * calcula la nueva posicion del balon en cada frame con rebotes en paredes y suelo
     */
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

    /**
     * detiene y recoloca el balon en el punto de saque
     * 
     * @param startX nueva coordenada x
     * @param startY nueva coordenada y
     */
    public void resetBall(double startX, double startY) {
        setPosition(startX, startY);
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.superShot = false;
    }

    /**
     * devuelve la caja de choque del balon
     * 
     * @return limites bounds del balon
     */
    @Override
    public Bounds getBounds() {
        return new Bounds(getX(), getY(), getWidth(), getHeight());
    }

    /**
     * procesa el choque del balon con un jugador o travesano
     * 
     * @param other el objeto contra el cual choco
     */
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

    /**
     * obtiene la velocidad en x
     * 
     * @return velocidad horizontal
     */
    public double getVelocityX() { 
        return velocityX; 
    }
    
    /**
     * cambia la velocidad en x
     * 
     * @param velocityX nuevo valor
     */
    public void setVelocityX(double velocityX) { 
        this.velocityX = velocityX; 
    }

    /**
     * obtiene la velocidad en y
     * 
     * @return velocidad vertical
     */
    public double getVelocityY() { 
        return velocityY; 
    }
    
    /**
     * cambia la velocidad en y
     * 
     * @param velocityY nuevo valor
     */
    public void setVelocityY(double velocityY) { 
        this.velocityY = velocityY; 
    }

    /**
     * obtiene el factor de rebote
     * 
     * @return porcentaje de restitucion
     */
    public double getBounceFactor() { 
        return bounceFactor; 
    }
    
    /**
     * modifica el factor de rebote
     * 
     * @param bounceFactor nuevo factor
     */
    public void setBounceFactor(double bounceFactor) { 
        this.bounceFactor = bounceFactor; 
    }

    /**
     * indica si esta en estado de super tiro
     * 
     * @return true si lleva efecto especial
     */
    public boolean isSuperShot() { 
        return superShot; 
    }
    
    /**
     * activa o desactiva el estado de super tiro
     * 
     * @param superShot nuevo estado
     */
    public void setSuperShot(boolean superShot) { 
        this.superShot = superShot; 
    }
}
