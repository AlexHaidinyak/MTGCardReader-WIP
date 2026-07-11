package api;

import card.Card;
import card.enums.CARD_TYPE;
import card.enums.COLOR_IDENTITY;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;

public class CardData {
    private JsonNode cardJson;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String  name,
                    setName,
                    setIdentifier,
                    rarity,
                    power,
                    toughness;
    private int manaValue;
    private boolean gameChanger,
                    isLegendary,
                    isCreature,
                    isLegendaryCreature;

    private ArrayList<String> imageUris = new ArrayList<>();
    private ArrayList<COLOR_IDENTITY> colorIdentities = new ArrayList<>();
    private ArrayList<CARD_TYPE> cardTypesList = new ArrayList<>();
    private ArrayList<String> subTypes = new ArrayList<>();

    public CardData(String set, String id){
        var apiInput = APILookup.lookupCard(set, id).body();
        cardJson = objectMapper.readTree(apiInput);
        setIdentifier = set + " " + id;
    }
    public CardData(String[] manualInfo){
        var apiInput = APILookup.lookupCard(manualInfo).body();
        cardJson = objectMapper.readTree(apiInput);

        String set = manualInfo[0];
        String id = cardJson.get("collector_number").asString();

        setIdentifier = set + " " + id;
    }

    public Card getCardData(){
        getName();
        getSetName();
        getRarity();
        getManaValue();
        isGameChanger();
        getCardTypes();
        getColorIdentities();
        getImageUris();
        isLegendaryCreature();

        return new Card(name,
                        setName,
                        setIdentifier,
                        rarity,
                        power,
                        toughness,
                        manaValue,
                        gameChanger,
                        isCreature,
                        isLegendaryCreature,
                        imageUris,
                        subTypes,
                        colorIdentities,
                        cardTypesList
                        );
    }

    public String checkError(){
        return cardJson.get("object").asString();
    }

    public String getImageUri(){
        var uriNode = cardJson.get("image_uris");
        return uriNode.get("normal").asString();
    }
    public String getCardName(){
        return cardJson.get("name").asString();
    }

    private void getName(){
        name = cardJson.get("name").asString();
    }
    private void getSetName(){
        setName = cardJson.get("set_name").asString();
    }
    private void getRarity(){
        rarity = cardJson.get("rarity").asString();
        rarity = rarity.substring(0, 1).toUpperCase() + rarity.substring(1).toLowerCase();
    }
    private void getManaValue(){
        manaValue = cardJson.get("cmc").asInt();
    }
    private void isGameChanger(){
        gameChanger = cardJson.get("game_changer").asBoolean();
    }
    private void getCardTypes(){
        String type_line = cardJson.get("type_line").asString();

        if(type_line.contains("Creature")){
            isCreature = true;
            power = cardJson.get("power").asString();
            toughness = cardJson.get("toughness").asString();
        }
        if(type_line.contains("Legendary")){
            isLegendary = true;
            type_line = type_line.strip().replace("Legendary", " ");
        }

        type_line = type_line.trim();

        if(type_line.contains("—")){
            int subTypeIndex = type_line.indexOf("—");

            String subType = type_line.substring(subTypeIndex).replace("—", "").trim();
            String[] subTypeArray = subType.split(" ");
            subTypes.addAll(Arrays.asList(subTypeArray));

            if(type_line.contains("Basic") && type_line.contains("Land")){
                cardTypesList.add(CARD_TYPE.BASIC_LAND);
                return;
            }
            String[] cardTypes = type_line.trim().substring(0, subTypeIndex).split(" ");
            for(String cardType : cardTypes){
                cardTypesList.add(CARD_TYPE.getType(cardType));
            }
        }
        else{
            String[] cardTypes = type_line.trim().split(" ");
            for(String cardType : cardTypes){
                cardTypesList.add(CARD_TYPE.getType(cardType));
            }
        }
    }
    private void getColorIdentities(){
        var colorNode = cardJson.get("color_identity");
        colorNode.forEach(x -> colorIdentities.add(COLOR_IDENTITY.getBySymbol(x.asString())));
    }
    private void getImageUris(){
        var imageUriNode = cardJson.get("image_uris");
        imageUris.add(imageUriNode.get("small").asString());
        imageUris.add(imageUriNode.get("normal").asString());
        imageUris.add(imageUriNode.get("large").asString());
    }
    private void isLegendaryCreature(){
        if(isCreature && isLegendary){
            isLegendaryCreature = true;
        }
    }
}
