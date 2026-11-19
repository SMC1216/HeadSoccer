package dominio;

/**
 * clase abstracta para los personajes que juegan en la cancha
 * maneja el movimiento, salto, gravedad, medidor especial y habilidades
 * 
 * @author gaby-mathias
 * @version 2026
 */
public abstract class Character extends GameObject implements ICollidable {
    private static final long serialVersionUID = 1L;

    /** velocidad horizontal al correr */
    private double speed;

    /** impulso vertical del salto */
    private double jumpForce;

    /** velocidad actual en el eje horizontal */
    private double velocityX;

    /** velocidad actual en el eje vertical */
    private double velocityY;

    /** indica si el personaje esta tocando el suelo */
    private boolean isGrounded;

    /** medidor de energia especial de 0 a 100 */
    private int specialMeter;

    /** habilidad especial asignada al personaje */
    private Ability specialAbility;

    /** direccion hacia donde esta mirando el personaje */
    private boolean facingRight;

    /**
     * constructor general para crear un personaje
     * 
     * @param x posicion inicial en x
     * @param y posicion inicial en y
     * @param width ancho del personaje
     * @param height alto del personaje
     * @param specialAbility habilidad especial que puede usar
     * @param facingRight true si mira a la derecha, false a la izquierda
     */
    public Character(double x, double y, int width, int height, Ability specialAbility, boolean facingRight) {
        super(x, y, width, height);
        this.speed = 6.0;
        this.jumpForce = 14.0;
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.isGrounded = true;
        this.specialMeter = 80;
        this.specialAbility = specialAbility;
        this.facingRight = facingRight;
    }

    /**
     * cambia la direccion y velocidad horizontal del personaje
     * 
     * @param direction 1 derecha, -1 izquierda, 0 detenerse
     */
    public void move(int direction) {
        this.velocityX = direction * speed;
        if (direction > 0) {
            this.facingRight = true;
        } else if (direction < 0) {
            this.facingRight = false;
        }
    }

    /**
     * hace que el personaje salte solo si esta sobre el piso
     */
    public void jump() {
        if (isGrounded) {
            this.velocityY = -jumpForce;
            this.isGrounded = false;
        }
    }

    /**
     * patea el balon si esta dentro del rango de alcance
     * 
     * @param ball el balon del partido
     */
    public void kick(Ball ball) {
        if (ball == null) {
            return;
        }
        Bounds kickArea = new Bounds(getX() - 18, getY() - 10, getWidth() + 36, getHeight() + 20);
        if (kickArea.intersects(ball.getBounds())) {
            double dir = facingRight ? 1.0 : -1.0;
            ball.addForce(dir * 13.0, -9.5);
            gainSpecial(20);
        }
    }

    /**
     * activa el poder especial si la barra esta llena y el cooldown disponible
     * 
     * @param ball el balon sobre el que puede actuar la habilidad
     * @return true si logro usar el poder, false si no pudo
     */
    public boolean useSpecial(Ball ball) {
        if (specialMeter >= 100 && specialAbility != null && specialAbility.isReady()) {
            specialAbility.execute(this, ball);
            specialMeter = 0;
            return true;
        }
        return false;
    }

    /**
     * actualiza los segundos restantes de recarga del poder
     */
    public void updateCooldown() {
        if (specialAbility != null) {
            specialAbility.updateTick();
        }
    }

    /**
     * incrementa la energia del poder sin pasarse de 100
     * 
     * @param amount cantidad de energia a sumar
     */
    public void gainSpecial(int amount) {
        specialMeter = Math.min(100, specialMeter + amount);
    }

    /**
     * actualiza la posicion por cada cuadro aplicando gravedad y limites de cancha
     */
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
    }

    /**
     * devuelve la caja de choque del personaje
     * 
     * @return objeto bounds con las coordenadas actuales
     */
    @Override
    public Bounds getBounds() {
        return new Bounds(getX(), getY(), getWidth(), getHeight());
    }

    /**
     * reaccion del personaje al chocar con otro objeto
     * 
     * @param other el objeto contra el que choco
     */
    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Ball) {
            gainSpecial(5);
        }
    }

    /**
     * indica si esta mirando a la derecha
     * 
     * @return true si mira a la derecha
     */
    public boolean isFacingRight() { 
        return facingRight; 
    }
    
    /**
     * cambia la direccion a la que mira
     * 
     * @param facingRight nueva direccion
     */
    public void setFacingRight(boolean facingRight) { 
        this.facingRight = facingRight; 
    }
    
    /**
     * obtiene el nivel de energia especial
     * 
     * @return porcentaje de poder
     */
    public int getSpecialMeter() { 
        return specialMeter; 
    }
    
    /**
     * modifica el medidor especial
     * 
     * @param specialMeter nuevo valor de 0 a 100
     */
    public void setSpecialMeter(int specialMeter) { 
        this.specialMeter = specialMeter; 
    }
    
    /**
     * obtiene la habilidad especial asignada
     * 
     * @return objeto de tipo ability
     */
    public Ability getSpecialAbility() { 
        return specialAbility; 
    }
}
