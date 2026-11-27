package dominio;

import java.io.Serializable;

/**
 * Clase abstracta base para los comportamientos del Bot (Patrón Strategy).
 * Define los 4 perfiles tácticos solicitados: Torpe, Ofensiva, Defensiva y Experta.
 *
 * @author gaby-mathias
 * @version 2026
 */
public abstract class BotStrategy implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STRATEGY_TORPE = "Torpe";
    public static final String STRATEGY_OFENSIVA = "Ofensiva";
    public static final String STRATEGY_DEFENSIVA = "Defensiva";
    public static final String STRATEGY_EXPERTA = "Experta";

    /**
     * Calcula y aplica el siguiente movimiento del bot en función del balón.
     *
     * @param bot Instancia del BotAI que se está controlando.
     * @param ball Balón del partido.
     */
    public abstract void decideMove(BotAI bot, Ball ball);

    /**
     * Nombre identificador del perfil de comportamiento.
     *
     * @return Nombre de la estrategia ("Torpe", "Ofensiva", "Defensiva", "Experta").
     */
    public abstract String getName();

    /**
     * Nivel numérico equivalente (1=Torpe, 2=Ofensiva, 3=Defensiva, 4=Experta).
     *
     * @return Código numérico del nivel.
     */
    public abstract int getLevelCode();

    /**
     * Método auxiliar compartido para patear hacia el arco izquierdo (rival)
     * únicamente cuando el bot está a la derecha del balón, evitando autogoles.
     *
     * @param bot El bot que realiza la patada.
     * @param ball El balón en juego.
     */
    protected void kickTowardsOpponentGoal(BotAI bot, Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = bot.getBounds().getCenterX();

        if (myCenter >= ballCenter - 12) {
            bot.setFacingRight(false);
            bot.kick(ball);
        }
    }
}
