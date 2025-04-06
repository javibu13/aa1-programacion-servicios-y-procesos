package com.sanvalero.imagefilters.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.filter.FilterStep;
import com.sanvalero.imagefilters.report.ReportManager;
import com.sanvalero.imagefilters.service.FilterService;
import com.sanvalero.imagefilters.service.VideoService;
import com.sanvalero.imagefilters.task.ReportTask;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;

public class VideoTabController implements Initializable {
    // This class is responsible for managing the video tab and its filters
    // It will be used to process the selected video and apply the selected filters
    private static final Logger logger = LoggerFactory.getLogger(VideoTabController.class);

    @FXML
    private Label tabProgressLabel;
    @FXML
    private ProgressBar tabProgressBar;
    
    private ReportManager reportManager;
    private VideoService videoService;
    private File selectedFile;
    private List<Filter> filterList;
    private Boolean applyFiltersOnInitialize;
    private String defaultFilePath;

    public VideoTabController(ReportManager reportManager, ExecutorService executorService, File selectedFile, Boolean applyFiltersOnInitialize, List<Filter> filterList) {
        this.reportManager = reportManager;
        this.videoService = new VideoService(executorService, selectedFile, filterList); // Initialize the video service
        this.selectedFile = selectedFile;
        this.filterList = filterList;
        this.applyFiltersOnInitialize = applyFiltersOnInitialize;
        this.defaultFilePath = selectedFile.getParent(); // Set the default file path to the directory of the selected file
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing VideoTabController...");
        tabProgressLabel.textProperty().bind(videoService.messageProperty());
        tabProgressBar.progressProperty().bind(videoService.progressProperty());
        videoService.stateProperty().addListener((obs, oldState, newState) -> {
            Alert alert = null;
            switch (newState) {
                case RUNNING:
                break;
                case SUCCEEDED:
                logger.info("Video processing completed successfully.");
                createReport(); // Create the report after successful processing
                // Show success alert
                alert = new Alert(Alert.AlertType.INFORMATION, "Filters applied successfully to "+ selectedFile.getName() + ".");
                alert.showAndWait();
                break;
                case FAILED:
                logger.error("Video processing failed: " + videoService.getException().getMessage());
                alert = new Alert(Alert.AlertType.ERROR, "Error applying filters: " + videoService.getException().getMessage());
                alert.showAndWait();
                break;
                case CANCELLED:
                // Avoid showing the alert if the task was cancelled by the restart method and oldState is not RUNNING
                if (oldState != Worker.State.RUNNING) {
                    return;
                }
                logger.warn("Video processing was cancelled.");
                alert = new Alert(Alert.AlertType.WARNING, "Video processing was cancelled.");
                alert.showAndWait();
                break;
                default:
                break;
            }
        });
        // Apply filters if the flag is set to true
        if (applyFiltersOnInitialize) {
            processVideoAndApplyFilters();
        }
    }

    private void processVideoAndApplyFilters() {
        logger.info("Processing video and applying filters...");
        // Set up the video service with the selected filters and the file to be processed
        videoService.setExecutionParameters(selectedFile, filterList); // Set the parameters for the filter service
        // Start the video service to process video and apply the filters
        videoService.restart();
    }

    private void createReport() {
        // Generate the report in a new thread
        ReportTask reportTask = new ReportTask(reportManager, LocalDateTime.now(), selectedFile.getAbsolutePath(), filterList);
        reportTask.stateProperty().addListener((obs, oldState, newState) -> {
            Alert alert = null;
            switch (newState) {
                case FAILED:
                logger.error("Failed to generate report: " + reportTask.getException().getMessage());
                alert = new Alert(Alert.AlertType.ERROR, "Failed to generate report: " + reportTask.getException().getMessage());
                alert.showAndWait();
                break;
                case CANCELLED:
                logger.warn("Report task was cancelled.");
                alert = new Alert(Alert.AlertType.WARNING, "Report task was cancelled.");
                alert.showAndWait();
                break;
                default:
                break;
            }
        });
        new Thread(reportTask).start();
    }
}
