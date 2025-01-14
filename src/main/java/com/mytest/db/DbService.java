package com.mytest.db;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Scanner;

/**
 * Třída databázových služeb.
 * Obsahuje metody pro nastavení, zrušení databáze a manipulaci s daty.
 * Obsahuje také metody pro čtení zdrojů z classpath.
 * @version 1.0
 */

public class DbService {
    private static final Logger logger = LoggerFactory.getLogger(DbService.class);
    private final Vertx vertx;
    private final JsonObject dbConfig;

    /**
     * Konstruktor třídy DbService.
     * @param vertx instance třídy Vertx
     */
    public DbService(Vertx vertx) {
        this.vertx = vertx;
        this.dbConfig = (JsonObject) vertx.sharedData().getLocalMap("app-config").get("dbConfig");
    }

    /**
     * Metoda pro čtení zdroje z classpath.
     * @param resource URL zdroje
     * @return obsah zdroje
     */
    public String readResourceStr(URL resource) {
        try (InputStream inputStream = resource.openStream();
             Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            logger.error("Nepodařilo se načíst soubor. Chyba: " + e.getMessage());
            return null;
        }
    }

    /**
     * Metoda pro testování připojení k databázi.
     * @return výsledek testu
     */
    public String testDatabase(){
        String result;
        JsonObject dbConfig =  (JsonObject) vertx.sharedData().getLocalMap("app-config").get("dbConfig");
        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))) {
            if(connection.isValid(5)) {
                result = "Připojení k databázi je v pořádku.";
            } else {
                result = "Připojení k databázi je neplatné.";
            }
        } catch (SQLException e) {
            result = "Nepodařilo se připojit k databázi. Chyba: " + e.getMessage();
        }
        return result;
    }

    /**
     * Metoda nastavení tabulek a klíčů v databázi.
     * @return výsledek nastavení databáze
     */
    public String setupDatabase() {
        String result;
        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))) {
            connection.createStatement().executeUpdate(
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'transaction') " +
                            "CREATE TABLE [transaction](" +
                            "[trxId] BIGINT IDENTITY(1000,1) NOT NULL," +
                            "[amount] NUMERIC(19, 2) NOT NULL," +
                            "[currency] NVARCHAR(3) NOT NULL," +
                            "[id] NVARCHAR(20) NOT NULL," +
                            "[bankref] NVARCHAR(20) NOT NULL," +
                            "[transactionId] NVARCHAR(20) NOT NULL," +
                            "[bookingDate] DATE NULL," +
                            "[postingDate] DATE NOT NULL," +
                            "[creditDebitIndicator] VARCHAR(4) NULL," +
                            "[ownAccountNumber] NVARCHAR(20) NULL," +
                            "[counterPartyAccount] BIGINT NOT NULL," +
                            "[detail1] NVARCHAR(50) NULL," +
                            "[detail2] NVARCHAR(50) NULL," +
                            "[detail3] NVARCHAR(50) NULL," +
                            "[detail4] NVARCHAR(50) NULL," +
                            "[productBankRef] NVARCHAR(50) NULL," +
                            "[transactionType] BIGINT NOT NULL," +
                            "[statement] BIGINT NOT NULL," +
                            "[constantSymbol] VARCHAR(10) NULL," +
                            "[specificSymbol] VARCHAR(10) NULL," +
                            "[variableSymbol] VARCHAR(10) NULL," +
                            "CONSTRAINT PK_transaction_trxId PRIMARY KEY (trxId))");
            connection.createStatement().executeUpdate(
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'transactionType') " +
                            "CREATE TABLE [transactionType](" +
                            "[trxTypeId] BIGINT IDENTITY(1000,1) NOT NULL," +
                            "[type] NVARCHAR(20) NOT NULL," +
                            "[code] INT NOT NULL," +
                            "CONSTRAINT PK_transactionType_trxTypeId PRIMARY KEY (trxTypeId))");
            connection.createStatement().executeUpdate(
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'statement') " +
                            "CREATE TABLE [statement](" +
                            "[statementId] BIGINT IDENTITY(1000,1) NOT NULL," +
                            "[number] NVARCHAR(20) NOT NULL," +
                            "[period] NVARCHAR(20) NOT NULL," +
                            "[description] NVARCHAR(1000) NULL," +
                            "CONSTRAINT PK_statement_statementId PRIMARY KEY (statementId))");
            connection.createStatement().executeUpdate(
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'account') " +
                            "CREATE TABLE [account](" +
                            "[accountId] BIGINT IDENTITY(1000,1) NOT NULL," +
                            "[name] NVARCHAR(50) NOT NULL," +
                            "[number] NVARCHAR(20) NOT NULL," +
                            "[code] NVARCHAR(4) NOT NULL," +
                            "CONSTRAINT PK_account_accountId PRIMARY KEY (accountId))");
            connection.createStatement().executeUpdate(
                    "IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_transaction_counterPartyAccount') " +
                            "ALTER TABLE [transaction] ADD CONSTRAINT FK_transaction_counterPartyAccount FOREIGN KEY (counterPartyAccount) REFERENCES account(accountId)");
            connection.createStatement().executeUpdate(
                    "IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_transaction_transactionType') " +
                            "ALTER TABLE [transaction] ADD CONSTRAINT FK_transaction_transactionType FOREIGN KEY (transactionType) REFERENCES transactionType(trxTypeId)");
            connection.createStatement().executeUpdate(
                    "IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_transaction_statement') " +
                            "ALTER TABLE [transaction] ADD CONSTRAINT FK_transaction_statement FOREIGN KEY (statement) REFERENCES statement(statementId)");

            result = "Nastavení databáze dokončeno.";
        } catch (SQLException e) {
            result = "Nepodařilo se nastavit databázi. Chyba: " + e.getMessage();
        }
        return result;
    }

    /**
     * Metoda pro zrušení tabulek v databázi.
     * @return výsledek zrušení tabulek
     */
    public String dropDatabase() {
        String result;
        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))) {
            connection.createStatement().executeUpdate("DROP TABLE IF EXISTS [transaction]");
            connection.createStatement().executeUpdate("DROP TABLE IF EXISTS [transactionType]");
            connection.createStatement().executeUpdate("DROP TABLE IF EXISTS [statement]");
            connection.createStatement().executeUpdate("DROP TABLE IF EXISTS [account]");
            result = "Tabulky databáze byly úspěšně zrušeny.";
        } catch (SQLException e) {
            result = "Nepodařilo se zrušit tabulky databáze. Chyba: " + e.getMessage();
        }
        return result;
    }

    /**
     * Metoda pro vytvoření účtu v databázi.
     * @param accountData - data účtu
     * @return výsledek vytvoření účtu
     */
    public String createAccount(String accountData) {
        String result;
        JsonObject accountJson = new JsonObject(accountData);
        String name = accountJson.getString("name");
        String number = accountJson.getString("number");
        String code = accountJson.getString("code");
        if((name == null || number == null || code == null) || name.isEmpty() || number.isEmpty() || code.isEmpty()) {
            return "Nepodařilo se vytvořit účet. Položky \"name\", \"number\" and \"code\" jsou povinné.";
        }

        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))) {
            String sql = "INSERT INTO account (name, number, code) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, number);
                pstmt.setString(3, code);
                pstmt.executeUpdate();
                result = "Účet byl úspěšně vytvořen.";
            }
        } catch (SQLException e) {
            result = "Nepodařilo se vytvořit účet. Chyba: " + e.getMessage();
        }
        return result;
    }

    /**
     * Metoda pro vytvoření typu transakce v databázi.
     * @param transactionTypeData - Údaje o typu transakce.
     * @return výsledek vytvoření typu transakce
     */
    public String createTransactionType(String transactionTypeData) {
        String result;
        JsonObject transactionTypeJson = new JsonObject(transactionTypeData);
        String type = transactionTypeJson.getString("type");
        Integer code;
        try {
            code = transactionTypeJson.getInteger("code");
        } catch (Exception e) {
            code = 0;
            logger.error("Nepodařilo se zpracovat kód typu transakce. Chyba: " + e.getMessage());
        }
        if(type == null || type.isEmpty()) {
            return "Nepodařilo se vytvořit typ transakce. Položky \"type\" and \"code\" jsou povinné.";
        }

        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))) {
            String sql = "INSERT INTO transactionType (type, code) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, type);
                pstmt.setInt(2, code);
                pstmt.executeUpdate();
                result = "Typ transakce byl úspěšně vytvořen.";
            }
        } catch (SQLException e) {
            result = "Nepodařilo se vytvořit typ transakce. Chyba: " + e.getMessage();
        }
        return result;
    }

    /**
     * Metoda pro vytvoření výpisu v databázi.
     * @param statementData - Údaje o výpisu.
     * @return výsledek vytvoření výpisu
     */
    public String createStatement(String statementData) {
        String result;
        JsonObject statementJson;
        try {
            statementJson = new JsonObject(statementData);
        } catch (Exception e) {
            return "Nepodařilo se zpracovat data výpisu. Chyba: " + e.getMessage();
        }

        String number = statementJson.getString("number", "001");
        String period = statementJson.getString("period", "2025");
        String description = statementJson.getString("description", "");

        if (number.isEmpty() || period.isEmpty()) {
            return "Nepodařilo se vytvořit výpis. Položky \"number\" and \"period\" jsou povinné.";
        }

        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))) {
            String sql = "INSERT INTO statement (number, period, description) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, number);
                pstmt.setString(2, period);
                pstmt.setString(3, description);
                pstmt.executeUpdate();
                result = "Výpis byl úspěšně vytvořen.";
            }
        } catch (SQLException e) {
            result = "Nepodařilo se vytvořit výpis. Chyba: " + e.getMessage();
        }
        return result;
    }

    /**
     * Metoda pro vytvoření transakce v databázi.
     * @param transactionData - Údaje o transakci
     * @return výsledek vytvoření transakce
     */
    public String createTransaction(String transactionData) {
        String result;
        JsonObject transactionJson;
        try {
            transactionJson = new JsonObject(transactionData);
        } catch (Exception e) {
            return "Nepodařilo se zpracovat data transakce. Chyba " + e.getMessage();
        }

        BigDecimal amount;
        try {
            String amountStr = transactionJson.getString("amount");
            amount = new BigDecimal(amountStr);
        } catch (Exception e) {
            amount = BigDecimal.ZERO;
            logger.error("Nepodařilo se zpracovat \"amount\". Chyba: " + e.getMessage());
        }

        String currency = transactionJson.getString("currency", "CZK");
        String bankref = transactionJson.getString("bankref", "NA");
        Date bookingDate;
        try {
            bookingDate = java.sql.Date.valueOf(transactionJson.getString("bookingDate"));
        } catch (Exception e) {
            bookingDate = null;
            logger.error("Nepodařilo se zpracovat \"bookingDate\". Chyba: " + e.getMessage());
        }

        Long counterPartyAccount;
        try {
            counterPartyAccount = transactionJson.getLong("counterPartyAccount");
        } catch (Exception e) {
            counterPartyAccount = 0L;
            logger.error("Nepodařilo se zpracovat \"counterPartyAccount\". Chyba: " + e.getMessage());
        }

        String creditDebitIndicator = transactionJson.getString("creditDebitIndicator");
        String detail1 = transactionJson.getString("detail1");
        String id = transactionJson.getString("id", "");
        String ownAccountNumber = transactionJson.getString("ownAccountNumber");
        Date postingDate;
        try {
            postingDate = java.sql.Date.valueOf(transactionJson.getString("postingDate"));
        } catch (Exception e) {
            postingDate = null;
            logger.error("Nepodařilo se zpracovat \"postingDate\". Chyba: " + e.getMessage());
        }

        String productBankRef = transactionJson.getString("productBankRef");
        String specificSymbol = transactionJson.getString("specificSymbol");
        Long statement;
        try {
            statement = transactionJson.getLong("statement");
        } catch (Exception e) {
            statement = 0L;
            logger.error("Nepodařilo se zpracovat \"statement\". Chyba: " + e.getMessage());
        }

        String transactionId = transactionJson.getString("transactionId");
        Long transactionType;
        try {
            transactionType = transactionJson.getLong("transactionType");
        } catch (Exception e) {
            transactionType = 0L;
            logger.error("Nepodařilo se zpracovat \"transactionType\". Chyba: " + e.getMessage());
        }

        String variableSymbol = transactionJson.getString("variableSymbol");

        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))) {
            String sql = "INSERT INTO [transaction] (amount, currency, bankref, bookingDate, counterPartyAccount, creditDebitIndicator, detail1, id, ownAccountNumber, postingDate, productBankRef, specificSymbol, statement, transactionId, transactionType, variableSymbol) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, amount);
                pstmt.setString(2, currency);
                pstmt.setString(3, bankref);
                if (bookingDate != null) {
                    pstmt.setDate(4, bookingDate);
                } else {
                    pstmt.setNull(4, java.sql.Types.DATE);
                }
                pstmt.setLong(5, counterPartyAccount);
                pstmt.setString(6, creditDebitIndicator);
                pstmt.setString(7, detail1);
                pstmt.setString(8, id);
                pstmt.setString(9, ownAccountNumber);
                if (postingDate != null) {
                    pstmt.setDate(10, postingDate);
                } else {
                    pstmt.setNull(10, java.sql.Types.DATE);
                }
                pstmt.setString(11, productBankRef);
                pstmt.setString(12, specificSymbol);
                pstmt.setLong(13, statement);
                pstmt.setString(14, transactionId);
                pstmt.setLong(15, transactionType);
                pstmt.setString(16, variableSymbol);
                pstmt.executeUpdate();
                result = "Transakce byla úspěšně vytvořena.";
            }
        } catch (SQLException e) {
            result = "Nepodařilo se vytvořit transakci. Chyba: " + e.getMessage();
        }
        return result;
    }

    /**
     * Metoda naplnění databáze ukázkovými daty.
     * @return výsledek naplnění databáze
     */
    public String fillUpDatabase() {
        String resourceName, result = "";
        String errorMsgBase = "Nepodařilo se vyplnit databázi.";
        String errorExpMsg = "Zdroj \"%s\" nebyl nalezen.";
        URL resource;

        try {
            resourceName = "accounts.json";
            resource = getClass().getClassLoader().getResource(resourceName);
            if(resource == null) return String.format(errorMsgBase+" "+errorExpMsg, resourceName);
            JsonArray accountsArray = new JsonArray(readResourceStr(resource));
            for (int i = 0; i < accountsArray.size(); i++) {
                result += createAccount(accountsArray.getJsonObject(i).encode()) + "\n";
            }

            resourceName = "statements.json";
            resource = getClass().getClassLoader().getResource(resourceName);
            if(resource == null) return String.format(errorMsgBase+" "+errorExpMsg, resourceName);
            JsonArray statementsArray = new JsonArray(readResourceStr(resource));
            for (int i = 0; i < statementsArray.size(); i++) {
                result += createStatement(statementsArray.getJsonObject(i).encode()) + "\n";
            }

            resourceName = "transactionTypes.json";
            resource = getClass().getClassLoader().getResource(resourceName);
            if(resource == null) return String.format(errorMsgBase+" "+errorExpMsg, resourceName);
            JsonArray transactionTypesArray = new JsonArray(readResourceStr(resource));
            for (int i = 0; i < transactionTypesArray.size(); i++) {
                result += createTransactionType(transactionTypesArray.getJsonObject(i).encode()) + "\n";
            }

            resourceName = "transactions.json";
            resource = getClass().getClassLoader().getResource(resourceName);
            if(resource == null) return String.format(errorMsgBase+" "+errorExpMsg, resourceName);
            JsonArray transactionsArray = new JsonArray(readResourceStr(resource));
            for (int i = 0; i < transactionsArray.size(); i++) {
                result += createTransaction(transactionsArray.getJsonObject(i).encode()) + "\n";
            }

            result += "Databáze byla úspěšně naplněna.";
        } catch (Exception e) {
            logger.error(errorMsgBase + " Chyba: " + e.getMessage());
            result = errorMsgBase + " Chyba: " + e.getMessage();
        }

        return result;
    }

    /**
     * Metoda pro získání transakcí podle čísla účtu.
     * @param accountNumber - Číslo účtu
     * @return transakce podle čísla účtu
     */
    public String getTransactionsByAccountNumber(String accountNumber) {
        JsonArray transactionsArray = new JsonArray();
        String sql = "SELECT t.*, a.name AS counterPartyAccountName, a.number AS counterPartyAccountNumber, a.code AS counterPartyAccountCode, " +
                "s.number AS statementNumber, s.period AS statementPeriod, " +
                "tt.type AS transactionTypeStr, tt.code AS transactionTypeCode " +
                "FROM [transaction] t " +
                "JOIN [account] a ON t.counterPartyAccount = a.accountId " +
                "JOIN [statement] s ON t.statement = s.statementId " +
                "JOIN [transactionType] tt ON t.transactionType = tt.trxTypeId " +
                "WHERE t.ownAccountNumber = ?";

        try (Connection connection = DriverManager.getConnection(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"));
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JsonObject transactionJson = new JsonObject();
                JsonObject amountJson = new JsonObject();
                amountJson.put("currency", rs.getString("currency"));
                amountJson.put("value", rs.getBigDecimal("amount"));
                transactionJson.put("amount", amountJson);

                transactionJson.put("bankref", rs.getString("bankref"));
                transactionJson.put("bookingDate", rs.getDate("bookingDate").toString());

                JsonObject counterPartyAccountJson = new JsonObject();
                counterPartyAccountJson.put("accountName", rs.getString("counterPartyAccountName"));
                counterPartyAccountJson.put("accountNumber", String.format("%016d", rs.getLong("counterPartyAccountNumber")));
                counterPartyAccountJson.put("bankCode", rs.getString("counterPartyAccountCode"));
                transactionJson.put("counterPartyAccount", counterPartyAccountJson);

                transactionJson.put("creditDebitIndicator", rs.getString("creditDebitIndicator"));

                JsonObject detailsJson = new JsonObject();
                if (rs.getString("detail1") != null) detailsJson.put("detail1", rs.getString("detail1"));
                if (rs.getString("detail2") != null) detailsJson.put("detail2", rs.getString("detail2"));
                if (rs.getString("detail3") != null) detailsJson.put("detail3", rs.getString("detail3"));
                if (rs.getString("detail4") != null) detailsJson.put("detail4", rs.getString("detail4"));
                if (!detailsJson.isEmpty()) transactionJson.put("details", detailsJson);

                transactionJson.put("id", rs.getString("id"));
                transactionJson.put("ownAccountNumber", rs.getString("ownAccountNumber"));
                transactionJson.put("postingDate", rs.getDate("postingDate").toString());
                transactionJson.put("productBankRef", rs.getString("productBankRef"));
                transactionJson.put("specificSymbol", rs.getString("specificSymbol"));
                transactionJson.put("statementNumber", rs.getString("statementNumber"));
                transactionJson.put("statementPeriod", rs.getString("statementPeriod"));
                transactionJson.put("transactionId", rs.getString("transactionId"));
                transactionJson.put("transactionType", rs.getString("transactionTypeStr"));
                transactionJson.put("transactionTypeCode", rs.getInt("transactionTypeCode"));
                transactionJson.put("variableSymbol", rs.getString("variableSymbol"));

                transactionsArray.add(transactionJson);
            }
            return transactionsArray.encodePrettily();
        } catch (SQLException e) {
            JsonObject errorJson = new JsonObject();
            errorJson.put("error", "Nepodařilo se vyhledat transakce podle čísla účtu.");
            errorJson.put("message", e.getMessage());
            return errorJson.encodePrettily();
        }
    }
}
