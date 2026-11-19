package dominio;

/**
 * interfaz para cualquier elemento que pueda chocar en la cancha
 * 
 * @author gaby-mathias
 * @version 2026
 */
public interface ICollidable {
    
    public Bounds getBounds();

    public void onCollision(ICollidable other);
}
