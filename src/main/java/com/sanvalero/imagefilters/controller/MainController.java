package com.sanvalero.imagefilters.controller;

import com.sanvalero.imagefilters.filter.Filter;
import com.sanvalero.imagefilters.filter.GrayscaleFilter;
import com.sanvalero.imagefilters.filter.InvertColorsFilter;
import com.sanvalero.imagefilters.filter.BrightnessFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private VBox rootVBox;

    private List<ChoiceBox<String>> mainFilterList = new ArrayList<>();
    @FXML
    private ChoiceBox<String> mainFilter1;
    @FXML
    private ChoiceBox<String> mainFilter2;
    @FXML
    private ChoiceBox<String> mainFilter3;

    @FXML
    private TabPane imagesTabPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
    
    private void createImageTab(File selectedFile, Boolean applyFilters, List<Filter> filterList) {
        logger.info("Creating image tab for: " + selectedFile.getName());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("imageTab.fxml"));
            ImageTabController imageTabController = new ImageTabController(selectedFile, applyFilters, filterList);
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
}
