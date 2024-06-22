package org.invaderz.nodes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.invaderz.App;
import org.invaderz.controller.IDE;
import org.invaderz.util.Parser;
import org.invaderz.util.Randomizer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CodeRow extends HBox {

    private CodeRow instance;
    IDE parent = IDE.getInstance();
    private Randomizer randomizer;

    private String language;

    @FXML private Label rowID;
    @FXML private HBox textStorage;
    
    public CodeRow(Randomizer randomizer, String language) throws IOException {

        this.instance = this;
        this.language = language;
        this.randomizer = randomizer;

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/codeRow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.load();

        getStylesheets().add(App.class.getResource("css/editor.css").toExternalForm());
    }

    @FXML
    public void setSelectedRow(MouseEvent mouseEvent) {

        if (parent != null) {
            if (parent.lookup("#selectedRow") != null) {
                parent.lookup("#selectedRow").setId("");
            }
            
            setId("selectedRow");
        }

        if (mouseEvent.getTarget() instanceof CodeRow) {
            TextField lastChild = (TextField) getChildren().get(getChildren().size() - 1);
            lastChild.requestFocus();
            lastChild.positionCaret(lastChild.getText().length());
        }
    }

    public void setSelectedRow() {

        IDE parent = IDE.getInstance();

        if (parent != null) {
            if (parent.lookup("#selectedRow") != null) {
                parent.lookup("#selectedRow").setId("");
            }
            
            setId("selectedRow");
        }

        TextField lastChild = (TextField) getChildren().get(getChildren().size() - 1);
        lastChild.requestFocus();
        lastChild.positionCaret(lastChild.getText().length());

    }

    public void setRowID(int level) {
        rowID.setText(String.valueOf(level));
    }

    public double getTextMeasure(String s) {
        Text text = new Text(s);
        text.setFont(Font.font("Inter", FontWeight.NORMAL, FontPosture.REGULAR, 16));

        return text.getBoundsInLocal().getWidth();
    }

    public double getBoldTextMeasure(String s) {
        Text text = new Text(s);
        text.setFont(Font.font("Inter", FontWeight.BOLD, FontPosture.REGULAR, 16));

        return text.getBoundsInLocal().getWidth();
    }

    public void fillRow(ArrayList<String> line) {

        Parser parser = new Parser(language);
        if (line.isEmpty()) line.add("");

        try {
            for (int i = 0; i < line.size(); i++) {
                String word = line.get(i);

                TextField codeNode = new TextField(word);
                codeNode.setFont(Font.font("Inter", FontWeight.NORMAL, FontPosture.REGULAR, 16));
                codeNode.setPrefWidth(getTextMeasure(word) + 0.5);
                codeNode.setMinWidth(getTextMeasure(word) + 0.5);
                codeNode.getStyleClass().add("codeField");
                setKeyListeners(codeNode, word);
                
                codeNode.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        setSelectedRow(mouseEvent);
                    }
                });

                String style = parser.getStyleCategory(word);
                if (style.equals("variable") && i < line.size() - 1) {
                    style = parser.checkMethod(line.get(i + 1));
                }
                codeNode.getStyleClass().add(style);

                if (randomizer.checkRandomString(word)) {

                    Randomizer.addRandomizedNode(codeNode, word);

                    codeNode.setFont(Font.font("Inter", FontWeight.BOLD, FontPosture.REGULAR, 16));
                    codeNode.getStyleClass().add("randomized");
                    codeNode.setPrefWidth(getBoldTextMeasure(word));
                    codeNode.setMinWidth(getBoldTextMeasure(word));

                    codeNode.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> ob, String oldValue, String newValue) {
                            
                            if (!newValue.equals("")) {
                                codeNode.setPrefWidth(getBoldTextMeasure(codeNode.getText()) + 1);
                                codeNode.setMinWidth(getBoldTextMeasure(codeNode.getText()) + 1);
                            } else {
                                codeNode.setPrefWidth(10);
                                codeNode.setMinWidth(10);
                            }
                        }    
                    });

                } else {

                    codeNode.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent keyEvent) {
                            keyEvent.consume();
                        }
                    });
                }

                textStorage.getChildren().add(codeNode);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void setKeyListeners(TextField codeNode, String word) {
        codeNode.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {

                IDE parent = IDE.getInstance();

                Parent editorStorage;
                int caretPosition;
                int currentRow;
                
                switch (keyEvent.getCode()) {
                    case KeyCode.LEFT:

                        caretPosition = codeNode.getCaretPosition();

                        if (caretPosition == 0 && getChildren().indexOf(codeNode) != 2) {
                            TextField prevNode = (TextField) getChildren().get(getChildren().indexOf(codeNode) - 1);
                            prevNode.requestFocus();
                            if (codeNode.getText().length() == 0) {
                                prevNode.positionCaret(prevNode.getText().length());
                            } else {
                                prevNode.positionCaret(prevNode.getText().length() - 1);
                            }
                        }

                        break;
                
                    case KeyCode.RIGHT:

                        caretPosition = codeNode.getCaretPosition();
                        if (caretPosition == codeNode.getText().length() && 
                            getChildren().indexOf(codeNode) != getChildren().size() - 1) {
                            
                            TextField nextNode = (TextField) getChildren().get(getChildren().indexOf(codeNode) + 1);
                            nextNode.requestFocus();
                            if (codeNode.getText().length() == 0) {
                                nextNode.positionCaret(0);
                            } else {
                                nextNode.positionCaret(1);
                            }
                        }
                        break;

                    case KeyCode.UP:

                        caretPosition = codeNode.getCaretPosition();
                        editorStorage = parent.lookup("#selectedRow").getParent();

                        currentRow = editorStorage.getChildrenUnmodifiable().indexOf(instance);
                        
                        if (currentRow != 0) {

                            CodeRow prevRow = (CodeRow) editorStorage.getChildrenUnmodifiable().get(currentRow - 1);
                            prevRow.setSelectedRow();
                        }
                        
                        break;
                    
                    case KeyCode.DOWN:

                        caretPosition = codeNode.getCaretPosition();
                        editorStorage = parent.lookup("#selectedRow").getParent();

                        currentRow = editorStorage.getChildrenUnmodifiable().indexOf(instance);

                        if (currentRow != editorStorage.getChildrenUnmodifiable().size() - 1) {
                            
                            CodeRow nextRow = (CodeRow) editorStorage.getChildrenUnmodifiable().get(currentRow + 1);
                            nextRow.setSelectedRow();

                        }

                        break;

                    case KeyCode.BACK_SPACE:

                        caretPosition = codeNode.getCaretPosition();

                        if (!randomizer.checkRandomString(word)) {
                            TextField prevNode = (TextField) getChildren().get(getChildren().indexOf(codeNode) - 1);
                            
                            if (prevNode.getStyleClass().contains("randomized") && caretPosition == 0  
                                && getChildren().indexOf(codeNode) != 2 & prevNode.getText().length() > 0) {
                                String randomizedWord = prevNode.getText();
                                
                                prevNode.setText(randomizedWord.substring(0, prevNode.getText().length() - 1));
                                prevNode.requestFocus();
                                prevNode.positionCaret(prevNode.getText().length());
                            }

                            keyEvent.consume();
                        } else {
                            
                        }
                        break;
                    
                    default:

                        if (!randomizer.checkRandomString(word)) {
                            keyEvent.consume();
                        }
                        break;
                }
            }
        });
    }

}