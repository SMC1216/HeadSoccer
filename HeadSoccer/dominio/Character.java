package dominio;

import java.awt.Color;

/**
 * Clase abstracta que representa a un personaje (muñeco) dentro de la cancha.
 * Gestiona movimiento, salto, patada, equipo, camiseta, estadísticas (1-10),
 * habilidad especial y el aura energética que emite cuando activa un poder.
 */
public abstract class Character extends GameObject implements ICollidable {
    private static final long serialVersionUID = 1L;

    private double speed;
    private double jumpForce;
    private double velocityX;
    private double velocityY;
    private boolean isGrounded;
    private int specialMeter;
    private Ability specialAbility;
    private boolean facingRight;

    // Personalización de estilo, equipo y estadísticas del jugador
    private Team team;
    private Color shirtColor;
    private PlayerStats stats;

    // Estado del aura al activar un poder especial
    private boolean auraActive;
    private Color auraColor;
    private int auraTickCounter;

    public Character(double x, double y, int width, int height, Ability specialAbility, boolean facingRight) {
        this(
            x,
            y,
            width,
            height,
            specialAbility,
            facingRight,
            facingRight ? Team.colombia() : Team.alemania()
        );
    }

    public Character(double x, double y, int width, int height, Ability specialAbility, boolean facingRight, Team team) {
        super(x, y, width, height);
        this.stats = new PlayerStats();
        this.speed = stats.getEffectiveSpeed();
        this.jumpForce = stats.getEffectiveJumpForce();
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.isGrounded = true;
        this.specialMeter = 80;
        this.specialAbility = specialAbility != null ? specialAbility : new FireStrike();
        this.facingRight = facingRight;
        this.team = team != null ? team : (facingRight ? Team.colombia() : Team.alemania());
        this.shirtColor = this.team.getPrimaryShirtColor();
        this.auraActive = false;
        this.auraColor = this.specialAbility.getAuraColor();
        this.auraTickCounter = 0;
    }

    public void move(int direction) {
        this.speed = getStats().getEffectiveSpeed();
        this.velocityX = direction * speed;
        if (direction > 0) this.facingRight = true;
        else if (direction < 0) this.facingRight = false;
    }

    public void jump() {
        if (isGrounded) {
            this.jumpForce = getStats().getEffectiveJumpForce();
            this.velocityY = -jumpForce;
            this.isGrounded = false;
        }
    }

    public void kick(Ball ball) {
        if (ball == null) return;
        Bounds kickArea = new Bounds(getX() - 18, getY() - 10, getWidth() + 36, getHeight() + 20);
        if (kickArea.intersects(ball.getBounds())) {
            double dir = facingRight ? 1.0 : -1.0;
            double forceX = getStats().getEffectiveKickForceX();
            double forceY = getStats().getEffectiveKickForceY();
            ball.addForce(dir * forceX, forceY);
            gainSpecial(20 + getStats().getEffectiveSpecialBonus());
        }
    }

    /**
     * Activa la habilidad especial del jugador si tiene energía al 100% y no está en cooldown.
     * Al activarse, despliega un aura del color propio del poder invocado.
     *
     * @param ball El balón sobre el cual actúa el poder.
     * @return true si el poder se activó exitosamente, false en caso contrario.
     */
    public boolean useSpecial(Ball ball) {
        if (specialMeter >= 100 && specialAbility != null && specialAbility.isReady()) {
            specialAbility.execute(this, ball);
            activatePowerAura(specialAbility.getAuraColor());
            specialMeter = 0;
            return true;
        }
        return false;
    }

    /**
     * Enciende el aura energética del personaje con el color del poder activado.
     *
     * @param powerColor Color del aura asociado al poder ejecutado.
     */
    public void activatePowerAura(Color powerColor) {
        this.auraActive = true;
        if (powerColor != null) {
            this.auraColor = powerColor;
        } else if (this.specialAbility != null && this.specialAbility.getAuraColor() != null) {
            this.auraColor = this.specialAbility.getAuraColor();
        } else {
            this.auraColor = new Color(255, 215, 0);
        }
        this.auraTickCounter = 0;
    }

    public void deactivatePowerAura() {
        this.auraActive = false;
        this.auraTickCounter = 0;
    }

    public boolean isAuraActive() {
        return auraActive || (specialAbility != null && specialAbility.isActive());
    }

    public Color getAuraColor() {
        if (auraColor != null) {
            return auraColor;
        }
        if (specialAbility != null && specialAbility.getAuraColor() != null) {
            return specialAbility.getAuraColor();
        }
        return new Color(255, 215, 0);
    }

    /**
     * Actualiza el enfriamiento de la habilidad especial y el estado del aura (invocado cada segundo).
     */
    public void updateCooldown() {
        if (specialAbility != null) {
            specialAbility.updateTick();
            if (!specialAbility.isActive()) {
                this.auraActive = false;
            }
        } else {
            this.auraActive = false;
        }
    }

    public void gainSpecial(int amount) {
        specialMeter = Math.min(100, specialMeter + amount);
    }

    @Override
    public void updatePosition() {
        if (!isGrounded) {
            velocityY += Ball.GRAVITY;
        }

        setX(getX() + velocityX);
        setY(getY() + velocityY);

        if (getY() + getHeight() >= Ball.GROUND_Y) {
            setY(Ball.GROUND_Y - getHeight());
            velocityY = 0.0;
            isGrounded = true;
        }

        if (getX() < Ball.LEFT_WALL + 55) {
            setX(Ball.LEFT_WALL + 55);
        } else if (getX() + getWidth() > Ball.RIGHT_WALL - 55) {
            setX(Ball.RIGHT_WALL - 55 - getWidth());
        }

        if (isAuraActive()) {
            auraTickCounter++;
        }
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getX(), getY(), getWidth(), getHeight());
    }

    public void resetState(double startX, double startY, boolean faceRight) {
        setPosition(startX, startY);
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.isGrounded = true;
        this.facingRight = faceRight;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other == null || other == this) {
            return;
        }
        if (other instanceof Ball) {
            gainSpecial(5 + (getStats().getEffectiveSpecialBonus() / 2));
        } else if (other instanceof Character) {
            resolveCharacterCollision((Character) other);
        }
    }

    private void resolveCharacterCollision(Character other) {
        Bounds myBounds = this.getBounds();
        Bounds otherBounds = other.getBounds();

        if (!myBounds.intersects(otherBounds)) {
            return;
        }

        double myCenterX = myBounds.getCenterX();
        double otherCenterX = otherBounds.getCenterX();
        double myCenterY = myBounds.getCenterY();
        double otherCenterY = otherBounds.getCenterY();

        if (Math.abs(myCenterY - otherCenterY) > 35 && Math.abs(myCenterX - otherCenterX) < 55) {
            if (myCenterY < otherCenterY) {
                this.velocityY = -getStats().getEffectiveJumpForce() * 0.55;
                this.isGrounded = false;
            } else {
                other.velocityY = -other.getStats().getEffectiveJumpForce() * 0.55;
                other.isGrounded = false;
            }
        }

        double minDistanceX = (this.getWidth() + other.getWidth()) / 2.0;
        double currentDistX = Math.abs(myCenterX - otherCenterX);
        double overlapX = minDistanceX - currentDistX;

        if (overlapX > 0) {
            boolean iAmLeft = (myCenterX < otherCenterX) || (myCenterX == otherCenterX && this.getX() <= other.getX());
            double push = (overlapX / 2.0) + 1.0;

            double minX = Ball.LEFT_WALL + 55;
            double maxThisX = Ball.RIGHT_WALL - 55 - this.getWidth();
            double maxOtherX = Ball.RIGHT_WALL - 55 - other.getWidth();

            if (iAmLeft) {
                double newThisX = Math.max(minX, this.getX() - push);
                double newOtherX = Math.min(maxOtherX, other.getX() + push);

                if (newThisX + this.getWidth() > newOtherX) {
                    newOtherX = Math.min(maxOtherX, newThisX + this.getWidth() + 1.0);
                }
                if (newThisX + this.getWidth() > newOtherX) {
                    newThisX = Math.max(minX, newOtherX - this.getWidth() - 1.0);
                }

                this.setX(newThisX);
                other.setX(newOtherX);

                if (this.velocityX > 0) this.velocityX = 0;
                if (other.velocityX < 0) other.velocityX = 0;
            } else {
                double newOtherX = Math.max(minX, other.getX() - push);
                double newThisX = Math.min(maxThisX, this.getX() + push);

                if (newOtherX + other.getWidth() > newThisX) {
                    newThisX = Math.min(maxThisX, newOtherX + other.getWidth() + 1.0);
                }
                if (newOtherX + other.getWidth() > newThisX) {
                    newOtherX = Math.max(minX, newThisX - other.getWidth() - 1.0);
                }

                other.setX(newOtherX);
                this.setX(newThisX);

                if (this.velocityX < 0) this.velocityX = 0;
                if (other.velocityX > 0) other.velocityX = 0;
            }
        }
    }

    public void setTeam(Team team) throws HeadSoccerException {
        if (team == null) {
            throw new HeadSoccerException(HeadSoccerException.EQUIPO_INVALIDO);
        }
        this.team = team;
        this.shirtColor = team.getPrimaryShirtColor();
    }

    public void setTeamByName(String teamName) throws HeadSoccerException {
        setTeam(TeamFactory.create(teamName));
    }

    public void setShirtColor(Color shirtColor) throws HeadSoccerException {
        if (shirtColor == null) {
            throw new HeadSoccerException(HeadSoccerException.COLOR_INVALIDO);
        }
        this.shirtColor = shirtColor;
    }

    public void setSpecialAbilityByName(String abilityName) throws HeadSoccerException {
        setSpecialAbility(AbilityFactory.create(abilityName));
    }

    public PlayerStats getStats() {
        if (stats == null) {
            stats = new PlayerStats();
        }
        return stats;
    }

    public void setStats(PlayerStats stats) {
        if (stats != null) {
            this.stats = stats;
            this.speed = stats.getEffectiveSpeed();
            this.jumpForce = stats.getEffectiveJumpForce();
        }
    }

    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public int getSpecialMeter() { return specialMeter; }
    public void setSpecialMeter(int specialMeter) { this.specialMeter = specialMeter; }
    public Ability getSpecialAbility() { return specialAbility; }
    public void setSpecialAbility(Ability specialAbility) {
        if (specialAbility != null) {
            this.specialAbility = specialAbility;
            this.auraColor = specialAbility.getAuraColor();
            this.auraActive = specialAbility.isActive();
        }
    }
    public Team getTeam() { return team != null ? team : Team.colombia(); }
    public Color getShirtColor() { return shirtColor != null ? shirtColor : getTeam().getPrimaryShirtColor(); }
    public int getAuraTickCounter() { return auraTickCounter; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public boolean isGrounded() { return isGrounded; }
}
