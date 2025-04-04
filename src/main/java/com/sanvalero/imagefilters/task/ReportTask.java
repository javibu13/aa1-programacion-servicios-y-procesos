package com.sanvalero.imagefilters.task;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.report.ReportManager;

import javafx.concurrent.Task;

public class ReportTask extends Task<Void> {
    private static final Logger logger = LoggerFactory.getLogger(ReportTask.class);

    private ReportManager reportManager;
    private LocalDateTime timestamp;
    private String imagePath;
    private List<Filter> filters;

    public ReportTask(ReportManager reportManager, LocalDateTime timestamp, String imagePath, List<Filter> filters) {
        this.reportManager = reportManager;
        this.timestamp = timestamp;
        this.imagePath = imagePath;
        this.filters = filters;
    }

    @Override
    protected Void call() throws Exception {
        logger.debug("Generating report for image: " + imagePath);
        updateMessage("Generating report...");
        // Write the report entry to the log file
        reportManager.writeFilterReportEntry(timestamp, imagePath, filters);
        updateMessage("Report finished!");
        return null;
    }

}
