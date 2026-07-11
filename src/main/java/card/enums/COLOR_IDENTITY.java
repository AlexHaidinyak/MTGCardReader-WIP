package card.enums;

public enum COLOR_IDENTITY {
    WHITE("W"), BLUE("U"), BLACK("B"), RED("R"), GREEN("G"), COLORLESS("C");

    private final String abreviation;

    COLOR_IDENTITY(String abreviation) {
        this.abreviation = abreviation;
    }

    public int getIndex(){
        return ordinal() + 1;
    }
    public String getSymbol(){
        return this.abreviation;
    }

    public static COLOR_IDENTITY getBySymbol(String symbol){
        for(COLOR_IDENTITY color: COLOR_IDENTITY.values()){
            if(color.getSymbol().equals(symbol)){
                return color;
            }
        }
        return null;
    }
}
