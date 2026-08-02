package database;

import card.enums.CARD_TYPE;
import card.enums.COLOR_IDENTITY;
import org.h2.command.Prepared;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateDatabase implements QuerySQLStatements, CreateTablesSQLStatements {

    private UpdateDatabase(){}



    public static boolean checkUniqueId(Connection conn, String setID) throws SQLException {
        try(var checkSetId = conn.prepareStatement(WorkaroundSQL)){
            checkSetId.setString(1, setID);
            try(var inDatabase = checkSetId.executeQuery()){
                if(inDatabase.next()){
                    //TO-DO Send message to messageScroll that updating count rather than adding card
                    try(var update = conn.prepareStatement(UpdateSQL)){
                        update.setString(1, setID);
                        if(update.executeUpdate() == 1){
                            conn.commit();
                        }
                    }
                    return true;
                }
                else{
                    try(var newSetId = conn.prepareStatement(InsertCheckSQL)){
                        newSetId.setString(1, setID);
                    }
                    return false;
                }
            }
        }
    }

    public static void addCardInfo(PreparedStatement cardInfo,
                                   String setID,
                                   String name,
                                   String rarity,
                                   int manaValue,
                                   String setName,
                                   String deckName,
                                   List<PreparedStatement> cardBatch) throws SQLException {
        cardInfo.setString(1, name);
        cardInfo.setString(2, setID);
        cardInfo.setString(3, rarity);
        cardInfo.setInt   (4, manaValue);
        cardInfo.setString(5, setName);
        cardInfo.setString(6, deckName);
        cardInfo.addBatch();

        cardBatch.add(cardInfo);
    }

    public static void addCardColorBatch(PreparedStatement colorAdd,
                                         String setID,
                                         List<COLOR_IDENTITY> colors,
                                         List<PreparedStatement> cardBatch) throws SQLException {
        for(COLOR_IDENTITY color : colors){
            colorAdd.setString(1, setID);
            colorAdd.setInt(2, color.getIndex());
            colorAdd.addBatch();
        }

        cardBatch.add(colorAdd);
    }

    public static void addCardTypeBatch(PreparedStatement typeAdd,
                                        String setID,
                                        List<CARD_TYPE> cardTypes,
                                        List<PreparedStatement> cardBatch) throws SQLException {
        for(CARD_TYPE cardType : cardTypes){
            typeAdd.setString(1, setID);
            typeAdd.setInt(2, cardType.getIndex());
            typeAdd.addBatch();
        }

        cardBatch.add(typeAdd);
    }

    public static void addCardSubTypeBatch(PreparedStatement subTypeAdd,
                                           String setID,
                                           List<String> cardSubTypeList,
                                           List<PreparedStatement> cardBatch) throws SQLException {
        for(String subType : cardSubTypeList){
            subTypeAdd.setString(1, setID);
            subTypeAdd.setString(2, subType);
            subTypeAdd.addBatch();
        }

        cardBatch.add(subTypeAdd);
    }

    public static void addCreatureStats(PreparedStatement statsAdd,
                                        String setID,
                                        String power,
                                        String toughness,
                                        List<PreparedStatement> cardBatch) throws SQLException {
        statsAdd.setString(1, setID);
        statsAdd.setString(2, power);
        statsAdd.setString(3, toughness);
        statsAdd.addBatch();
    }

    public static void addLegendaryCreature(PreparedStatement legendAdd,
                                            String setID,
                                            List<PreparedStatement> cardBatch) throws SQLException {
        legendAdd.setString(1, setID);
        legendAdd.addBatch();

        cardBatch.add(legendAdd);
    }

    public static void addGameChanger(PreparedStatement gameChangerAdd,
                                      String setID,
                                      List<PreparedStatement> cardBatch) throws SQLException {
        gameChangerAdd.setString(1, setID);
        gameChangerAdd.addBatch();

        cardBatch.add(gameChangerAdd);
    }

    public static void addURL(PreparedStatement urlAdd,
                              String setID,
                              List<String> urls,
                              List<PreparedStatement> cardBatch) throws SQLException {
        urlAdd.setString(1, setID);
        urlAdd.setString(2, urls.get(0));
        urlAdd.setString(3, urls.get(1));
        urlAdd.setString(4, urls.get(2));
        urlAdd.addBatch();

        cardBatch.add(urlAdd);
    }

    public static Map<Integer, String> getDecks(Connection conn){

        Map<Integer, String> decks = new HashMap<Integer, String>();

        JTextField deckNameField = new JTextField(50);

        try(PreparedStatement getDeckName = conn.prepareStatement(GetDeckNameSQL)) {
            var resultSet = getDeckName.executeQuery();

            while(resultSet.next()){
                decks.put(resultSet.getInt(1), resultSet.getString(2));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return decks;
    }

    public static boolean addNewDeck(Connection conn, String deckName) throws SQLException {

        try(var addDeck = conn.prepareStatement(DeckNameSQL)){

            addDeck.setString(1, deckName);
            var result = addDeck.executeUpdate();

            if(result == 1){
                return true;
            }
            else{
                return false;
            }
        }
    }

    public static void createTables(Connection conn) throws SQLException {
        createFirstBatch(conn);
        createSecondBatch(conn);
        populateStaticTables(conn);
    }

    private static void createFirstBatch(Connection conn) throws SQLException {

        try(PreparedStatement cardTable  = conn.prepareStatement(CreateCARD_TABLESQL);
            PreparedStatement cardType   = conn.prepareStatement(CreateCARD_TYPE_TABLESQL);
            PreparedStatement colorTable = conn.prepareStatement(CreateCOLOR_TABLESQL);
            PreparedStatement decks      = conn.prepareStatement(CreateDECKSSQL)) {

            cardTable.executeUpdate();
            cardType.executeUpdate();
            colorTable.executeUpdate();
            decks.executeUpdate();
        }
    }
    private static void createSecondBatch(Connection conn) throws SQLException {

        try(PreparedStatement cardTypeMiddle  = conn.prepareStatement(CreateCARD_IDENTITYSQL);
            PreparedStatement cardSubType     = conn.prepareStatement(CreateCARD_SUB_TYPESQL);
            PreparedStatement checkId         = conn.prepareStatement(CreateCHECK_SET_IDSQL);
            PreparedStatement cardColorMiddle = conn.prepareStatement(CreateCOLOR_IDENTITYSQL);
            PreparedStatement creatureStats   = conn.prepareStatement(CreateCREATURE_STATSSQL);
            PreparedStatement deckMiddle      = conn.prepareStatement(CreateDECKS_IDENTITYSQL);
            PreparedStatement gameChanger     = conn.prepareStatement(CreateGAME_CHANGERSQL);
            PreparedStatement imageURLS       = conn.prepareStatement(CreateIMAGE_URLSQL);
            PreparedStatement legendaryCard   = conn.prepareStatement(CreateLEGENDARY_CREATURESQL)) {

            cardTypeMiddle.executeUpdate();
            cardSubType.executeUpdate();
            checkId.executeUpdate();
            cardColorMiddle.executeUpdate();
            creatureStats.executeUpdate();
            deckMiddle.executeUpdate();
            gameChanger.executeUpdate();
            imageURLS.executeUpdate();
            legendaryCard.executeUpdate();
        }
    }
    private static void populateStaticTables(Connection conn) throws SQLException {

        try(PreparedStatement staticColorAdd = conn.prepareStatement(PopulateCOLOR_TABLESQL);
            PreparedStatement staticTypeAdd = conn.prepareStatement(PopulateCARD_TYPE_TABLESQL)){

            for(COLOR_IDENTITY color: COLOR_IDENTITY.values()){
                staticColorAdd.setString(1, color.name());
                staticColorAdd.addBatch();
            }
            staticTypeAdd.executeUpdate();

            for(CARD_TYPE cardType: CARD_TYPE.values()){
                staticTypeAdd.setString(1, cardType.name());
                staticTypeAdd.addBatch();
            }
            staticTypeAdd.executeUpdate();
        }
    }

}
