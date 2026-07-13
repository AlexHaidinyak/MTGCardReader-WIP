package database;

/**
 * Stores all the SQL statements
 * */
public interface SQLStatements {

    /**
     * SQL statement for inserting into Color_Identity
     * */
    String ColorSQL = """
            INSERT INTO 
                Color_Identity
            VALUES
                (?, ?);""";

    /**
     * The SQL statement for inserting into Card_Identity
     * */
    String CardTypeSQL = """
            INSERT INTO
                Card_Identity
            VALUES
                (?, ?);""";

    /**
     * SQL statement for inserting into Card_Table
     * */
    String CardTableSQL = """
            INSERT INTO
                Card_Table
            VALUES
                (?,?,?,?,1,?);""";

    /**
     * SQL statement for updating the count column of Card_Table
     * */
    String UpdateSQL = """
            UPDATE 
                Card_Table
            SET 
                count = count + 1
            WHERE 
                set_id = ?;""";

    /**
     * SQL statement for inserting into Check_Set_Id
     * */
    String InsertCheckSQL = """
            INSERT INTO
                Check_Set_Id(set_index, set_id)
            VALUES(DEFAULT, ?);""";

    /**
     * SQL statement for querying the set_id from Check_Set_Id
     * */
    String WorkaroundSQL = """
            SELECT
                s.set_id
            FROM
                Check_Set_Id AS s
            WHERE
                s.set_id = ?;""";


    String CreatureStatSQL = """
            INSERT INTO
                Creature_Stats
            VALUES
                (?,?,?);""";

    String GameChangerSQL = """
            INSERT INTO
                Game_Changer
            VALUES
                (DEFAULT, ?);""";

    String LegendaryCreatureSQL = """
            INSERT INTO
                Legendary_Creature
            VALUES
                (DEFAULT, ?);""";

    String ImageUrlSQL = """
            INSERT INTO
                Image_Url
            VALUES
                (?,?,?,?);""";

    String CardSubTypeSQL = """
            INSERT INTO
                Card_Sub_Type
            VALUES
                (?,?)""";
    String PrintCardSQL = """
            SELECT
                name,
                set_name,
                Card_Table.set_id,
                rarity,
                mana_value,
                count,
                Color_Table.color,
                Card_Type_Table.card_type
            FROM
                Card_Table
            JOIN
                Color_Identity
            ON
                Card_Table.set_id = Color_Identity.set_id
            JOIN
                Color_Table
            ON
                Color_Identity.color_id = Color_Table.color_index
            JOIN
                Card_Identity
            ON
                Card_Table.set_id = Card_Identity.set_Id
            JOIN
                Card_Type_Table
            ON
                Card_Identity.card_id = Card_Type_Table.card_type_index
            WHERE 
                Card_Table.set_id = ?;""";
}

