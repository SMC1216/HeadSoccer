package dominio;

import java.io.Serializable;

/**
 * Representa una portería (arco) dentro de la cancha.
 * Implementa ICollidable para evaluar si el balón ingresa y anotar gol,
 * o si rebota elásticamente en los postes y travesaños sin cruzar la meta.
 */
public class Goal implements ICollidable, Serializable {
    private static final long serialVersionUID = 1L;

    private String side; // "LEFT" (Izquierda) o "RIGHT" (Derecha)
    private double x;
    private double y;
    private int width;
    private int height;

    /**
     * Constructor de la portería.
     * @param side Lado de la cancha ("LEFT" o "RIGHT").
     * @param x Posición horizontal de la portería.
     * @param y Posición vertical superior (travesaño).
     * @param width Profundidad del arco.
     * @param height Altura de la portería.
     */
    public Goal(String side, double x, double y, int width, int height) {
        this.side = side;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Verifica si el balón ha cruzado la línea de gol válida bajo el travesaño.
     * Diferencia claramente un balón que golpea el exterior o travesaño de uno que anota.
     * @param ball El balón del partido.
     * @return true si el balón cruzó la línea de gol reglamentaria, false en caso contrario.
     */
    public boolean checkGoal(Ball ball) {
        if (ball == null) {
            return false;
        }
        Bounds ballBounds = ball.getBounds();
        double ballCenterX = ballBounds.getCenterX();
        double ballCenterY = ballBounds.getCenterY();

        // El balón debe encontrarse verticalmente bajo el travesaño y dentro del alto del arco
        boolean bajoTravesano = ballCenterY > (this.y + 12) && ballCenterY <= (this.y + this.height + 4);

        if (!bajoTravesano) {
            return false;
        }

        if ("LEFT".equals(side)) {
            // Entra si el centro del balón cruza el poste frontal hacia la red izquierda
            return ballCenterX < (this.x + this.width) && ballBounds.intersects(this.getBounds());
        } else {
            // Entra si el centro del balón cruza el poste frontal hacia la red derecha
            return ballCenterX > this.x && ballBounds.intersects(this.getBounds());
        }
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    /**
     * Gestiona la colisión física contra el travesaño superior o el poste frontal.
     * Si golpea la estructura superior, rebota elásticamente hacia afuera.
     * @param other El otro objeto en contacto (balón).
     */
    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Ball) {
            Ball ball = (Ball) other;
            Bounds ballBounds = ball.getBounds();

            // Detectar impacto en la zona superior del travesaño (primeros 14 píxeles)
            boolean golpeaTravesano = (ballBounds.getY() + ballBounds.getHeight() >= this.y - 4) &&
                                      (ballBounds.getY() <= this.y + 14);

            if (golpeaTravesano && ballBounds.intersects(this.getBounds())) {
                ball.setVelocityY(-Math.abs(ball.getVelocityY()) * 0.82);
                double reboteX = "LEFT".equals(side) ? 5.5 : -5.5;
                ball.setVelocityX(reboteX);

                // Evitar penetración o atasco en el travesaño
                if (ball.getY() + ball.getHeight() > this.y) {
                    ball.setY(this.y - ball.getHeight() - 1);
                }
            }
        }
    }

    // Métodos de consulta (Getters)
    public String getSide() { return side; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
