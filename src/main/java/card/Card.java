package card;

import card.enums.CARD_TYPE;
import card.enums.COLOR_IDENTITY;
import database.SQLStatements;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public record Card(String cardName,
                   String setName,
                   String setIdentifier,
                   String rarity,
                   String power,
                   String toughness,
                   int manaValue,
                   boolean gameChanger,
                   boolean isCreature,
                   boolean isLegendaryCreature,
                   ArrayList<String> urls,
                   ArrayList<String> cardSubType,
                   ArrayList<COLOR_IDENTITY> colorIdentity,
                   ArrayList<CARD_TYPE> cardType) implements SQLStatements {

    public void addCard(Connection conn)throws SQLException {

        boolean notIntTable;
    }

}
