package dominio;

import java.io.Serializable;

/**
 * clase abstracta base para todos los objetos del juego que tienen posicion y tamano
 * 
 * @author gaby-mathias
 * @version 2026
 */
public abstract class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;

    /** coordenada horizontal x del objeto en la pantalla */
    private double x;

    /** coordenada vertical y del objeto en la pantalla */
    private double y;

    /** ancho en pixeles del objeto */
    private int width;

    /** alto en pixeles del objeto */
    private int height;

    /**
     * constructor principal para darle posicion y dimensiones a un objeto
     * 
     * @param x posicion inicial en el eje x
     * @param y posicion inicial en el eje y
     * @param width ancho del objeto
     * @param height alto del objeto
     */
    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * metodo abstracto que cada objeto hijo debe implementar para actualizar su movimiento en cada frame
     */
    public abstract void updatePosition();

    /**
     * cambia la posicion x y y del objeto por ejemplo al reiniciar el juego
     * 
     * @param newX nueva coordenada en x
     * @param newY nueva coordenada en y
     */
    public void setPosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    /**
     * obtiene la coordenada x actual
     * 
     * @return posicion en x
     */
    public double getX() { 
        return x; 
    }
    
    /**
     * modifica la coordenada x
     * 
     * @param x nuevo valor para x
     */
    public void setX(double x) { 
        this.x = x; 
    }

    /**
     * obtiene la coordenada y actual
     * 
     * @return posicion en y
     */
    public double getY() { 
        return y; 
    }
    
    /**
     * modifica la coordenada y
     * 
     * @param y nuevo valor para y
     */
    public void setY(double y) { 
        this.y = y; 
    }

    /**
     * obtiene el ancho del objeto
     * 
     * @return ancho en pixeles
     */
    public int getWidth() { 
        return width; 
    }
    
    /**
     * modifica el ancho del objeto
     * 
     * @param width nuevo ancho
     */
    public void setWidth(int width) { 
        this.width = width; 
    }

    /**
     * obtiene el alto del objeto
     * 
     * @return alto en pixeles
     */
    public int getHeight() { 
        return height; 
    }
    
    /**
     * modifica el alto del objeto
     * 
     * @param height nuevo alto
     */
    public void setHeight(int height) { 
        this.height = height; 
    }
}
