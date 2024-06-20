package org.invaderz.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.invaderz.App;
import org.invaderz.controller.IDE;
import org.invaderz.util.Database;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ChapterButton extends Button {

    private IDE rootParent;
    
    boolean isOpen = false;

    private String username;
    private String chapter;
    private String language;

    public ChapterButton(int index, String username, String chapter, String language) throws IOException {

        this.username = username;
        this.chapter = chapter;
        this.language = language;

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/chapterButton.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.load();

        setText(String.format("%03d-%s", index + 1, chapter));
        setId(chapter);
    }

    @FXML
    protected void openFolder(MouseEvent mouseEvent) {

        if (isOpen) {
            getStyleClass().remove("open");
            getStyleClass().add("close");
            
            ((VBox) getParent()).getChildren()
                                .subList(1,
                                         getParent().getChildrenUnmodifiable()
                                                    .size())
                                .clear();
            
        } else {
            getStyleClass().remove("close");
            getStyleClass().add("open");
            loadLevels();
        }

        isOpen = !isOpen;
    }

    protected void loadLevels() {

        getParent().getChildrenUnmodifiable()
                   .subList(1,
                            getParent().getChildrenUnmodifiable()
                                       .size()).clear();
        
        Task<ArrayList<LevelButton>> task = new Task<ArrayList<LevelButton>>() {
            
            @Override
            protected ArrayList<LevelButton> call() throws Exception {

                rootParent = IDE.getInstance();

                int lastUnlocked = -1;
                ArrayList<LevelButton> levelButtonArray = new ArrayList<LevelButton>();
                HashMap<Integer, String> problems = Database.fetchAllQuestions(language, chapter);

                rootParent.setProblems(problems);
                ArrayList<Integer> questionKeys = new ArrayList<Integer>(problems.keySet());

                for (int i = 0; i < questionKeys.size(); i++) {
                    
                    int level = i + 1;
                    if (Database.checkUnlocked(username, questionKeys.get(i))) {
                        lastUnlocked = i;
                    }

                    LevelButton levelButton = new LevelButton(questionKeys.get(i), 
                                                              level,
                                                              Database.checkUnlocked(username, questionKeys.get(i)));
                    levelButton.setPrefWidth(rootParent.getExplorerAnchor().getMaxWidth());
                    
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

            Platform.runLater(() -> {
                ((VBox) getParent()).getChildren().addAll(levelButtonArray);
            });
        });

        task.setOnFailed(evt -> {
            System.out.println(task.getException().getMessage());
        });

        new Thread(task).start();
    }

}
