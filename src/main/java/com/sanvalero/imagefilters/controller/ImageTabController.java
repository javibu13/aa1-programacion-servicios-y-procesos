package com.sanvalero.imagefilters.controller;

import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.task.FilterTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;

public class ImageTabController implements Initializable {
    // This class is responsible for managing the image tab and its filters
    // It will be used to apply filters to the image in the tab
    private static final Logger logger = LoggerFactory.getLogger(ImageTabController.class);

    @FXML
    private ImageView tabImageOriginal;
    @FXML
    private ImageView tabImageEdited;

    private List<ChoiceBox<String>> tabFilterList;
    @FXML
    private ChoiceBox<String> tabFilter1;
    @FXML
    private ChoiceBox<String> tabFilter2;
    @FXML
    private ChoiceBox<String> tabFilter3;

    private File selectedFile; // The file selected to be opened in the tab
    private Boolean applyFiltersOnInitialize; // Flag to indicate if filters should be applied on initialization
    private List<Filter> filterList = new ArrayList<>(); // List of filters to be applied to the image

    private FilterTask filterTask; // Task to apply the filters to the image

    public ImageTabController(File selectedFile, Boolean applyFiltersOnInitialize, List<Filter> filterList) {
        this.selectedFile = selectedFile;
        this.filterList = filterList;
        this.applyFiltersOnInitialize = applyFiltersOnInitialize;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing ImageTabController...");
        tabFilter1.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        tabFilter2.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        tabFilter3.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        tabFilterList = List.of(tabFilter1, tabFilter2, tabFilter3);
        // Load the image into the ImageView
        tabImageOriginal.setImage(new Image(selectedFile.toURI().toString()));
        tabImageEdited.setImage(new Image(selectedFile.toURI().toString()));
        // Apply filters if the flag is set to true
        if (applyFiltersOnInitialize) {
            applyFilters();
        }
    }

    private void applyFilters() {
        logger.info("Applying filters to the image...");
        // Create a new FilterTask to apply the filters to the image
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(tabImageOriginal.getImage(), null);
        filterTask = new FilterTask(bufferedImage, filterList);
        // Start the task in a new thread
        new Thread(filterTask).start();
        filterTask.stateProperty().addListener((obs, oldState, newState) -> {
            Alert alert = null;
            switch (newState) {
                case RUNNING:
                    break;
                case SUCCEEDED:
                    logger.info("Filters applied successfully.");
                    tabImageEdited.setImage(SwingFXUtils.toFXImage(filterTask.getValue(), null));
                    // alert = new Alert(Alert.AlertType.INFORMATION, "Filters applied successfully.");
                    // alert.showAndWait();
                    break;
                case FAILED:
                    logger.error("Failed to apply filters: " + filterTask.getException().getMessage());
                    alert = new Alert(Alert.AlertType.ERROR, "Failed to apply filters: " + filterTask.getException().getMessage());
                    alert.showAndWait();
                    break;
                case CANCELLED:
                    logger.warn("Filter task was cancelled.");
                    alert = new Alert(Alert.AlertType.WARNING, "Filter task was cancelled.");
                    alert.showAndWait();
                    break;
                default:
                    break;
            }
        });
    }

}
