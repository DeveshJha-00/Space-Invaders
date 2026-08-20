import org.ietf.jgss.GSSManager;

import javax.swing.*;

public class GameFrame extends JFrame{

    GameFrame(){
        this.setTitle("Sasta Space Invader");
        this.setSize(GamePanel.WIDTH,GamePanel.HEIGHT);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.setResizable(false);

        GamePanel panel = new GamePanel();
        this.add(panel);
        this.pack();
        this.setVisible(true);

    }
}
