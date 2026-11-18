package dominio;

import java.io.Serializable;

/**
 * Representa una portería (arco) dentro de la cancha.
 * Implementa ICollidable para evaluar si el balón ingresa y anotar gol.
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
     * Verifica si el balón ha ingresado dentro del área válida de gol.
     * @param ball El balón del partido.
     * @return true si el balón cruzó la línea de gol bajo el travesaño, false si no.
     */
    public boolean checkGoal(Ball ball) {
        if (ball == null) {
            return false;
        }
        Bounds ballBounds = ball.getBounds();
        Bounds goalBounds = this.getBounds();

        // Hay gol si las cajas se intersectan y el centro del balón está debajo del travesaño
        return goalBounds.intersects(ballBounds) && ballBounds.getCenterY() > (this.y + 8);
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    /**
     * Si el balón golpea justo el borde superior (el travesaño), rebota hacia afuera.
     */
    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Ball) {
            Ball ball = (Ball) other;
            // Si golpea la parte alta del travesaño (los primeros 10 píxeles de la portería)
            if (ball.getY() + ball.getHeight() <= this.y + 14) {
                ball.setVelocityY(-Math.abs(ball.getVelocityY()) * 0.8);
                ball.setVelocityX(-ball.getVelocityX() * 0.9);
            }
        }
    }

    // Getters
    public String getSide() { return side; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}