// Ventana principal del juego que contiene todos los componentes 

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


class GameFrame extends JFrame {
    
    private GamePanel gamePanel;
    
    public GameFrame() {
        setTitle("Brick Breaker Multihilo con Poderes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.stopGame();
            }
        });
    }
}
