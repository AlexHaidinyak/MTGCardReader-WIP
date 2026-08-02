package database;

public interface CreateTablesSQLStatements {

    String CreateCARD_IDENTITYSQL = """
            CREATE TABLE card_identity(
                set_id VARCHAR(10),
                card_id INTEGER,
                CONSTRAINT
                    pk_card_identity PRIMARY KEY (set_id, card_id),
                CONSTRAINT
                    fk_set_card_index FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id),
                CONSTRAINT
                    fk_card_type_index FOREIGN KEY(card_id)
                    REFERENCES card_type_table(card_type_index)
            ),;""";

    String CreateCARD_SUB_TYPESQL = """
            CREATE TABLE IF NOT EXISTS card_sub_type(
                set_id VARCHAR(10),
                sub_type VARCHAR(100),
                CONSTRAINT
                    pk_sub_type UNIQUE PRIMARY KEY(set_id, sub_type),
                CONSTRAINT
                    fk_sub_type_id FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id)
            );""";

    String CreateCARD_TABLESQL = """
            CREATE TABLE IF NOT EXISTS card_table(
                name VARCHAR(100),
                rarity VARCHAR(10),
                mana_value INTEGER,
                count INTEGER,
                deck_name VARCHAR(100),
                set_id VARCHAR(10) UNIQUE PRIMARY KEY,
                set_name VARCHAR(100)
            );""";

    String CreateCARD_TYPE_TABLESQL = """
            CREATE TABLE IF NOT EXISTS card_type_table(
                card_type_index INTEGER AUTO_INCREMENT UNIQUE PRIMARY KEY,
                card_type VARCHAR(100));""";

    String CreateCHECK_SET_IDSQL = """
            CREATE TABLE IF NOT EXISTS check_set_id(
                set_index INTEGER UNIQUE AUTO_INCREMENT PRIMARY KEY,,
                set_id VARCHAR(10) UNIQUE,
                CONSTRAINT
                    fk_check_set_id FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id)
            );""";

    String CreateCOLOR_IDENTITYSQL = """
            CREATE TABLE IF NOT EXISTS color_identity(
                set_id VARCHAR(10),
                color_id INTEGER,
                CONSTRAINT
                    pk_color_identity PRIMARY KEY(set_id, color_id),
                CONSTRAINT
                    fk_set_color_index FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id),
                CONSTRAINT
                    fk_color_index FOREIGN KEY(color_id)
                    REFERENCES color_table(color_index)
            );""";

    String CreateCOLOR_TABLESQL = """
            CREATE TABLE IF NOT EXISTS color_table(
                color_index INTEGER UNIQUE AUTO_INCREMENT PRIMARY KEY,
                color VARCHAR(10);
            );""";

    String CreateCREATURE_STATSSQL = """
            CREATE TABLE IF NOT EXISTS creature_stats(
                set_id VARCHAR(10) UNIQUE PRIMARY KEY,
                power VARCHAR(10),
                toughness VARCHAR(10)
                CONSTRAINT
                    fk_creature_id FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id),
            );""";

    String CreateDECKS_IDENTITYSQL = """
            CREATE TABLE IF NOT EXISTS deck_identity(
                set_id VARCHAR(10),
                deck_id INTEGER,
                CONSTRAINT
                    pk_set_deck_identity PRIMARY KEY(set_id, deck_id),
                CONSTRAINT
                    fk_set_deck_index FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id),
                CONSTRAINT
                    fk_set_deck_id FOREIGN KEY(deck_id)
                    REFERENCES decks(id)
            );""";

    String CreateDECKSSQL = """
            CREATE TABLE IF NOT EXISTS decks(
                id INTEGER AUTO_INCREMENT UNIQUE PRIMARY KEY,
                deck_name VARCHAR(255)
            );""";

    String CreateGAME_CHANGERSQL = """
            CREATE TABLE IF NOT EXISTS game_changer(
                index_id INTEGER AUTO_INCREMENT,
                set_id VARCHAR(10) UNIQUE PRIMARY KEY,
                CONSTRAINT
                    fk_game_changer_id FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id)
            );""";

    String CreateIMAGE_URLSQL = """
            CREATE TABLE IF NOT EXISTS image_url(
                set_id VARCHAR(10) UNIQUE PRIMARY KEY,
                small_image VARCHAR(255),
                normal_image VARCHAR(255),
                large_image VARCHAR(255),
                CONSTRAINT
                    fk_set_image_index FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id)
            );""";

    String CreateLEGENDARY_CREATURESQL = """
            CREATE TABLE IF NOT EXISTS legendary_creature(
                index_id INTEGER UNIQUE AUTO_INCREMENT,
                set_id VARCHAR(10) UNIQUE PRIMARY KEY,
                CONSTRAINT
                    fk_legendary_creature_id FOREIGN KEY(set_id)
                    REFERENCES card_table(set_id)
            );""";

    String PopulateCOLOR_TABLESQL = """
            INSERT INTO
                color_table
            VALUES
                (DEFAULT, ?);""";

    String PopulateCARD_TYPE_TABLESQL = """
            INSERT INTO
                card_type_table
            VALUES
                (DEFAULT, ?);""";
}
