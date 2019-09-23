module com.abc.java9.processmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.abc.java9.processmanager to javafx.fxml;
    exports com.abc.java9.processmanager;
}