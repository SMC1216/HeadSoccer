package dominio;

/**
 * Clase abstracta base para cualquier entidad móvil o posicionable dentro de la cancha.
 * Encapsula la posición y el tamaño aplicando ocultamiento de información.
 */
public abstract class GameObject {
    private double x;
    private double y;
    private int width;
    private int height;

    /**
     * Constructor base para inicializar posición y dimensiones.
     * @param x Posición inicial en el eje horizontal.
     * @param y Posición inicial en el eje vertical.
     * @param width Ancho del objeto en unidades lógicas.
     * @param height Alto del objeto en unidades lógicas.
     */
    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Método abstracto que obliga a las subclases a definir cómo actualizan
     * su posición en cada fotograma del partido.
     */
    public abstract void updatePosition();

    /**
     * Permite reposicionar el objeto directamente (útil al reiniciar tras un gol).
     * @param newX Nueva coordenada X.
     * @param newY Nueva coordenada Y.
     */
    public void setPosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    // Métodos de acceso y modificación (Getters y Setters)
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}