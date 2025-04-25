// Representa la paleta controlada por el jugador
import java.awt.Color;
import java.awt.Graphics2D;


class Paddle {
    private int x;
    private int y;
    private int width;
    private int height;
    
    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void moveTo(int mouseX) {
        // Centrar el paddle en la posición del mouse
        this.x = mouseX - (width / 2);
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, width, height);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
}

