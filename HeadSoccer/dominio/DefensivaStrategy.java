package dominio;

/**
 * Estrategia Defensiva para el Bot: prioriza resguardar su portería en el lado derecho,
 * despeja balones que entran a su área y reserva su poder para emergencias defensivas.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class DefensivaStrategy extends BotStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public void decideMove(BotAI bot, Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = bot.getBounds().getCenterX();

        // Solo sale a presionar cuando el balón pasa a su mitad de cancha
        double targetX;
        if (ballCenter > 430) {
            targetX = Math.min(745.0, ballCenter + 34.0);
        } else {
            // Se mantiene cuidando su portería
            targetX = 695.0;
        }

        if (myCenter > targetX + 14) {
            bot.move(-1);
        } else if (myCenter < targetX - 14) {
            bot.move(1);
        } else {
            bot.move(0);
            bot.setFacingRight(false);
        }

        // Salta para bloquear tiros altos hacia su arco o recuperar posición defensiva
        if (ballCenter > myCenter && (ballCenter - myCenter) < 80) {
            bot.jump();
        } else if (ballCenter > 480 && Math.abs(ballCenter - myCenter) < 95 && ball.getY() < bot.getY() + 15) {
            bot.jump();
        }

        if (Math.abs(ballCenter - myCenter) < 88) {
            kickTowardsOpponentGoal(bot, ball);
        }

        // Usa el poder cuando el balón se acerca con peligro a su portería
        if (bot.getSpecialMeter() >= 100 && bot.getSpecialAbility() != null && bot.getSpecialAbility().isReady()) {
            boolean peligroEnArco = ballCenter > 520 && ball.getVelocityX() > 2.0;
            if (peligroEnArco) {
                bot.setFacingRight(false);
                bot.useSpecial(ball);
                kickTowardsOpponentGoal(bot, ball);
            }
        }
    }

    @Override
    public String getName() {
        return STRATEGY_DEFENSIVA;
    }

    @Override
    public int getLevelCode() {
        return BotAI.LEVEL_DEFENSIVA;
    }
}
