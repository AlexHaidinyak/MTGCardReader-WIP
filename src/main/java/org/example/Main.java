package org.example;

import api.CardData;
import camera.BoundingBox;
import camera.MTGCardBoundingBox;
import camera.ui.AssortedFrame;
import camera.ui.ManualInput;
import camera.ui.ScanFrame;
import com.github.sarxos.webcam.Webcam;
import database.UpdateDatabase;
import ocr.RunOCR;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static Webcam webcam = Webcam.getDefault();
    private static BoundingBox boundingBox = new BoundingBox();
    private static MTGCardBoundingBox cardContourBox = new MTGCardBoundingBox();
    private static String[] ocrResults;
    private static CardData cardData;


    public static void main(String[] args) throws InterruptedException {

        ScanFrame frame = new ScanFrame(webcam, boundingBox, cardContourBox);
        ManualInput.getBoundingBox();
        String serverURL = "jdbc:h2:tcp://localhost/~/test";
        //frame.changeResolution(2560, 1600);
        //webcam.open();

        try(Connection conn = DriverManager.getConnection(serverURL)) {

            System.out.println("Connected to H2 database");
            Map<Integer, String> deckNames = UpdateDatabase.getDecks(conn);
            while(deckNames.isEmpty()){
                String firstName = frame.getFirstDeck(conn);
//                UpdateDatabase.addNewDeck(conn);
            }
            while (true) {
                BufferedImage image = webcam.getImage();
                Rectangle bounds = boundingBox.getContourBounds();
                if (image == null) {
                    break;
                }
                if (bounds == null) {
                    Thread.sleep(50);
                } else {
                    double scaleX = webcam.getViewSize().getWidth() / frame.getWebcamWidth();
                    double scaleY = webcam.getViewSize().getHeight() / frame.getWebcamHeight();
                    Rectangle scaledBounds = new Rectangle(
                            (int) (bounds.x * scaleX),
                            (int) (bounds.y * scaleY),
                            (int) (bounds.width * scaleX),
                            (int) (bounds.height * scaleY)
                    );
                    BufferedImage subImage = image.getSubimage(scaledBounds.x, scaledBounds.y, scaledBounds.width, scaledBounds.height);
                    if (!boundingBox.getMotionDetector().motionDetected()) {
                        boundingBox.getMotionDetector().checkForMotion(subImage);
                        Thread.sleep(300);
                    } else {
                        BufferedImage cardContours = cardContourBox.detectAndDraw(image);
                        cardContourBox.setDisplayImage(cardContours);
                        cardContourBox.repaint();
//                    if(cardContourBox.isSmoothed()){
//
//                        try{
//                            int attempts = 0;
//                            boolean success = false;
//                                Path ocrPath = cardContourBox.getImagePath();
//
//                            while(attempts < 3 && !success){
//                                ocrResults = new RunOCR().runImage(ocrPath);
//                                String setId = ocrResults[0];
//                                String setNumber = ocrResults[1];
//                                cardData = new CardData(setId, setNumber);
//                                String apiResult = cardData.checkError();
//
//                                if(apiResult==null || apiResult.contains("error")){
//                                    attempts++;
//                                    System.out.println("Error");
//                                    continue;
//                                }
//                                else{
//                                    success = true;
//                                    System.out.println("Success");
//                                    //set up card info for record and update database
//                                }
//                            }
//
//                            if(attempts >= 3){
//                                //prompt user for setID
//                            }
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        if(ocrResults.length>1 && !cardData.checkError().contains("error")){
//                            System.out.println("Reset");
//                            cardContourBox.reset();
//                            boundingBox.resetMotionDetector();
//                            cardContourBox.setDisplayImage(image);
//                            cardContourBox.repaint();
//                            String thumbnail = cardData.getImageUri();
//                        }
//                    }
                        if (cardContourBox.isSmoothed()) {
                            try {
                                int attempts = 0;
                                boolean success = false;

                                while (attempts < 3 && !success) {
                                    // Fresh capture each attempt — let the card settle between tries
                                    if (attempts > 0) {
                                        System.out.println("Retrying with fresh capture...");
                                        Thread.sleep(100); // let any vibration settle
                                        cardContourBox.reset();

                                        // Wait for smoother to re-lock on the card
                                        while (!cardContourBox.isSmoothed()) {
                                            BufferedImage retryImage = webcam.getImage();
                                            if (retryImage != null) {
                                                cardContours = cardContourBox.detectAndDraw(retryImage);
                                                cardContourBox.setDisplayImage(cardContours);
                                                cardContourBox.repaint();
                                            }
                                            Thread.sleep(100);
                                        }
                                    }


                                    Path ocrPath = cardContourBox.getImagePath();
                                    ocrResults = new RunOCR().runImage(ocrPath);
                                    String setId = ocrResults[0];
                                    String setNumber = ocrResults[1];
                                    cardData = new CardData(setId, setNumber);
                                    String apiResult = cardData.checkError();

                                    if (apiResult == null || apiResult.contains("error")) {
                                        attempts++;
                                        frame.addOCRErrorMessage(attempts);
                                        System.out.println("Error on attempt " + attempts);
                                    } else {
                                        success = true;
                                        System.out.println("Success on attempt " + (attempts + 1));
                                    }
                                }

                                if (attempts >= 3) {
                                    // prompt user for manual setID entry
                                    System.out.println("Manual entry required");
                                    frame.addOCRManualMessage();
                                    String[] manualInput = ManualInput.getManualInput();
                                    String apiResult = null;
                                    boolean goodInput = false;

                                    while (!goodInput) {
                                        if (manualInput != null && manualInput[0] != "") {
                                            String setId = manualInput[0];
                                            String setNumber = manualInput[1];
                                            cardData = new CardData(setId, setNumber);
                                            apiResult = cardData.checkError();
                                        } else if (manualInput != null && manualInput[2] != "") {
                                            String setId = manualInput[2];
                                            String cardName = manualInput[3];
                                            String[] input = new String[]{setId, cardName};

                                            cardData = new CardData(input);
                                            apiResult = cardData.checkError();
                                        }

                                        if (!(apiResult == null || apiResult.contains("error"))) {
                                            goodInput = true;
                                        } else {
                                            frame.addOCRManualMessage();
                                            manualInput = ManualInput.getManualInput();
                                        }
                                    }

                                }

                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            if (ocrResults != null && cardData != null &&
                                    ocrResults.length > 1 && !cardData.checkError().contains("error")) {

                                frame.addThumbnail(cardData.getImageUri());
                                frame.addAddedMessage(cardData.getCardName());

                                System.out.println("Reset");
                                cardContourBox.reset();
                                boundingBox.resetMotionDetector();
                                cardContourBox.setDisplayImage(webcam.getImage());
                                cardContourBox.repaint();

                            }
                        }
                    }
                    Thread.sleep(100);
                }

            }//end of while statement
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}