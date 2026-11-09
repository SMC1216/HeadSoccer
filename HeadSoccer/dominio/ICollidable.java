package dominio;

/**
 * Interfaz que define el comportamiento para todos los objetos del juego
 * que pueden colisionar físicamente entre sí.
 */
public interface ICollidable {
    
    /**
     * Obtiene el área rectangular actual del objeto para calcular colisiones.
     * @return Objeto Bounds con las coordenadas y dimensiones actuales.
     */
    public Bounds getBounds();

    /**
     * Define la reacción del objeto cuando colisiona con otro elemento físico.
     * @param other El otro objeto con el que se produjo la colisión.
     */
    public void onCollision(ICollidable other);
}