package com.abc.java9.processmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ProcessManager with JavaFX
 * 
 * @author brian
 *
 */
public class ProcessManager extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(FXMLLoader.load(getClass().getResource("fxml/processmanager.fxml")));
        scene.getStylesheets().add(getClass().getResource("css/styles.css").toString());
        
        stage.setTitle("Process Manager App");
        stage.setScene(scene);
        stage.show();
    }

    
    public static void main(String[] args) {
        launch();
    }

}