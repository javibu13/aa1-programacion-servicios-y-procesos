package com.sanvalero.imagefilters.controller;

import com.sanvalero.imagefilters.filter.BrightnessFilter;
import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.filter.GrayscaleFilter;
import com.sanvalero.imagefilters.filter.InvertColorsFilter;
import com.sanvalero.imagefilters.report.ReportManager;
import com.sanvalero.imagefilters.service.FilterService;
import com.sanvalero.imagefilters.task.ReportTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.concurrent.Worker;

public class ImageTabController implements Initializable {
    // This class is responsible for managing the image tab and its filters
    // It will be used to apply filters to the image in the tab
    private static final Logger logger = LoggerFactory.getLogger(ImageTabController.class);

    @FXML
    private ImageView tabImageOriginal;
    @FXML
    private ImageView tabImageEdited;

    @FXML
    private Label tabProgressLabel;
    @FXML
    private ProgressBar tabProgressBar;
    
    @FXML
    private Button tabApplyBtn;
    @FXML
    private Button tabUndoBtn;
    @FXML
    private Button tabRedoBtn;
    @FXML
    private Button tabSaveBtn;

    private List<ChoiceBox<String>> tabFilterList;
    @FXML
    private ChoiceBox<String> tabFilter1;
    @FXML
    private ChoiceBox<String> tabFilter2;
    @FXML
    private ChoiceBox<String> tabFilter3;

    private ReportManager reportManager;
    private File selectedFile; // The file selected to be opened in the tab
    private Boolean applyFiltersOnInitialize; // Flag to indicate if filters should be applied on initialization
    private List<Filter> filterList = new ArrayList<>(); // List of filters to be applied to the image
    private String defaultFilePath = "user.home"; // Default file path to save the image

    private FilterService filterService; // Service to apply filters to the image

    public ImageTabController(ReportManager reportManager, ExecutorService executorService, File selectedFile, Boolean applyFiltersOnInitialize, List<Filter> filterList) {
        this.reportManager = reportManager;
        this.filterService = new FilterService(); // Initialize the filter service
        this.filterService.setExecutor(executorService); // Set the executor for the filter service
        this.selectedFile = selectedFile;
        this.filterList = filterList;
        this.applyFiltersOnInitialize = applyFiltersOnInitialize;
        this.defaultFilePath = selectedFile.getParent(); // Set the default file path to the directory of the selected file
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing ImageTabController...");
        tabFilter1.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        tabFilter2.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        tabFilter3.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        tabFilterList = List.of(tabFilter1, tabFilter2, tabFilter3);
        // Set bindings for Service to update the progress bar and label
        tabProgressLabel.textProperty().bind(filterService.messageProperty());
        tabProgressBar.progressProperty().bind(filterService.progressProperty());
        filterService.stateProperty().addListener((obs, oldState, newState) -> {
            Alert alert = null;
            switch (newState) {
                case RUNNING:
                deactivateButtons();
                break;
                case SUCCEEDED:
                logger.info("Filters applied successfully.");
                createReport(); // Create the report after the filters are applied
                tabImageEdited.setImage(SwingFXUtils.toFXImage(filterService.getValue(), null));
                alert = new Alert(Alert.AlertType.INFORMATION, "Filters applied successfully to "+ selectedFile.getName() + ".");
                alert.showAndWait();
                reactivateButtons(); // Reactivate the buttons after the filters are applied
                break;
                case FAILED:
                logger.error("Failed to apply filters: " + filterService.getException().getMessage());
                alert = new Alert(Alert.AlertType.ERROR, "Failed to apply filters: " + filterService.getException().getMessage());
                alert.showAndWait();
                reactivateButtons();
                break;
                case CANCELLED:
                // Avoid showing the alert if the task was cancelled by the restart method and oldState is not RUNNING
                if (oldState != Worker.State.RUNNING) {
                    reactivateButtons();
                    return;
                }
                logger.warn("Filter task was cancelled.");
                alert = new Alert(Alert.AlertType.WARNING, "Filter task was cancelled.");
                alert.showAndWait();
                reactivateButtons();
                break;
                default:
                break;
            }
        });
        // Load the image into the ImageView
        tabImageOriginal.setImage(new Image(selectedFile.toURI().toString()));
        // Apply filters if the flag is set to true
        if (applyFiltersOnInitialize) {
            applyFilters();
        }
    }

    @FXML
    private void applyNewFilters() {
        logger.info("Applying new filters to the image...");
        // Add the selected filters to the filter list
        filterList = getSelectedFilters();
        tabImageOriginal.setImage(tabImageEdited.getImage()); // Set the original image as the new image to apply filters to
        tabImageEdited.setImage(null); // Clear the edited image
        applyFilters();
    }

    private void applyFilters() {
        logger.info("Applying filters to the image...");
        // Set up the filter service with the selected filters and the image to be filtered
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(tabImageOriginal.getImage(), null);
        filterService.setExecutionParameters(bufferedImage, filterList); // Set the parameters for the filter service
        // Start the filter service to apply the filters
        filterService.restart();
    }

    private void createReport() {
        // Generate the report in a new thread
        ReportTask reportTask = new ReportTask(reportManager, LocalDateTime.now(), selectedFile.getAbsolutePath(), filterList);
        reportTask.stateProperty().addListener((obs2, oldState2, newState2) -> {
            Alert alert2 = null;
            switch (newState2) {
                case FAILED:
                logger.error("Failed to generate report: " + reportTask.getException().getMessage());
                alert2 = new Alert(Alert.AlertType.ERROR, "Failed to generate report: " + reportTask.getException().getMessage());
                alert2.showAndWait();
                break;
                case CANCELLED:
                logger.warn("Report task was cancelled.");
                alert2 = new Alert(Alert.AlertType.WARNING, "Report task was cancelled.");
                alert2.showAndWait();
                break;
                default:
                break;
            }
        });
        new Thread(reportTask).start();
    }

    private void reactivateButtons() {
        tabApplyBtn.setDisable(false);
        // TODO: Set the undo and redo buttons to be enabled only if there are historical changes
        // tabUndoBtn.setDisable(false);
        // tabRedoBtn.setDisable(false);
        tabSaveBtn.setDisable(false);
    }

    private void deactivateButtons() {
        tabApplyBtn.setDisable(true);
        tabUndoBtn.setDisable(true);
        tabRedoBtn.setDisable(true);
        tabSaveBtn.setDisable(true);
    }

    private List<Filter> getSelectedFilters() {
        List<Filter> filterList = new ArrayList<>();
        for (ChoiceBox<String> filter : tabFilterList) {
            if (filter.getValue() != null && !filter.getValue().isEmpty()) {
                logger.info("Selected filter: " + filter.getValue());
                Boolean filterAdded = true;
                switch (filter.getValue()) {
                    case "Grayscale":
                        filterList.add(new GrayscaleFilter());
                        break;
                    case "Invert Colors":
                        filterList.add(new InvertColorsFilter());
                        break;
                    case "Brightness":
                        filterList.add(new BrightnessFilter(20)); // Example value, replace with actual value
                        break;
                    default:
                        filterAdded = false;
                        logger.warn("Unknown filter: " + filter.getValue());
                }
                if (filterAdded) {
                    logger.info("Filter added to list: " + filter.getValue());
                } else {
                    logger.warn("Filter not added to list: " + filter.getValue());
                }
            }
        }
        return filterList;
    }

    @FXML
    private void saveImage(ActionEvent event) {
        // Save the image to a file
        logger.info("Saving image...");
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(tabImageEdited.getImage(), null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        // Set the initial file name and directory for the file chooser
        fileChooser.setInitialFileName(
            selectedFile.getName().split("\\.(?=[^\\.]+$)")[0] + 
            "_" + 
            LocalDateTime.now()
            .toString()
            .replace(":", "-")
            .replace("-", "")
            .replace("T", "_").split("\\.")[0]
        );
        File defaultDirectory = new File(defaultFilePath);
        if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(defaultDirectory);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        File selectedFile = fileChooser.showSaveDialog(tabSaveBtn.getScene().getWindow());
        if (selectedFile != null) {
            try {
                ImageIO.write(bufferedImage, "png", selectedFile);
                logger.info("Image saved successfully to: " + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Error saving image: " + e.getMessage());
            }
        }
    }

    // This method can be called from the main controller to set the default file path for saving images
    public void updateDefaultFilePath(String newPath) {
        this.defaultFilePath = newPath;
        logger.info("Default file path updated to: " + newPath);
    }

    public void updateExecutorService(ExecutorService executorService) {
        this.filterService.setExecutor(executorService); // Update the executor service for the filter service
        logger.info("Executor service updated.");
    }

}
