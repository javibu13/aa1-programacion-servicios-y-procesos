package com.sanvalero.imagefilters.task;

import com.sanvalero.imagefilters.filter.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import javafx.concurrent.Task;

public class FilterTask extends Task<BufferedImage> {
    private static final Logger logger = LoggerFactory.getLogger(FilterTask.class);
    
    private BufferedImage prevImage;
    private List<Filter> filters;

    public FilterTask(BufferedImage prevImage, List<Filter> filters) {
        this.prevImage = prevImage;
        this.filters = filters;
    }

    @Override
    protected BufferedImage call() throws Exception {
        updateMessage("Starting filter...");
        logger.info("Applying filters to the image...");
        BufferedImage filteredImage = new BufferedImage(prevImage.getWidth(), prevImage.getHeight(), prevImage.getType());
        int imageSize = prevImage.getWidth() * prevImage.getHeight();
        int processedPixels = 0;
        // Loop through the pixels of the image and apply the filters to each pixel
        for (int x = 0; x < prevImage.getWidth(); x++) {
            Thread.sleep(10); // Simulate a delay for the task
            for (int y = 0; y < prevImage.getHeight(); y++) {
                Color pixelColor = new Color(prevImage.getRGB(x, y));
                // Apply each filter to the pixel
                for (Filter filter : filters) {
                    pixelColor = filter.apply(pixelColor);
                }
                filteredImage.setRGB(x, y, pixelColor.getRGB());
                processedPixels++;
                // Update the progress of the task
                updateProgress(processedPixels, imageSize);
                // Update the message to show the progress in %
                updateMessage("Processing " + (processedPixels * 100 / imageSize) + "% of the image...");
            }
        }
        // Update the progress of the task
        updateProgress(processedPixels, imageSize);
        // Update the message to show that the task is finished
        updateMessage("Done!");
        return filteredImage;
    }

}
