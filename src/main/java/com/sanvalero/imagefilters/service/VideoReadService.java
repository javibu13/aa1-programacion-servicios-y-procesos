package com.sanvalero.imagefilters.service;

import com.sanvalero.imagefilters.controller.VideoTabController;
import com.sanvalero.imagefilters.filter.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.core.Mat;

public class VideoReadService extends Service<List<FilterService>> {
    // This class is responsible for managing the video processing service
    // It will be used to process the selected video and apply the selected filters
    private static final Logger logger = LoggerFactory.getLogger(VideoReadService.class);

    private ExecutorService executorService;
    private File selectedFile;
    private List<Filter> filters;
    private int totalFrames;
    private int width;
    private int height;
    private double fps;
    private String outputPath;

    public VideoReadService() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.selectedFile = null;
        this.filters = null;
    }
    
    public VideoReadService(File selectedFile, List<Filter> filters) {
        this.executorService = Executors.newFixedThreadPool(10);
        this.selectedFile = selectedFile;
        this.filters = filters;
    }

    public VideoReadService(ExecutorService executorService, File selectedFile, List<Filter> filters) {
        this.executorService = executorService;
        this.selectedFile = selectedFile;
        this.filters = filters;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getFps() {
        return fps;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setExecutionParameters(File selectedFile, List<Filter> filters) {
        // Set the parameters for the video processing
        logger.info("Setting execution parameters for VideoService...");
        this.selectedFile = selectedFile;
        this.filters = filters;
    }

    @Override
    protected Task<List<FilterService>> createTask() {
        logger.info("Creating VideoReadTask...");
        return new Task<>() {
            @Override
            protected List<FilterService> call() throws Exception {
                updateMessage("Starting video frames reading...");
                logger.debug("Setting up video for frames reading...");
                VideoCapture capture = new VideoCapture(selectedFile.getAbsolutePath());
                if (!capture.isOpened()) {
                    throw new IOException("Cannot open video file: " + selectedFile.getAbsolutePath());
                }
                totalFrames = (int) capture.get(Videoio.CAP_PROP_FRAME_COUNT);
                width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
                height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
                fps = capture.get(Videoio.CAP_PROP_FPS);
                outputPath = getOutputPathUpdated();
                logger.info("Video properties: total frames = {}, width = {}, height = {}, fps = {}", totalFrames, width, height, fps);
                logger.debug("Getting video frames...");
                Mat frame = new Mat();
                int frameCount = 0;
                List<FilterService> filterServices = new ArrayList<>();
                while (capture.read(frame)) {
                    Mat currentFrame = frame.clone();
                    BufferedImage bufferedImage = VideoTabController.matToBufferedImage(currentFrame);
                    FilterService filterService = new FilterService(frameCount, bufferedImage, filters);
                    filterService.setExecutor(executorService);
                    filterServices.add(filterService);
                    frameCount++;
                    // Update the progress of the task
                    updateProgress(frameCount, totalFrames);
                    // Update the message to show the progress in %
                    updateMessage("Read frames: " + (frameCount * 100 / totalFrames) + "%");
                }
                capture.release();
                logger.info("Video frames read successfully.");
                return filterServices;
            }
        };
    }

    public String getOutputPathUpdated() {
        // Get the output file path for the processed video
        return selectedFile.getParent() 
        + File.separator 
        + selectedFile.getName().split("\\.(?=[^\\.]+$)")[0] 
        + "_" 
        + LocalDateTime.now()
                        .toString()
                        .replace(":", "-")
                        .replace("-", "")
                        .replace("T", "_").split("\\.")[0]
        + "_filtered.mp4";
    }

}
