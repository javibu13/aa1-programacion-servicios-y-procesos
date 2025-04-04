package com.sanvalero.imagefilters;

import com.sanvalero.imagefilters.controller.MainController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting JavaFX application...");
        // Load the FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));
        // Set the controller for the FXML file
        MainController mainController = new MainController();
        fxmlLoader.setController(mainController);
        // Load the FXML file and set it as the scene
        scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setScene(scene);
        stage.setTitle("Image Filters");
        // Set the icon for the application
        // stage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("icon.png")));
        stage.show();
        logger.info("JavaFX application started successfully.");
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}