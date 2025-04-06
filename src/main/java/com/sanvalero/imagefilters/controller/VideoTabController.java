package com.sanvalero.imagefilters.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.report.ReportManager;
import com.sanvalero.imagefilters.service.FilterService;
import com.sanvalero.imagefilters.service.VideoReadService;
import com.sanvalero.imagefilters.service.VideoWriteService;
import com.sanvalero.imagefilters.task.ReportTask;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class VideoTabController implements Initializable {
    // This class is responsible for managing the video tab and its filters
    // It will be used to process the selected video and apply the selected filters
    private static final Logger logger = LoggerFactory.getLogger(VideoTabController.class);

    @FXML
    private Label tabProgressLabel;
    @FXML
    private ProgressBar tabProgressBar;
    
    private ReportManager reportManager;
    private VideoReadService videoReadService;
    private File selectedFile;
    private List<Filter> filterList;
    private Boolean applyFiltersOnInitialize;
    private int totalFrames;
    private Lock lockMatFramesMap = new java.util.concurrent.locks.ReentrantLock();
    private Map<Integer, Mat> matFramesMap = new HashMap<>();
    private Map<Integer, Mat> matFramesFailedMap = new HashMap<>();

    public VideoTabController(ReportManager reportManager, ExecutorService executorService, File selectedFile, Boolean applyFiltersOnInitialize, List<Filter> filterList) {
        this.reportManager = reportManager;
        this.videoReadService = new VideoReadService(executorService, selectedFile, filterList); // Initialize the video service
        this.videoReadService.setExecutor(executorService);
        this.selectedFile = selectedFile;
        this.filterList = filterList;
        this.applyFiltersOnInitialize = applyFiltersOnInitialize;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing VideoTabController...");
        tabProgressLabel.textProperty().bind(videoReadService.messageProperty());
        tabProgressBar.progressProperty().bind(videoReadService.progressProperty());
        videoReadService.stateProperty().addListener((obs, oldState, newState) -> {
            Alert alert = null;
            switch (newState) {
                case RUNNING:
                break;
                case SUCCEEDED:
                // Get the list of filter services from the video service
                List<FilterService> filterServices = videoReadService.getValue();
                totalFrames = filterServices.size();
                tabProgressLabel.textProperty().unbind();
                tabProgressLabel.setText("Processing frames...");
                tabProgressBar.progressProperty().unbind();
                tabProgressBar.setProgress(0);
                // Process each filter service and apply the filters to the frames
                for (FilterService filterService : filterServices) {
                    filterService.setOnSucceeded(ev -> {
                        // SUCCESSFUL
                        BufferedImage filteredImage = filterService.getValue();
                        Mat filteredMat = bufferedImageToMat(filteredImage);
                        lockMatFramesMap.lock();
                        try {
                            matFramesMap.put(filterService.getId(), filteredMat);
                            // Update the progress bar and label in percent
                            int framePercent = (int) ((double) (matFramesMap.size() + matFramesFailedMap.size()) / totalFrames * 100);
                            tabProgressLabel.setText("Processed frames: " + framePercent + "%");
                            tabProgressBar.setProgress((double) (matFramesMap.size() + matFramesFailedMap.size()) / totalFrames);
                            logger.info("Frame " + filterService.getId() + " processed successfully.");
                        } finally {
                            lockMatFramesMap.unlock();
                            if (isVideoProcessingFinished()) {
                                logger.info("All frames processed.");
                                saveProcessedFrames(); // Save the processed frames to a new video file
                            }
                        }
                    });
                    filterService.setOnFailed(ev -> {
                        // FAILED
                        logger.error("Error processing frame " + filterService.getId() + ": " + filterService.getException().getMessage());
                        lockMatFramesMap.lock();
                        try {
                            matFramesFailedMap.put(filterService.getId(), null);
                            // Update the progress bar and label in percent
                            int framePercent = (int) ((double) (matFramesMap.size() + matFramesFailedMap.size()) / totalFrames * 100);
                            tabProgressLabel.setText("Processed frames: " + framePercent + "%");
                            tabProgressBar.setProgress((double) (matFramesMap.size() + matFramesFailedMap.size()) / totalFrames);
                        } finally {
                            lockMatFramesMap.unlock();
                            if (isVideoProcessingFinished()) {
                                logger.info("All frames processed.");
                                saveProcessedFrames(); // Save the processed frames to a new video file
                            }
                        }
                    });
                    filterService.setOnCancelled(ev -> {
                        // CANCELLED
                        logger.warn("Frame " + filterService.getId() + " processing was cancelled.");
                        lockMatFramesMap.lock();
                        try {
                            matFramesFailedMap.put(filterService.getId(), null);
                            // Update the progress bar and label in percent
                            int framePercent = (int) ((double) (matFramesMap.size() + matFramesFailedMap.size()) / totalFrames * 100);
                            tabProgressLabel.setText("Processed frames: " + framePercent + "%");
                            tabProgressBar.setProgress((double) (matFramesMap.size() + matFramesFailedMap.size()) / totalFrames);
                        } finally {
                            lockMatFramesMap.unlock();
                            if (isVideoProcessingFinished()) {
                                logger.info("All frames processed.");
                                saveProcessedFrames(); // Save the processed frames to a new video file
                            }
                        }
                    });
                    filterService.start();
                }
                break;
                case FAILED:
                logger.error("Video processing failed: " + videoReadService.getException().getMessage());
                alert = new Alert(Alert.AlertType.ERROR, "Error applying filters: " + videoReadService.getException().getMessage());
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
            logger.info("Applying filters on initialize...");
            videoReadService.start();
        } else {
            logger.info("Filters will not be processed (Start on demand feature not implemented yet).");
        }
    }

    private Boolean isVideoProcessingFinished() {
        // Check if the video processing is finished
        return matFramesMap.size() + matFramesFailedMap.size() == totalFrames;
    }

    private void saveProcessedFrames() {
        logger.warn(matFramesMap.size() + " frames processed successfully, " + matFramesFailedMap.size() + " frames failed to process.");
        logger.info("Save processed frames to a new video file...");
        // Save the processed frames to a new video file
        VideoWriteService videoWriteService = new VideoWriteService(videoReadService, matFramesMap, matFramesFailedMap);
        videoWriteService.setExecutor(videoReadService.getExecutor());
        tabProgressLabel.textProperty().bind(videoWriteService.messageProperty());
        tabProgressBar.progressProperty().bind(videoWriteService.progressProperty());
        videoWriteService.setOnSucceeded(ev -> {
            // SUCCESSFUL
            String outputPath = videoWriteService.getValue();
            tabProgressLabel.textProperty().unbind();
            tabProgressLabel.setText("Video saved successfully!\nPath: " + outputPath);
            tabProgressBar.progressProperty().unbind();
            tabProgressBar.setProgress(1.0);
            logger.info("Video saved successfully! Path: " + outputPath);
            createReport(); // Create the report after saving the video
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Video saved successfully!\nPath: " + outputPath);
            alert.showAndWait();
        });
        videoWriteService.setOnFailed(ev -> {
            // FAILED
            logger.error("Error saving video: " + videoWriteService.getException().getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving video: " + videoWriteService.getException().getMessage());
            alert.showAndWait();
        });
        videoWriteService.setOnCancelled(ev -> {
            // CANCELLED
            logger.warn("Video saving was cancelled.");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Video saving was cancelled.");
            alert.showAndWait();
        });
        videoWriteService.start();
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

    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if (mat.channels() == 1) type = BufferedImage.TYPE_BYTE_GRAY;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), b);
        return image;
    }

    public static Mat bufferedImageToMat(BufferedImage bufferedImage) {
        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }
}
