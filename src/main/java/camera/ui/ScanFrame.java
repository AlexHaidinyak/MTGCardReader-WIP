package camera.ui;

import camera.BoundingBox;
import camera.MTGCardBoundingBox;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanFrame {

    private BoundingBox boundingBox;
    private MTGCardBoundingBox cardContourBox;
    private Webcam webcam;
    private WebcamPanel webcamPanel;

    private JLabel thumbNailLabel = new JLabel();
    private JLabel addedLabel = new JLabel("Added Card", SwingConstants.CENTER);

    private JPanel rightSidePanel = new JPanel();
    private JPanel deckPanel;

    private JScrollPane deckPane;

    private JLayeredPane layeredPane = new JLayeredPane();

    private JTextArea messageScrollText = new JTextArea();

    private JFrame frame = new JFrame();

    private Map<Integer, String> deckNames = new HashMap<>();

    private final int RIGHT_PANEL_WIDTH = 250;
    private final int THUMBNAIL_WIDTH = 244;
    private final int THUMBNAIL_HEIGHT = 340;
    private final int BUTTON_WIDTH = 240;
    private final int BUTTON_HEIGHT = 30;
    private final int MESSAGE_SCROLL_HEIGHT = 150;

    private double ASPECT_RATIO = 0;

    private boolean startProcess = false;

    public ScanFrame(Webcam webcam, BoundingBox boundingBox, MTGCardBoundingBox cardContourBox) {
        this.webcam = webcam;
        this.boundingBox = boundingBox;
        this.cardContourBox = cardContourBox;

        changeResolution(2560, 1600);

        double ratioWidth = webcam.getViewSize().getWidth();
        double ratioHeight = webcam.getViewSize().getHeight();
        ASPECT_RATIO = ratioWidth / ratioHeight;

        int initialWebcamHeight = 800;
        int initialWebcamWidth = (int)(initialWebcamHeight * ASPECT_RATIO);
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setPreferredSize(new Dimension(initialWebcamWidth, initialWebcamHeight));
        webcamPanel.setBounds(0, 0, initialWebcamWidth, initialWebcamHeight);
        webcamPanel.setMinimumSize(new Dimension(initialWebcamWidth, initialWebcamHeight));
        webcamPanel.setFPSDisplayed(false);
        webcamPanel.setImageSizeDisplayed(false);
        webcamPanel.setDisplayDebugInfo(false);
        webcamPanel.setDrawMode(WebcamPanel.DrawMode.FILL);
        webcamPanel.setBackground(Color.DARK_GRAY);

        boundingBox.setOpaque(false);
        boundingBox.setPreferredSize(new Dimension(initialWebcamWidth, initialWebcamHeight));
        boundingBox.setBounds(0, 0, initialWebcamWidth, initialWebcamHeight);
        boundingBox.setBackground(Color.DARK_GRAY);

        cardContourBox.setOpaque(false);
        cardContourBox.setPreferredSize(new Dimension(initialWebcamWidth, initialWebcamHeight));
        cardContourBox.setBounds(0, 0, initialWebcamWidth, initialWebcamHeight);
        cardContourBox.setBackground(Color.DARK_GRAY);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(initialWebcamWidth, initialWebcamHeight));
        layeredPane.add(webcamPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(boundingBox, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(cardContourBox, JLayeredPane.PALETTE_LAYER);
        layeredPane.setBackground(Color.DARK_GRAY);
        layeredPane.setOpaque(true);

        messageScrollText.setEditable(false);
        messageScrollText.setLineWrap(true);
        messageScrollText.setWrapStyleWord(true);
        messageScrollText.setForeground(Color.WHITE);
        messageScrollText.setBackground(Color.DARK_GRAY);
        DefaultCaret caret = (DefaultCaret) messageScrollText.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(messageScrollText);
        scrollPane.setPreferredSize(new Dimension(initialWebcamWidth, MESSAGE_SCROLL_HEIGHT));
        scrollPane.setMinimumSize(new Dimension(initialWebcamWidth, MESSAGE_SCROLL_HEIGHT));
//        scrollPane.setBorder(BorderFactory.createEmptyBorder());
//        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());




//        deckPane.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        buttonPanel.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        buttonPanel.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
                System.exit(0);//<--- GET RID WHEN DONE
            }
        });
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        addedLabel.setVisible(false);
        addedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addedLabel.setForeground(Color.WHITE);
        addedLabel.setBackground(Color.DARK_GRAY);

        thumbNailLabel.setPreferredSize(new Dimension(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
        thumbNailLabel.setMinimumSize(new Dimension(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
        thumbNailLabel.setMaximumSize(new Dimension(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
        thumbNailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        rightSidePanel.setLayout(new BoxLayout(rightSidePanel, BoxLayout.Y_AXIS));
        rightSidePanel.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, initialWebcamHeight));
        rightSidePanel.setMinimumSize(new Dimension(RIGHT_PANEL_WIDTH, initialWebcamHeight));
        rightSidePanel.setMaximumSize(new Dimension(RIGHT_PANEL_WIDTH, initialWebcamHeight));
        rightSidePanel.setBackground(Color.DARK_GRAY);
        rightSidePanel.add(deckPanel);
        rightSidePanel.add(Box.createVerticalGlue());
        rightSidePanel.add(addedLabel);
        rightSidePanel.add(Box.createVerticalStrut(10));
        rightSidePanel.add(thumbNailLabel);
        rightSidePanel.add(Box.createVerticalStrut(10));
        rightSidePanel.add(buttonPanel);

        frame.setLayout(new BorderLayout());
        frame.add(layeredPane, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);
        frame.add(rightSidePanel, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int availWidth = frame.getWidth() - RIGHT_PANEL_WIDTH;
                int availHeight = frame.getHeight() - MESSAGE_SCROLL_HEIGHT -
                                  frame.getInsets().top - frame.getInsets().bottom;

                int newWidth, newHeight;
                if((double) availWidth / availHeight > ASPECT_RATIO) {
                    newHeight = availHeight;
                    newWidth = (int)(availHeight * ASPECT_RATIO);
                }
                else{
                    newWidth = availWidth;
                    newHeight = (int)(availWidth / ASPECT_RATIO);
                }

                int offsetX, offsetY;
                offsetX = (availWidth - newWidth) / 2;
                offsetY = (availHeight - newHeight) / 2;

                webcamPanel.setBounds(0,0,newWidth, newHeight);
                boundingBox.setBounds(0,0,newWidth, newHeight);
                cardContourBox.setBounds(0,0,newWidth, newHeight);
                layeredPane.setBounds(offsetX,offsetY,newWidth,newHeight);
                layeredPane.setPreferredSize(new Dimension(newWidth, newHeight));

                frame.revalidate();
                frame.repaint();
            }
        });

    }

    public void changeResolution(int width, int height) {
        if(webcam != null) {

            webcam.setCustomViewSizes(new Dimension(width, height));
            webcam.setViewSize(new Dimension(width, height));

            double newWidth = webcam.getViewSize().getWidth();
            double newHeight = webcam.getViewSize().getHeight();

            ASPECT_RATIO = (newWidth / newHeight);

            webcam.open();
        }
    }

    public void addThumbnail(String uri){
        try{
            if(!addedLabel.isVisible()){
                addedLabel.setVisible(true);
            }
            URI image = new URI(uri);
            ImageIcon originalImage = new ImageIcon(image.toURL());
            Image scaled = originalImage.getImage().getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_SMOOTH);
            thumbNailLabel.setIcon(new ImageIcon(scaled));
        }
        catch(Exception e){
            thumbNailLabel.setText("");
        }

        frame.validate();
        frame.repaint();
    }

    public int getWebcamWidth() {
        return webcamPanel.getWidth();
    }
    public int getWebcamHeight() {
        return webcamPanel.getHeight();
    }

    public void addAddedMessage(String cardName) {
        messageScrollText.append("Added: " + cardName + "\n\n");
    }

    public void addOCRErrorMessage(int attemptNumber){
        messageScrollText.append("Error in reading card info, retrying: attempt " + attemptNumber + "/3\n\n");
    }
    public void addOCRManualMessage(){
        messageScrollText.append("OCR not reading properly, require manual input\n\n");
    }

    public void addThumbnailErrorMessage(){
        messageScrollText.append("Error in pulling thumbnail icon\n\n");
    }

    private void createDeckScrollPanel(){


        List<String> decks = new ArrayList<>();
        List<String> finalDecks = decks;

        deckNames.forEach((Integer i, String name) ->
            finalDecks.add(name));

        if(deckNames.isEmpty()) {
            setupFirstDeck();
        }

        for(String name: finalDecks){
            decks.add("• " + name);
        }



        decks.add("• A");
        decks.add("• C");
        decks.add("• B");
        decks.add("• Some other deck here");
        decks.add("• Another deck here");

        decks = decks.stream().sorted().toList();
        DefaultListModel<String> listModel = new DefaultListModel<>();

        for(String deck: decks){
            listModel.addElement(deck);
        }

        JList<String> list = new JList<>(listModel);
        list.setBackground(Color.DARK_GRAY);
        list.setForeground(Color.WHITE);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 3. Add double-click functionality
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index != -1) {
                        String selectedItem = listModel.getElementAt(index);
                        selectedItem = selectedItem.substring(1).trim();
                        JLabel label = new JLabel(selectedItem);
                        int result = JOptionPane.showConfirmDialog(frame, label, "Change to this deck?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//                            JOptionPane.showMessageDialog(frame, label);

                        if (result == JOptionPane.OK_OPTION) {
                            System.out.println(selectedItem);
                        }
                    }
                }
            }
        });
        deckPane = new JScrollPane(list);
        deckPane.setPreferredSize(new Dimension(THUMBNAIL_WIDTH, 200));
        deckPane.setBackground(Color.DARK_GRAY);
        JLabel deckLabel = new JLabel("Your Decks: ");
        deckLabel.setForeground(Color.WHITE);
        deckLabel.setBackground(Color.DARK_GRAY);
        deckPanel = new JPanel();
        deckPanel.add(deckLabel);
        deckPanel.add(deckPane);
        deckPanel.setBackground(Color.DARK_GRAY);
    }
    private void getDeckNames(){

    }
    public void setupFirstDeck(){

    }
}
