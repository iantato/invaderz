package org.invaderz.util;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javafx.scene.control.TextField;

public class Randomizer {
    
    int minRandom = 2;
    int maxRandom = 6;
    int currentRandomized = minRandom;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String CAPITAL_ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static String SPECIAL_CHARACTERS = "!@$%*-_+:";
    private static final String ALL_CHARACTERS = ALPHABET + CAPITAL_ALPHABETS + NUMBERS + SPECIAL_CHARACTERS;

    static HashMap<TextField, String> nodesStorage = new HashMap<TextField, String>();
    static HashMap<String, String> randomizedContainer = new HashMap<String, String>();

    public Randomizer getInstance() {
        return this;
    }

    public List<String> getRemovables(int question_id) throws SQLException {
        String[] removables = Database.fetchRemovables(question_id);
        return Arrays.asList(removables);
    }

    public String[] randomizeQuestion(int question_id, String question, String language) throws SQLException {

        Random rand = new Random();
        Parser parser = new Parser(language);
        String[] parsedQuestion = parser.parseCode(question);

        while (currentRandomized <= maxRandom) {
            for (int i = 0; i < parsedQuestion.length; i++) {

                if (parsedQuestion[i].contains("\n")) {
                    parser.resetCategories();
                }
                
                String style = parser.getStyleCategory(parsedQuestion[i]);
                if (style.equals("variable") && i < parsedQuestion.length - 1) {
                    style = parser.checkMethod(parsedQuestion[i + 1]);
                }

                if (getRemovables(question_id).contains(parsedQuestion[i])
                    && rand.nextBoolean() && !parser.isComment()) {
                    
                    String randomizedWord = "";

                    for (int j = 0; j < parsedQuestion[i].length(); j++) {
                        randomizedWord += ALL_CHARACTERS.charAt(rand.nextInt(ALL_CHARACTERS.length()));
                    }

                    randomizedContainer.put(randomizedWord, parsedQuestion[i]);
                    parsedQuestion[i] = randomizedWord;

                    currentRandomized++;

                }
            }
        }

        return parsedQuestion;
    }

    public boolean checkRandomString(String text) {
        if (randomizedContainer.containsKey(text)) {
            return true;
        }
        
        return false;
    }

    public int getMaximumRandomized() {
        return this.maxRandom;
    }

    public int getCurrentRandomized() {
        return this.currentRandomized;
    }

    public static void addRandomizedNode(TextField node, String correction) {
        nodesStorage.put(node, correction);
    }

    public static void clearRandomizedNode() {
        nodesStorage.clear();
    }

    public static void clearRandomizedCorrections() {
        randomizedContainer.clear();
    }

    public static HashMap<TextField, String> getRandomizedNodes() {
        return nodesStorage;
    }

    public static boolean checkRandomizedCorrection(String userInput, String randomized) {
        String answer = randomizedContainer.get(randomized);

        if (userInput.equals(answer)) {
            return true;
        } else {
            return false;
        }
    }
}
