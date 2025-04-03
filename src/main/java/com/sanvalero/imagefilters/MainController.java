package com.sanvalero.imagefilters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private VBox rootVBox;

    @FXML
    private ChoiceBox<String> mainFilter1;
    @FXML
    private ChoiceBox<String> mainFilter2;
    @FXML
    private ChoiceBox<String> mainFilter3;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mainFilter1.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        mainFilter2.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        mainFilter3.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
    }

    @FXML
    private void openSingleImage(ActionEvent event) {
        logger.info("Opening single image...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(rootVBox.getScene().getWindow());
        
        if (selectedFile != null) {
            logger.info("Selected image: " + selectedFile.getAbsolutePath());
            // TODO: Develop logic to display the image in the interface
        }
    }

    @FXML
    private void openMultipleImages(ActionEvent event) {
        logger.info("Opening multiple images...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(rootVBox.getScene().getWindow());
        
        if (selectedFiles.size() > 0) {
            logger.info("Selected images: ");
            for (File file : selectedFiles) {
                logger.info(file.getAbsolutePath());
                // TODO: Develop logic to display the images in the interface
            }
        }
    }

}
