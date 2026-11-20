package dominio;

import java.io.Serializable;

/**
 * representa la porteria o arco donde se anotan los goles
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Goal implements ICollidable, Serializable {
    private static final long serialVersionUID = 1L;

    /** indica si es el arco izquierdo LEFT o derecho RIGHT */
    private String side;

    /** coordenada x donde empieza el arco */
    private double x;

    /** coordenada y donde esta el travesano */
    private double y;

    /** profundidad horizontal del arco */
    private int width;

    /** altura total del arco */
    private int height;

    /**
     * constructor de la porteria con su lado y dimensiones
     * 
     * @param side LEFT o RIGHT
     * @param x posicion horizontal
     * @param y posicion vertical
     * @param width ancho del arco
     * @param height alto del arco
     */
    public Goal(String side, double x, double y, int width, int height) {
        this.side = side;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * comprueba si el balon paso la linea reglamentaria de gol por debajo del travesano
     * 
     * @param ball el balon del juego
     * @return true si es gol valido, false si no
     */
    public boolean checkGoal(Ball ball) {
        if (ball == null) {
            return false;
        }
        Bounds ballBounds = ball.getBounds();
        double ballCenterX = ballBounds.getCenterX();
        double ballCenterY = ballBounds.getCenterY();

        boolean bajoTravesano = ballCenterY > (this.y + 12) && ballCenterY <= (this.y + this.height + 4);
        if (!bajoTravesano) {
            return false;
        }

        if ("LEFT".equals(side)) {
            return ballCenterX < (this.x + this.width) && ballBounds.intersects(this.getBounds());
        } else {
            return ballCenterX > this.x && ballBounds.intersects(this.getBounds());
        }
    }

    /**
     * obtiene los limites rectangulares del arco
     * 
     * @return objeto bounds de la porteria
     */
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    /**
     * calcula el rebote del balon si pega contra el travesano
     * 
     * @param other el objeto que choco contra el arco
     */
    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Ball) {
            Ball ball = (Ball) other;
            Bounds ballBounds = ball.getBounds();

            boolean golpeaTravesano = (ballBounds.getY() + ballBounds.getHeight() >= this.y - 4) &&
                                      (ballBounds.getY() <= this.y + 14);

            if (golpeaTravesano && ballBounds.intersects(this.getBounds())) {
                ball.setVelocityY(-Math.abs(ball.getVelocityY()) * 0.82);
                double reboteX = "LEFT".equals(side) ? 5.5 : -5.5;
                ball.setVelocityX(reboteX);

                if (ball.getY() + ball.getHeight() > this.y) {
                    ball.setY(this.y - ball.getHeight() - 1);
                }
            }
        }
    }

    /**
     * obtiene el lado del arco
     * 
     * @return LEFT o RIGHT
     */
    public String getSide() { 
        return side; 
    }
    
    /**
     * obtiene la coordenada x del arco
     * 
     * @return posicion x
     */
    public double getX() { 
        return x; 
    }
    
    /**
     * obtiene la coordenada y del travesano
     * 
     * @return posicion y
     */
    public double getY() { 
        return y; 
    }
    
    /**
     * obtiene el ancho del arco
     * 
     * @return ancho
     */
    public int getWidth() { 
        return width; 
    }
    
    /**
     * obtiene el alto del arco
     * 
     * @return altura
     */
    public int getHeight() { 
        return height; 
    }
}
