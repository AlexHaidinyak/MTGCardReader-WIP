package camera.ui;

import database.SQLStatements;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ManualInput implements SQLStatements {
//    public static void main(String[] args) {
//        // 1. Initialize the input text fields
//        JTextField setCode = new JTextField(10);
//        JTextField setNumber = new JTextField(10);
//        JTextField secondSetCode = new JTextField(10);
//        JTextField cardName = new JTextField(30);
//
//        // 2. Arrange elements using a layout-driven JPanel
//        JPanel myPanel = new JPanel(new GridLayout(0, 2, 5, 5));
//        myPanel.add(new JLabel("Set code:"));
//        myPanel.add(setCode);
//        myPanel.add(new JLabel("Set number:"));
//        myPanel.add(setNumber);
//        myPanel.add(new JLabel("-OR-"));
//        myPanel.add(new JLabel());
//        myPanel.add(new JLabel("Set code:"));
//        myPanel.add(secondSetCode);
//        myPanel.add(new JLabel("Card name:"));
//        myPanel.add(cardName);
//
//        // 3. Display everything inside a unified confirmation window
//        int result = JOptionPane.showConfirmDialog(
//                null,
//                myPanel,
//                "Error in detection, please enter details",
//                JOptionPane.DEFAULT_OPTION,
//                JOptionPane.PLAIN_MESSAGE
//        );
//
//        // 4. Extract data only if the user confirms action
//        if (result == JOptionPane.OK_OPTION) {
//            String setCodeInput = setCode.getText();
//            String setNumberInput = setNumber.getText();
//
//            String secondSetInfo = secondSetCode.getText();
//            String cardNameInput = cardName.getText();
//
//            System.out.println("Name: " + setCodeInput + " " + setNumberInput);
//            System.out.println("Email: " + secondSetInfo + " " + cardNameInput);
//
//        } else {
//            System.out.println("User canceled the prompt dialog.");
//        }
//    }

    public static String[] getManualInput(){
        // 1. Initialize the input text fields
        JTextField setCode = new JTextField(10);
        JTextField setNumber = new JTextField(10);
        JTextField secondSetCode = new JTextField(10);
        JTextField cardName = new JTextField(30);

        // 2. Arrange elements using a layout-driven JPanel
        JPanel manualInfoInput = new JPanel(new GridLayout(0, 2, 5, 5));
        manualInfoInput.add(new JLabel("Set code:"));
        manualInfoInput.add(setCode);
        manualInfoInput.add(new JLabel("Set number:"));
        manualInfoInput.add(setNumber);
        manualInfoInput.add(new JLabel("-OR-"));
        manualInfoInput.add(new JLabel());
        manualInfoInput.add(new JLabel("Set code:"));
        manualInfoInput.add(secondSetCode);
        manualInfoInput.add(new JLabel("Card name:"));
        manualInfoInput.add(cardName);

        // 3. Display everything inside a unified confirmation window
        int result = JOptionPane.showConfirmDialog(
                null,
                manualInfoInput,
                "Error in detection, please enter details",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        // 4. Extract data only if the user confirms action
        if (result == JOptionPane.OK_OPTION) {
            String setCodeInput = setCode.getText();
            String setNumberInput = setNumber.getText();

            String secondSetInfo = secondSetCode.getText();
            String cardNameInput = cardName.getText();

            System.out.println("Name: " + setCodeInput + " " + setNumberInput);
            System.out.println("Email: " + secondSetInfo + " " + cardNameInput);


            String[] userInput = new String[] {setCodeInput, setNumberInput, secondSetInfo, cardNameInput};

            return userInput;
        } else {
            System.out.println("User canceled the prompt dialog.");
            return null;
        }
    }

    public static void getBoundingBox(){
        JPanel alertUser = new JPanel();
        alertUser.add(new JLabel("Click and drag a box closely around where you plan on placing the cards to be scanned"));
        JOptionPane.showConfirmDialog(null, alertUser, "First Step", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    public static Map<Integer, String> getDecks(Connection conn){

        Map<Integer, String> decks = new HashMap<Integer, String>();

        try(PreparedStatement getDeckName = conn.prepareStatement(GetDeckNameSQL)) {
            var resultSet = getDeckName.executeQuery();

            while(resultSet.next()){
                decks.put(resultSet.getInt(1), resultSet.getString(2));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if(!decks.isEmpty()){
            return decks;
        }
        return null;
    }
}
