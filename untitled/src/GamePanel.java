import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    //FIELDS
    static final int WIDTH = 400;
    static final int HEIGHT = 400;
    private Thread thread;
    private boolean running;
    private BufferedImage image; //canvas
    private Graphics2D g; //paintbrush
    //FPS LOGIC
    private int FPS = 30;
    private double averageFPS;

    public static Player player;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<PowerUp> powerUps;

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private boolean waveStart;
    private int waveDelay = 2000; //2s gap btwn each wave to display

    private boolean gameOver;

    //CONSTRUCTOR
    GamePanel(){
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.requestFocus();
    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
    }

    // New method to reset game state
    private void resetGame() {
        player = new Player();
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        powerUps = new ArrayList<>();
        waveStartTimer = 0;
        waveStartTimerDiff = 0;
        waveStart = true;
        waveNumber = 0;
        gameOver = false;
    }

    public void run () {
        running = true;
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics(); //paintbrush to the image for off-screen image
        //to make smoother
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        resetGame();


        //FPS LOGIC
        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime=0;
        int frameCount=0;
        int maxFrameCount=30;
        long targetTime=1000/FPS; //time taken by one loop to run to maintain 30FPS
        //GAME LOOP
        while (running) {
            startTime = System.nanoTime(); //curr time in nanosec

            gameUpdate();
            gameRender();
            gameDraw();

            URDTimeMillis = (System.nanoTime() - startTime)/1000000;
            waitTime = targetTime - URDTimeMillis; //extra time we need to wait after each loop (each loop has to be 1000/FPS sec long)
            try{thread.sleep(waitTime);}
            catch (Exception e){System.out.println(e);}
            totalTime += System.nanoTime() - startTime; //total loop times
            frameCount++;
            if (frameCount == maxFrameCount){
                averageFPS = 1000.0/((totalTime/frameCount)/1000000);
                frameCount=0;
                totalTime=0;
            }

        }
    }


    //FUNCTIONS
    //update everything and game logic - positions, projectiles, collisions, etc
    public void gameUpdate(){
        if (gameOver) return;

        //new wave
        if (waveStartTimer ==0 && enemies.size()==0){
            if (waveNumber >= 6) {
                gameOver = true;
                return;
            }
            waveNumber++;
            waveStart=false; //dont create enemies yet, delay to display waveNumber
            waveStartTimer = System.nanoTime();
        }
        else{
            waveStartTimerDiff = (System.nanoTime() - waveStartTimer)/1000000;
            if (waveStartTimerDiff > waveDelay){
                waveStart = true;
                waveStartTimer=0;
                waveStartTimerDiff=0;
            }
        }

        //create enemies in each wave after each wave starts
        if (waveStart && enemies.size()==0 ){
            createNewEnemies();
        }

        //player update
        player.update();

        //bullet update
        for (int i=0;i<bullets.size();i++){
            boolean ifRemove = bullets.get(i).update();
            if (ifRemove){
                bullets.remove(i);
                i--;
            }
        }

        //enemy update
        for (int i=0;i<enemies.size();i++){
            enemies.get(i).update();
        }

        //powerUp update
        for (int i=0;i<powerUps.size();i++){
            boolean ifRemove = powerUps.get(i).update();
            if (ifRemove){
                powerUps.remove(i);
                i--;
            }
        }

        //bullet-enemy collision
        for (int i=0;i<bullets.size();i++){
            Bullet b = bullets.get(i);
            double bx = b.getX();
            double by = b.getY();
            double br = b.getR();
            for (int j=0;j<enemies.size();j++){
                Enemy e = enemies.get(j);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();

                //check for collision using pythagoreas theorem
                double dx = bx-ex;
                double dy = by-ey;
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist < br+er){
                    e.hit();
                    bullets.remove(i);
                    i--;
                    break;
                }

            }
        }

//        check dead enemies
        for (int i=0;i<enemies.size();i++){
            Enemy e = enemies.get(i);
            if (e.isDead()){
                //powerUp chance
                double random = Math.random();
                if (random<0.001){
                    powerUps.add(new PowerUp(1,e.getX(),e.getY()));
                }else if (random <0.02){
                    powerUps.add(new PowerUp(3,e.getX(),e.getY()));
                }else if (random<0.12){
                    powerUps.add(new PowerUp(2,e.getX(),e.getY()));
                }

                player.addScore(e.getType() + e.getRank());
                enemies.remove(i);
                i--;
            }
        }


        //player-enemy collision
        if (!player.isRecovering()){
            int px = player.getX();
            int py = player.getY();
            int pr = player.getR();
            for (int i=0;i<enemies.size();i++){
                Enemy e = enemies.get(i);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();
                double dx = px-ex;
                double dy = py-ey;
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist < pr+er){ //player-enemy hit
                    player.loseLife();
                    if (player.getLives()==0){
                        gameOver=true;
                    }
                }
            }
        }

        //player-powerUp collision
        int px = player.getX();
        int py = player.getY();
        int pr = player.getR();
        for (int i=0;i<powerUps.size();i++){
            PowerUp p = powerUps.get(i);
            double x = p.getX();
            double y = p.getY();
            double r = p.getR();
            double dx = px-x;
            double dy = py-y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            if (dist < pr+r){ //collected powerUp
                int type = p.getType();
                if (type==1) player.addLife();
                if (type==2) player.increasePower(1);
                if (type==3) player.increasePower(2);
                powerUps.remove(i);
                i--;
            }
        }
    }


    //Draw everything as off-screen image
    public void gameRender() {
        //draw background
        g.setColor(new Color(0, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        if (!gameOver) {
            //draw player
            player.draw(g);

            //draw bullet
            for (int i = 0; i < bullets.size(); i++) {
                bullets.get(i).draw(g);
            }

            //draw enemy
            for (int i = 0; i < enemies.size(); i++) {
                enemies.get(i).draw(g);
            }

            //draw powerUps
            for (int i = 0; i < powerUps.size(); i++) {
                powerUps.get(i).draw(g);
            }

            //draw waveNumber
            if (waveStartTimer != 0) {
                g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
                String msg = "- W A V E - " + waveNumber + " -";
                int len = (int) g.getFontMetrics().getStringBounds(msg, g).getWidth();
                int alpha = (int) (255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay)); //for transparency
                if (alpha > 255) alpha = 255;
                g.setColor(new Color(255, 255, 255, alpha));
                g.drawString(msg, WIDTH / 2 - len / 2, HEIGHT / 2);
            }

            //draw player lives
            for (int i = 0; i < player.getLives(); i++) {
                g.setColor(Color.white);
                g.fillOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
                g.setStroke(new BasicStroke(3));
                g.setColor(Color.WHITE.darker());
                g.drawOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
                g.setStroke(new BasicStroke(3));
            }

            //draw player score
            g.setColor(Color.white);
            g.setFont(new Font("Century Gothic", Font.PLAIN, 15));
            g.drawString("SCORE : " + player.getScore(), WIDTH - 100, 30);
        } else {
            // Draw game over screen
            g.setFont(new Font("Century Gothic", Font.BOLD, 30));
            String gameOverMsg = "GAME OVER";
            String pressSpaceMsg = "Press SPACE to restart";

            int gameOverLen = (int) g.getFontMetrics().getStringBounds(gameOverMsg, g).getWidth();
            g.setColor(Color.WHITE);
            g.drawString(gameOverMsg, WIDTH / 2 - gameOverLen / 2, HEIGHT / 2 - 30);

            g.setFont(new Font("Century Gothic", Font.PLAIN, 20));
            int pressSpaceLen = (int) g.getFontMetrics().getStringBounds(pressSpaceMsg, g).getWidth();
            g.drawString(pressSpaceMsg, WIDTH / 2 - pressSpaceLen / 2, HEIGHT / 2 + 30);

            // Draw final score
            g.setFont(new Font("Century Gothic", Font.PLAIN, 20));
            String finalScoreMsg = "Final Score: " + player.getScore();
            int scoreLen = (int) g.getFontMetrics().getStringBounds(finalScoreMsg, g).getWidth();
            g.drawString(finalScoreMsg, WIDTH / 2 - scoreLen / 2, HEIGHT / 2 + 70);
        }
    }

    //Drawing onto the panel
    public void gameDraw(){
        Graphics g2 = this.getGraphics(); //paintbrush for actual screen
        g2.drawImage(image,0,0,null); //draw actual image on the screen
        g2.dispose();
    }


    private void createNewEnemies(){
        enemies.clear();
        if (waveNumber==1){
            for (int i=0;i<4;i++){
                enemies.add(new Enemy(1,1));
            }
        }
        if (waveNumber==2){
            for (int i=0;i<4;i++){
                enemies.add(new Enemy(1,1));
                enemies.add(new Enemy(1,2));
            }
        }
        if (waveNumber==3){
            for (int i=0;i<3;i++){
                enemies.add(new Enemy(1,1));
                enemies.add(new Enemy(1,2));
                enemies.add(new Enemy(2,1));
            }
        }
        if (waveNumber==4){
            for (int i=0;i<3;i++){
                enemies.add(new Enemy(1,2));
                enemies.add(new Enemy(2,1));
                enemies.add(new Enemy(2,2));
            }
        }
        if (waveNumber==5){
            for (int i=0;i<3;i++){
                enemies.add(new Enemy(2,1));
                enemies.add(new Enemy(2,2));
                enemies.add(new Enemy(3,1));
            }
        }
        if (waveNumber==6){
            for (int i=0;i<3;i++){
                enemies.add(new Enemy(2,2));
                enemies.add(new Enemy(3,1));
                enemies.add(new Enemy(3,2));
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                resetGame();
                return;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT){
            player.setLeft(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT){
            player.setRight(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_UP){
            player.setUp(true);
        }if (e.getKeyCode() == KeyEvent.VK_DOWN){
            player.setDown(true);
        }

        if (e.getKeyCode() == KeyEvent.VK_Z){
            player.setFiring(true);
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT){
            player.setLeft(false);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT){
            player.setRight(false);
        }
        if (e.getKeyCode() == KeyEvent.VK_UP){
            player.setUp(false);
        }if (e.getKeyCode() == KeyEvent.VK_DOWN){
            player.setDown(false);
        }
        if (e.getKeyCode() == KeyEvent.VK_Z){
            player.setFiring(false);
        }
    }
}
