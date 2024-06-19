package org.invaderz.util;

import java.sql.SQLException;

public class Parser {

    String language;

    boolean isComment = false;
    boolean isString = false;

    public Parser(String language) {
        this.language = language;
    }
    
    public String[] parseCode(String codeBlock) {
        String delimiters = " |\"|[(]|[)]|\\.|;|\\[|\\]|\n";
        String regex = String.format("((?=%s)|(?<=%s))", delimiters, delimiters);

        String[] parsedCode = codeBlock.split(regex);
        return parsedCode;
    }

    public String getStyleCategory(String code) throws SQLException {
        
        checkString(code);
        if (Database.getStyleCategory(language, code) != null && code.contains("\"")) {
            return Database.getStyleCategory(language, code);
        }
        if (isString) {
            return "string";
        }

        checkComment(code);
        if (isComment) {
            return "comment";
        }

        if (checkClass(code).equals("attribute")) {
            return checkClass(code);
        }

        if (Database.getStyleCategory(language, code) != null) {
            return Database.getStyleCategory(language, code); 
        }

        return "variable";
    }

    public void checkString(String code) {
        if (code.contains("\"") && !isComment) {
            isString = !isString;
        }
    }

    public void checkComment(String code) {
        if (code.contains("//") && !isString) {
            isComment = !isComment;
        }
    }

    public String checkClass(String code) {
        if (!code.isEmpty()) {
            if (Character.isUpperCase(code.charAt(0))) {
                return "attribute";
            }
        }

        return "variable";
    }

    public String checkMethod(String nextCode) {
        if (nextCode.equals("(")) {
            return "method";
        } else {
            return "variable";
        }
    }

    public boolean isComment() {
        return isComment;
    }

    public void resetCategories() {
        isString = false;
        isComment = false;
    }

}
