// Clase principal que contiene el método main para iniciar el juego

import javax.swing.*;

public class BrickBreakerGame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame gameFrame = new GameFrame();
            gameFrame.setVisible(true);
        });
    }
}
