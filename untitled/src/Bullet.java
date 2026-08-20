import java.awt.*;

public class Bullet {

    //FIELDS
    private double x;
    private double y;
    private int r;

    private double rad;
    private double speed;
    private double dx;
    private double dy;

    private Color color1;


    //CONSTRUCTOR
     Bullet(double angle, int x, int y){
        this.x = x;
        this.y=y;
        r=10;
        speed=13;
        rad = Math.toRadians(angle);
        dx = Math.cos(rad)*speed;
        dy = Math.sin(rad)*speed;
        color1 = Color.yellow;
     }


     //FUNCTIONS

    //GETTERS
    public double getX() {return x;}
    public double getY() {return y;}
    public int getR() {return r;}

     public boolean update(){
         x += dx;
         y += dy;
         //removing bullet if out of bounds
         if (x<-r || x>GamePanel.WIDTH+r ||
             y<-r || y>GamePanel.HEIGHT+r) return true;
         return false;
     }

    public void draw(Graphics2D g){
         g.setColor(color1);
         g.fillOval((int)x-r,(int)y-r,2*r,2*r);

    }



}
