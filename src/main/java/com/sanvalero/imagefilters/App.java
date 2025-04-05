package com.sanvalero.imagefilters;

import com.sanvalero.imagefilters.controller.MainController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static MainController mainController;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting JavaFX application...");
        showSplash(stage);
    }

    private void showSplash(Stage primaryStage) {
        logger.info("Starting Splash Screen...");
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);

        Image splashImage = new Image(App.class.getResourceAsStream("splashScreen.png"));
        ImageView splashImageView = new ImageView(splashImage);
        splashImageView.setPreserveRatio(true);
        splashImageView.setFitWidth(600);

        StackPane splashLayout = new StackPane(splashImageView);
        Scene splashScene = new Scene(splashLayout);
        splashStage.setScene(splashScene);
        splashStage.show();

        // Simulate a delay for the splash screen
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            splashStage.close();
            showMainWindow(primaryStage);
        });
        delay.play();
    }

    private void showMainWindow(Stage primaryStage) {
        logger.info("Starting Main Window...");
        // Load the FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));
        // Set the controller for the FXML file
        mainController = new MainController();
        fxmlLoader.setController(mainController);
        // Load the FXML file and set it as the scene
        try {
            scene = new Scene(fxmlLoader.load(), 900, 600);
        } catch (IOException e) {
            logger.error("Failed to load the main FXML file", e);
            return;
        }
        primaryStage.setScene(scene);
        primaryStage.setTitle("Image Filters");
        // Set the icon for the application
        // stage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("icon.png")));
        primaryStage.show();
        logger.info("JavaFX application started successfully.");
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() throws Exception {
        mainController.shutdownExecutorService();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }

}