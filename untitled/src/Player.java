import java.awt.*;

public class Player {

    //FIELDS
    private int x;
    private int y;
    private int r;

    private int dx;
    private int dy;
    private int speed;

    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;

    private boolean firing;
    private long firingTimer;
    private long firingDelay;

    private int lives;

    private Color color1;
    private Color color2;

    private boolean recovering;
    private long recoveryTime;

    private int score;

    //player collects power --> when enough power he increases power level and power gets reset
    private int powerLevel;
    private int power;
    private int[] requiredPower = {1,2,3,4,5};

    //CONSTRUCTOR
    Player(){
        x = GamePanel.WIDTH/2;
        y = GamePanel.HEIGHT/2;
        r = 5;

        dx=0;
        dy=0;
        speed=5;
        lives=3;
        color1 = Color.WHITE;
        color2 = Color.RED;

        firing=false;
        firingTimer = System.nanoTime();
        firingDelay=200; //5 bullets per sec
    }


    //FUNCTIONS

    //GETTERS
    public int getX(){return x;}
    public int getY(){return y;}
    public int getR(){return r;}
    public int getLives(){return lives;}
    public boolean isRecovering(){return recovering;}
    public int getScore(){return score; }

    //SETTERS
    public void setLeft(boolean left) {
        this.left = left;
    }
    public void setRight(boolean right) {
        this.right = right;
    }
    public void setUp(boolean up) {
        this.up = up;
    }
    public void setDown(boolean down) {
        this.down = down;
    }
    public void setFiring(boolean firing) {this.firing = firing;}

    public void loseLife(){
        lives--;
        recovering=true;
        recoveryTime = System.nanoTime();
    }

    public void addScore(int i){
        score += i;
    }

    public void addLife(){
        lives++;
    }

    public void increasePower(int n){
        power += n;
        if (power >= requiredPower[powerLevel]){
            power -= requiredPower[powerLevel];
            powerLevel++;
        }
    }

    public void update(){
        //adding speed in particular dirn
        if (left) dx = -speed;
        if (right) dx = speed;
        if (up) dy = -speed;
        if (down) dy = speed;

        //changing dirn (movement)
        x += dx;
        y += dy;

        //checking bounds
        if (x<r) x=r; //left
        if (y<r); //up
        if (x>GamePanel.WIDTH-r) x=GamePanel.WIDTH-r; //right
        if (y>GamePanel.HEIGHT-r) y=GamePanel.HEIGHT-r; //down

        dx=0;
        dy=0;

        //firing bullets and its timer logic
        if (firing){
            long elapsedTime = (System.nanoTime() - firingTimer) /1000000;
            if (elapsedTime > firingDelay){
                GamePanel.bullets.add(new Bullet(270,x,y));
                firingTimer=System.nanoTime();
            }
        }

        //getting hit, recovering and its timer logic
        long elapsedTime = (System.nanoTime() - recoveryTime)/100000;
        if (elapsedTime > 2000){ //2s invincible delay
            recovering=false;
            recoveryTime=0;
        }

    }

    public void draw(Graphics g){
        if (recovering){
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color2);
            g2.fillOval(x-r,y-r,2*r,2*r);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(color2.darker());
            g2.drawOval(x-r,y-r,2*r,2*r);
            g2.setStroke(new BasicStroke(1));
        }else{
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color1);
            g2.fillOval(x - r, y - r, 2 * r, 2 * r);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(color1.darker());
            g2.drawOval(x - r, y - r, 2 * r, 2 * r);
            g2.setStroke(new BasicStroke(1));
        }
    }


}
