package com.sanvalero.imagefilters.service;

import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.task.FilterTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class FilterService extends Service<BufferedImage> {
    private static final Logger logger = LoggerFactory.getLogger(FilterService.class);

    private BufferedImage prevImage;
    private List<Filter> filters;

    public FilterService() {
        this.prevImage = null;
        this.filters = null;   
    }

    public FilterService(BufferedImage prevImage, List<Filter> filters) {
        this.prevImage = prevImage;
        this.filters = filters;
    }
    
    public void setExecutionParameters(BufferedImage prevImage, List<Filter> filters) {
        this.prevImage = prevImage;
        this.filters = filters;
    }

    @Override
    protected Task<BufferedImage> createTask() {
        logger.info("Creating FilterTask...");
        return new FilterTask(prevImage, filters);
    }
}