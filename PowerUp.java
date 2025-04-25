// Representa un power-up que puede ser recogido por el jugador
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;

class PowerUp {
    private float x;
    private float y;
    private final int width = 20;
    private final int height = 20;
    private final float fallSpeed = 2.0f;
    private PowerUpType type;
    
    // Tipos de power-ups
    public enum PowerUpType {
        MULTI_BALL,    // Multiplica el número de bolas
        EXTRA_LIFE,    // Da una vida extra
        WIDER_PADDLE,  // Ensancha la paleta
        SLOW_BALL      // Ralentiza todas las bolas
    }
    
    public PowerUp(float x, float y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
    
    public void move() {
        y += fallSpeed;
    }
    
    public void draw(Graphics2D g) {
        // Color según el tipo de power-up
        switch (type) {
            case MULTI_BALL:
                g.setColor(Color.MAGENTA);
                break;
            case EXTRA_LIFE:
                g.setColor(Color.RED);
                break;
            case WIDER_PADDLE:
                g.setColor(Color.BLUE);
                break;
            case SLOW_BALL:
                g.setColor(Color.CYAN);
                break;
        }
        
        // Dibujar el power-up como un círculo con letra indicadora
        g.fillOval((int)x - width/2, (int)y - height/2, width, height);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        
        String letter;
        switch (type) {
            case MULTI_BALL:
                letter = "M";
                break;
            case EXTRA_LIFE:
                letter = "L";
                break;
            case WIDER_PADDLE:
                letter = "W";
                break;
            case SLOW_BALL:
                letter = "S";
                break;
            default:
                letter = "?";
        }
        
        FontMetrics fm = g.getFontMetrics();
        int textX = (int)x - fm.stringWidth(letter) / 2;
        int textY = (int)y + fm.getHeight() / 3;
        g.drawString(letter, textX, textY);
    }
    
    public boolean intersects(Paddle paddle) {
        return (x >= paddle.getX() && x <= paddle.getX() + paddle.getWidth() &&
                y + height/2 >= paddle.getY() && y - height/2 <= paddle.getY() + paddle.getHeight());
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public PowerUpType getType() {
        return type;
    }
}

