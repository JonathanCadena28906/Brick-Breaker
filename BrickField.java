// Campo de ladrillos que gestiona la creación y colisión con ladrillos

import java.awt.Color;
import java.awt.Graphics2D;

class BrickField {
    private Brick[][] bricks;
    private int rows;
    private int cols;
    private final int BRICK_WIDTH = 75;
    private final int BRICK_HEIGHT = 20;
    private final int TOP_OFFSET = 50;
    private final int SIDE_OFFSET = 50;
    
    public BrickField(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        initBricks();
    }
    
    private void initBricks() {
        bricks = new Brick[rows][cols];
        
        // Definir colores y puntos para diferentes filas
        Color[] rowColors = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.PINK, Color.CYAN
        };
        
        int[] rowPoints = {7, 7, 5, 5, 3, 3, 1, 1};
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Color color = rowColors[i % rowColors.length];
                int points = rowPoints[i % rowPoints.length];
                int x = j * (BRICK_WIDTH + 10) + SIDE_OFFSET;
                int y = i * (BRICK_HEIGHT + 5) + TOP_OFFSET;
                bricks[i][j] = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, color, points);
            }
        }
    }
    
    public void draw(Graphics2D g) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (bricks[i][j] != null) {
                    bricks[i][j].draw(g);
                }
            }
        }
    }
    
    public Brick checkCollision(Ball ball) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (bricks[i][j] != null && bricks[i][j].isActive() && ball.intersects(bricks[i][j])) {
                    bricks[i][j].setActive(false);
                    return bricks[i][j];
                }
            }
        }
        return null;
    }
    
    public boolean isEmpty() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (bricks[i][j] != null && bricks[i][j].isActive()) {
                    return false;
                }
            }
        }
        return true;
    }
}
