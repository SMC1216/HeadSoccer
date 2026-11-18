package dominio;

import java.io.Serializable;

/**
 * Representa el área rectangular de colisión (hitbox) de un objeto en el dominio.
 * Permite detectar intersecciones físicas sin depender de java.awt.Rectangle.
 */
public class Bounds implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private int width;
    private int height;

    /**
     * Constructor de la caja de colisión.
     * @param x Coordenada horizontal superior izquierda.
     * @param y Coordenada vertical superior izquierda.
     * @param width Ancho de la caja.
     * @param height Alto de la caja.
     */
    public Bounds(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Verifica si esta caja de colisión se superpone con otra (choque o contacto).
     * @param other La otra caja de colisión a evaluar.
     * @return true si hay contacto físico, false en caso contrario.
     */
    public boolean intersects(Bounds other) {
        if (other == null) {
            return false;
        }
        return this.x < other.x + other.width &&
               this.x + this.width > other.x &&
               this.y < other.y + other.height &&
               this.y + this.height > other.y;
    }

    // Métodos de consulta (Getters)
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getCenterX() { return x + (width / 2.0); }
    public double getCenterY() { return y + (height / 2.0); }
}