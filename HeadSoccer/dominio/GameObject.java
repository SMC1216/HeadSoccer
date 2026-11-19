package dominio;

import java.io.Serializable;

/**
 * clase base para todos los objetos del juego que tienen posicion y tamano en la cancha
 * 
 * @author gaby-mathias
 * @version 2026
 */
public abstract class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private int width;
    private int height;

    /**
     * constructor principal para darle posicion y tamano al objeto
     */
    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void updatePosition();

    public void setPosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    public double getX() { 
        return x; 
    }
    
    public void setX(double x) { 
        this.x = x; 
    }

    public double getY() { 
        return y; 
    }
    
    public void setY(double y) { 
        this.y = y; 
    }

    public int getWidth() { 
        return width; 
    }
    
    public void setWidth(int width) { 
        this.width = width; 
    }

    public int getHeight() { 
        return height; 
    }
    
    public void setHeight(int height) { 
        this.height = height; 
    }
}
