package dominio;

import java.io.Serializable;

/**
 * clase para representar la caja de choque de los objetos
 * no usamos rectangle de java awt para no mezclar interfaz con logica pura
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Bounds implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private int width;
    private int height;

    public Bounds(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean intersects(Bounds other) {
        if (other == null) {
            return false;
        }
        boolean cruzaX = this.x < other.x + other.width && this.x + this.width > other.x;
        boolean cruzaY = this.y < other.y + other.height && this.y + this.height > other.y;
        return cruzaX && cruzaY;
    }

    public double getX() { 
        return x; 
    }
    
    public double getY() { 
        return y; 
    }
    
    public int getWidth() { 
        return width; 
    }
    
    public int getHeight() { 
        return height; 
    }
    
    public double getCenterX() { 
        return x + (width / 2.0); 
    }
    
    public double getCenterY() { 
        return y + (height / 2.0); 
    }
}
