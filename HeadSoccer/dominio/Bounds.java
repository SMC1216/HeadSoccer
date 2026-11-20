package dominio;

import java.io.Serializable;

/**
 * representa la caja de colision rectangular de cada objeto para detectar choques
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Bounds implements Serializable {
    private static final long serialVersionUID = 1L;

    /** coordenada x superior izquierda de la caja */
    private double x;

    /** coordenada y superior izquierda de la caja */
    private double y;

    /** ancho total de la caja de choque */
    private int width;

    /** alto total de la caja de choque */
    private int height;

    /**
     * constructor de la caja de colision con su posicion y medidas
     * 
     * @param x posicion inicial en x
     * @param y posicion inicial en y
     * @param width ancho de la caja
     * @param height alto de la caja
     */
    public Bounds(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * verifica si este rectangulo se cruza con otro rectangulo
     * 
     * @param other la otra caja de colision
     * @return true si se tocan o cruzan en x y y, false de lo contrario
     */
    public boolean intersects(Bounds other) {
        if (other == null) {
            return false;
        }
        boolean cruzaX = this.x < other.x + other.width && this.x + this.width > other.x;
        boolean cruzaY = this.y < other.y + other.height && this.y + this.height > other.y;
        return cruzaX && cruzaY;
    }

    /**
     * obtiene la coordenada x de la caja
     * 
     * @return posicion x
     */
    public double getX() { 
        return x; 
    }
    
    /**
     * obtiene la coordenada y de la caja
     * 
     * @return posicion y
     */
    public double getY() { 
        return y; 
    }
    
    /**
     * obtiene el ancho de la caja
     * 
     * @return ancho
     */
    public int getWidth() { 
        return width; 
    }
    
    /**
     * obtiene el alto de la caja
     * 
     * @return alto
     */
    public int getHeight() { 
        return height; 
    }
    
    /**
     * calcula el punto medio horizontal de la caja
     * 
     * @return centro en el eje x
     */
    public double getCenterX() { 
        return x + (width / 2.0); 
    }
    
    /**
     * calcula el punto medio vertical de la caja
     * 
     * @return centro en el eje y
     */
    public double getCenterY() { 
        return y + (height / 2.0); 
    }
}
