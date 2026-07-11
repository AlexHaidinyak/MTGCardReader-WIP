package card.enums;

public enum CARD_TYPE {

    ARTIFACT, CREATURE, ENCHANTMENT, BASIC_LAND, LAND, INSTANT, SORCERY, PLANESWALKER, BATTLE;

    public int getIndex(){
        return ordinal() + 1;
    }

    public static CARD_TYPE getType(String type){
        for(CARD_TYPE c : CARD_TYPE.values()){
            if(c.toString().equalsIgnoreCase(type)){
                return c;
            }
        }
        return null;
    }
}
