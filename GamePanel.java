// Panel del juego donde se renderiza todo y se controla la lógica del juego

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


class GamePanel extends JPanel {
    
    // Componentes del juego
    private Paddle paddle;
    private ArrayList<Ball> balls;
    private BrickField brickField;
    private ScoreManager scoreManager;
    private ArrayList<PowerUp> powerUps;
    
    // Estado del juego
    private boolean isRunning;
    private boolean isPaused;
    private boolean gameOver;
    private int lives;
    
    // Hilos del juego
    private GameLogicThread gameLogicThread;
    private RenderThread renderThread;
    private BrickCollisionThread brickCollisionThread;
    private PowerUpThread powerUpThread;
    
    // Generador de números aleatorios
    private Random random;
    
    // Sincronización
    private final Object gameStateLock = new Object();
    
    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        random = new Random();
        initGame();
        setupInputHandlers();
    }
    
    private void initGame() {
        // Inicializar componentes
        paddle = new Paddle(350, 530, 100, 15);
        balls = new ArrayList<>();
        balls.add(new Ball(400, 400, 10, 3, -3));
        brickField = new BrickField(8, 8);
        scoreManager = new ScoreManager();
        powerUps = new ArrayList<>();
        
        // Estado inicial
        isRunning = true;
        isPaused = false;
        gameOver = false;
        lives = 3;
        
        // Iniciar hilos
        startThreads();
    }
    
    private void setupInputHandlers() {
        // Controlar el paddle con el mouse
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                synchronized(gameStateLock) {
                    if (isRunning && !isPaused) {
                        paddle.moveTo(e.getX());
                    }
                }
            }
        });
        
        // Teclas para pausar y reiniciar
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized(gameStateLock) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        isPaused = !isPaused;
                        if (!isPaused) {
                            gameStateLock.notifyAll(); // Notificar a los hilos que esperan
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_R) {
                        if (gameOver) {
                            resetGame();
                        }
                    }
                }
            }
        });
    }
    
    private void startThreads() {
        // Hilo para la lógica del juego (movimiento, colisiones básicas)
        gameLogicThread = new GameLogicThread();
        gameLogicThread.start();
        
        // Hilo para renderizar el juego
        renderThread = new RenderThread();
        renderThread.start();
        
        // Hilo para detectar colisiones con los ladrillos
        brickCollisionThread = new BrickCollisionThread();
        brickCollisionThread.start();
        
        // Hilo para manejar los power-ups
        powerUpThread = new PowerUpThread();
        powerUpThread.start();
    }
    
    public void stopGame() {
        synchronized(gameStateLock) {
            isRunning = false;
            gameStateLock.notifyAll(); // Despertar a todos los hilos que están esperando
        }
        
        // Esperar a que los hilos terminen
        try {
            if (gameLogicThread != null) gameLogicThread.join();
            if (renderThread != null) renderThread.join();
            if (brickCollisionThread != null) brickCollisionThread.join();
            if (powerUpThread != null) powerUpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void resetGame() {
        synchronized(gameStateLock) {
            // Reiniciar componentes
            paddle = new Paddle(350, 530, 100, 15);
            balls = new ArrayList<>();
            balls.add(new Ball(400, 400, 10, 3, -3));
            brickField = new BrickField(8, 8);
            scoreManager.resetScore();
            powerUps = new ArrayList<>();
            
            // Reiniciar estado
            isPaused = false;
            gameOver = false;
            lives = 3;
            
            // Notificar a los hilos
            gameStateLock.notifyAll();
        }
    }
    
    // Método para crear un power-up con cierta probabilidad
    private void spawnPowerUp(int x, int y) {
        // 25% de probabilidad de generar un power-up
        if (random.nextInt(100) < 25) {
            PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
            PowerUp.PowerUpType randomType = types[random.nextInt(types.length)];
            
            PowerUp powerUp = new PowerUp(x, y, randomType);
            synchronized(gameStateLock) {
                powerUps.add(powerUp);
            }
        }
    }
    
    // Método para activar un power-up
    private void activatePowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case MULTI_BALL:
                // Duplicar todas las bolas actuales
                synchronized(gameStateLock) {
                    int currentBallCount = balls.size();
                    for (int i = 0; i < currentBallCount; i++) {
                        Ball originalBall = balls.get(i);
                        // Crear dos bolas nuevas con direcciones ligeramente diferentes
                        balls.add(new Ball(
                            originalBall.getX(), 
                            originalBall.getY(), 
                            originalBall.getSize(), 
                            originalBall.getVelocityX() * 0.9f, 
                            originalBall.getVelocityY() * 1.1f
                        ));
                        balls.add(new Ball(
                            originalBall.getX(), 
                            originalBall.getY(), 
                            originalBall.getSize(), 
                            originalBall.getVelocityX() * 1.1f, 
                            originalBall.getVelocityY() * 0.9f
                        ));
                    }
                }
                break;
                
            case EXTRA_LIFE:
                synchronized(gameStateLock) {
                    lives++;
                }
                break;
                
            case WIDER_PADDLE:
                synchronized(gameStateLock) {
                    paddle.setWidth(paddle.getWidth() + 30); // Aumentar ancho del paddle temporalmente
                    // En un juego real, podríamos hacer que este efecto sea temporal
                }
                break;
                
            case SLOW_BALL:
                synchronized(gameStateLock) {
                    for (Ball ball : balls) {
                        ball.slowDown();
                    }
                }
                break;
        }
        
        scoreManager.addPoints(25); // Bonus por atrapar power-up
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Dibujar con antialiasing
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        synchronized(gameStateLock) {
            // Dibujar componentes
            paddle.draw(g2d);
            
            // Dibujar todas las bolas
            for (Ball ball : balls) {
                ball.draw(g2d);
            }
            
            brickField.draw(g2d);
            
            // Dibujar power-ups
            for (PowerUp powerUp : powerUps) {
                powerUp.draw(g2d);
            }
            
            // Dibujar información del juego
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2d.drawString("Score: " + scoreManager.getScore(), 20, 30);
            g2d.drawString("Lives: " + lives, 700, 30);
            g2d.drawString("Balls: " + balls.size(), 580, 30);
            
            // Mensajes del estado del juego
            if (isPaused) {
                drawCenteredString(g2d, "PAUSED", getWidth() / 2, getHeight() / 2);
                drawCenteredString(g2d, "Press SPACE to continue", getWidth() / 2, getHeight() / 2 + 30);
            }
            
            if (gameOver) {
                drawCenteredString(g2d, "GAME OVER", getWidth() / 2, getHeight() / 2);
                drawCenteredString(g2d, "Final Score: " + scoreManager.getScore(), getWidth() / 2, getHeight() / 2 + 30);
                drawCenteredString(g2d, "Press R to restart", getWidth() / 2, getHeight() / 2 + 60);
            }
            
            if (brickField.isEmpty() && !gameOver) {
                drawCenteredString(g2d, "VICTORY!", getWidth() / 2, getHeight() / 2);
                drawCenteredString(g2d, "Final Score: " + scoreManager.getScore(), getWidth() / 2, getHeight() / 2 + 30);
                drawCenteredString(g2d, "Press R to restart", getWidth() / 2, getHeight() / 2 + 60);
                gameOver = true;
            }
        }
    }
    
    private void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        g.drawString(text, x - textWidth / 2, y);
    }
    
    // Hilo para la lógica del juego (movimiento de la bola, colisiones con paddle y paredes)
    class GameLogicThread extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                synchronized(gameStateLock) {
                    while (isPaused && isRunning) {
                        try {
                            gameStateLock.wait(); // Esperar a que el juego continúe
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    
                    if (!gameOver && isRunning) {
                        // Lista para almacenar bolas que se han perdido
                        ArrayList<Ball> lostBalls = new ArrayList<>();
                        
                        // Mover todas las bolas y comprobar colisiones
                        for (Ball ball : balls) {
                            // Mover la bola
                            ball.move();
                            
                            // Comprobar colisión con paredes
                            if (ball.getX() <= 0 || ball.getX() >= getWidth() - ball.getSize()) {
                                ball.reverseX();
                            }
                            
                            if (ball.getY() <= 0) {
                                ball.reverseY();
                            }
                            
                            // Comprobar si la bola cae
                            if (ball.getY() >= getHeight()) {
                                lostBalls.add(ball);
                            }
                            
                            // Comprobar colisión con paddle
                            if (ball.intersects(paddle)) {
                                ball.reverseY();
                                
                                // Cambiar ángulo de rebote según donde golpee en el paddle
                                float relativeIntersect = (ball.getX() - paddle.getX()) / paddle.getWidth();
                                float bounceAngle = (relativeIntersect - 0.5f) * 1.5f;
                                ball.adjustVelocity(bounceAngle);
                            }
                        }
                        
                        // Eliminar bolas perdidas
                        if (!lostBalls.isEmpty()) {
                            balls.removeAll(lostBalls);
                            
                            // Si no quedan bolas, perder una vida
                            if (balls.isEmpty()) {
                                lives--;
                                if (lives <= 0) {
                                    gameOver = true;
                                } else {
                                    // Reposicionar una nueva bola
                                    balls.add(new Ball(400, 400, 10, 3, -3));
                                }
                            }
                        }
                    }
                }
                
                try {
                    Thread.sleep(16); // 60 FPS aproximadamente
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    
    // Hilo para renderizar el juego
    class RenderThread extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                // Hacer que se repinte el panel
                repaint();
                
                try {
                    Thread.sleep(16); // 60 FPS aproximadamente
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    
    // Hilo para detectar colisiones con los ladrillos
    class BrickCollisionThread extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                synchronized(gameStateLock) {
                    while (isPaused && isRunning) {
                        try {
                            gameStateLock.wait(); // Esperar a que el juego continúe
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    
                    if (!gameOver && isRunning) {
                        // Comprobar colisiones con ladrillos para cada bola
                        for (Ball ball : new ArrayList<>(balls)) { // Copia para evitar ConcurrentModificationException
                            Brick hitBrick = brickField.checkCollision(ball);
                            if (hitBrick != null) {
                                ball.reverseY();
                                scoreManager.addPoints(hitBrick.getPoints());
                                
                                // Generar power-up con cierta probabilidad
                                spawnPowerUp(hitBrick.getX() + hitBrick.getWidth()/2, hitBrick.getY() + hitBrick.getHeight()/2);
                            }
                        }
                    }
                }
                
                try {
                    Thread.sleep(10); // Comprobar colisiones con más frecuencia
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    
    // Hilo para manejar los power-ups (movimiento y colisiones)
    class PowerUpThread extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                synchronized(gameStateLock) {
                    while (isPaused && isRunning) {
                        try {
                            gameStateLock.wait(); // Esperar a que el juego continúe
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    
                    if (!gameOver && isRunning) {
                        // Mover power-ups y comprobar colisiones con el paddle
                        Iterator<PowerUp> iterator = powerUps.iterator();
                        while (iterator.hasNext()) {
                            PowerUp powerUp = iterator.next();
                            powerUp.move();
                            
                            // Eliminar si sale de la pantalla
                            if (powerUp.getY() > getHeight()) {
                                iterator.remove();
                                continue;
                            }
                            
                            // Comprobar colisión con paddle
                            if (powerUp.intersects(paddle)) {
                                activatePowerUp(powerUp);
                                iterator.remove();
                            }
                        }
                    }
                }
                
                try {
                    Thread.sleep(16); // Actualizar al mismo ritmo que la lógica del juego
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}

