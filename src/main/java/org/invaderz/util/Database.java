package org.invaderz.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Database {

    public Database() throws SQLException {
        this.createTables();
    }

    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:invaderz.db";
        Connection connection = DriverManager.getConnection(url);
        return connection;
    }

    public static void disconnect(Connection connection) throws SQLException {
        if (connection != null) connection.close();
    }

    private void createTables() throws SQLException {
        
        Connection connection = connect();
        Statement stmt = connection.createStatement();
        String sql;

        sql = """
                CREATE TABLE IF NOT EXISTS Questions (
                    question_id INTEGER PRIMARY KEY,
                    language VARCHAR(32),
                    chapter VARCHAR(32),
                    question VARCHAR(4096),
                    removables VARCHAR(255),
                    time INTEGER(255)
                );  
              """;
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS Categories (
                    category_id INTEGER PRIMARY KEY,
                    language VARCHAR(32) NOT NULL,
                    keyword VARCHAR(255),
                    styleCategory VARCHAR(32),
                    problemCategory VARCHAR(32),
                    CONSTRAINT fk_language FOREIGN KEY (language) REFERENCES Questions(language)
                );
              """;
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS WordStorage (
                    keyword_id INTEGER PRIMARY KEY,
                    language VARCHAR(32) NOT NULL,
                    chapter VARCHAR(32) NOT NULL,
                    level SHORT,
                    occurences INTEGER(32),
                    keyword VARCHAR(255),
                    category VARCHAR(32) NOT NULL,
                    CONSTRAINT fk_language FOREIGN KEY (language) REFERENCES Questions(language),
                    CONSTRAINT fk_chapter FOREIGN KEY (chapter) REFERENCES Questions(chapter),
                    CONSTRAINT fk_problemCategory FOREIGN KEY (category) REFERENCES Categories(problemCategory)
                );  
              """;
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS Users (
                    username VARCHAR(32) PRIMARY KEY,
                    password VARCHAR(255),
                    money INTEGER(255)
                );  
              """;
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS Saves (
                    save_id INTEGER PRIMARY KEY,
                    username VARCHAR(32) NOT NULL,
                    question_id INTEGER NOT NULL,
                    time VARCHAR(255),
                    CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES Users(username),
                    CONSTRAINT fk_question_id FOREIGN KEY (question_id) REFERENCES Questions(question_id)
                );  
              """;
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS Powerups (
                    power_id INTEGER PRIMARY KEY,
                    name VARCHAR(32),
                    rarity VARCHAR(255),
                    cost INTEGER(255),
                    description VARCHAR(4096)
                );  
              """;
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS Inventory (
                    item_slot INTEGER PRIMARY KEY,
                    username VARCHAR(32) NOT NULL,
                    power_id INTEGER NOT NULL,
                    CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES Users(username),
                    CONSTRAINT fk_power_id FOREIGN KEY (power_id) REFERENCES Powerups(power_id)
                );  
              """;
        stmt.execute(sql);


        disconnect(connection);
    }
    
    /* 
     * INSERTING
     */
    public static void insertQuestion(String language, String chapter, String question, String[] removables, int time) throws SQLException {

        Connection connection = connect();

        String sql = "INSERT INTO Questions (language, chapter, question, removables, time) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, chapter);
        pstmt.setString(3, question);
        pstmt.setString(4, Arrays.toString(removables));
        pstmt.setInt(5, time);

        pstmt.executeUpdate();

        addKeywordsOccurences(question, language, chapter);

        disconnect(connection);

    }

    public static void insertCategory(String language, String keyword, String styleCategory, String problemCategory) throws SQLException {

        Connection connection = connect();
        
        String sql = "INSERT INTO Categories (language, keyword, styleCategory, problemCategory) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, keyword);
        pstmt.setString(3, styleCategory);
        pstmt.setString(4, problemCategory);

        pstmt.executeUpdate();

        disconnect(connection);
    }

    public static void insertUser(String username, String password, int money) throws SQLException {

        Connection connection = connect();
        
        String sql = "INSERT INTO User (username, password, money) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setInt(3, money);

        pstmt.executeUpdate();

        disconnect(connection);
    }

    public static void insertPowerup(String name, String rarity, int cost, String description) throws SQLException {

        Connection connection = connect();

        String sql = "INSERT INTO Powerups (name, rarity, cost, description) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, name);
        pstmt.setString(2, rarity);
        pstmt.setInt(3, cost);
        pstmt.setString(4, description);

        pstmt.executeUpdate();

        disconnect(connection);
    }

    public static void insertInventory(String username, int power_id, int quantity) throws SQLException {
        
        Connection connection = connect();

        String sql = "INSERT INTO User (username, power_id, quantity) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, username);
        pstmt.setInt(2, power_id);
        pstmt.setInt(3, quantity);

        pstmt.executeUpdate();

        disconnect(connection);
    }

    public static void insertSave(String username, int question_id, String time) throws SQLException {

        Connection connection = connect();

        String sql = "INSERT INTO Saves (username, question_id, time) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, username);
        pstmt.setInt(2, question_id);
        pstmt.setString(3, time);

        pstmt.executeUpdate();

        disconnect(connection);
    }

    public static void insertKeyword(String language, String chapter, int level,
                                     int occurences, String keyword, String category) throws SQLException {
        
        Connection connection = connect();

        String sql = "INSERT INTO WordStorage (language, chapter, level, occurences, keyword, category) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, chapter);
        pstmt.setInt(3, level);
        pstmt.setInt(4, occurences);
        pstmt.setString(5, keyword);
        pstmt.setString(6, category);

        pstmt.executeUpdate();

        disconnect(connection);
    }

    /*
     * FETCHING
     */
    public static HashMap<Integer, String> fetchAllQuestions(String language, String chapter) throws SQLException {
        
        Connection connection = connect();
        
        String sql = "SELECT question_id, question FROM Questions WHERE language = ? AND chapter = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, chapter);

        HashMap<Integer, String> rsMap = new HashMap<Integer, String>();
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            rsMap.put(rs.getInt("question_id"), rs.getString("question"));
        }
        
        disconnect(connection);
        return rsMap;
    }

    public static ArrayList<String> fetchAllKeywords(String language, String chapter) throws SQLException {
        
        Connection connection = connect();

        String sql = "SELECT keyword FROM WordStorage WHERE language = ? AND chapter = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, chapter);

        ArrayList<String> rsArray = new ArrayList<String>();
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            rsArray.add(rs.getString("keyword"));
        }

        disconnect(connection);
        return rsArray;
    }

    public static int fetchKeywordOccurence(String language, String chapter, int level, String keyword) throws SQLException {

        Connection connection = connect();

        String sql = "SELECT occurences FROM WordStorage WHERE language = ? AND chapter = ? AND level = ? AND keyword = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, chapter);
        pstmt.setInt(3, level);
        pstmt.setString(4, keyword);

        ArrayList<Integer> rsArray = new ArrayList<Integer>();
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            rsArray.add(rs.getInt("occurences"));
        }

        if (rsArray.isEmpty()) {
            disconnect(connection);
            return 0;
        } else {
            disconnect(connection);
            return rsArray.get(0);
        }

    }

    public static boolean checkUnlocked(String username, int question_id) throws SQLException {
        
        Connection connection = connect();
        
        String sql = "SELECT save_id FROM Saves WHERE username = ? AND question_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, username);
        pstmt.setInt(2, question_id);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (!rs.isBeforeFirst()) {
            disconnect(connection);
            return false;
        } else {
            disconnect(connection);
            return true;
        }
    }

    public static String getStyleCategory(String language, String keyword) throws SQLException {
        
        Connection connection = connect();

        String sql = "SELECT styleCategory FROM Categories WHERE language = ? AND keyword = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, keyword);

        String rsString = "";
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            rsString = rs.getString("styleCategory");
        }

        disconnect(connection);
        return rsString;
    }

    public static String getProblemCategory(String language, String keyword) throws SQLException {

        Connection connection = connect();

        String sql = "SELECT problemCategory FROM Categories WHERE language = ? AND keyword = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, language);
        pstmt.setString(2, keyword);

        String rsString = "";
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            rsString = rs.getString("problemCategory");
        }

        disconnect(connection);
        return rsString;
    }

    public static String[] fetchRemovables(int question_id) throws SQLException {

        Connection connection = connect();

        String sql = "SELECT removables FROM Questions WHERE question_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, question_id);

        String removablesString = "";
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.isBeforeFirst()) {
            removablesString = rs.getString("removables");
        }

        String[] removables = removablesString.substring(1)
                                              .substring(0, removablesString
                                              .length() - 2).split(",");

        for (int i = 0; i < removables.length; i++) {
            removables[i] = removables[i].trim();
        }

        return removables;
    }

    /*
     * RANDOM ADDITIONAL METHODS
     */

    public static void updateKeywordAmount(String keyword, int occurences, String language,
                                           String chapter, int level) throws SQLException {
        
        Connection connection = connect();

        String sql = "UPDATE WordStorage SET occurences = ? WHERE language = ? AND chapter = ? AND level = ? AND keyword = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, occurences + 1);
        pstmt.setString(2, language);
        pstmt.setString(3, chapter);
        pstmt.setInt(4, level);
        pstmt.setString(5, keyword);

        pstmt.executeUpdate();

        disconnect(connection);
    }

    public static void addKeywordsOccurences(String problem, String language, String chapter) throws SQLException {

        Parser parser = new Parser(language);
        ArrayList<String> parsedQuestion = new ArrayList<String>(Arrays.asList(
                                                                 parser.parseCode(problem)));
        parsedQuestion.removeAll(Arrays.asList(" ", null));

        for (int i = 0; i < parsedQuestion.size(); i++) {
            
            String code = parsedQuestion.get(i);

            if (code.equals("\n")) {
                parser.resetCategories();
                continue;
            }

            String styleCategory = parser.getStyleCategory(code);
            if (styleCategory.equals("variable") && i < parsedQuestion.size() - 1) {
                styleCategory = parser.checkMethod(parsedQuestion.get(i + 1));
            }

            int level = fetchAllQuestions(language, chapter).size();

            if (fetchKeywordOccurence(language, chapter, level, code) != 0) {
                int occurence = fetchKeywordOccurence(language, chapter, level, code);
                updateKeywordAmount(code, occurence, language, chapter, level);
            } else if ((!styleCategory.equals("comment") || code.equals("//"))
                       && !styleCategory.equals("string")) {
                
                insertKeyword(language, chapter, level, 1, code, getProblemCategory(language, code));
            }
        }
    }


    public static void main(String[] args) {


        String problem = """
// Java program to take an integer
// as input and print it
import java.io.*;
import java.util.Scanner;
    
// Driver Class
class GFG {
        // main function
    public static void main(String[] args)
    {
        // Declare the variables
        int num;
    
        // Input the integer
        System.out.println("Enter the integer: ");
    
        // Create Scanner object
        Scanner s = new Scanner(System.in);
    
        // Read the next integer from the screen
        num = s.nextInt();
    
        // Display the integer
        System.out.println("Entered integer is: "
                            + num);
    }
}
        """;

        try {

            // System.out.println(Database.getStyleCategory("JAVA", "int"));

            // new Database();

            // Database.insertCategory("JAVA", "abstract", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "boolean", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "byte", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "char", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "class", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "const", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "default", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "double", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "exports", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "extends", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "final", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "finally", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "float", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "implements", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "import", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "instanceof", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "int", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "interface", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "long", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "package", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "private", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "protected", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "public", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "short", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "static", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "super", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "this", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "throw", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "throws", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "var", "keyword", "TESTS");
            // Database.insertCategory("JAVA", "void", "keyword", "TESTS");
            
            // Database.insertCategory("JAVA", "+", "operator", "TESTS");
            // Database.insertCategory("JAVA", "-", "operator", "TESTS");
            // Database.insertCategory("JAVA", "/", "operator", "TESTS");
            // Database.insertCategory("JAVA", "*", "operator", "TESTS");
            // Database.insertCategory("JAVA", "\"", "operator", "TESTS");
            // Database.insertCategory("JAVA", ".", "operator", "TESTS");
            // Database.insertCategory("JAVA", ":", "operator", "TESTS");
            // Database.insertCategory("JAVA", ";", "operator", "TESTS");
            // Database.insertCategory("JAVA", "%", "operator", "TESTS");
            // Database.insertCategory("JAVA", "++", "operator", "TESTS");
            // Database.insertCategory("JAVA", "--", "operator", "TESTS");
            // Database.insertCategory("JAVA", "=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "+=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "-=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "*=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "/=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "%=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "==", "operator", "TESTS");
            // Database.insertCategory("JAVA", "!=", "operator", "TESTS");
            // Database.insertCategory("JAVA", ">", "operator", "TESTS");
            // Database.insertCategory("JAVA", "<", "operator", "TESTS");
            // Database.insertCategory("JAVA", ">=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "<=", "operator", "TESTS");
            // Database.insertCategory("JAVA", "&&", "operator", "TESTS");
            // Database.insertCategory("JAVA", "||", "operator", "TESTS");
            // Database.insertCategory("JAVA", "!", "operator", "TESTS");
            // Database.insertCategory("JAVA", ")", "operator", "TESTS");
            // Database.insertCategory("JAVA", "(", "operator", "TESTS");
            // Database.insertCategory("JAVA", "}", "operator", "TESTS");
            // Database.insertCategory("JAVA", "{", "operator", "TESTS");
            // Database.insertCategory("JAVA", "]", "operator", "TESTS");
            // Database.insertCategory("JAVA", "[", "operator", "TESTS");
            // Database.insertCategory("JAVA", "true", "operator", "TESTS");
            // Database.insertCategory("JAVA", "false", "operator", "TESTS");
            // Database.insertCategory("JAVA", "return", "operator", "TESTS");
            // Database.insertCategory("JAVA", "new", "operator", "TESTS");
            // Database.insertCategory("JAVA", "while", "operator", "TESTS");
            // Database.insertCategory("JAVA", "for", "operator", "TESTS");
            // Database.insertCategory("JAVA", "if", "operator", "TESTS");
            // Database.insertCategory("JAVA", "else", "operator", "TESTS");
            // Database.insertCategory("JAVA", "try", "operator", "TESTS");
            // Database.insertCategory("JAVA", "catch", "operator", "TESTS");
            // Database.insertCategory("JAVA", "switch", "operator", "TESTS");
            // Database.insertCategory("JAVA", "case", "operator", "TESTS");
            // Database.insertCategory("JAVA", "continue", "operator", "TESTS");
            // Database.insertCategory("JAVA", "break", "operator", "TESTS");
            // Database.insertCategory("JAVA", "do", "operator", "TESTS");

            // Database.insertCategory("JAVA", "0", "number", "TESTS");
            // Database.insertCategory("JAVA", "1", "number", "TESTS");
            // Database.insertCategory("JAVA", "2", "number", "TESTS");
            // Database.insertCategory("JAVA", "3", "number", "TESTS");
            // Database.insertCategory("JAVA", "4", "number", "TESTS");
            // Database.insertCategory("JAVA", "5", "number", "TESTS");
            // Database.insertCategory("JAVA", "6", "number", "TESTS");
            // Database.insertCategory("JAVA", "7", "number", "TESTS");
            // Database.insertCategory("JAVA", "8", "number", "TESTS");
            // Database.insertCategory("JAVA", "9", "number", "TESTS");

            // Database.insertQuestion("JAVA", "TESTS", problem, new String[] {"Scanner", "int", "."}, 1000);

            System.out.println( Arrays.toString(Database.fetchRemovables(1)));;


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
