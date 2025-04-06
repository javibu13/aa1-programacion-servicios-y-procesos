package com.sanvalero.imagefilters.controller;

import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.filter.GrayscaleFilter;
import com.sanvalero.imagefilters.filter.InvertColorsFilter;
import com.sanvalero.imagefilters.App;
import com.sanvalero.imagefilters.filter.BrightnessFilter;
import com.sanvalero.imagefilters.report.ReportManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private ReportManager reportManager = new ReportManager();
    private int maxThreadNumber = 2; // Default value, can be changed by the user
    private ExecutorService executorService = Executors.newFixedThreadPool(maxThreadNumber);

    @FXML
    private VBox rootVBox;

    @FXML
    private MenuItem openVideoMenuBtn;

    private List<ChoiceBox<String>> mainFilterList = new ArrayList<>();
    @FXML
    private ChoiceBox<String> mainFilter1;
    @FXML
    private ChoiceBox<String> mainFilter2;
    @FXML
    private ChoiceBox<String> mainFilter3;

    @FXML
    private TabPane imagesTabPane;

    public void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            logger.info("Executor service shut down.");
        } else {
            logger.warn("Executor service is already shut down or null.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing MainController...");
        if (App.isVideoProcessingSupported()) {
            logger.info("Video processing is supported.");
        } else {
            logger.warn("Video processing is not supported.");
            openVideoMenuBtn.setDisable(true);
        }
        mainFilter1.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        mainFilter2.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        mainFilter3.getItems().addAll("", "Grayscale", "Invert Colors", "Brightness");
        mainFilterList.add(mainFilter1);
        mainFilterList.add(mainFilter2);
        mainFilterList.add(mainFilter3);
    }

    @FXML
    private void openSingleImage(ActionEvent event) {
        logger.info("Opening single image...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(rootVBox.getScene().getWindow());
        
        if (selectedFile != null) {
            logger.info("Selected image: " + selectedFile.getAbsolutePath());
            List<Filter> filterList = getSelectedFilters();
            createImageTab(selectedFile, true, filterList); // Change true by variable applyFilters if needed or implemented in the future
        }
    }

    @FXML
    private void openMultipleImages(ActionEvent event) {
        logger.info("Opening multiple images...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(rootVBox.getScene().getWindow());
        
        if (selectedFiles.size() > 0) {
            logger.info("Selected images: ");
            List<Filter> filterList = getSelectedFilters();
            for (File selectedFile : selectedFiles) {
                logger.info(selectedFile.getAbsolutePath());
                createImageTab(selectedFile, true, filterList); // Change true by variable applyFilters if needed or implemented in the future
            }
        }
    }

    @FXML
    private void openImagesFromFolder(ActionEvent event) {
        logger.info("Opening images from folder...");
        // Create a FileChooser to select a folder
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File selectedDirectory = directoryChooser.showDialog(rootVBox.getScene().getWindow());

        if (selectedDirectory != null) {
            logger.info("Selected folder: " + selectedDirectory.getAbsolutePath());
            // List all image files in the selected directory
            File[] imageFiles = selectedDirectory.listFiles((dir, name) -> {
                return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp");
            });

            if (imageFiles != null && imageFiles.length > 0) {
                logger.info("Found " + imageFiles.length + " image(s) in the folder.");
                List<Filter> filterList = getSelectedFilters();
                for (File imageFile : imageFiles) {
                    logger.info("Opening image: " + imageFile.getAbsolutePath());
                    createImageTab(imageFile, true, filterList);
                }
            } else {
                logger.info("No image files found in the folder.");
            }
        }
    }

    @FXML
    private void openVideo(ActionEvent event) {
        logger.info("Opening video...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mov", "*.mkv")
        );
        File selectedFile = fileChooser.showOpenDialog(rootVBox.getScene().getWindow());
        if (selectedFile != null) {
            logger.info("Selected video: " + selectedFile.getAbsolutePath());
            List<Filter> filterList = getSelectedFilters();
            // Create a new tab for the video processing
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("videoTab.fxml"));
                VideoTabController videoTabController = new VideoTabController(reportManager, executorService, selectedFile, true, filterList); // Change true by variable applyFilters if needed or implemented in the future
                fxmlLoader.setController(videoTabController);
                Tab newTab = new Tab(selectedFile.getName(), fxmlLoader.load());
                newTab.setUserData(videoTabController); // Store the controller in the tab for later access
                imagesTabPane.getTabs().add(newTab);
                logger.info("Video tab created for: " + selectedFile.getName());
            } catch (Exception e) {
                logger.error("Error creating video tab: " + e.getMessage(), e);
            }
        }
    }
    
    private void createImageTab(File selectedFile, Boolean applyFilters, List<Filter> filterList) {
        logger.info("Creating image tab for: " + selectedFile.getName());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("imageTab.fxml"));
            ImageTabController imageTabController = new ImageTabController(reportManager, executorService, selectedFile, applyFilters, filterList);
            fxmlLoader.setController(imageTabController);
            Tab newTab = new Tab(selectedFile.getName(), fxmlLoader.load());
            newTab.setUserData(imageTabController); // Store the controller in the tab for later access
            imagesTabPane.getTabs().add(newTab);
            logger.info("Image tab created for: " + selectedFile.getName());
        } catch (Exception e) {
            logger.error("Error creating image tab: " + e.getMessage(), e);
        }
    }

    private List<Filter> getSelectedFilters() {
        List<Filter> filterList = new ArrayList<>();
        for (ChoiceBox<String> filter : mainFilterList) {
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
    private void modifyDefaultFilePath(ActionEvent event) {
        logger.info("Modifying default file path of opened tabs...");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Default File Path");
        File selectedDirectory = directoryChooser.showDialog(rootVBox.getScene().getWindow());

        if (selectedDirectory != null) {
            String defaultFilePath = selectedDirectory.getAbsolutePath();
            logger.info("Set default file path to: " + defaultFilePath);
            // Update the default file path for all opened tabs
            for (Tab tab : imagesTabPane.getTabs()) {
                logger.info(tab.toString());
                ImageTabController imageTabController = (ImageTabController) tab.getUserData();
                if (imageTabController != null) {
                    logger.info("Updating default file path for tab: " + tab.getText());
                    imageTabController.updateDefaultFilePath(defaultFilePath);
                }
            }
        } else {
            logger.warn("No directory selected for default file path.");
        }
    }

    @FXML
    public void showReportTable() {
        Stage stage = new Stage();
        stage.setTitle("History Report");
        TableView<ObservableList<String>> tableView = new TableView<>();
        // Create columns for the table
        String[] columnTitles = {"DateTime", "Path", "Filters"};
        for (int i = 0; i < columnTitles.length; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnTitles[i]);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(colIndex)));
            tableView.getColumns().add(column);
        }

        List<List<String>> reportEntries = reportManager.readFilterReportEntries();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (List<String> entry : reportEntries) {
            ObservableList<String> row = FXCollections.observableArrayList(entry);
            data.add(row);
        }
        tableView.setItems(data);
        tableView.setPrefWidth(900);
        tableView.setPrefHeight(600);

        VBox root = new VBox(tableView);
        root.setPadding(new Insets(10));
        root.setPrefSize(900, 600);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void showAboutWindow() {
        Stage splashStage = new Stage();

        Image splashImage = new Image(App.class.getResourceAsStream("splashScreen.png"));
        ImageView splashImageView = new ImageView(splashImage);
        splashImageView.setPreserveRatio(true);
        splashImageView.setFitWidth(600);

        StackPane splashLayout = new StackPane(splashImageView);
        Scene splashScene = new Scene(splashLayout);
        splashStage.setScene(splashScene);
        splashStage.show();
    }

    @FXML
    public void modifyMaxThreadNumber() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Max Thread Number");
        dialog.setHeaderText("Set the maximum number of threads for the executor service.\n"
                            + "Actual value: " + maxThreadNumber + "\n"
                            + "*This change will not be applied to current waiting tasks.");
        dialog.setContentText("Enter the maximum number of threads:");
        dialog.setGraphic(null); // Remove the default graphic icon
        // Wait for the user to enter a number
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(number -> {
            try {
                int num = Integer.parseInt(number);
                if (num <= 0) {
                    throw new NumberFormatException("Number must be greater than 0.");
                }
                logger.info("Setting max thread number to: " + num);
                maxThreadNumber = num;
                ExecutorService newExecutorService = Executors.newFixedThreadPool(num);
                // Assign the new executor service to the image tab controllers
                for (Tab tab : imagesTabPane.getTabs()) {
                    ImageTabController imageTabController = (ImageTabController) tab.getUserData();
                    if (imageTabController != null) {
                        imageTabController.updateExecutorService(newExecutorService);
                    }
                }
                // Shutdown the old executor service
                shutdownExecutorService();
                // Set the new executor service
                executorService = newExecutorService;
            } catch (NumberFormatException e) {
                logger.error("Invalid number format: " + number);
                logger.debug(e.toString());
            }
        });
    }
}
