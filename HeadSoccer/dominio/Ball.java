package dominio;

/**
 * Representa el balón del partido.
 * Hereda sus coordenadas de GameObject e implementa ICollidable para rebotar.
 */
public class Ball extends GameObject implements ICollidable {
    private double velocityX;
    private double velocityY;
    private double bounceFactor;
    private boolean superShot; // Indica si lleva el efecto de una habilidad especial

    // Constantes físicas de la cancha
    public static final double GRAVITY = 0.65;
    public static final double FRICTION = 0.985;
    public static final double GROUND_Y = 420.0; // Nivel del suelo lógico
    public static final double LEFT_WALL = 20.0;
    public static final double RIGHT_WALL = 860.0;
    public static final double CEILING_Y = 20.0;
    private static final double MAX_SPEED = 22.0;

    /**
     * Constructor del balón.
     * @param x Posición horizontal inicial.
     * @param y Posición vertical inicial.
     * @param size Diámetro del balón.
     */
    public Ball(double x, double y, int size) {
        super(x, y, size, size);
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.bounceFactor = 0.78; // Conserva el 78% de la energía al rebotar
        this.superShot = false;
    }

    /**
     * Aplica la aceleración de la gravedad a la velocidad vertical del balón.
     */
    public void applyGravity() {
        this.velocityY += GRAVITY;
    }

    /**
     * Añade un impulso o fuerza al balón (al ser pateado, cabeceado o recibir un poder).
     * @param forceX Fuerza horizontal (positiva hacia la derecha, negativa hacia la izquierda).
     * @param forceY Fuerza vertical (negativa impulsa hacia arriba).
     */
    public void addForce(double forceX, double forceY) {
        this.velocityX += forceX;
        this.velocityY += forceY;

        // Limitar la velocidad máxima para evitar que atraviese paredes
        if (this.velocityX > MAX_SPEED) this.velocityX = MAX_SPEED;
        if (this.velocityX < -MAX_SPEED) this.velocityX = -MAX_SPEED;
        if (this.velocityY > MAX_SPEED) this.velocityY = MAX_SPEED;
        if (this.velocityY < -MAX_SPEED) this.velocityY = -MAX_SPEED;
    }

    /**
     * Actualiza la posición del balón en cada fotograma, aplicando gravedad,
     * fricción del aire y rebotes contra el suelo, techo y límites laterales.
     */
    @Override
    public void updatePosition() {
        applyGravity();

        setX(getX() + velocityX);
        setY(getY() + velocityY);

        // Fricción horizontal suave
        velocityX *= FRICTION;
        if (Math.abs(velocityX) < 0.08) {
            velocityX = 0.0;
            superShot = false; // Se apaga el efecto especial cuando pierde velocidad
        }

        // Rebote contra el suelo
        if (getY() + getHeight() >= GROUND_Y) {
            setY(GROUND_Y - getHeight());
            velocityY = -velocityY * bounceFactor;

            // Si el rebote ya es muy pequeño, deja de saltar en el césped
            if (Math.abs(velocityY) < 1.2) {
                velocityY = 0.0;
            }
        }

        // Rebote contra el techo del estadio
        if (getY() < CEILING_Y) {
            setY(CEILING_Y);
            velocityY = -velocityY * bounceFactor;
        }

        // Rebote contra los muros laterales del estadio
        if (getX() < LEFT_WALL) {
            setX(LEFT_WALL);
            velocityX = -velocityX * bounceFactor;
        } else if (getX() + getWidth() > RIGHT_WALL) {
            setX(RIGHT_WALL - getWidth());
            velocityX = -velocityX * bounceFactor;
        }
    }

    /**
     * Detiene por completo el balón y lo ubica en las coordenadas indicadas (tras un gol).
     */
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

    /**
     * Gestiona el rebote físico cuando el balón choca con otro objeto colisionable
     * (por ejemplo, el cuerpo o la cabeza de un personaje o el travesaño).
     */
    @Override
    public void onCollision(ICollidable other) {
        if (other == null) return;
        Bounds myBounds = this.getBounds();
        Bounds otherBounds = other.getBounds();

        if (myBounds.intersects(otherBounds)) {
            // Calculamos la dirección del rebote según los centros de ambos objetos
            double dx = myBounds.getCenterX() - otherBounds.getCenterX();
            double dy = myBounds.getCenterY() - otherBounds.getCenterY();

            // Empuje horizontal y rebote ligeramente hacia arriba (efecto cabezazo)
            double pushX = (dx >= 0) ? 7.5 : -7.5;
            double pushY = (dy < 0) ? -8.5 : -4.0;

            this.velocityX = pushX + (dx * 0.12);
            this.velocityY = pushY;
        }
    }

    // Getters y Setters
    public double getVelocityX() { return velocityX; }
    public void setVelocityX(double velocityX) { this.velocityX = velocityX; }

    public double getVelocityY() { return velocityY; }
    public void setVelocityY(double velocityY) { this.velocityY = velocityY; }

    public double getBounceFactor() { return bounceFactor; }
    public void setBounceFactor(double bounceFactor) { this.bounceFactor = bounceFactor; }

    public boolean isSuperShot() { return superShot; }
    public void setSuperShot(boolean superShot) { this.superShot = superShot; }
}