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

public class VideoWriteService extends Service<String> {
    // This class is responsible for managing the video processing service
    // It will be used to process the selected video and apply the selected filters
    private static final Logger logger = LoggerFactory.getLogger(VideoWriteService.class);

    private VideoReadService videoReadService;
    private File selectedFile;
    private Map<Integer, Mat> matFramesMap = new HashMap<>();
    private Map<Integer, Mat> matFramesFailedMap = new HashMap<>();
    private final String outputPath;
    

    public VideoWriteService(VideoReadService videoReadService, Map<Integer, Mat> matFramesMap, Map<Integer, Mat> matFramesFailedMap) {
        this.selectedFile = videoReadService.getSelectedFile();
        this.matFramesMap = matFramesMap;
        this.matFramesFailedMap = matFramesFailedMap;
        this.outputPath = videoReadService.getOutputPathUpdated();
        this.videoReadService = videoReadService;
    }

    @Override
    protected Task<String> createTask() {
        logger.info("Creating VideoWriteTask...");
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Starting video saving...");
                logger.info("Setting up video saving...");
                VideoWriter writer = new VideoWriter(
                        outputPath,
                        VideoWriter.fourcc('m', 'p', '4', 'v'),
                        videoReadService.getFps(),
                        new Size(videoReadService.getWidth(), videoReadService.getHeight())
                );
                if (!writer.isOpened()) {
                    throw new IOException("Cannot open output video file.");
                }
                // Write the processed frames to the output video file
                for (int i = 0; i < videoReadService.getTotalFrames(); i++) {
                    if (matFramesFailedMap.containsKey(i)) {
                        updateMessage("Frame " + i + " failed to process. Skipping...");
                        logger.error("Frame " + i + " failed to process. Skipping...");
                        continue;
                    }
                    if (matFramesMap.containsKey(i)) {
                        logger.debug("Writing frame " + i + "...");
                    } else {
                        updateMessage("Frame " + i + " not found. Skipping...");
                        logger.error("Frame " + i + " not found. Skipping...");
                        continue;
                    }
                    Mat filteredMat = matFramesMap.get(i);
                    if (filteredMat != null) {
                        writer.write(filteredMat);
                        updateMessage("Saved frames:  " + ((int) ((double) i / videoReadService.getTotalFrames() * 100)) + "%");
                        updateProgress(i, videoReadService.getTotalFrames());
                    }
                }
                writer.release();
                updateMessage("Done!");
                return outputPath;
            }
        };
    }

}
