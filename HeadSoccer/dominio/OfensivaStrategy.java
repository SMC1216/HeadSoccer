package dominio;

/**
 * Estrategia Ofensiva para el Bot: presiona constantemente en toda la cancha
 * buscando el balón para rematar al arco rival y usa su poder especial al atacar.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class OfensivaStrategy extends BotStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public void decideMove(BotAI bot, Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = bot.getBounds().getCenterX();

        // Presiona incluso en campo rival buscando colocarse detrás del balón
        double targetX = ballCenter + 32.0;
        if (targetX < 130) targetX = 130;
        if (targetX > 745) targetX = 745;

        if (myCenter > targetX + 14) {
            bot.move(-1);
        } else if (myCenter < targetX - 14) {
            bot.move(1);
        } else {
            bot.move(0);
            bot.setFacingRight(false);
        }

        // Salta si el balón quedó detrás o si viene por aire para cabecear al arco rival
        if (ballCenter > myCenter && (ballCenter - myCenter) < 75) {
            bot.jump();
        } else if (Math.abs(ballCenter - myCenter) < 95 && ball.getY() < bot.getY() + 15) {
            bot.jump();
        }

        if (Math.abs(ballCenter - myCenter) < 90) {
            kickTowardsOpponentGoal(bot, ball);
        }

        // Uso ofensivo del poder apenas tiene oportunidad de disparo
        if (bot.getSpecialMeter() >= 100 && bot.getSpecialAbility() != null && bot.getSpecialAbility().isReady()) {
            boolean enAtaque = ballCenter < 580 && myCenter >= ballCenter - 20;
            if (enAtaque) {
                bot.setFacingRight(false);
                bot.useSpecial(ball);
                kickTowardsOpponentGoal(bot, ball);
            }
        }
    }

    @Override
    public String getName() {
        return STRATEGY_OFENSIVA;
    }

    @Override
    public int getLevelCode() {
        return BotAI.LEVEL_OFENSIVA;
    }
}
