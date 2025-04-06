package com.sanvalero.imagefilters.service;

import com.sanvalero.imagefilters.filter.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

public class VideoService extends Service<String> {
    // This class is responsible for managing the video processing service
    // It will be used to process the selected video and apply the selected filters
    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    private ExecutorService executorService;
    private File selectedFile;
    private List<Filter> filters;
    private Map<Integer, Mat> matFramesMap = new HashMap<>();
    private Map<Integer, Mat> matFramesFailedMap = new HashMap<>();
    private Lock lockMatFramesMap = new java.util.concurrent.locks.ReentrantLock();
    private Lock lockMatFramesFailedMap = new java.util.concurrent.locks.ReentrantLock();
    

    public VideoService() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.selectedFile = null;
        this.filters = null;
    }
    
    public VideoService(File selectedFile, List<Filter> filters) {
        this.executorService = Executors.newFixedThreadPool(10);
        this.selectedFile = selectedFile;
        this.filters = filters;
    }

    public VideoService(ExecutorService executorService, File selectedFile, List<Filter> filters) {
        this.executorService = executorService;
        this.selectedFile = selectedFile;
        this.filters = filters;
    }

    public void setExecutionParameters(File selectedFile, List<Filter> filters) {
        // Set the parameters for the video processing
        logger.info("Setting execution parameters for VideoService...");
        this.selectedFile = selectedFile;
        this.filters = filters;
    }

    @Override
    protected Task<String> createTask() {
        logger.info("Creating VideoTask...");
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Starting video processing...");
                logger.info("Setting up video processing...");
                VideoCapture capture = new VideoCapture(selectedFile.getAbsolutePath());
                if (!capture.isOpened()) {
                    throw new IOException("Cannot open video file: " + selectedFile.getAbsolutePath());
                }
                int totalFrames = (int) capture.get(Videoio.CAP_PROP_FRAME_COUNT);
                int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
                int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
                double fps = capture.get(Videoio.CAP_PROP_FPS);
                String outputPath = selectedFile.getParent() 
                                    + File.separator 
                                    + selectedFile.getName().split("\\.(?=[^\\.]+$)")[0] 
                                    + "_" 
                                    + LocalDateTime.now()
                                                    .toString()
                                                    .replace(":", "-")
                                                    .replace("-", "")
                                                    .replace("T", "_").split("\\.")[0];
                
                logger.info("Processing video frames...");
                Mat frame = new Mat();
                int frameCount = 0;
                CountDownLatch latch = new CountDownLatch(totalFrames);
                while (capture.read(frame)) {
                    Mat currentFrame = frame.clone();
                    BufferedImage bufferedImage = matToBufferedImage(currentFrame);
                    FilterService filterService = new FilterService(frameCount, bufferedImage, filters);
                    filterService.setExecutor(executorService);
                    filterService.stateProperty().addListener((obs, oldState, newState) -> {
                        switch (newState) {
                            case RUNNING:
                                logger.info("Processing frame " + filterService.getId() + "...");
                                break;
                            case SUCCEEDED:
                                latch.countDown();
                                BufferedImage filteredImage = filterService.getValue();
                                Mat filteredMat = bufferedImageToMat(filteredImage);
                                lockMatFramesMap.lock();
                                try {
                                    matFramesMap.put(filterService.getId(), filteredMat);
                                    updateProgress(matFramesMap.size(), totalFrames);
                                    updateMessage((matFramesMap.size() * 100 / totalFrames) + "%");
                                } finally {
                                    lockMatFramesMap.unlock();
                                }
                                logger.info("Frame " + filterService.getId() + " processed successfully.");
                                break;
                            case FAILED:
                                latch.countDown();
                                lockMatFramesFailedMap.lock();
                                try {
                                    matFramesFailedMap.put(filterService.getId(), currentFrame);
                                } finally {
                                    lockMatFramesFailedMap.unlock();
                                }
                                logger.error("Error applying filters: " + filterService.getException().getMessage());
                                updateMessage("Error applying filters: " + filterService.getException().getMessage());
                                break;
                            case CANCELLED:
                                if (oldState != Worker.State.RUNNING) {
                                    return;
                                }
                                latch.countDown();
                                lockMatFramesFailedMap.lock();
                                try {
                                    matFramesFailedMap.put(filterService.getId(), currentFrame);
                                } finally {
                                    lockMatFramesFailedMap.unlock();
                                }
                                logger.warn("Video processing was cancelled.");
                                updateMessage("Video processing was cancelled.");
                                break;
                            default:
                                break;
                        }
                    });                    
                    filterService.start();
                    frameCount++;
                }
                capture.release();
                // Wait for all tasks to finish
                latch.await();
                logger.info("Video processing completed, saving output video...");
                VideoWriter writer = new VideoWriter(
                        outputPath,
                        VideoWriter.fourcc('m', 'p', '4', 'v'),
                        fps,
                        new Size(width, height)
                );
                if (!writer.isOpened()) {
                    throw new IOException("Cannot open output video file.");
                }
                // Write the processed frames to the output video file
                for (int i = 0; i < frameCount; i++) {
                    Mat filteredMat = matFramesMap.get(i);
                    if (filteredMat != null) {
                        writer.write(filteredMat);
                    }
                }
                writer.release();
                updateMessage("Finalizado. Guardado en: " + outputPath);
                return null;
            }
        };
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if (mat.channels() == 1) type = BufferedImage.TYPE_BYTE_GRAY;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), b);
        return image;
    }

    private Mat bufferedImageToMat(BufferedImage bufferedImage) {
        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

}
