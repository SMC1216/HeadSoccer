package dominio;

import java.io.Serializable;

/**
 * Representa las estadísticas de un jugador que se aplican a todos sus personajes.
 * Todas las estadísticas comienzan en 1 punto y pueden mejorarse hasta 10 puntos.
 * Incluye un sistema de puntos de mejora con opción de reiniciar y redistribuir puntos.
 *
 * @author gaby-mathias
 * @version 2026
 */
public class PlayerStats implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_STAT = 1;
    public static final int MAX_STAT = 10;
    public static final int INITIAL_UPGRADE_POINTS = 4;

    public static final String STAT_SPEED = "SPEED";
    public static final String STAT_JUMP = "JUMP";
    public static final String STAT_KICK = "KICK";
    public static final String STAT_POWER = "POWER";

    private int speed;
    private int jump;
    private int kick;
    private int power;

    private int availablePoints;
    private int totalEarnedPoints;

    /**
     * Constructor por defecto: todas las estadísticas inician en 1 punto
     * y el jugador recibe puntos iniciales para poder aplicar mejoras.
     */
    public PlayerStats() {
        this.speed = MIN_STAT;
        this.jump = MIN_STAT;
        this.kick = MIN_STAT;
        this.power = MIN_STAT;
        this.availablePoints = INITIAL_UPGRADE_POINTS;
        this.totalEarnedPoints = INITIAL_UPGRADE_POINTS;
    }

    /**
     * Mejora en 1 punto la estadística indicada si no ha llegado a 10 y hay puntos disponibles.
     *
     * @param statType Tipo de estadística ("SPEED", "JUMP", "KICK", "POWER").
     * @throws HeadSoccerException Si no hay puntos disponibles, ya está en 10 o el tipo es inválido.
     */
    public void upgradeStat(String statType) throws HeadSoccerException {
        if (statType == null || availablePoints <= 0) {
            throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_INVALIDA);
        }
        String clean = statType.trim().toUpperCase();
        switch (clean) {
            case STAT_SPEED:
                if (speed >= MAX_STAT) throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_INVALIDA);
                speed++;
                availablePoints--;
                break;
            case STAT_JUMP:
                if (jump >= MAX_STAT) throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_INVALIDA);
                jump++;
                availablePoints--;
                break;
            case STAT_KICK:
                if (kick >= MAX_STAT) throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_INVALIDA);
                kick++;
                availablePoints--;
                break;
            case STAT_POWER:
                if (power >= MAX_STAT) throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_INVALIDA);
                power++;
                availablePoints--;
                break;
            default:
                throw new HeadSoccerException(HeadSoccerException.ESTADISTICA_INVALIDA);
        }
    }

    public void upgradeSpeed() throws HeadSoccerException {
        upgradeStat(STAT_SPEED);
    }

    public void upgradeJump() throws HeadSoccerException {
        upgradeStat(STAT_JUMP);
    }

    public void upgradeKick() throws HeadSoccerException {
        upgradeStat(STAT_KICK);
    }

    public void upgradePower() throws HeadSoccerException {
        upgradeStat(STAT_POWER);
    }

    /**
     * Reinicia todas las estadísticas a 1 punto y devuelve todos los puntos ganados
     * para que el jugador pueda volver a repartirlos como prefiera.
     */
    public void resetPoints() {
        this.speed = MIN_STAT;
        this.jump = MIN_STAT;
        this.kick = MIN_STAT;
        this.power = MIN_STAT;
        this.availablePoints = this.totalEarnedPoints;
    }

    /**
     * Otorga puntos de mejora adicionales al jugador (por ejemplo al terminar un partido).
     *
     * @param points Cantidad de puntos a sumar.
     */
    public void addUpgradePoints(int points) {
        if (points > 0) {
            this.availablePoints += points;
            this.totalEarnedPoints += points;
        }
    }

    // =========================================================================
    // CÁLCULO DE VALORES FÍSICOS EFECTIVOS (En nivel 1 conservan los valores base)
    // =========================================================================

    public double getEffectiveSpeed() {
        return 6.0 + ((speed - 1) * 0.4);
    }

    public double getEffectiveJumpForce() {
        return 14.0 + ((jump - 1) * 0.45);
    }

    public double getEffectiveKickForceX() {
        return 13.0 + ((kick - 1) * 0.6);
    }

    public double getEffectiveKickForceY() {
        return -9.5 - ((kick - 1) * 0.25);
    }

    public int getEffectiveSpecialBonus() {
        return power - 1;
    }

    public int getSpeed() { return speed; }
    public int getJump() { return jump; }
    public int getKick() { return kick; }
    public int getPower() { return power; }
    public int getAvailablePoints() { return availablePoints; }
    public int getTotalEarnedPoints() { return totalEarnedPoints; }
}
