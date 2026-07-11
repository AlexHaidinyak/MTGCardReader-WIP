package camera.ui;

import camera.BoundingBox;
import camera.MTGCardBoundingBox;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;

public class AssortedFrame {
    private Webcam webcam;
    private BoundingBox boundingBox;
    private MTGCardBoundingBox cardContourBox;
    private JLabel thumbNailLabel = new JLabel();
    private JFrame frame = new JFrame();
    private JPanel rightSidePanel = new JPanel();
    private boolean startProcess = false;
    private WebcamPanel webcamPanel;

    public AssortedFrame(Webcam webcam, BoundingBox boundingBox, MTGCardBoundingBox cardContourBox) {
        this.webcam = webcam;
        this.boundingBox = boundingBox;
        this.cardContourBox = cardContourBox;

        webcam.setCustomViewSizes(new Dimension(2560, 1600));
        webcam.setViewSize(new Dimension(2560, 1600));
        webcam.open();

        int headerH = 37;
        int displayW = 1280;
        int displayH = (int)(displayW * (1600 / 2560.0)) - headerH;

        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setPreferredSize(new Dimension(displayW, displayH));
        webcamPanel.setBounds(0,0,displayW, displayH);
        webcamPanel.setFPSDisplayed(false);
        webcamPanel.setDisplayDebugInfo(false);
        webcamPanel.setImageSizeDisplayed(false);
        webcamPanel.setDrawMode(WebcamPanel.DrawMode.FIT);

        //boundingBox = new BoundingBox();
        boundingBox.setOpaque(false);
        boundingBox.setBounds(0,0,displayW, displayH);

        //cardContourBox = new MTGCardBoundingBox();
        cardContourBox.setOpaque(false);
        cardContourBox.setBounds(0,0,displayW, displayH);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(displayW, displayH));
        layeredPane.add(webcamPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(boundingBox, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(cardContourBox, JLayeredPane.PALETTE_LAYER);

        JPanel cameraPanel = new JPanel(null);
        cameraPanel.setPreferredSize(new Dimension(displayW, displayH));
        cameraPanel.setMinimumSize(new Dimension(displayW, displayH));
        cameraPanel.add(layeredPane);
        layeredPane.setBounds(0,0,displayW, displayH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(displayW, 150));
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);

//        JPanel thumbNailPanel = new JPanel(new GridLayout(2,1));
//
//        thumbNailLabel.setPreferredSize(new Dimension(300, 400));
//        thumbNailPanel.add(new JLabel("Added Card"));
//        thumbNailPanel.add(thumbNailLabel);
//        thumbNailPanel.setVisible(true);

        JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5, 0));
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setPreferredSize(new Dimension(240, 30));
        buttonPanel.setMinimumSize(new Dimension(240, 30));
        buttonPanel.setMaximumSize(new Dimension(240, 30));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
//        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startProcess = true;
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startProcess = false;
                System.exit(0);
            }
        });

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);


        rightSidePanel.setLayout(new BoxLayout(rightSidePanel, BoxLayout.Y_AXIS));
        rightSidePanel.setPreferredSize(new Dimension(250, 400));
        rightSidePanel.setMinimumSize(new Dimension(250, 400));
        rightSidePanel.setBackground(Color.DARK_GRAY);
        rightSidePanel.setMaximumSize(new Dimension(250, 400));

        JLabel addedLabel = new JLabel("Added Card");
        addedLabel.setForeground(Color.WHITE);
        addedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        thumbNailLabel.setPreferredSize(new Dimension(244, 340));
        thumbNailLabel.setMinimumSize(new Dimension(244, 340));
        thumbNailLabel.setMaximumSize(new Dimension(244, 340));
        thumbNailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        startButton.setSize(new Dimension(rightSidePanel.getWidth()/2, 50));
        stopButton.setSize(new Dimension(rightSidePanel.getWidth()/2, 50));


        rightSidePanel.add(Box.createVerticalGlue());
        rightSidePanel.add(addedLabel);
        rightSidePanel.add(Box.createVerticalStrut(5));
        rightSidePanel.add(thumbNailLabel);
        rightSidePanel.add(Box.createVerticalStrut(20));
        rightSidePanel.add(buttonPanel);
        rightSidePanel.add(Box.createVerticalStrut(10));


        frame.setLayout(new BorderLayout());
        frame.add(cameraPanel, BorderLayout.CENTER);
        frame.add(rightSidePanel, BorderLayout.EAST);
        frame.add(scrollPane, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1530, 800);
        frame.setVisible(true);
        frame.pack();
        frame.setResizable(true);

        textArea.append(" Please enter some info\n\n");
        textArea.append(" Next bit goes here");
    }

    public void addThumbnail(String uri){
//        thumbNailLabel.setIcon(new ImageIcon(uri));
//        System.out.println(uri);
//        frame.repaint();

        try{
            ImageIcon original = new ImageIcon(new java.net.URL(uri));
            Image scaled = original.getImage().getScaledInstance(244, 340, Image.SCALE_SMOOTH);
            thumbNailLabel.setIcon(new ImageIcon(scaled));
            thumbNailLabel.setText("");
        } catch (MalformedURLException e) {
//            System.out.println("Failed to load thumbnail");
            thumbNailLabel.setText("Failed to load thumbnail");
            throw new RuntimeException(e);
        }

        frame.validate();
        frame.repaint();
    }

    public boolean startProcess() {
        return startProcess;
    }

    public int getWebcamPanelWidth() {
        return webcamPanel.getWidth();
    }
    public int getWebcamPanelHeight() {
        return webcamPanel.getHeight();
    }

//    public static void main(String[] args) throws InterruptedException {
//        webcam = Webcam.getDefault();
//
//        webcam.setCustomViewSizes(new Dimension(2560, 1600));
//        webcam.setViewSize(new Dimension(2560, 1600));
//        webcam.open();
//
//        WebcamPanel webcamPanel = new WebcamPanel(webcam);
//        webcamPanel.setBounds(0,0,2560, 1600);
//
//        boundingBox = new BoundingBox();
//        boundingBox.setOpaque(false);
//        boundingBox.setBounds(0,0,2560, 1600);
//
//        cardContourBox = new MTGCardBoundingBox();
//        cardContourBox.setOpaque(false);
//        cardContourBox.setBounds(0,0,2560, 1600);
//
//        JLayeredPane layeredPane = new JLayeredPane();
//        layeredPane.setPreferredSize(new Dimension(2560, 1600));
//        layeredPane.add(webcamPanel, JLayeredPane.DEFAULT_LAYER);
//        layeredPane.add(boundingBox, JLayeredPane.PALETTE_LAYER);
//        layeredPane.add(cardContourBox, JLayeredPane.PALETTE_LAYER);
//
//        JFrame frame = new JFrame();
//        frame.add(layeredPane);
//        //frame.add(cardContourBox);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//      //  frame.pack();
//        frame.setSize(2560, 1600);
//        frame.setVisible(true);
//
//        while(true){
//            BufferedImage image = webcam.getImage();
//            Rectangle bounds = boundingBox.getContourBounds();
//
//            if(image==null){ break; }
//            if(bounds == null){
//                Thread.sleep(50);
//            }
//            else{
//                BufferedImage subImage = image.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
//
//                if(!boundingBox.getMotionDetector().motionDetected()){
//                    boundingBox.getMotionDetector().checkForMotion(subImage);
//                    Thread.sleep(300);
//                }
//                else{
//                    BufferedImage cardContours = cardContourBox.detectAndDraw(image);
//                    cardContourBox.setDisplayImage(cardContours);
//                    cardContourBox.repaint();
//
//                    if(cardContourBox.isSmoothed()){
//                        Path ocrPath = cardContourBox.getImage();
//                        boolean success;
//                        try{
//                            success = new RunOCR().runImage(ocrPath);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        if(success){
//                            cardContourBox.reset();
//                            boundingBox.resetMotionDetector();
//                            cardContourBox.setDisplayImage(image);
//                            cardContourBox.repaint();
//                            success = false;
//                        }
//                    }
//                }
//                Thread.sleep(100);
//            }
//
//        }
//    }
}

//public class AssortedFrame {
//    private static Webcam webcam;
//    private static BoundingBox boundingBox;
//    private static MTGCardBoundingBox cardContourBox;
//    private static JLabel displayLabel; // single display surface
//
//    public static void main(String[] args) throws InterruptedException {
//        webcam = Webcam.getDefault();
//        webcam.setCustomViewSizes(new Dimension(2560, 1600));
//        webcam.setViewSize(new Dimension(2560, 1600));
//        webcam.open();
//
//        boundingBox    = new BoundingBox();
//        cardContourBox = new MTGCardBoundingBox();
//
//        JLabel displayLabel = new JLabel();
//        displayLabel.setBounds(0, 0, 2560, 1600);
//
//        JFrame frame = new JFrame();
//        frame.add(displayLabel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(2560, 1600);
//        frame.setVisible(true);
//
//        while (true) {
//            BufferedImage image = webcam.getImage();
//            if (image == null) break;
//
//            Rectangle bounds = boundingBox.getContourBounds();
//
//            // Draw red box directly onto the webcam image
//            if (bounds != null) {
//                Graphics2D g = image.createGraphics();
//                g.setColor(Color.RED);
//                g.setStroke(new BasicStroke(3));
//                g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
//                g.dispose();
//            }
//
//            if (bounds == null) {
//                displayLabel.setIcon(new ImageIcon(image));
//                Thread.sleep(50);
//                continue;
//            }
//
//            BufferedImage subImage = image.getSubimage(
//                    bounds.x, bounds.y, bounds.width, bounds.height
//            );
//
//            if (!boundingBox.getMotionDetector().motionDetected()) {
//                boundingBox.getMotionDetector().checkForMotion(subImage);
//                displayLabel.setIcon(new ImageIcon(image));
//                Thread.sleep(300);
//            } else {
//                // detectAndDraw composites onto the same image — all boxes share coordinates
//                BufferedImage result = cardContourBox.detectAndDraw(image);
//                displayLabel.setIcon(new ImageIcon(result));
//            }
//
//            Thread.sleep(100);
//        }
//    }
//}
