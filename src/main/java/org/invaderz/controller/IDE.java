package org.invaderz.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.invaderz.App;
import org.invaderz.nodes.CodeRow;
import org.invaderz.nodes.LevelButton;
import org.invaderz.util.Database;
import org.invaderz.util.Randomizer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class IDE extends AnchorPane {
    
    private static IDE instance;
    private final int SLEEP_MS = 100;
    private boolean isLoading = false;

    private String username;
    private String language;

    private String chapter;

    private HashMap<Integer, String> problems;
    private ArrayList<CodeRow> rowBuffer = new ArrayList<CodeRow>();

    @FXML private AnchorPane explorerAnchor;
    @FXML private VBox explorerStorage;
    @FXML private VBox editorStorage;
    @FXML private ScrollPane editorScroll;

    public IDE(String username, String language, String chapter) throws IOException {
        this.username = username;
        this.language = language;
        this.chapter = chapter;
        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/ide.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.load();
    }
    
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            this.loadLevels(language, chapter);
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
        
    }

    public static IDE getInstance(String username, String language, String chapter) throws IOException {
        if (instance == null) {
            instance = new IDE(username, language, chapter);
        }
        
        return instance;
    }

    public static IDE getInstance() {
        return instance;
    }

    protected void loadLevels(String language, String chapter) {

        Task<ArrayList<LevelButton>> task = new Task<ArrayList<LevelButton>>() {
            @Override
            protected ArrayList<LevelButton> call() throws Exception {

                
                int lastUnlocked = -1;
                ArrayList<LevelButton> levelButtonArray = new ArrayList<LevelButton>();
                
                problems = Database.fetchAllQuestions(language, chapter);
                ArrayList<Integer> questionKeys = new ArrayList<Integer>(problems.keySet());

                for (int i = 0; i < questionKeys.size(); i++) {
                    
                    int level = i + 1;
                    if (Database.checkUnlocked(username, questionKeys.get(i))) {
                        lastUnlocked = i;
                    }

                    LevelButton levelButton = new LevelButton(questionKeys.get(i), 
                                                              level, 
                                                              Database.checkUnlocked(username, questionKeys.get(i)));
                    
                    levelButton.setPrefWidth(explorerAnchor.getMaxWidth());

                    levelButtonArray.add(levelButton);
                }

                LevelButton levelButton = levelButtonArray.get(lastUnlocked + 1);
                levelButton.getStyleClass().remove("locked");
                levelButton.setUnlocked(true);
                levelButton.getStyleClass().add("unfinished");


                return levelButtonArray;
            } 
        };

        task.setOnSucceeded(evt -> {
            ArrayList<LevelButton> levelButtonArray = task.getValue();
            explorerStorage.getChildren().addAll(levelButtonArray);
        });

        task.setOnFailed(evt -> {
            System.out.println(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    public void loadCode(int question_id) {

        Runnable task = new Runnable() {
            @Override
            public void run() {

                isLoading = true;
                rowBuffer.clear();

                Randomizer randomizer = new Randomizer();
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

    public boolean getIfLoading() {
        return isLoading;
    }

    public ScrollPane getEditorScroll() {
        return editorScroll;
    }


}
