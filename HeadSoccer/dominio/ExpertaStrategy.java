package dominio;

/**
 * Estrategia Experta para el Bot: reacción instantánea, predicción balística
 * de la trayectoria del balón, control en toda la cancha y uso táctico del poder.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class ExpertaStrategy extends BotStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public void decideMove(BotAI bot, Ball ball) {
        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = bot.getBounds().getCenterX();

        // Predicción de dónde estará el balón en los siguientes fotogramas
        double projectedBallX = ballCenter + (ball.getVelocityX() * 8.0);
        double targetX = projectedBallX + 35.0;

        if (targetX < 150) targetX = 150;
        if (targetX > 745) targetX = 745;

        if (myCenter < targetX - 10) {
            bot.move(1);
        } else if (myCenter > targetX + 10) {
            bot.move(-1);
        } else {
            bot.move(0);
            bot.setFacingRight(false);
        }

        if (ballCenter > myCenter && (ballCenter - myCenter) < 80) {
            bot.jump();
        } else if (Math.abs(projectedBallX - myCenter) < 110 && ball.getY() < bot.getY() + 25) {
            bot.jump();
        }

        if (Math.abs(ballCenter - myCenter) < 95) {
            kickTowardsOpponentGoal(bot, ball);
        }

        if (bot.getSpecialMeter() >= 100 && bot.getSpecialAbility() != null && bot.getSpecialAbility().isReady()) {
            boolean peligro = ball.getVelocityX() > 2.0 && ballCenter > 500;
            boolean contragolpe = ballCenter > 300 && ballCenter < 630 && ball.getY() < Ball.GROUND_Y - 35;
            if (peligro || contragolpe) {
                bot.setFacingRight(false);
                bot.useSpecial(ball);
                kickTowardsOpponentGoal(bot, ball);
            }
        }
    }

    @Override
    public String getName() {
        return STRATEGY_EXPERTA;
    }

    @Override
    public int getLevelCode() {
        return BotAI.LEVEL_EXPERTA;
    }
}
