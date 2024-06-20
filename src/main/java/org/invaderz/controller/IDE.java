package org.invaderz.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.invaderz.App;
import org.invaderz.nodes.ChapterButton;
import org.invaderz.nodes.CodeRow;
import org.invaderz.nodes.LevelButton;
import org.invaderz.util.Database;
import org.invaderz.util.Randomizer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class IDE extends AnchorPane {
    
    private static IDE instance;
    private final int SLEEP_MS = 100;
    private boolean isLoading = false;

    private String username;
    private String language;

    private HashMap<Integer, String> problems;
    private ArrayList<CodeRow> rowBuffer = new ArrayList<CodeRow>();

    @FXML private AnchorPane explorerAnchor;
    @FXML private VBox explorerStorage;
    @FXML private VBox editorStorage;
    @FXML private Label outputLabel;

    public IDE(String username, String language) throws IOException {
        this.username = username;
        this.language = language;
        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/ide.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.load();
    }
    
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // this.loadLevels(language, chapter);
            this.loadChapters(language);
        });
    }

    @FXML
    public void resetEditor(MouseEvent mouseEvent) {
        
        Runnable task = new Runnable() {
            @Override
            public void run() {

                isLoading = true;

                try {
                    for (CodeRow codeRow : rowBuffer) {
                        Thread.sleep(SLEEP_MS);

                        Platform.runLater(() -> {
                            editorStorage.getChildren().add(codeRow);
                        });

                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                } finally {
                    isLoading = false;
                }
            }
        };
        
        if (!isLoading) {

            editorStorage.getChildren().clear();
            new Thread(task).start();

        }
    }

    @FXML
    public void runCode(MouseEvent mouseEvent) {
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                if (!Randomizer.getRandomizedNodes().isEmpty() && !isLoading) {


                    for (Map.Entry<TextField,String> entry : Randomizer.getRandomizedNodes().entrySet()) {

                        TextField node = entry.getKey();
                        String randomizedCounterPart = entry.getValue();

                        if (!Randomizer.checkRandomizedCorrection(node.getText(), randomizedCounterPart)) {
                            return false;
                        }

                    }

                    return true;
                }

                return null;
            }
        };

        task.setOnSucceeded(evt -> {
            try {
                addTerminalInput(task.getValue());
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        });

        task.setOnFailed(evt -> {
            System.out.println(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    public static IDE getInstance(String username, String language, String chapter) throws IOException {
        if (instance == null) {
            instance = new IDE(username, language);
        }
    
        return instance;
    }

    public static IDE getInstance() {
        return instance;
    }

    protected void loadChapters(String language) {
       
        Runnable task = new Runnable() {
            @Override
            public void run() {

                try {
                    
                    ArrayList<String> chapters = Database.fetchAllChapters(language);

                    for (int i = 0; i < chapters.size(); i++) {
                        VBox chapterStorage = new VBox();
                        chapterStorage.setMaxWidth(Double.MAX_VALUE);
                        chapterStorage.setFillWidth(true);

                        ChapterButton chapterButton = new ChapterButton(i, username, chapters.get(i), language);

                        chapterStorage.getChildren().add(chapterButton);

                        Platform.runLater(() -> {
                            explorerStorage.getChildren().add(chapterStorage);
                        });
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            }
        };

        new Thread(task).start();



    }

    public void unlockNextLevel() throws SQLException {
        
        VBox levelStorage = (VBox) lookup("#selectedLevel").getParent();
        int currentLevelIndex = levelStorage.getChildren().indexOf(levelStorage.lookup("#selectedLevel"));
        LevelButton currentLevel = (LevelButton) levelStorage.lookup("#selectedLevel");
        if (!currentLevel.checkUnlocked()) {
            currentLevel.setFinished(username);
        }

        LevelButton nextLevel = (LevelButton) levelStorage.getChildren().get(currentLevelIndex + 1);
        if (!nextLevel.checkUnlocked()) {
            nextLevel.setUnlocked(true);
        }
    }

    public void loadCode(int question_id) {

        Runnable task = new Runnable() {
            @Override
            public void run() {

                isLoading = true;
                rowBuffer.clear();

                Randomizer randomizer = new Randomizer();
                Randomizer.clearRandomizedCorrections();
                Randomizer.clearRandomizedNode();
                String question = problems.get(question_id);

                try {
                    String[] randomized = randomizer.randomizeQuestion(question_id, question, language);
                    ArrayList<String> line = new ArrayList<String>();
                    int rowID = 1;

                    for (int i = 0; i < randomized.length; i++) {

                        if (!randomized[i].contains("\n")) {
                            line.add(randomized[i]);
                        
                        } else {
                            Thread.sleep(SLEEP_MS);
                            CodeRow row = new CodeRow(randomizer.getInstance(), language);
                            row.setRowID(rowID);
                            row.fillRow(line);

                            Platform.runLater(() -> {
                                editorStorage.getChildren().add(row);
                            });

                            rowBuffer.add(row);
                            line.clear();
                            rowID++;
                            continue;
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    isLoading = false;
                }
            }
        };

        if (!isLoading) {
            editorStorage.getChildren().clear();
            new Thread(task).start();
        }
    }

    public void addTerminalInput(Boolean output) throws SQLException {

        if (output == null) {
            outputLabel.setText("ERROR! File might not be fully loaded.");
            outputLabel.setId("errorOutput");
        } else if (output) {
            outputLabel.setText("SUCCESS! Please proceed to the next level.");
            outputLabel.setId("successOutput");
            unlockNextLevel();

        } else {
            outputLabel.setText("ERROR! Please check again.");
            outputLabel.setId("errorOutput");
        }
    }

    public boolean getIfLoading() {
        return isLoading;
    }

    public AnchorPane getExplorerAnchor() {
        return explorerAnchor;
    }

    public void setProblems(HashMap<Integer, String> problems) {
        this.problems = problems;
    }
}