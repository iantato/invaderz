package org.invaderz.nodes;

import java.io.IOException;

import org.invaderz.App;
import org.invaderz.controller.IDE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class LevelButton extends Button {

    int question_id;
    int level;
    boolean unlocked, finished;
    
    public LevelButton(int question_id, int level, boolean unlocked) throws IOException {

        this.question_id = question_id;
        this.finished = unlocked;
        this.level = level;
        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/levelButton.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.load();

        setUnlocked(unlocked);
        setText(String.format("Level%s", level));
    }

    public void setUnlocked(boolean unlocked) {
        if (unlocked) {
            getStyleClass().add("unlocked");
        } else {
            getStyleClass().add("locked");
        }

        this.unlocked = unlocked;
    }

    @FXML
    public void setState(ActionEvent e) {
        if (unlocked) {

           IDE parent = IDE.getInstance(); 

            if (parent != null) {
                if (!parent.getIfLoading()){
                    if (parent.lookup("#selectedLevel") != null) {
                        parent.lookup("#selectedLevel").setId("");
                    }

                    setId("selectedLevel");
                    parent.loadCode(question_id);
                }
            }
        }
    }
}