package dominio;

import java.io.Serializable;

/**
 * representa la porteria o arco de cada lado de la cancha
 * detecta si hubo gol reglamentario o si el balon reboto en el travesano
 * 
 * @author gaby-mathias
 * @version 2026
 */
public class Goal implements ICollidable, Serializable {
    private static final long serialVersionUID = 1L;

    private String side;
    private double x;
    private double y;
    private int width;
    private int height;

    public Goal(String side, double x, double y, int width, int height) {
        this.side = side;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

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

    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

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

    public String getSide() { 
        return side; 
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
}
