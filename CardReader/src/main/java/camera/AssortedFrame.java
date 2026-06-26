package camera;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AssortedFrame {
    private static Webcam webcam;
    private static BoundingBox boundingBox;


    public static void main(String[] args) throws InterruptedException {
        webcam = Webcam.getDefault();

        webcam.setCustomViewSizes(new Dimension(2560, 1600));
        webcam.setViewSize(new Dimension(2560, 1600));

        WebcamPanel webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setBounds(0,0,2560, 1600);

        boundingBox = new BoundingBox();
        boundingBox.setOpaque(false);
        boundingBox.setBounds(0,0,2560, 1600);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(2560, 1600));
        layeredPane.add(webcamPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(boundingBox, JLayeredPane.PALETTE_LAYER);

        JFrame frame = new JFrame();
        frame.add(layeredPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        while(true){
            BufferedImage image = webcam.getImage();
            Rectangle bounds = boundingBox.getContourBounds();

            if(image==null){ break; }
            if(bounds == null){
                Thread.sleep(50);
            }
            else{
                BufferedImage subImage = image.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
                boundingBox.getMotionDetector().checkForMotion(subImage);
                if(boundingBox.getMotionDetector().motionDetected()){
                    System.out.println("Motion detected");
                }
                Thread.sleep(500);
            }

        }
    }
}
