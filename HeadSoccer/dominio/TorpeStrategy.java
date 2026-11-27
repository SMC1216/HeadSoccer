package dominio;

/**
 * Estrategia Torpe para el Bot: reacción lenta con demora de frames,
 * menor alcance de patada y sin uso de poderes especiales.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class TorpeStrategy extends BotStrategy {
    private static final long serialVersionUID = 1L;

    private int delayCounter = 0;
    private int currentDirection = 0;

    @Override
    public void decideMove(BotAI bot, Ball ball) {
        delayCounter++;
        if (delayCounter < 6) {
            bot.move(currentDirection);
            return;
        }
        delayCounter = 0;

        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = bot.getBounds().getCenterX();

        if (ballCenter > 360) {
            double targetX = Math.min(745.0, ballCenter + 30.0);
            if (myCenter > targetX + 25) {
                currentDirection = -1;
            } else if (myCenter < targetX - 25) {
                currentDirection = 1;
            } else {
                currentDirection = 0;
            }
        } else {
            if (myCenter < 680) currentDirection = 1;
            else if (myCenter > 720) currentDirection = -1;
            else currentDirection = 0;
        }

        bot.move(currentDirection);
        if (currentDirection == 0) {
            bot.setFacingRight(false);
        }

        if (Math.abs(ballCenter - myCenter) < 60 && ball.getY() < bot.getY()) {
            bot.jump();
        }

        if (Math.abs(ballCenter - myCenter) < 62) {
            kickTowardsOpponentGoal(bot, ball);
        }
    }

    @Override
    public String getName() {
        return STRATEGY_TORPE;
    }

    @Override
    public int getLevelCode() {
        return BotAI.LEVEL_TORPE;
    }
}
