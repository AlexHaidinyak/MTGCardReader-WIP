package camera;

import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoundingBox extends JPanel{

    private Point startPoint;
    private Point endPoint;
    private Point topLeft;

    private int width;
    private int height;

    private boolean goodBox = false;

    private MotionDetector motionDetector;
    private Rectangle contourBounds = null;

    public BoundingBox(Webcam webcam) {

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                endPoint = startPoint;
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                motionDetector = new MotionDetector(webcam.getImage(), getTopLeft(), width, height);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public BoundingBox() {

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if(!goodBox) {
                    startPoint = e.getPoint();
                    endPoint = startPoint;
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {

                if(!goodBox) {
                    endPoint = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                if(!goodBox) {
                    JPanel confirmWithUser = new JPanel();
                    confirmWithUser.add(new JLabel("Please confirm, is this the box accurate?"));
                    int userConfirm = JOptionPane.showConfirmDialog(null, confirmWithUser, "Confirm", JOptionPane.YES_NO_OPTION);

                    if(userConfirm == JOptionPane.YES_OPTION) {
                        goodBox = true;
                        motionDetector = new MotionDetector(getTopLeft(), width, height);
                        contourBounds = new Rectangle(getTopLeft().x, getTopLeft().y, width, height);
                    }
                }

            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(startPoint != null && endPoint != null) {
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);

            width = Math.abs(startPoint.x - endPoint.x);
            height = Math.abs(startPoint.y - endPoint.y);

            topLeft = new Point(x, y);

            g.setColor(Color.red);
            g.drawRect(x,y,width,height);
        }//end of if statement for finding width, height, topLeft Point, and then drawing rectangle
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public int getWidthBounds(){
        return width;
    }

    public int getHeightBounds(){
        return height;
    }

    public Rectangle getContourBounds() {
        return contourBounds == null ? null : new Rectangle(contourBounds);
    }

    public MotionDetector getMotionDetector() {
        return motionDetector;
    }

    public void resetMotionDetector(){
        motionDetector.reset();
    }
}
