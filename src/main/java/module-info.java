module org.invaderz {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    
    requires java.sql;

    opens org.invaderz to javafx.fxml;
    opens org.invaderz.nodes to javafx.fxml;
    opens org.invaderz.controller to javafx.fxml;

    exports org.invaderz;
    exports org.invaderz.controller;
}
