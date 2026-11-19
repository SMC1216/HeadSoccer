package dominio;

/**
 * interfaz para cualquier elemento del juego que pueda participar en choques
 * 
 * @author gaby-mathias
 * @version 2026
 */
public interface ICollidable {
    
    /**
     * devuelve la caja de choque del objeto
     * 
     * @return caja de limites bounds
     */
    public Bounds getBounds();

    /**
     * define la reaccion que tiene el objeto cuando choca contra otro
     * 
     * @param other el otro objeto con el que choco
     */
    public void onCollision(ICollidable other);
}
