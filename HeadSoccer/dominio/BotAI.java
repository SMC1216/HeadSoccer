package dominio;

/**
 * Representa al oponente controlado automáticamente por el computador.
 */
public class BotAI extends Character {
    private int difficultyLevel;

    public BotAI(double x, double y, int difficultyLevel) {
        super(x, y, 72, 92, new Teleport(), false);
        this.difficultyLevel = difficultyLevel;
    }

    public void calculateMove(Ball ball) {
        if (ball == null) return;

        double ballCenter = ball.getBounds().getCenterX();
        double myCenter = this.getBounds().getCenterX();

        // El Bot defiende su arco a la derecha y persigue el balón cuando se acerca
        if (ballCenter > 420) {
            if (ballCenter < myCenter - 20) {
                move(-1);
            } else if (ballCenter > myCenter + 20) {
                move(1);
            } else {
                move(0);
            }
        } else {
            // Regresa a cubrir su portería
            if (myCenter < 680) move(1);
            else if (myCenter > 710) move(-1);
            else move(0);
        }

        this.setFacingRight(false);

        // Salta si el balón viene por aire cerca de él
        if (Math.abs(ballCenter - myCenter) < 95 && ball.getY() < getY() + 10) {
            jump();
        }

        // Patea si tiene el balón cerca
        if (Math.abs(ballCenter - myCenter) < 85) {
            kick(ball);
        }

        // Uso inteligente de la habilidad especial (Teleport) cuando está cargada al 100%
        if (getSpecialMeter() >= 100) {
            // Se teletransporta si el balón supera su posición hacia su arco o si está en mitad contraria
            boolean balonPeligroso = ball.getVelocityX() > 3.0 && ballCenter > 550;
            boolean oportunidadAtaque = ballCenter > 450 && ballCenter < 650;
            if (balonPeligroso || oportunidadAtaque) {
                useSpecial(ball);
                kick(ball);
            }
        }
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}